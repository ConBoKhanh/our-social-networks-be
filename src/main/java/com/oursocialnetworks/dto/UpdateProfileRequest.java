package com.oursocialnetworks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request để cập nhật profile")
public class UpdateProfileRequest {

    @Schema(description = "Tên hiển thị", example = "Nguyễn Văn A")
    private String username;

    @Schema(description = "Mô tả bản thân", example = "Developer | Coffee lover ☕")
    private String description;

    @Schema(description = "Nơi ở hiện tại", example = "Hà Nội, Việt Nam")
    @JsonProperty("place_of_residence")
    private String placeOfResidence;

    @Schema(description = "URL ảnh đại diện", example = "https://example.com/avatar.jpg")
    private String image;
}
