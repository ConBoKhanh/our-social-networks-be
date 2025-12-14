package com.oursocialnetworks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller để hiển thị trang login với OAuth2
 */
@Controller
public class OAuth2LoginController {

    /**
     * Trang login - hiển thị UI với button "Login with Google"
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Trả về view login.html từ templates/login.html
    }

    /**
     * Trang đổi mật khẩu
     */
    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "change-password"; // Trả về view change-password.html từ templates/change-password.html
    }
}