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

import jp.co.dsas.azureadb2c.sample.Exception.SampleException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Error handling for the controller.
 */
@ControllerAdvice
public class SampleControllerAdvice {
    /**
     * Handle the exception.
     *
     * @param e     The exception.
     * @param model model for the user's information
     * @return The error page.
     */
    @ExceptionHandler(SampleException.class)
    public String handleException(SampleException e, Model model) {
        // Save the error message to the model.
        var message = e.getMessage();
        model.addAttribute("message", message);

        // Forward to the error page.
        return "error";
    }
}
