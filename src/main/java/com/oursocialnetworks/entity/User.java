package com.oursocialnetworks.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User entity representing a social network user")
public class User {

    private Long id;

    @JsonProperty("createDate")
    @Schema(description = "Date and time when the user was created", example = "2024-01-15T10:30:00Z")
    private OffsetDateTime createDate;

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
    private OffsetDateTime updateDate;

    private Integer status;
}
