/**
 * Constants class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.util;

public class Constants {

	public static final String INTERNAL_SERVER_ERROR = "Internal server error";
	public static final String MESSAGE = "Message: %s";
	public static final String STAFF_ID_SEPARATOR = "-";
	public static final String DOCX_EXTENSION = ".docx";
	public static final String REGEX_PATTERN = "\\d{8}-[A-Za-z ñ]+_[A-Z]{2}\\";
	public static final String VALID_FILENAME_PATTERN = "CV filename pattern is valid: %s";
	public static final String INVALID_FILENAME_PATTERN = "CV filename pattern is invalid: %s";
	public static final String UNDERSCORE = "_";
	public static final String TIMES_NEW_ROMAN = "Times New Roman";
	public static final String NO_CV_FILES_FOUND = "No CV files found";
	public static final String NO_EXCEL_FILES_FOUND = "No Excel file found";

	public static final int MAX_RETRIES = 3;
	public static final int NEXT_RETRY_WAIT_TIME = 30000; // 30 seconds
	public static final String NO_MSWORD_FILES_FOUND = "No MS Word files found.";
	public static final String NO_MSEXCEL_FILES_FOUND = "No MS Excel files found.";
	public static final String RESOURCETRACKER = "resourcetracker";
	public static final String PROCESSED = "processed";
	public static final String DOWNLOAD = "download";
	public static final String TEMPLATE = "template";
	public static final String SFTP_FILE_SEPARATOR = "/";
	public static final String ERROR = "ERROR: %s";
	public static final String FILE_PATH = "filePath: %s";

	public static final String SHEET_NAME = "Staff wih LWD (PH Masterlist)";
	public static final String COLUMN_NAME = "Employee No.";
	public static final String PH_SHEET_NAME = "PH ODC Resource Tracker";
	public static final String MY_SHEET_NAME = "MY ODC Resource Tracker";
	public static final String DATE_PATTERN = "MMM dd, yyyy";

	public static final String WHITE = "FFFFFF";
	public static final String GRAY = "D9D9D9";
	public static final String BLACK = "000000";

	public static final String PH_COUNTRY_CODE = "PH";
	public static final String MY_COUNTRY_CODE = "MY";
	public static final String PH_FULLNAME = "Philippines";
	public static final String MY_FULLNAME = "Malaysia";

	public static final String COUNTRY = " Country";
	public static final String CAREER_STEP = " Career Step";
	public static final String PROJECT = " Project";
	public static final String TEAM = " Team";
	public static final String CURRENT_ROLE = " Current Role";
	public static final String ROLL_OFF_DATE = " Roll Off Date";
	public static final String WILLING_TO_RELOCATE_TO_HK = " Willing To Relocate To HK";
	public static final String WILLING_TO_RELOCATE_TO_SG = " Willing To Relocate To SG";
	public static final String WILLING_TO_RELOCATE_TO_MY = " Willing To Relocate To MY";
	public static final String AVAILABILITY_OR_STATUS = " Availability or Status";
	public static final String END_DATE = " End Date";

	public static final String EIGHT_NINES = "99999999";
	public static final String SHEET_NAME_EXISTS = "Sheet Name: '%s' exists.";
	public static final String EXCEL_FILE_EXISTS = "Excel File: '%s' exists.";
	public static final String EXCEL_FILE_DOES_NOT_EXISTS = "File does not exist at path: %s";
	
	public static final String RESIGNED = "Resigned";
	public static final String RETRENCHED = "Retrenched";
	public static final String SERVING_NOTICE = "Serving Notice";

	public static final String ROLE = "Role";
	public static final String NAME = "Name";
	public static final String AVAILABILITY = "Availability";
	public static final String SKILLS_PROFILE = "Skills Profile";
	public static final String SKILLS = "Skills";
	public static final String PROFILE_SEPARATOR = "*****";
	public static final String IN_PROJECT = "In Project";
	public static final String COLON_SPACE = ": ";
	public static final String NEWLINE = "\n";
	public static final String NO_TEXT_FILES_FOUND = "No text files found.";
	public static final String NO_FILES_FOUND = "No files found";
	public static final String NO_RESULT = "No result";
	public static final String NO_FILES_FOUND_SFTP = "No files found in SFTP";
	
	private Constants() {
		/** Empty private constructor to prevent instantiation of this class **/
	}
}
