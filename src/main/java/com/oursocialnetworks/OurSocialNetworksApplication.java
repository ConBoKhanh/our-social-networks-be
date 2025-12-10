package com.oursocialnetworks;

import com.oursocialnetworks.config.SupabaseConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
public class OurSocialNetworksApplication {

    public static void main(String[] args) {
        SpringApplication.run(OurSocialNetworksApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);  // 15 seconds connect timeout
        factory.setReadTimeout(15000);     // 15 seconds read timeout

        return new RestTemplate(factory);
    }

}
