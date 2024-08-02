package com.lps.cvp.helper;

public class AuditTrailHelper {

	private AuditTrailHelper() {
	}

	public static final String T_AUDIT_TRAIL = "CVP.AUDIT_TRAIL";
	public static final String F_ID = "ID";
	public static final String F_USER_ID = "USER_ID";
	public static final String F_ACTION = "ACTION";
	public static final String F_AT_TIMESTAMP = "AT_TIMESTAMP";
	public static final String F_DETAILS = "DETAILS";
	public static final String F_IP_ADDRESS = "IP_ADDRESS";
	public static final String F_USER_AGENT = "USER_AGENT";		
	
	public static final String T_USERS = "CVP.USERS";
	public static final String F_USER_NAME = "USER_NAME";
	
	public static final String PARAM_WITH_COMMA = " = ?,";
	public static final String PARAM_NO_COMMA = " = ?";
	public static final String WHERE = " WHERE ";	
	public static final String DATE_FORMAT = "yyyy-MM-dd HH24:MI:SS";

}
