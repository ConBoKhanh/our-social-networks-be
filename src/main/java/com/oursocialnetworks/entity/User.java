package com.oursocialnetworks.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User entity representing a social network user")
public class User {

    @Schema(description = "User ID", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id; // Supabase dùng uuid

    @JsonProperty("createDate")
    private LocalDate createDate;

    @JsonProperty("username_login")
    private String usernameLogin;

    @JsonProperty("password_login")
    private String passwordLogin;

    private String image;

    private String username;

    private String description;

    @JsonProperty("place_of_residence")
    private String placeOfResidence;

    @JsonProperty("id_friends")
    private Long idFriends;

    @JsonProperty("date-of-birth")
    private LocalDate dateOfBirth;

    @JsonProperty("id_relationship")
    private Long idRelationship;

    @JsonProperty("updateDate")
    private LocalDate  updateDate;

    // Contact fields
    private String email;                  // email
    @JsonProperty("gmail")
    private String gmail;                  // gmail (giữ trường cũ)
    @JsonProperty("provider")
    private String provider;               // google / password / ...
    @JsonProperty("openid_sub")
    private String openidSub;              // sub từ Google
    @JsonProperty("email_verified")
    private Boolean emailVerified;         // email verified flag

    private Integer status;

    @JsonProperty("role_id")
    private UUID roleId; // Foreign key UUID

    // JOIN Role(*)
    @JsonProperty("Role")
    @Schema(description = "Role info (read-only)", accessMode = Schema.AccessMode.READ_ONLY)
    private Role role;  // Supabase tự trả object Role
}
