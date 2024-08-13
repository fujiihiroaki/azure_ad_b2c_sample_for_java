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
import jp.co.dsas.azureadb2c.sample.Exception.SampleException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the log-out page
 */
@Controller
public class SignOutController {

    private final HttpSession _session;

    /**
     * Constructor
     * 
     * @param session HttpSession
     */
    public SignOutController(HttpSession session) {
        this._session = session;
    }
    
    /**
     * Redirect to the sign-out page
     * 
     * @param state The state for checking the request and response are matched
     * @return The sign-out page
     * @throws SampleException The exception class for the invalid state
     */
    @GetMapping("sign_out")
    public String signOutView( @RequestParam(name = "state", required = false) String state) throws SampleException {
        // Get the state from the session
        String check_state = (String) _session.getAttribute("state");
        // validate the state, then if state from the request is not equal to the state from the session, forward to an error page
        if (state == null || !state.equals(check_state)) {
            // If the state is invalid, forward to an error page
            // Forward to an error page
            throw new SampleException("The state is invalid.");
        }
        
        // Invalidate the session
        _session.invalidate();
        
        // Forward to the sign-out page
        return "out";
    }
}
