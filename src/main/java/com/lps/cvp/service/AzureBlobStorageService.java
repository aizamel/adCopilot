/**
 * AzureBlobStorageService class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.lps.cvp.util.Encrypter;

@Service
public class AzureBlobStorageService {

	/***
	private final AuditTrailService auditTrailService;

	public AzureBlobStorageService(AuditTrailService auditTrailService) {
		this.auditTrailService = auditTrailService;
	}
	***/
	@Value("${azure.connection.key}")
	private String azureConnectionKey;

	@Value("${azure.connection.enc}")
	private String azureConnectionEnc;

	@Value("${sftp.port}")
	private Integer port;

	@Value("${azure.storage.container-name}")
	private String azureStorageContainerName;

	public String uploadFileToBlobStorage(MultipartFile file //, String userAgent
			) throws IOException {
		String fileName = Objects.requireNonNull(file.getOriginalFilename());

		BlobClient blobClient = new BlobClientBuilder().connectionString(getAzureStorageConnectionString())
				.containerName(azureStorageContainerName).blobName(fileName).buildClient();
		boolean exists = blobClient.exists();
		blobClient.upload(file.getInputStream(), file.getSize(), true);		
		String action = "UPLOAD";
		String details = fileName + " uploaded successfully to Azure Blob Storage.";
		if (exists) {
			action = "UPDATE";
			details = fileName + " updated successfully in Azure Blob Storage.";
		}
		/*
		// Get the username
		String userName = System.getProperty("user.name");
		int userId = Integer.parseInt(userName);
		// Get the IP address
		InetAddress ipAddress = InetAddress.getLocalHost();
		Date date = new Date();
		Timestamp atTimestamp = new Timestamp(date.getTime());

		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUserId(userId);
		auditTrail.setAction(action);
		auditTrail.setAtTimestamp(atTimestamp);
		auditTrail.setDetails(details);
		auditTrail.setIpAddress(ipAddress.toString());
		auditTrail.setUserAgent(userAgent);
		*** auditTrailService.insertAuditTrail(auditTrail); ***
		*/
		return fileName + " uploaded successfully to Azure Blob Storage.";
	}

	public String deleteFileFromBlobStorage(List<String> fileNamesToRemove, String userAgent) throws IOException {
		StringBuilder sb = new StringBuilder();

		for (String fileName : fileNamesToRemove) {
			BlobClient blobClient = new BlobClientBuilder().connectionString(getAzureStorageConnectionString())
					.containerName(azureStorageContainerName).blobName(fileName).buildClient();

			boolean deleted = blobClient.deleteIfExists();

			if (deleted) {
				sb.append(fileName);
				sb.append(", ");
				/*
				// Get the username
				String userName = System.getProperty("user.name");
				int userId = Integer.parseInt(userName);
				// Get the IP address
				InetAddress ipAddress = InetAddress.getLocalHost();
				Date date = new Date();
				Timestamp atTimestamp = new Timestamp(date.getTime());

				AuditTrail auditTrail = new AuditTrail();
				auditTrail.setUserId(userId);
				auditTrail.setAction("DELETE");
				auditTrail.setAtTimestamp(atTimestamp);
				auditTrail.setDetails(fileName + " deleted successfully from Azure Blob Storage.");
				auditTrail.setIpAddress(ipAddress.toString());
				auditTrail.setUserAgent(userAgent);
				*** auditTrailService.insertAuditTrail(auditTrail); ***
				*/
			}
		}

		String fileNamesDeleted = sb.toString();
		if (fileNamesDeleted.endsWith(", ")) {
			fileNamesDeleted = fileNamesDeleted.substring(0, fileNamesDeleted.length() - 2);
		}
		return fileNamesDeleted + " deleted successfully from Azure Blob Storage.";
	}

	private String getAzureStorageConnectionString() {
		Encrypter encrypter = new Encrypter(azureConnectionKey);
		return encrypter.decrypt(azureConnectionEnc);
	}
}
