package pfe.stockWatch.Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsGlobalConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // autoriser toutes les routes
                .allowedOrigins("*") // autoriser toutes les origines (app mobile, navigateur, etc.)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // autoriser les méthodes HTTP
                .allowedHeaders("*"); // autoriser tous les headers
    }
}
