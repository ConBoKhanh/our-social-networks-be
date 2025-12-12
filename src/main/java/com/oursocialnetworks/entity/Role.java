package com.oursocialnetworks.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role entity representing user permissions")
public class Role {

    private UUID id;

    @JsonProperty("createDate")
    private LocalDate createDate; // Sửa từ OffsetDateTime → LocalDate

    @JsonProperty("udpateDate")
    private LocalDate updateDate; // Cũng sửa

    private String role;

    private Integer status;
}
