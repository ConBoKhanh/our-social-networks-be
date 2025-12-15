package com.oursocialnetworks.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("id_user")
    private UUID idUser;  // Người gửi lời mời kết bạn

    @JsonProperty("friend_id")
    private UUID friendId;  // Người nhận lời mời kết bạn

    @JsonProperty("status_fr")
    private String statusFr;  // "Pending" hoặc "Done"

    @JsonProperty("status")
    private Integer status;  // 1 = active, 0 = deleted
}
