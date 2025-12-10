package com.oursocialnetworks;

import com.oursocialnetworks.config.SupabaseConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class OurSocialNetworksApplication {

    public static void main(String[] args) {
        SpringApplication.run(OurSocialNetworksApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
