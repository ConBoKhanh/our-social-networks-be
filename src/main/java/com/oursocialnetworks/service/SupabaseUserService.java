package com.oursocialnetworks.service;

import com.oursocialnetworks.config.SupabaseConfig;
import com.oursocialnetworks.entity.Role;
import com.oursocialnetworks.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupabaseUserService {

    private final SupabaseConfig config;
    private final RestTemplate restTemplate;

    private HttpHeaders buildHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Prefer", "return=representation"); // Always return data
        return headers;
    }

    private String buildUrl(String baseUrl, String table, Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl)
                .append("/rest/v1/")
                .append(table);

        if (params != null && !params.isEmpty()) {
            String queryString = params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
            url.append("?").append(queryString);
        }

        return url.toString();
    }

    // =========================
    // HELPER: User payload without nested Role
    // =========================
    private Map<String, Object> toUserPayload(User user) {
        Map<String, Object> payload = new HashMap<>();
        if (user.getId() != null) payload.put("id", user.getId());
        payload.put("username_login", user.getUsernameLogin());
        payload.put("password_login", user.getPasswordLogin());
        payload.put("image", user.getImage());
        payload.put("username", user.getUsername());
        payload.put("description", user.getDescription());
        payload.put("place_of_residence", user.getPlaceOfResidence());
        payload.put("id_friends", user.getIdFriends());
        payload.put("date-of-birth", user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        payload.put("id_relationship", user.getIdRelationship());
        payload.put("createDate", user.getCreateDate() != null ? user.getCreateDate().toString() : null);
        payload.put("updateDate", user.getUpdateDate() != null ? user.getUpdateDate().toString() : null);
        payload.put("email", user.getEmail());
        payload.put("gmail", user.getGmail());
        payload.put("provider", user.getProvider());
        payload.put("openid_sub", user.getOpenidSub());
        payload.put("email_verified", user.getEmailVerified());
        payload.put("status", user.getStatus());
        if (user.getRoleId() != null) {
            payload.put("role_id", user.getRoleId());
        }
        // Role object intentionally excluded (view only)
        return payload;
    }

    // =========================
    // GENERIC GET
    // =========================
    public <T> ResponseEntity<T> get(String domain, Map<String, String> params, Class<T> responseType) {
        var d = config.getDomains().get(domain);
        var headers = buildHeaders(d.getKey());
        var url = buildUrl(d.getUrl(), d.getTable(), params);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
    }

    // =========================
    // GENERIC POST
    // =========================
    public <T, R> ResponseEntity<R> post(String domain, T body, Class<R> responseType) {
        var d = config.getDomains().get(domain);
        var headers = buildHeaders(d.getKey());
        headers.set("Prefer", "return=representation"); // Return created object
        var url = d.getUrl() + "/rest/v1/" + d.getTable();

        try {
            HttpEntity<T> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
        } catch (RestClientResponseException ex) {
            System.err.println("[Supabase POST error] status=" + ex.getStatusCode() + " body=" + ex.getResponseBodyAsString());
            throw ex;
        }
    }

    // =========================
    // GENERIC PUT
    // =========================
    public <T, R> ResponseEntity<R> put(String domain, Map<String, String> params, T body, Class<R> responseType) {
        var d = config.getDomains().get(domain);
        var headers = buildHeaders(d.getKey());
        headers.set("Prefer", "return=representation"); // Return updated object
        var url = buildUrl(d.getUrl(), d.getTable(), params);

        try {
            System.out.println("========== SUPABASE PUT DEBUG ==========");
            System.out.println("URL: " + url);
            System.out.println("Params: " + params);
            System.out.println("Body: " + body);
            System.out.println("Headers: " + headers);
            System.out.println("========================================");
            
            HttpEntity<T> entity = new HttpEntity<>(body, headers);
            ResponseEntity<R> response = restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
            
            System.out.println("========== SUPABASE PUT RESPONSE ==========");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Response Headers: " + response.getHeaders());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("==========================================");
            
            return response;
        } catch (RestClientResponseException ex) {
            System.err.println("[Supabase PUT error] status=" + ex.getStatusCode() + " body=" + ex.getResponseBodyAsString());
            System.err.println("URL was: " + url);
            System.err.println("Request body was: " + body);
            throw ex;
        }
    }

    // =========================
    // GENERIC PATCH (Alternative to PUT)
    // =========================
    public <T, R> ResponseEntity<R> patch(String domain, Map<String, String> params, T body, Class<R> responseType) {
        var d = config.getDomains().get(domain);
        var headers = buildHeaders(d.getKey());
        headers.set("Prefer", "return=representation"); // Return updated object
        var url = buildUrl(d.getUrl(), d.getTable(), params);

        try {
            System.out.println("========== SUPABASE PATCH DEBUG ==========");
            System.out.println("URL: " + url);
            System.out.println("Params: " + params);
            System.out.println("Body: " + body);
            System.out.println("==========================================");
            
            HttpEntity<T> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, HttpMethod.PATCH, entity, responseType);
        } catch (RestClientResponseException ex) {
            System.err.println("[Supabase PATCH error] status=" + ex.getStatusCode() + " body=" + ex.getResponseBodyAsString());
            System.err.println("URL was: " + url);
            throw ex;
        }
    }

    // =========================
    // GENERIC DELETE
    // =========================
    public <T> ResponseEntity<T> delete(String domain, Map<String, String> params, Class<T> responseType) {
        var d = config.getDomains().get(domain);
        var headers = buildHeaders(d.getKey());
        var url = buildUrl(d.getUrl(), d.getTable(), params);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType);
    }

    // =========================
    // USER SPECIFIC METHODS - OPTIMIZED
    // =========================

    /**
     * Get all active users with pagination
     */
    public <T> ResponseEntity<T> getAllActiveUsers(Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("select", "*,Role(*)");
        params.put("status", "eq.1");
        params.put("order", "id.desc"); // Newest first
        params.put("limit", "100"); // Limit to prevent timeout

        System.out.println("========== GET ALL ACTIVE USERS (OPTIMIZED) SUPPERUSERSVERICE ==========");
        ResponseEntity<T> response = get("user", params, responseType);
        System.out.println("Response status: " + response.getStatusCode());
        if (response.getBody() != null && response.getBody().getClass().isArray()) {
            Object[] array = (Object[]) response.getBody();
            System.out.println("Number of users: " + array.length);
            for (Object obj : array) {
                System.out.println("User: " + obj);
            }
        }
        System.out.println("====================================================");

        return response;
    }

    /**
     * Get user by ID with status = 1
     */
    public <T> ResponseEntity<T> getUserById(String id, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);
        params.put("status", "eq.1");
        params.put("select", "*,Role(*)");
        return get("user", params, responseType);
    }

    /**
     * Search user by username
     */
    public <T> ResponseEntity<T> searchUserByUsername(String username, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("username", "ilike.*" + username + "*");
        params.put("status", "eq.1");
        params.put("select", "*,Role(*)");
        params.put("limit", "50"); // Limit search results
        return get("user", params, responseType);
    }

    /**
     * Create new user
     */
    public <R> ResponseEntity<R> createUser(User user, Class<R> responseType) {
        return post("user", toUserPayload(user), responseType);
    }

    /**
     * Update user by ID (UUID version)
     */
    public <T, R> ResponseEntity<R> updateUserById(java.util.UUID id, T user, Class<R> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id.toString());

        Object body = user;

        // ✅ Cách cũ - tương thích Java 15
        if (user instanceof User) {
            User u = (User) user;
            body = toUserPayload(u);
        }

        return put("user", params, body, responseType);
    }

    /**
     * Update user by ID (Long version - legacy)
     */
    public <T, R> ResponseEntity<R> updateUserById(Long id, T user, Class<R> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);

        Object body = user;

        // ✅ Cách cũ - tương thích Java 15
        if (user instanceof User) {
            User u = (User) user;
            body = toUserPayload(u);
        }

        return put("user", params, body, responseType);
    }

    /**
     * Soft delete - Update status = 0 (UUID version)
     */
    public <T> ResponseEntity<T> softDeleteUser(java.util.UUID id, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id.toString());

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", 0);
        updateData.put("updateDate", java.time.LocalDate.now().toString());

        return patch("user", params, updateData, responseType);
    }

    /**
     * Restore user (set status = 1) (UUID version)
     */
    public <T> ResponseEntity<T> restoreUser(java.util.UUID id, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id.toString());

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", 1);
        updateData.put("updateDate", java.time.LocalDate.now().toString());

        return patch("user", params, updateData, responseType);
    }

    /**
     * Soft delete - Update status = 0 (Long version - legacy)
     */
    public <T> ResponseEntity<T> softDeleteUser(Long id, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", 0);
        updateData.put("updateDate", java.time.LocalDate.now().toString());

        return put("user", params, updateData, responseType);
    }

    /**
     * Restore user (set status = 1) (Long version - legacy)
     */
    public <T> ResponseEntity<T> restoreUser(Long id, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", 1);
        updateData.put("updateDate", java.time.LocalDate.now().toString());

        return patch("user", params, updateData, responseType);
    }

    /**
     * Get all deleted users (status = 0)
     */
    public <T> ResponseEntity<T> getDeletedUsers(Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("status", "eq.0");
        params.put("select", "*,Role(*)");
        params.put("order", "updateDate.desc");
        params.put("limit", "50");
        return get("user", params, responseType);
    }

    /**
     * Login check
     */
    public <T> ResponseEntity<T> loginUser(String username, String password, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("username_login", "eq." + username);
        params.put("password_login", "eq." + password);
        params.put("status", "eq.1");
        params.put("select", "*,Role(*)");
        params.put("limit", "1"); // Only need 1 user
        return get("user", params, responseType);
    }

    /**
     * Find user by email.
     * If not exist → create new user with random password and status = 2.
     * Returns UserCreationResult with user info and creation status.
     */
    public UserCreationResult findOrCreateUser(String email) {
        try {
            // 1. Tìm user theo email
            Map<String, String> params = new HashMap<>();
            params.put("email", "eq." + email);
            params.put("limit", "1");
            params.put("select", "*,Role(*)");

            ResponseEntity<User[]> response = get("user", params, User[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                System.out.println("User đã tồn tại: " + email);
                User existingUser = response.getBody()[0];
                return new UserCreationResult(existingUser, false, null);
            }

            // 2. Tìm role "User" để lấy UUID
            UUID defaultRoleId = getDefaultRoleId();

            // 3. Tạo username và password random
            String username = email.split("@")[0] + "_" + System.currentTimeMillis();
            String tempPassword = generateRandomPassword();

            // 4. Tạo user mới với status = 2 (cần đổi mật khẩu)
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("email", email);
            newUser.put("gmail", email);
            newUser.put("username", username);
            newUser.put("username_login", username);
            newUser.put("password_login", tempPassword);
            newUser.put("status", 2);  // ✅ Status = 2 (cần đổi mật khẩu)
            newUser.put("provider", "google");
            newUser.put("email_verified", true);
            newUser.put("createDate", LocalDate.now().toString());
            newUser.put("updateDate", LocalDate.now().toString());
            newUser.put("role_id", defaultRoleId.toString());

            ResponseEntity<User[]> created = post("user", newUser, User[].class);

            if (created.getBody() != null && created.getBody().length > 0) {
                System.out.println("Tạo user mới thành công: " + email);
                User newUserObj = created.getBody()[0];
                return new UserCreationResult(newUserObj, true, tempPassword);
            }

            throw new RuntimeException("Không thể tạo user");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi xử lý findOrCreateUser: " + e.getMessage());
        }
    }

    /**
     * Generate random password (8 characters)
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return password.toString();
    }

    /**
     * Result class for user creation
     */
    public static class UserCreationResult {
        private final User user;
        private final boolean isNewUser;
        private final String tempPassword;

        public UserCreationResult(User user, boolean isNewUser, String tempPassword) {
            this.user = user;
            this.isNewUser = isNewUser;
            this.tempPassword = tempPassword;
        }

        public User getUser() { return user; }
        public boolean isNewUser() { return isNewUser; }
        public String getTempPassword() { return tempPassword; }
    }

    // ✅ Hàm tìm role "User"
    private UUID getDefaultRoleId() {
        try {
            // Tạo UUID mặc định cho role "User" (có thể hardcode hoặc query từ DB)
            // Thay thế bằng UUID thực tế của role "User" trong database của bạn
            return UUID.fromString("550e8400-e29b-41d4-a716-446655440000"); // Example UUID
            
            /* Nếu muốn query từ DB, uncomment code dưới:
            Map<String, String> params = new HashMap<>();
            params.put("role", "eq.User");  // Tìm role có tên là "User"
            params.put("status", "eq.1");
            params.put("limit", "1");

            ResponseEntity<Role[]> response = get("role", params, Role[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                return response.getBody()[0].getId();
            }

            throw new RuntimeException("Không tìm thấy role 'User' trong hệ thống");
            */

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to default UUID if error
            return UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        }
    }

}