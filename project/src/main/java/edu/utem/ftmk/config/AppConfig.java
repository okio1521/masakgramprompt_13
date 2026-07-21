package edu.utem.ftmk.config;

import edu.utem.ftmk.client.WebApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public WebApiClient webApiClient() {
        return new WebApiClient("http://localhost:8080");
    }
}