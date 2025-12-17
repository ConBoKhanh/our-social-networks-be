package com.oursocialnetworks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request để follow user (deprecated - dùng POST /follow/{userId})")
public class FollowRequest {

    @Schema(description = "ID của user muốn follow", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private String friendId;
}
