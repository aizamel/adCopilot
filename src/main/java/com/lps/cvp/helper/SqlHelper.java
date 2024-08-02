package com.lps.cvp.helper;

public class SqlHelper {

	private SqlHelper() {
	}

	public static final String DISTINCT = " DISTINCT ";
	public static final String TO_CHAR = " TO_CHAR ";
	public static final String ORDER_BY = " ORDER BY ";
	public static final String AND = " AND ";
	public static final String OR = " OR ";
	public static final String ASC = " ASC ";
	public static final String DESC = " DESC ";

	public static String selectStatement(String columns, String table, String criteria) {
		return "SELECT " + columns + " FROM " + table + " WHERE 1=1 AND " + criteria;
	}

	public static String insertStatement(String table, String columns, String values) {
		return "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")";
	}

	public static String updateStatement(String table, String criteria) {
		return "UPDATE " + table + " SET " + criteria;
	}

	public static String deleteStatement(String table, String criteria) {
		return "DELETE FROM " + table + criteria;
	}
}
