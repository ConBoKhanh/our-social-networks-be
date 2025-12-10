package com.oursocialnetworks.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "supabase")
public class SupabaseConfig {

    private Map<String, DomainConfig> domains = new HashMap<>();

    @Data
    public static class DomainConfig {
        private String url;
        private String key;
        private String table;
    }

    // Initialize from properties
    public void setUser(DomainConfig user) {
        domains.put("user", user);
    }

//    @PostConstruct
//    public void init() {
//        System.out.println("========== SUPABASE CONFIG ==========");
//        System.out.println("Domains size: " + domains.size());
//        if (domains.containsKey("user")) {
//            DomainConfig user = domains.get("user");
//            System.out.println("User URL: " + user.getUrl());
//            System.out.println("User Table: " + user.getTable());
//            System.out.println("User Key exists: " + (user.getKey() != null));
//        } else {
//            System.out.println("ERROR: 'user' domain not found!");
//        }
//        System.out.println("====================================");
//    }
}