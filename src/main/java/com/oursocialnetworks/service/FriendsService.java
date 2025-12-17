package com.oursocialnetworks.service;

import com.oursocialnetworks.config.SupabaseConfig;
import com.oursocialnetworks.entity.FriendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendsService {

    private final SupabaseConfig config;
    private final RestTemplate restTemplate;

    private HttpHeaders buildHeaders() {
        var d = config.getDomains().get("friends");
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", d.getKey());
        headers.set("Authorization", "Bearer " + d.getKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Prefer", "return=representation");
        return headers;
    }

    private String buildUrl(Map<String, String> params) {
        var d = config.getDomains().get("friends");
        StringBuilder url = new StringBuilder(d.getUrl())
                .append("/rest/v1/")
                .append(d.getTable());

        if (params != null && !params.isEmpty()) {
            String queryString = params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
            url.append("?").append(queryString);
        }
        return url.toString();
    }


    /**
     * Lấy danh sách lời mời follow đang chờ (Pending)
     * Người nhận = currentUserId
     */
    public FriendRequest[] getPendingRequests(UUID currentUserId, int page, int size) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("friend_id", "eq." + currentUserId.toString());
            params.put("status_fr", "eq.Pending");
            params.put("status", "eq.1");
            params.put("order", "id.desc");
            params.put("limit", String.valueOf(size));
            params.put("offset", String.valueOf(page * size));

            String url = buildUrl(params);
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders());

            System.out.println("========== GET PENDING REQUESTS ==========");
            System.out.println("URL: " + url);
            System.out.println("Current User ID: " + currentUserId);
            System.out.println("Page: " + page + ", Size: " + size);

            ResponseEntity<FriendRequest[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, FriendRequest[].class);

            System.out.println("Response: " + Arrays.toString(response.getBody()));
            return response.getBody() != null ? response.getBody() : new FriendRequest[0];

        } catch (RestClientResponseException ex) {
            System.err.println("[Friends GET error] " + ex.getResponseBodyAsString());
            throw ex;
        }
    }

    /**
     * Lấy danh sách người đang follow mình (Followers)
     * friend_id = currentUserId AND status_fr = Done
     */
    public FriendRequest[] getFollowers(UUID currentUserId, int page, int size) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("friend_id", "eq." + currentUserId.toString());
            params.put("status_fr", "eq.Done");
            params.put("status", "eq.1");
            params.put("order", "id.desc");
            params.put("limit", String.valueOf(size));
            params.put("offset", String.valueOf(page * size));

            String url = buildUrl(params);
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders());

            ResponseEntity<FriendRequest[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, FriendRequest[].class);

            return response.getBody() != null ? response.getBody() : new FriendRequest[0];

        } catch (RestClientResponseException ex) {
            System.err.println("[Friends GET error] " + ex.getResponseBodyAsString());
            throw ex;
        }
    }

    /**
     * Lấy danh sách người mình đang follow (Following)
     * id_user = currentUserId AND status_fr = Done
     */
    public FriendRequest[] getFollowing(UUID currentUserId, int page, int size) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("id_user", "eq." + currentUserId.toString());
            params.put("status_fr", "eq.Done");
            params.put("status", "eq.1");
            params.put("order", "id.desc");
            params.put("limit", String.valueOf(size));
            params.put("offset", String.valueOf(page * size));

            String url = buildUrl(params);
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders());

            ResponseEntity<FriendRequest[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, FriendRequest[].class);

            return response.getBody() != null ? response.getBody() : new FriendRequest[0];

        } catch (RestClientResponseException ex) {
            System.err.println("[Friends GET error] " + ex.getResponseBodyAsString());
            throw ex;
        }
    }



    /**
     * Gửi lời mời kết bạn
     */
    public FriendRequest sendFriendRequest(UUID senderId, UUID receiverId) {
        try {
            // Kiểm tra đã có request chưa
            if (checkExistingRequest(senderId, receiverId)) {
                throw new RuntimeException("Lời mời kết bạn đã tồn tại!");
            }

            var d = config.getDomains().get("friends");
            String url = d.getUrl() + "/rest/v1/" + d.getTable();

            Map<String, Object> body = new HashMap<>();
            body.put("id_user", senderId.toString());
            body.put("friend_id", receiverId.toString());
            body.put("status_fr", "Pending");
            body.put("status", 1);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());

            System.out.println("========== SEND FRIEND REQUEST ==========");
            System.out.println("URL: " + url);
            System.out.println("Body: " + body);

            ResponseEntity<FriendRequest[]> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, FriendRequest[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                return response.getBody()[0];
            }
            throw new RuntimeException("Không thể gửi lời mời kết bạn");

        } catch (RestClientResponseException ex) {
            System.err.println("[Friends POST error] " + ex.getResponseBodyAsString());
            throw ex;
        }
    }

    /**
     * Chấp nhận lời mời kết bạn
     */
    public FriendRequest acceptFriendRequest(Long requestId, UUID currentUserId) {
        try {
            // Verify request belongs to current user
            FriendRequest request = getRequestById(requestId);
            if (request == null) {
                throw new RuntimeException("Không tìm thấy lời mời kết bạn!");
            }
            if (!request.getFriendId().equals(currentUserId)) {
                throw new RuntimeException("Bạn không có quyền chấp nhận lời mời này!");
            }

            Map<String, String> params = new HashMap<>();
            params.put("id", "eq." + requestId);

            String url = buildUrl(params);

            Map<String, Object> body = new HashMap<>();
            body.put("status_fr", "Done");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());

            System.out.println("========== ACCEPT FRIEND REQUEST ==========");
            System.out.println("URL: " + url);

            ResponseEntity<FriendRequest[]> response = restTemplate.exchange(
                    url, HttpMethod.PATCH, entity, FriendRequest[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                return response.getBody()[0];
            }
            throw new RuntimeException("Không thể chấp nhận lời mời");

        } catch (RestClientResponseException ex) {
            System.err.println("[Friends PATCH error] " + ex.getResponseBodyAsString());
            throw ex;
        }
    }

    /**
     * Từ chối lời mời kết bạn (soft delete)
     */
    public FriendRequest rejectFriendRequest(Long requestId, UUID currentUserId) {
        try {
            FriendRequest request = getRequestById(requestId);
            if (request == null) {
                throw new RuntimeException("Không tìm thấy lời mời kết bạn!");
            }
            if (!request.getFriendId().equals(currentUserId)) {
                throw new RuntimeException("Bạn không có quyền từ chối lời mời này!");
            }

            Map<String, String> params = new HashMap<>();
            params.put("id", "eq." + requestId);

            String url = buildUrl(params);

            Map<String, Object> body = new HashMap<>();
            body.put("status", 0);  // Soft delete

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());

            System.out.println("========== REJECT FRIEND REQUEST ==========");
            System.out.println("URL: " + url);

            ResponseEntity<FriendRequest[]> response = restTemplate.exchange(
                    url, HttpMethod.PATCH, entity, FriendRequest[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                return response.getBody()[0];
            }
            throw new RuntimeException("Không thể từ chối lời mời");

        } catch (RestClientResponseException ex) {
            System.err.println("[Friends PATCH error] " + ex.getResponseBodyAsString());
            throw ex;
        }
    }

    /**
     * Kiểm tra trạng thái follow giữa 2 user
     * Returns: 
     * - "none": Không có record trong DB
     * - "pending_sent": Tôi gửi yêu cầu (id_user=me, friend_id=them, status_fr=Pending)
     * - "pending_received": Tôi nhận yêu cầu (id_user=them, friend_id=me, status_fr=Pending)
     * - "following": Tôi follow họ (id_user=me, friend_id=them, status_fr=Done)
     * - "follower": Họ follow tôi (id_user=them, friend_id=me, status_fr=Done)
     * - "mutual": Cả 2 follow nhau (2 records Done)
     */
    public String checkFollowStatus(UUID currentUserId, UUID targetUserId) {
        try {
            // Check: Tôi -> Họ (id_user=me, friend_id=them)
            Map<String, String> params1 = new HashMap<>();
            params1.put("id_user", "eq." + currentUserId);
            params1.put("friend_id", "eq." + targetUserId);
            params1.put("status", "eq.1");
            
            String url1 = buildUrl(params1);
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders());
            ResponseEntity<FriendRequest[]> response1 = restTemplate.exchange(
                    url1, HttpMethod.GET, entity, FriendRequest[].class);
            
            // Check: Họ -> Tôi (id_user=them, friend_id=me)
            Map<String, String> params2 = new HashMap<>();
            params2.put("id_user", "eq." + targetUserId);
            params2.put("friend_id", "eq." + currentUserId);
            params2.put("status", "eq.1");
            
            String url2 = buildUrl(params2);
            ResponseEntity<FriendRequest[]> response2 = restTemplate.exchange(
                    url2, HttpMethod.GET, entity, FriendRequest[].class);
            
            FriendRequest meToThem = (response1.getBody() != null && response1.getBody().length > 0) 
                ? response1.getBody()[0] : null;
            FriendRequest themToMe = (response2.getBody() != null && response2.getBody().length > 0) 
                ? response2.getBody()[0] : null;
            
            // Cả 2 đều Done = mutual
            if (meToThem != null && "Done".equals(meToThem.getStatusFr()) &&
                themToMe != null && "Done".equals(themToMe.getStatusFr())) {
                return "mutual";
            }
            
            // Tôi -> Họ
            if (meToThem != null) {
                return "Pending".equals(meToThem.getStatusFr()) ? "pending_sent" : "following";
            }
            
            // Họ -> Tôi
            if (themToMe != null) {
                return "Pending".equals(themToMe.getStatusFr()) ? "pending_received" : "follower";
            }
            
            // Không có record
            return "none";
            
        } catch (Exception e) {
            System.err.println("[Check follow status error] " + e.getMessage());
            return "none";
        }
    }

    /**
     * Unfollow user
     */
    public boolean unfollowUser(UUID currentUserId, UUID targetUserId) {
        try {
            // Tìm record where id_user = current AND friend_id = target
            Map<String, String> params = new HashMap<>();
            params.put("id_user", "eq." + currentUserId);
            params.put("friend_id", "eq." + targetUserId);
            params.put("status", "eq.1");
            
            String url = buildUrl(params);
            HttpEntity<?> entity = new HttpEntity<>(buildHeaders());
            ResponseEntity<FriendRequest[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, FriendRequest[].class);
            
            if (response.getBody() != null && response.getBody().length > 0) {
                Long requestId = response.getBody()[0].getId();
                
                // Soft delete
                Map<String, String> deleteParams = new HashMap<>();
                deleteParams.put("id", "eq." + requestId);
                String deleteUrl = buildUrl(deleteParams);
                
                Map<String, Object> body = new HashMap<>();
                body.put("status", 0);
                
                HttpEntity<Map<String, Object>> deleteEntity = new HttpEntity<>(body, buildHeaders());
                restTemplate.exchange(deleteUrl, HttpMethod.PATCH, deleteEntity, FriendRequest[].class);
                
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("[Unfollow error] " + e.getMessage());
            return false;
        }
    }

    /**
     * Hủy kết bạn (deprecated)
     */
    public boolean unfriend(Long requestId, UUID currentUserId) {
        try {
            FriendRequest request = getRequestById(requestId);
            if (request == null) {
                throw new RuntimeException("Không tìm thấy quan hệ bạn bè!");
            }
            // User phải là 1 trong 2 người trong quan hệ
            if (!request.getIdUser().equals(currentUserId) && !request.getFriendId().equals(currentUserId)) {
                throw new RuntimeException("Bạn không có quyền hủy kết bạn này!");
            }

            Map<String, String> params = new HashMap<>();
            params.put("id", "eq." + requestId);

            String url = buildUrl(params);

            Map<String, Object> body = new HashMap<>();
            body.put("status", 0);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());

            restTemplate.exchange(url, HttpMethod.PATCH, entity, FriendRequest[].class);
            return true;

        } catch (RestClientResponseException ex) {
            System.err.println("[Friends PATCH error] " + ex.getResponseBodyAsString());
            throw ex;
        }
    }

    // ========== HELPER METHODS ==========

    private FriendRequest getRequestById(Long id) {
        Map<String, String> params = new HashMap<>();
        params.put("id", "eq." + id);
        params.put("status", "eq.1");

        String url = buildUrl(params);
        HttpEntity<?> entity = new HttpEntity<>(buildHeaders());

        ResponseEntity<FriendRequest[]> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, FriendRequest[].class);

        if (response.getBody() != null && response.getBody().length > 0) {
            return response.getBody()[0];
        }
        return null;
    }

    private boolean checkExistingRequest(UUID senderId, UUID receiverId) {
        Map<String, String> params = new HashMap<>();
        // Check cả 2 chiều
        params.put("or", "(and(id_user.eq." + senderId + ",friend_id.eq." + receiverId + ")," +
                "and(id_user.eq." + receiverId + ",friend_id.eq." + senderId + "))");
        params.put("status", "eq.1");

        String url = buildUrl(params);
        HttpEntity<?> entity = new HttpEntity<>(buildHeaders());

        ResponseEntity<FriendRequest[]> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, FriendRequest[].class);

        return response.getBody() != null && response.getBody().length > 0;
    }
}
