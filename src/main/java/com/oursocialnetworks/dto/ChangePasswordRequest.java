package com.oursocialnetworks.dto;

public class ChangePasswordRequest {
    private String currentPassword;  // Mật khẩu hiện tại
    private String newPassword;      // Mật khẩu mới
    private String confirmPassword;  // Xác nhận mật khẩu mới

    public ChangePasswordRequest() {}

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}