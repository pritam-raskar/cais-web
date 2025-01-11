package com.dair.cais;

import com.dair.cais.common.config.JpaConfig;
import com.dair.cais.common.config.DataSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.SpringBootVersion;

@SpringBootApplication
@EntityScan(basePackages = "com.dair.cais")
@Import({JpaConfig.class, DataSourceConfig.class})
public class CaisAlertApplication {

	public static void main(String[] args) {
		String springBootVersion = SpringBootVersion.getVersion();
		System.out.println("Spring Boot Version: " + springBootVersion);

		SpringApplication.run(CaisAlertApplication.class, args);
	}

	@Configuration
	public static class WebConfig implements WebMvcConfigurer {
		@Override
		public void addCorsMappings(CorsRegistry registry) {
			registry.addMapping("/**")
					.allowedOrigins("*")
					.allowedHeaders("*")
					.allowedMethods("*");
		}
	}
}