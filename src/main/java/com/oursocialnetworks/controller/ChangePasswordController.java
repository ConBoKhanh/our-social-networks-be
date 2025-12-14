package com.oursocialnetworks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChangePasswordController {

    @GetMapping("/change-password")
    public String changePasswordPage(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String isNewUser,
            @RequestParam(required = false) String message,
            Model model
    ) {
        model.addAttribute("email", email != null ? email : "");
        model.addAttribute("isNewUser", "true".equals(isNewUser));
        model.addAttribute("message", message != null ? message : "");
        
        return "change-password";
    }
}