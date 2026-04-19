package com.indralokbright.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String expired,
            Model model) {
        if (error != null) model.addAttribute("errorMsg", "Invalid username or password.");
        if (logout != null) model.addAttribute("successMsg", "You have been logged out successfully.");
        if (expired != null) model.addAttribute("errorMsg", "Session expired. Please login again.");
        return "auth/login";
    }
}
