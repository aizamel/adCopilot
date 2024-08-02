/**
 * DocumentIntelligenceService class
 * Purpose: For extracting resume from Azure Blob Container, 
 * save it to text file and upload it to SFTP server.
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-06-11
 */

package com.lps.cvp.service;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.lps.cvp.util.Constants;
import com.lps.cvp.util.Encrypter;

@Service
public class DocumentIntelligenceService {

	private Logger logger = LoggerFactory.getLogger(DocumentIntelligenceService.class);

	@Value("${lps.cvp.text}")
	private String lpsCvpText;

	@Value("${azure.connection.key}")
	private String azureConnectionKey;

	@Value("${azure.connection.enc}")
	private String azureConnectionEnc;

	@Value("${azure.storage.container-name}")
	private String azureStorageContainerName;

	private final FileTransferService fileTransferService;

	public DocumentIntelligenceService(FileTransferService fileTransferService) {
		this.fileTransferService = fileTransferService;
	}

	public void extractTextFromResumes() throws IOException {

		// Create BlobServiceClient from connection string
		BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
				.connectionString(getAzureStorageConnectionString(azureConnectionKey, azureConnectionEnc))
				.buildClient();

		// Get the container client
		BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureStorageContainerName);

		// List blobs in the container
		PagedIterable<BlobItem> blobs = containerClient.listBlobs();
		StringBuilder sb = new StringBuilder();
		StringBuilder complete = new StringBuilder();
		String input = "";

		int ctr = 0;
		// Iterate through blobs
		for (BlobItem blobItem : blobs) {
			
			BlobClient blobClient = containerClient.getBlobClient(blobItem.getName());
			ctr++;
			// Use a ByteArrayOutputStream to avoid file system issues
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			// Use the new downloadStream method
			blobClient.downloadStream(outputStream);

			// Read the contents of the MS Word document
			try (ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
					XWPFDocument document = new XWPFDocument(inputStream)) {

				// Extract text from tables in the document
				String label = "";
				String value = "";
				String country = "";
				for (XWPFTable table : document.getTables()) {
					for (XWPFTableRow row : table.getRows()) {
						StringBuilder rowText = new StringBuilder();
						int colCtr = 0;
						for (XWPFTableCell cell : row.getTableCells()) {
							if (colCtr == 0) {
								label = cell.getText().trim();
								rowText.append(label);
							}
							if (colCtr == 1) {
								value = cell.getText().trim();
								if (label.equals(Constants.ROLL_OFF_DATE)
										&& (value == null || value.trim().equals(""))) {
									value = Constants.IN_PROJECT;
								}
								if (label.equals(Constants.SKILLS_PROFILE)) {
									value = cell.getText();
								}								
								rowText.append(value);
								if(value.equals(Constants.MY_FULLNAME)) {
									country = "Malaysia";								
								}
								if(value.equals(Constants.WILLING_TO_RELOCATE_TO_SG) && country.equals("Malaysia")) {
									rowText.append("Willing To Relocate To MY: N/A");
								}
							}							
							if (colCtr == 0) {
								rowText.append(Constants.COLON_SPACE);
							} else {
								rowText.append(Constants.NEWLINE);
							}
							colCtr++;
						}
						if (label.equals(Constants.ROLE.trim()) || label.equals(Constants.NAME.trim())
								|| label.equals(Constants.COUNTRY.trim()) || label.equals(Constants.CAREER_STEP.trim())
								|| label.equals(Constants.PROJECT.trim()) || label.equals(Constants.TEAM.trim())
								|| label.equals(Constants.CURRENT_ROLE.trim())
								|| label.equals(Constants.ROLL_OFF_DATE.trim())
								|| label.equals(Constants.WILLING_TO_RELOCATE_TO_HK.trim())
								|| label.equals(Constants.WILLING_TO_RELOCATE_TO_SG.trim())
								|| label.equals(Constants.WILLING_TO_RELOCATE_TO_MY.trim())
								|| label.equals(Constants.AVAILABILITY_OR_STATUS.trim())
								|| label.equals(Constants.SKILLS_PROFILE)) {
							sb.append(rowText.toString());							
							String testValue = rowText.toString().toLowerCase();
							String hk = Constants.WILLING_TO_RELOCATE_TO_HK.trim().toLowerCase();
							String sg = Constants.WILLING_TO_RELOCATE_TO_SG.trim().toLowerCase();
							String my = Constants.WILLING_TO_RELOCATE_TO_MY.trim().toLowerCase();
							if(testValue.contains(hk) && !(testValue.contains("yes") || testValue.contains("no") || testValue.contains("n/a"))) {
								sb.deleteCharAt(sb.length() - 1);
								sb.append("No\n");
							}
							if(testValue.contains(sg) && !(testValue.contains("yes") || testValue.contains("no") || testValue.contains("n/a"))) {
								sb.deleteCharAt(sb.length() - 1);
								sb.append("No\n");
							}
							if(testValue.contains(my) && !(testValue.contains("yes") || testValue.contains("no") || testValue.contains("n/a"))) {
								sb.deleteCharAt(sb.length() - 1);
								sb.append("No\n");
							}
							if (label.equals(Constants.WILLING_TO_RELOCATE_TO_SG.trim()) && country.equals("Malaysia")) {
								sb.append("Willing To Relocate To MY: N/A\n");
							}
						}
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			input = sb.toString();
			input = input.replace(Constants.AVAILABILITY_OR_STATUS.trim(), Constants.AVAILABILITY);
			input = input.replace(Constants.SKILLS_PROFILE, Constants.SKILLS);
			complete.append(input);
			complete.append(Constants.PROFILE_SEPARATOR);
			sb = new StringBuilder();
		}
		/***
		System.out.println("Found files: " + ctr);
		System.out.println("Result: " + complete);
		***/
		String dirText = lpsCvpText;
		// Create text directory if not exist yet
		createDirectory(dirText);
		String filePath = lpsCvpText + File.separator + "resume.txt";
		filePath = checkPath(filePath);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(complete.toString());
			logger.info("File written successfully.");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		String result = fileTransferService.uploadResumeText(dirText);
		logger.info(result);
		fileTransferService.deleteAll(lpsCvpText);
	}

	private String getAzureStorageConnectionString(String azureConnectionKey, String azureConnectionEnc) {
		Encrypter encrypter = new Encrypter(azureConnectionKey);
		return encrypter.decrypt(azureConnectionEnc);
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
}