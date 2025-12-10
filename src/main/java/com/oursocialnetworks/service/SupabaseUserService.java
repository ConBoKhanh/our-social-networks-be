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
    // USER SPECIFIC METHODS
    // =========================

    /**
     * Lấy TẤT CẢ users KHÔNG filter (để test)
     */
    public <T> ResponseEntity<T> getAllUsersNoFilter(Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("select", "*");

//        System.out.println("========== GET ALL USERS (NO FILTER) ==========");
        ResponseEntity<T> response = get("user", params, responseType);
//        System.out.println("Response status: " + response.getStatusCode());

        // Convert array to readable string
//        if (response.getBody() != null && response.getBody().getClass().isArray()) {
//            System.out.println("Response body: " + Arrays.toString((Object[]) response.getBody()));
//        } else {
//            System.out.println("Response body: " + response.getBody());
//        }

//        System.out.println("===============================================");
        return response;
    }

    /**
     * Lấy tất cả users với status = 1 (active)
     */
    public <T> ResponseEntity<T> getAllActiveUsers(Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
//        params.put("status", "eq.1");
        params.put("select", "*");

//        System.out.println("========== GET ALL ACTIVE USERS ==========");
//        System.out.println("Domain config exists: " + (config.getDomains().get("user") != null));

        if (config.getDomains().get("user") != null) {
            var d = config.getDomains().get("user");
//            System.out.println("URL: " + d.getUrl());
//            System.out.println("Table: " + d.getTable());
//            System.out.println("Key exists: " + (d.getKey() != null));
        }

        ResponseEntity<T> response = get("user", params, responseType);
//        System.out.println("Response status: " + response.getStatusCode());

        // Convert array to readable string
        if (response.getBody() != null && response.getBody().getClass().isArray()) {
            Object[] array = (Object[]) response.getBody();
//            System.out.println("Number of users: " + array.length);
//            System.out.println("Response body: " + Arrays.toString(array));
        } else {
//            System.out.println("Response body: " + response.getBody());
        }

//        System.out.println("==========================================");
        return response;
    }

    /**
     * Lấy user theo ID với status = 1
     */
    public <T> ResponseEntity<T> getUserById(Long id, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);
//        params.put("status", "eq.1");
        params.put("select", "*");
        return get("user", params, responseType);
    }

    /**
     * Tìm kiếm user theo username
     */
    public <T> ResponseEntity<T> searchUserByUsername(String username, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("username", "ilike.*" + username + "*");
//        params.put("status", "eq.1");
        params.put("select", "*");
        return get("user", params, responseType);
    }

    /**
     * Tạo user mới
     */
    public <T, R> ResponseEntity<R> createUser(T user, Class<R> responseType) {
        return post("user", user, responseType);
    }

    /**
     * Cập nhật user theo ID
     */
    public <T, R> ResponseEntity<R> updateUserById(Long id, T user, Class<R> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);
        return put("user", params, user, responseType);
    }

    /**
     * Xóa mềm - Cập nhật status = 0 (soft delete)
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
     * Khôi phục user (set status = 1)
     */
    public <T> ResponseEntity<T> restoreUser(Long id, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", 1);
        updateData.put("updateDate", java.time.LocalDate.now().toString());

        return put("user", params, updateData, responseType);
    }

    /**
     * Lấy tất cả users đã xóa (status = 0)
     */
    public <T> ResponseEntity<T> getDeletedUsers(Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("status", "eq.0");
        params.put("select", "*");
        return get("user", params, responseType);
    }

    /**
     * Kiểm tra login
     */
    public <T> ResponseEntity<T> loginUser(String username, String password, Class<T> responseType) {
        Map<String, String> params = new HashMap<>();
        params.put("username_login", "eq." + username);
        params.put("password_login", "eq." + password);
        params.put("status", "eq.1");
        params.put("select", "*");
        return get("user", params, responseType);
    }
}