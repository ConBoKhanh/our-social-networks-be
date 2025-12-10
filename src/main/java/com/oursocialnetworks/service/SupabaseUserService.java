package com.oursocialnetworks.service;

import com.oursocialnetworks.config.SupabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
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

        HttpEntity<T> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
    }

    // =========================
    // GENERIC PUT
    // =========================
    public <T, R> ResponseEntity<R> put(String domain, Map<String, String> params, T body, Class<R> responseType) {
        var d = config.getDomains().get(domain);
        var headers = buildHeaders(d.getKey());
        headers.set("Prefer", "return=representation"); // Return updated object
        var url = buildUrl(d.getUrl(), d.getTable(), params);

        HttpEntity<T> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType);
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
        params.put("select", "*");
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
    public <T> ResponseEntity<T> getUserById(Long id, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);
        params.put("status", "eq.1");
        params.put("select", "*");
        return get("user", params, responseType);
    }

    /**
     * Search user by username
     */
    public <T> ResponseEntity<T> searchUserByUsername(String username, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("username", "ilike.*" + username + "*");
        params.put("status", "eq.1");
        params.put("select", "*");
        params.put("limit", "50"); // Limit search results
        return get("user", params, responseType);
    }

    /**
     * Create new user
     */
    public <T, R> ResponseEntity<R> createUser(T user, Class<R> responseType) {
        return post("user", user, responseType);
    }

    /**
     * Update user by ID
     */
    public <T, R> ResponseEntity<R> updateUserById(Long id, T user, Class<R> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);
        return put("user", params, user, responseType);
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
        params.put("select", "*");
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
        params.put("select", "*");
        params.put("limit", "1"); // Only need 1 user
        return get("user", params, responseType);
    }
}