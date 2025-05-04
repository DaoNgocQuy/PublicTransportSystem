package com.pts.configs;

import com.pts.pojo.Routes;
import com.pts.pojo.Vehicles;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
    "com.pts.controllers"
})
public class WebAppContextConfigs implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Converter cho Route
        registry.addConverter(String.class, Routes.class, source -> {
            if (source == null || source.isEmpty()) {
                return null;
            }
            Routes route = new Routes();
            route.setId(Integer.parseInt(source));
            return route;
        });
        
        // Converter cho Vehicles
        registry.addConverter(String.class, Vehicles.class, source -> {
            if (source == null || source.isEmpty()) {
                return null;
            }
            Vehicles vehicle = new Vehicles();
            vehicle.setId(Integer.parseInt(source));
            return vehicle;
        });
    }
}