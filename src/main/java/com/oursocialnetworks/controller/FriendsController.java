package com.oursocialnetworks.controller;

import com.oursocialnetworks.component.AuthUtils;
import com.oursocialnetworks.entity.FriendRequest;
import com.oursocialnetworks.service.FriendsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "Friends", description = "API quản lý bạn bè")
@SecurityRequirement(name = "Bearer Authentication")
public class FriendsController {

    private final FriendsService friendsService;
    private final AuthUtils authUtils;

    @GetMapping("/requests")
    @Operation(summary = "Lấy danh sách lời mời follow đang chờ")
    public ResponseEntity<?> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            FriendRequest[] requests = friendsService.getPendingRequests(currentUserId, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", requests);
            response.put("count", requests.length);
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/followers")
    @Operation(summary = "Lấy danh sách người đang follow mình (Followers)")
    public ResponseEntity<?> getFollowers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            FriendRequest[] followers = friendsService.getFollowers(currentUserId, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", followers);
            response.put("count", followers.length);
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/following")
    @Operation(summary = "Lấy danh sách người mình đang follow (Following)")
    public ResponseEntity<?> getFollowing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            FriendRequest[] following = friendsService.getFollowing(currentUserId, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", following);
            response.put("count", following.length);
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/followers/{userId}")
    @Operation(summary = "Lấy danh sách followers của user khác")
    public ResponseEntity<?> getUserFollowers(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            UUID targetUserId = UUID.fromString(userId);
            FriendRequest[] followers = friendsService.getFollowers(targetUserId, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", followers);
            response.put("count", followers.length);
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/following/{userId}")
    @Operation(summary = "Lấy danh sách following của user khác")
    public ResponseEntity<?> getUserFollowing(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            UUID targetUserId = UUID.fromString(userId);
            FriendRequest[] following = friendsService.getFollowing(targetUserId, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", following);
            response.put("count", following.length);
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/status/{userId}")
    @Operation(
        summary = "Kiểm tra trạng thái follow với user",
        description = "Trả về:\n" +
            "- none: Không có quan hệ\n" +
            "- pending_sent: Đã gửi yêu cầu follow\n" +
            "- pending_received: Nhận được yêu cầu follow\n" +
            "- following: Đang follow họ\n" +
            "- follower: Họ đang follow mình\n" +
            "- mutual: Follow lẫn nhau"
    )
    public ResponseEntity<?> checkFollowStatus(@PathVariable String userId) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            UUID targetUserId = UUID.fromString(userId);
            
            String status = friendsService.checkFollowStatus(currentUserId, targetUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("followStatus", status);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @PostMapping("/follow/{userId}")
    @Operation(summary = "Follow user (gửi yêu cầu follow)")
    public ResponseEntity<?> followUser(@PathVariable String userId) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            UUID targetUserId = UUID.fromString(userId);
            
            if (currentUserId.equals(targetUserId)) {
                return authUtils.buildErrorResponse("Không thể follow chính mình!");
            }
            
            FriendRequest request = friendsService.sendFriendRequest(currentUserId, targetUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã gửi yêu cầu follow!");
            response.put("data", request);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @DeleteMapping("/unfollow/{userId}")
    @Operation(summary = "Unfollow user")
    public ResponseEntity<?> unfollowUser(@PathVariable String userId) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            UUID targetUserId = UUID.fromString(userId);
            
            boolean success = friendsService.unfollowUser(currentUserId, targetUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", success ? "Đã unfollow!" : "Không tìm thấy quan hệ follow");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @PostMapping("/request")
    @Operation(
        summary = "Gửi lời mời follow (deprecated - dùng POST /follow/{userId})",
        description = "API cũ, khuyến nghị dùng POST /api/friends/follow/{userId}"
    )
    public ResponseEntity<?> sendFriendRequest(@RequestBody com.oursocialnetworks.dto.FollowRequest request) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            String friendIdStr = request.getFriendId();

            if (friendIdStr == null || friendIdStr.isEmpty()) {
                return authUtils.buildErrorResponse("friendId là bắt buộc!");
            }

            UUID friendId;
            try {
                friendId = UUID.fromString(friendIdStr);
            } catch (IllegalArgumentException e) {
                return authUtils.buildErrorResponse("friendId không hợp lệ!");
            }

            if (currentUserId.equals(friendId)) {
                return authUtils.buildErrorResponse("Không thể follow chính mình!");
            }

            FriendRequest friendRequest = friendsService.sendFriendRequest(currentUserId, friendId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã gửi yêu cầu follow!");
            response.put("data", friendRequest);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @PutMapping("/accept/{id}")
    @Operation(summary = "Chấp nhận lời mời kết bạn")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long id) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            FriendRequest request = friendsService.acceptFriendRequest(id, currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã chấp nhận lời mời kết bạn!");
            response.put("data", request);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @PutMapping("/reject/{id}")
    @Operation(summary = "Từ chối lời mời kết bạn")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long id) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            FriendRequest request = friendsService.rejectFriendRequest(id, currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã từ chối lời mời kết bạn!");
            response.put("data", request);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }

    @DeleteMapping("/unfriend/{id}")
    @Operation(summary = "Hủy kết bạn")
    public ResponseEntity<?> unfriend(@PathVariable Long id) {
        try {
            UUID currentUserId = authUtils.getCurrentUserId();
            friendsService.unfriend(id, currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã hủy kết bạn!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return authUtils.buildErrorResponse(e.getMessage());
        }
    }
}
