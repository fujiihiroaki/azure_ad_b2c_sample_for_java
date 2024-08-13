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

import jakarta.servlet.http.HttpSession;
import jp.co.dsas.azureadb2c.sample.config.WebAppConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

/**
 * Controller for the logout
 */
@Controller
public class LogoutController {

    private final WebAppConfig _webAppConfig;

    private final HttpSession _session;

    /**
     * Constructor
     *
     * @param webAppConfig web app config
     * @param session      session
     */
    public LogoutController(WebAppConfig webAppConfig, HttpSession session) {
        this._webAppConfig = webAppConfig;
        this._session = session;
    }

    /**
     * Redirect to the azure ad b2c sign-out page
     *
     * @return The sign-out page
     */
    @GetMapping("logout")
    public String logoutView() {
        // Create the URL for the azure ad b2c sign-out page
        StringBuilder url = new StringBuilder();
        url.append("https://");
        url.append(_webAppConfig.getTenant());
        url.append(".b2clogin.com/");
        url.append(_webAppConfig.getTenant());
        url.append(".onmicrosoft.com/");
        url.append(_webAppConfig.getUserFlow());
        url.append("/oauth2/v2.0/logout?");
        url.append("redirect_uri=http://localhost:8080/sign_out");
        url.append("&state=");
        UUID uuid = UUID.randomUUID();
        String state = uuid.toString();
        url.append(state);

        // Save the state in the session for later verification
        _session.setAttribute("state", state);

        // redirect to the azure ad b2c sign-out page
        String redirectUri = url.toString();
        System.out.println(redirectUri);

        return "redirect:" + redirectUri;
    }
}
