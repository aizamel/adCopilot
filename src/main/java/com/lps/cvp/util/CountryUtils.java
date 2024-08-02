/**
 * CountryUtils class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.util;

public class CountryUtils {

	public enum Country {
		PH("Philippines"), MY("Malaysia");

		private String fullName;

		private Country(String fullName) {
			this.fullName = fullName;
		}

		public String getFullName() {
			return fullName;
		}
	}

	public String getFullName(String countryCode) {
		for (Country country : Country.values()) {
			if (country.name().equalsIgnoreCase(countryCode)) {
				return country.getFullName();
			}
		}
		// Handle the case when the country code is not found
		return "Unknown Country";
	}
}
