package com.oursocialnetworks.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;

@Service
public class OtpService {
    
    // Store OTP with expiration: email -> {otp, expireTime, type}
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    
    // OTP valid for 5 minutes
    private static final long OTP_VALIDITY_MS = 5 * 60 * 1000;
    
    public static class OtpData {
        public String otp;
        public long expireTime;
        public String type; // "register" or "forgot"
        
        public OtpData(String otp, long expireTime, String type) {
            this.otp = otp;
            this.expireTime = expireTime;
            this.type = type;
        }
    }
    
    /**
     * Generate 6-digit OTP
     */
    public String generateOtp(String email, String type) {
        String otp = String.format("%06d", random.nextInt(1000000));
        long expireTime = System.currentTimeMillis() + OTP_VALIDITY_MS;
        otpStore.put(email.toLowerCase(), new OtpData(otp, expireTime, type));
        
        System.out.println("🔐 [OTP] Generated for " + email + ": " + otp + " (type: " + type + ")");
        return otp;
    }
    
    /**
     * Verify OTP
     */
    public boolean verifyOtp(String email, String otp, String type) {
        OtpData data = otpStore.get(email.toLowerCase());
        
        if (data == null) {
            System.out.println("🔐 [OTP] No OTP found for: " + email);
            return false;
        }
        
        if (System.currentTimeMillis() > data.expireTime) {
            System.out.println("🔐 [OTP] Expired for: " + email);
            otpStore.remove(email.toLowerCase());
            return false;
        }
        
        if (!data.type.equals(type)) {
            System.out.println("🔐 [OTP] Type mismatch for: " + email);
            return false;
        }
        
        if (!data.otp.equals(otp)) {
            System.out.println("🔐 [OTP] Invalid OTP for: " + email);
            return false;
        }
        
        System.out.println("✅ [OTP] Verified for: " + email);
        return true;
    }
    
    /**
     * Remove OTP after successful verification
     */
    public void removeOtp(String email) {
        otpStore.remove(email.toLowerCase());
    }
    
    /**
     * Check if OTP exists and not expired
     */
    public boolean hasValidOtp(String email) {
        OtpData data = otpStore.get(email.toLowerCase());
        return data != null && System.currentTimeMillis() <= data.expireTime;
    }
}
