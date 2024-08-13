/*
 * MIT License
 *
 * Copyright © 2024 Hiroaki Fujii
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package jp.co.dsas.azureadb2c.sample.controller;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import jp.co.dsas.azureadb2c.sample.Exception.SampleException;
import jp.co.dsas.azureadb2c.sample.config.WebAppConfig;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The controller class for the sign-in success page
 */
@Controller
public class SuccessController {

    /**
     * The configuration class for the user's information
     */
    private final WebAppConfig _webAppConfig;

    /**
     * The session for the user's information
     */
    private final HttpSession _session;

    /**
     * Constructor
     *
     * @param webAppConfig The web app configuration
     * @param session      The session
     */
    public SuccessController(WebAppConfig webAppConfig, HttpSession session) {
        this._webAppConfig = webAppConfig;
        this._session = session;
    }

    /**
     * the method for authentication and forwarding to the sign-in success page
     *
     * @param model            model for the user's information
     * @param state            state for checking the request and response are matched
     * @param idToken          id token
     * @param code             authorization code
     * @param error            error code.
     * @param errorDescription error description message.
     * @return The sign-in success page
     * @throws SampleException         The exception class for the invalid state or id token
     * @throws JsonProcessingException The exception class for the JSON processing
     * @throws MalformedURLException   The exception class for the invalid URL
     * @throws JwkException            The exception class for the JSON Web Key
     */
    @GetMapping("success")
    public String successView(Model model,
                              @RequestParam(name = "state", required = false) String state,
                              @RequestParam(name = "id_token", required = false) String idToken,
                              @RequestParam(name = "code", required = false) String code,
                              @RequestParam(name = "error", required = false) String error,
                              @RequestParam(name = "error_description", required = false) String errorDescription)
            throws SampleException, JsonProcessingException, MalformedURLException, JwkException {

        // Get the state and nonce from the session
        String check_state = (String) _session.getAttribute("state");
        // validate the state, then if state from the request is not equal to the state from the session, forward to an error page
        if (state == null || !state.equals(check_state)) {
            // If the state is invalid, forward to an error page
            // Forward to an error page
            throw new SampleException("The state is invalid.");
        }
        // If the error is not null, forward to an error page
        if (error != null) {
            throw new SampleException(errorDescription);
        }

        // validate the id token, then if the id token is null, forward to an error page
        if (idToken == null) {
            // If the id token is invalid, forward to an error page
            throw new SampleException("The id token is null.");
        }
        // validate the id token, then if the id token is invalid, forward to an error page
        var nonce = (Integer) _session.getAttribute("nonce");
        if (!isValidIdToken(idToken, nonce)) {
            throw new SampleException("The id token is invalid.");
        }

        // Get the user's name and access token from the id token
        var template = new RestTemplate();
        var url = "https://" +
                _webAppConfig.getTenant() +
                ".b2clogin.com/" +
                _webAppConfig.getTenant() +
                ".onmicrosoft.com/" +
                _webAppConfig.getUserFlow() +
                "/oauth2/v2.0/token?grant_type={grant_type}&client_id={client_id}&scope={scope}&code={code}&redirect_uri={redirect_uri}&client_secret={client_secret}";

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", _webAppConfig.getClientId());
        String scope = _webAppConfig.getClientId() + " offline_access";
        params.put("scope", scope);
        params.put("code", code);
        params.put("redirect_uri", "urn:ietf:wg:oauth:2.0:oob");
        params.put("client_secret", _webAppConfig.getSecret());
        ResponseEntity<String> response = template.exchange(url, HttpMethod.POST, null, String.class, params);
        String responseBody = response.getBody();

        // Convert to Json strings from HTTP response body.
        var mapper = new ObjectMapper();
        var token = mapper.readValue(responseBody, TokenInfo.class);

        // Get the user's name from the profile_info in Json because the profile info is encoded by BASE64.
        var decoder = Base64.getDecoder();
        var userInfoString = new String(decoder.decode(token.getProfileInfoEncoded()), StandardCharsets.UTF_8);
        var userInfo = mapper.readValue(userInfoString, UserInfo.class);

        // Set the user's name, access token, refresh token to the model.
        var success = new Success();
        success.setUserName(userInfo.getName());
        success.setToken(token.getAccessToken());
        success.setRefreshToken(token.getRefreshToken());
        // Convert the epoch time when the access token becomes valid to a local date/time string
        var nbf = Long.parseLong(token.getNotBefore());
        success.setNbf(_convertEpochToDate(nbf));
        // Convert the epoch time when the access token becomes invalid to a local date/time string
        var exp = nbf + Long.parseLong(token.getExpiresIn());
        success.setExp(_convertEpochToDate(exp));
        // Convert the epoch time when the refresh token becomes invalid to a local date/time string
        exp = nbf + Long.parseLong(token.getRefreshTokenExpiresIn());
        success.setRefreshTokenExp(_convertEpochToDate(exp));
        
        model.addAttribute("success", success);

        // forward to the sign-in success page
        return "success";
    }

    /**
     * validate the id token
     *
     * @param idToken The id token
     * @param nonce   The value that application generated randomly
     * @return true if the id token is valid, otherwise false
     * @throws SampleException       The exception class for the invalid id token
     * @throws MalformedURLException The exception class for the invalid URL
     * @throws JwkException          The exception class for the JSON Web Key
     */
    private boolean isValidIdToken(String idToken, Integer nonce) throws SampleException, MalformedURLException, JwkException {
        // Decode the id token
        var decodedJwt = JWT.decode(idToken);
        var kid = decodedJwt.getKeyId();

        // Get public key from the jwk_uri
        var url = "https://" +
                _webAppConfig.getTenant() +
                ".b2clogin.com/" +
                _webAppConfig.getTenant() +
                ".onmicrosoft.com/" +
                _webAppConfig.getUserFlow() +
                "/discovery/v2.0/keys/";
        var target = new URL(url);
        var provider = new JwkProviderBuilder(target).build();
        var jwk = provider.get(kid);
        var publicKey = (RSAPublicKey) jwk.getPublicKey();

        // verify by the public key and the id token
        var algorithm = Algorithm.RSA256(publicKey, null);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(decodedJwt.getClaim("iss").asString())
                .withSubject(decodedJwt.getClaim("sub").asString())
                .withClaim("nonce", nonce.toString())     // nonce is String in the id token
                .withAudience(decodedJwt.getClaim("aud").asString())
                .build();
        try {
            verifier.verify(idToken);
            // If the id token valid, return true
            return true;
        } catch (JWTVerificationException e) {
            // If the id token invalid, throw an exception
            throw new SampleException(e.getMessage());
        }
    }

    /**
     * Convert the epoch time to the date string
     *
     * @param epochTime The epoch time
     * @return The local date string
     */
    private static String _convertEpochToDate(long epochTime) {
        Locale locale = Locale.JAPAN; // Example locale
        
        // Convert epoch time to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTime), ZoneId.systemDefault());

        // Define the date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", locale);

        // Format the LocalDateTime to a string
        return dateTime.format(formatter);
    }    
}
