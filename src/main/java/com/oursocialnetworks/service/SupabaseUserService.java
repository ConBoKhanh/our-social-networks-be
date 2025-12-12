package com.oursocialnetworks.service;

import com.oursocialnetworks.config.SupabaseConfig;
import com.oursocialnetworks.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
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
            HttpEntity<T> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
        } catch (RestClientResponseException ex) {
            System.err.println("[Supabase PUT error] status=" + ex.getStatusCode() + " body=" + ex.getResponseBodyAsString());
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
     * Update user by ID
     */
    public <T, R> ResponseEntity<R> updateUserById(Long id, T user, Class<R> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);
        Object body = user;
        if (user instanceof User u) {
            body = toUserPayload(u);
        }
        return put("user", params, body, responseType);
    }

    /**
     * Soft delete - Update status = 0
     */
    public <T> ResponseEntity<T> softDeleteUser(Long id, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", 0);
        updateData.put("updateDate", java.time.OffsetDateTime.now().toString());

        return put("user", params, updateData, responseType);
    }

    /**
     * Restore user (set status = 1)
     */
    public <T> ResponseEntity<T> restoreUser(Long id, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", 1);
        updateData.put("updateDate", java.time.OffsetDateTime.now().toString());

        return put("user", params, updateData, responseType);
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
     * If not exist → create new user.
     */
    public User findOrCreateUser(String email) {

        try {
            // 1. Tìm user theo email
            Map<String, String> params = new HashMap<>();
            params.put("email", "eq." + email);
            params.put("limit", "1");

            ResponseEntity<User[]> response =
                    get("user", params, User[].class);

            if (response.getBody() != null &&
                    response.getBody().length > 0) {

                System.out.println("User đã tồn tại: " + email);
                return response.getBody()[0];
            }

            // 2. Không tìm thấy → tạo user mới
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("email", email);
            newUser.put("username", email.split("@")[0]);
            newUser.put("status", 1);
            newUser.put("createDate", java.time.OffsetDateTime.now().toString());
            newUser.put("updateDate", java.time.OffsetDateTime.now().toString());

            ResponseEntity<User[]> created =
                    post("user", newUser, User[].class);

            if (created.getBody() != null && created.getBody().length > 0) {
                System.out.println("Tạo user mới thành công: " + email);
                return created.getBody()[0];
            }

            throw new RuntimeException("Không thể tạo user");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi xử lý findOrCreateUser: " + e.getMessage());
        }
    }

}