/**
 * CVPApplication class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lps.cvp")
public class CVPApplication {

	public static void main(String[] args) {
		SpringApplication.run(CVPApplication.class, args);
	}

}
