/**
 * OpenAPIConfig class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

	@Value("${lps.openapi.dev.url}")
	private String devUrl;

	@Value("${lps.openapi.prod.url}")
	private String prodUrl;

	@Value("${lps.openapi.dev.desc}")
	private String devDesc;

	@Value("${lps.openapi.prod.desc}")
	private String prodDesc;

	@Value("${contact.email}")
	private String contactEmail;

	@Value("${contact.name}")
	private String contactName;

	@Value("${contact.url}")
	private String contactUrl;

	@Value("${info.title}")
	private String infoTitle;

	@Value("${info.version}")
	private String infoVersion;

	@Value("${info.desc}")
	private String infoDesc;
	
	@Value("${spring.profiles.active}")
	private String springProfilesActive;

	@Bean
	OpenAPI myOpenAPI() {
		Server server = new Server();
		server.setUrl(springProfilesActive.equals("prod") ? prodUrl : devUrl);

		Contact contact = new Contact();
		contact.setEmail(contactEmail);
		contact.setName(contactName);
		contact.setUrl(contactUrl);

		Info info = new Info().title(infoTitle).version(infoVersion).contact(contact).description(infoDesc);

		return new OpenAPI().info(info).servers(Collections.singletonList(server));
	}
}