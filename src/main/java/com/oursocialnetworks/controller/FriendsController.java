package com.oursocialnetworks.controller;

import com.oursocialnetworks.entity.FriendRequest;
import com.oursocialnetworks.service.FriendsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "Friends", description = "API quản lý bạn bè")
@SecurityRequirement(name = "bearerAuth")
public class FriendsController {

    private final FriendsService friendsService;

    /**
     * Lấy user ID từ JWT token
     */
    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            String principal = auth.getPrincipal().toString();
            System.out.println("========== GET CURRENT USER ID ==========");
            System.out.println("Principal: " + principal);
            try {
                return UUID.fromString(principal);
            } catch (IllegalArgumentException e) {
                System.err.println("Cannot parse UUID from principal: " + principal);
            }
        }
        throw new RuntimeException("Không thể xác định user hiện tại!");
    }

    @GetMapping("/requests")
    @Operation(summary = "Lấy danh sách lời mời kết bạn đang chờ")
    public ResponseEntity<?> getPendingRequests() {
        try {
            UUID currentUserId = getCurrentUserId();
            FriendRequest[] requests = friendsService.getPendingRequests(currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", requests);
            response.put("count", requests.length);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage());
        }
    }


    @GetMapping("/list")
    @Operation(summary = "Lấy danh sách bạn bè")
    public ResponseEntity<?> getFriendsList() {
        try {
            UUID currentUserId = getCurrentUserId();
            FriendRequest[] friends = friendsService.getFriendsList(currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", friends);
            response.put("count", friends.length);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage());
        }
    }

    @PostMapping("/request")
    @Operation(summary = "Gửi lời mời kết bạn")
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, String> body) {
        try {
            UUID currentUserId = getCurrentUserId();
            String friendIdStr = body.get("friendId");

            if (friendIdStr == null || friendIdStr.isEmpty()) {
                return buildErrorResponse("friendId là bắt buộc!");
            }

            UUID friendId;
            try {
                friendId = UUID.fromString(friendIdStr);
            } catch (IllegalArgumentException e) {
                return buildErrorResponse("friendId không hợp lệ!");
            }

            if (currentUserId.equals(friendId)) {
                return buildErrorResponse("Không thể gửi lời mời kết bạn cho chính mình!");
            }

            FriendRequest request = friendsService.sendFriendRequest(currentUserId, friendId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã gửi lời mời kết bạn!");
            response.put("data", request);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage());
        }
    }

    @PutMapping("/accept/{id}")
    @Operation(summary = "Chấp nhận lời mời kết bạn")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long id) {
        try {
            UUID currentUserId = getCurrentUserId();
            FriendRequest request = friendsService.acceptFriendRequest(id, currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã chấp nhận lời mời kết bạn!");
            response.put("data", request);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage());
        }
    }

    @PutMapping("/reject/{id}")
    @Operation(summary = "Từ chối lời mời kết bạn")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long id) {
        try {
            UUID currentUserId = getCurrentUserId();
            FriendRequest request = friendsService.rejectFriendRequest(id, currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã từ chối lời mời kết bạn!");
            response.put("data", request);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage());
        }
    }

    @DeleteMapping("/unfriend/{id}")
    @Operation(summary = "Hủy kết bạn")
    public ResponseEntity<?> unfriend(@PathVariable Long id) {
        try {
            UUID currentUserId = getCurrentUserId();
            friendsService.unfriend(id, currentUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đã hủy kết bạn!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage());
        }
    }

    private ResponseEntity<?> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return ResponseEntity.badRequest().body(response);
    }
}
