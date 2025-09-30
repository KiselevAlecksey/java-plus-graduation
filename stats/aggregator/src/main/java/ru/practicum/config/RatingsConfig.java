package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties("ratings")
public class RatingsConfig {
    private double view = 0.4;
    private double register = 0.8;
    private double like = 1.0;
}