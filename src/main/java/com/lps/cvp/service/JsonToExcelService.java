/**
 * JsonToExcelService class
 * Purpose: Convert JSON To Excel
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-06-17
 */

package com.lps.cvp.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JsonToExcelService {
	
	private Logger logger = LoggerFactory.getLogger(JsonToExcelService.class);
	
	@Value("${lps.cvp.json}")
	private String lpsCvpJson;
	
	@Value("${lps.cvp.excel}")
	private String lpsCvpExcel;

	private final FileTransferService fileTransferService;

	public JsonToExcelService(FileTransferService fileTransferService) {
		this.fileTransferService = fileTransferService;
	}
	
    public String convertJsonToExcel(String json) {
    	
    	String dirJson = lpsCvpJson;
		// Create json directory if not exist yet
		createDirectory(dirJson);
		// Path to the JSON file
		// Get the current date and time
		LocalDateTime now = LocalDateTime.now();
		// Format the date and time as a string
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
		String formattedDateTime = now.format(formatter);
		String jsonFilePath = dirJson + File.separator + "data_" + formattedDateTime + ".json";
		jsonFilePath = convertToPath(jsonFilePath, false);
		/*** System.out.println("jsonFilePath: " + jsonFilePath); ***/
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFilePath))) {
			writer.write(json);
			logger.info("File written successfully.");
		} catch (IOException e) {
			logger.error(e.getMessage());
			return e.getMessage();
		}
		
		String dirExcel = lpsCvpExcel;
		// Create excel directory if not exist yet
		createDirectory(dirExcel);
		// Path to the output Excel file
		String excelFilePath = dirExcel + File.separator + "excel_" + formattedDateTime + ".xlsx";
		excelFilePath = convertToPath(jsonFilePath, false);
		/*** System.out.println("excelFilePath: " + excelFilePath); ***/
        try {
            // Read JSON file and parse it to a JsonNode
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(new File(jsonFilePath));

            // Create a new Excel workbook and sheets using try-with-resources
            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {

                // Create a single sheet for results
                Sheet resultSheet = workbook.createSheet("Result");

                // Create a style for headers
                CellStyle headerStyle = createHeaderStyle(workbook);

                // Write count data to the result sheet
                int rowIndex = writeCountHeaders(resultSheet, headerStyle);
                rowIndex = writeCountToSheet(jsonNode.get("count"), resultSheet, rowIndex);

                // Write employee headers below the count data
                rowIndex++;
                rowIndex = writeEmployeeHeaders(resultSheet, rowIndex, headerStyle);

                // Write employee data below the employee headers
                rowIndex = writeEmployeesToSheet(jsonNode.get("employees"), resultSheet, rowIndex);

                // Autosize all columns
                autoSizeColumns(resultSheet, 13);

                // Write the workbook to an Excel file
                workbook.write(fileOut);
            }

        } catch (IOException e) {
        	logger.error(e.getMessage());
			return e.getMessage();
        }
        String base64String = "";
        try {
            // Read Excel file into byte array
            byte[] fileBytes = FileUtils.readFileToByteArray(new File(excelFilePath));
            
            // Encode byte array to base64 string
            base64String = Base64.getEncoder().encodeToString(fileBytes);            
        } catch (IOException e) {
        	logger.error(e.getMessage());
			return e.getMessage();
        }
        fileTransferService.deleteAll(lpsCvpJson);
		fileTransferService.deleteAll(lpsCvpExcel);
		
		/***
		// Test back
        // Decode the Base64 string to get the bytes
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        // Specify the path and name of the Excel file
		String testbackFilePath = lpsCvpExcel + File.separator + "testback.xlsx";
		testbackFilePath = checkPath(testbackFilePath);
        // Write the decoded bytes to the Excel file
        try (FileOutputStream fos = new FileOutputStream(testbackFilePath)) {
            fos.write(decodedBytes);
            logger.info("Excel file created successfully: " + testbackFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ***/
        return base64String;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        headerStyle.setFont(font);

        return headerStyle;
    }

    private int writeCountHeaders(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Country", headerStyle);
        createCell(headerRow, 1, "Count", headerStyle);
        return 1; // Next row index
    }

    private int writeEmployeeHeaders(Sheet sheet, int startRowIndex, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(startRowIndex);
        createCell(headerRow, 0, "Name", headerStyle);
        createCell(headerRow, 1, "Country", headerStyle);
        createCell(headerRow, 2, "Role", headerStyle);
        createCell(headerRow, 3, "Career Step", headerStyle);
        createCell(headerRow, 4, "Project", headerStyle);
        createCell(headerRow, 5, "Team", headerStyle);
        createCell(headerRow, 6, "Current Role", headerStyle);
        createCell(headerRow, 7, "Roll Off Date", headerStyle);
        createCell(headerRow, 8, "Willing To Relocate To HK", headerStyle);
        createCell(headerRow, 9, "Willing To Relocate To SG", headerStyle);
        createCell(headerRow, 10, "Willing To Relocate To MY", headerStyle);
        createCell(headerRow, 11, "Availability", headerStyle);
        createCell(headerRow, 12, "Skills", headerStyle);
        return startRowIndex + 1; // Next row index
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private int writeCountToSheet(JsonNode countNode, Sheet sheet, int startRowIndex) {
        if (countNode.isArray()) {
            int rowIndex = startRowIndex;
            for (JsonNode node : countNode) {
                Row row = sheet.createRow(rowIndex++);
                Iterator<String> fieldNames = node.fieldNames();
                while (fieldNames.hasNext()) {
                    String country = fieldNames.next();
                    row.createCell(0).setCellValue(country);
                    row.createCell(1).setCellValue(node.get(country).asInt());
                }
            }
            return rowIndex; // Next row index
        }
        return startRowIndex;
    }

    private int writeEmployeesToSheet(JsonNode employeesNode, Sheet sheet, int startRowIndex) {
        if (employeesNode.isArray()) {
            int rowIndex = startRowIndex;
            for (JsonNode employeeNode : employeesNode) {
                rowIndex = writeEmployeeToRows(employeeNode, sheet, rowIndex);
            }
            return rowIndex; // Next row index
        }
        return startRowIndex;
    }

    private int writeEmployeeToRows(JsonNode employeeNode, Sheet sheet, int startRowIndex) {
        int cellIndex = 0;
        Row row = sheet.createRow(startRowIndex++);

        // Write basic fields
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Name").asText());
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Country").asText());
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Role").asText());
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Career Step").asText());
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Project").asText());
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Team").asText());
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Current Role").asText());
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Roll Off Date").asText(""));
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Willing To Relocate To HK").asText());
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Willing To Relocate To SG").asText());
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Willing To Relocate To MY").asText());
        row.createCell(cellIndex++).setCellValue(employeeNode.get("Availability").asText());

        // Write first skill in the same row
        JsonNode skillsNode = employeeNode.get("Skills");
        if (skillsNode.isArray() && skillsNode.size() > 0) {
            row.createCell(cellIndex).setCellValue(skillsNode.get(0).asText());
            // Write remaining skills in new rows
            for (int i = 1; i < skillsNode.size(); i++) {
                Row skillRow = sheet.createRow(startRowIndex++);
                skillRow.createCell(cellIndex).setCellValue(skillsNode.get(i).asText());
            }
        }
        return startRowIndex;
    }

    private void autoSizeColumns(Sheet sheet, int numColumns) {
        for (int i = 0; i < numColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createDirectory(String dir) {
		File newDir = new File(dir);
		if (!newDir.exists()) {
			newDir.mkdirs();
		}
	}
	
	private String checkPath(String path) {
		String correctPath = "";
		if(File.separator.equals("\\") ) {
			correctPath = path.replace("\\","/");
		}
		return correctPath;
	}
	
	private String convertToPath(String path, boolean isSFTPServer) {
		Path envPath = Paths.get(path);
		String correctPath = envPath.toString();
		if (isSFTPServer) {
			correctPath = correctPath.replace("\\", "/");
		}
		return correctPath;
	}
}