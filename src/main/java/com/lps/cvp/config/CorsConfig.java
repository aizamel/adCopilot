/**
 * CorsConfig class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Value("${cross.origin.local}")
	private String crossOriginLocal;

	@Value("${cross.origin.prod}")
	private String crossOriginProd;

	@Value("${spring.profiles.active}")
	private String springProfilesActive;

	// Constants for CORS mapping values
	private static final String[] ALLOWED_METHODS = { "GET", "POST", "PUT", "DELETE" };
	private static final String[] ALLOWED_HEADERS = { "Authorization", "Content-Type" };

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		String[] allowedOrigins = springProfilesActive.equals("prod") ? new String[] { crossOriginProd }
				: new String[] { crossOriginLocal };

		registry.addMapping("/**").allowedOrigins(allowedOrigins).allowedMethods(ALLOWED_METHODS)
				.allowedHeaders(ALLOWED_HEADERS).allowCredentials(false); // Avoid setting credentials when using
																			// wildcard origin
	}
}
