/**
 * FileTransferService class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.lps.cvp.exception.CustomException;
import com.lps.cvp.util.Constants;
import com.lps.cvp.util.Encrypter;

@Service
public class FileTransferService {

	private Logger logger = LoggerFactory.getLogger(FileTransferService.class);

	@Value("${upload.cv}")
	private String uploadCv;

	@Value("${upload.cv.resigned}")
	private String uploadCvResigned;

	@Value("${sftp.host.enc}")
	private String host;

	@Value("${sftp.port}")
	private Integer port;

	@Value("${sftp.username.enc}")
	private String username;

	@Value("${sftp.sessionTimeout}")
	private Integer sessionTimeout;

	@Value("${sftp.channelTimeout}")
	private Integer channelTimeout;

	@Value("${lps.cvp.output}")
	private String lpsCvpOutput;

	@Value("${lps.cvp.resourcetracker}")
	private String lpsCvpResourcetracker;

	@Value("${lps.cvp.download}")
	private String lpsCvpDownload;
	
	@Value("${lps.cvp.template}")
	private String lpsCvpTemplate;

	@Value("${sftp.directory}")
	private String sftpDirectory;

	@Value("${sftp.directory.resourcetracker}")
	private String sftpDirectoryResourcetracker;

	@Value("${sftp.directory.processed}")
	private String sftpDirectoryProcessed;

	@Value("${sftp.directory.download}")
	private String sftpDirectoryDownload;

	@Value("${sftp.password.key}")
	private String passwordKey;

	@Value("${sftp.password.enc}")
	private String passwordEnc;
	
	@Value("${lps.cvp.text}")
	private String lpsCvpText;

	private void addToZip(ZipOutputStream zipOutputStream, String file, File zFile) {
		try (InputStream inputStream = new FileInputStream(file)) {
			ZipEntry zipEntry = new ZipEntry(zFile.getName());
			zipOutputStream.putNextEntry(zipEntry);
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				zipOutputStream.write(buffer, 0, bytesRead);
			}
			zipOutputStream.closeEntry();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private String downloadZipFiles(ChannelSftp channelSftp, String dirOutput, ByteArrayOutputStream baos,
			String countryCode) throws IOException {
		// Get the current date and time
		LocalDateTime now = LocalDateTime.now();
		// Format the date and time as a string
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
		String formattedDateTime = now.format(formatter);
		String zipFileName = dirOutput + File.separator + "downloaded_files_" + formattedDateTime + ".zip";
		zipFileName = convertToPath(zipFileName, false);
		/***
		String log = String.format("zipFileName: %s", zipFileName);
		logger.info(log);
		***/
		try (FileOutputStream fos = new FileOutputStream(zipFileName)) {
			fos.write(baos.toByteArray());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		/***
		log = String.format("Downloaded zipFileName: %s", zipFileName);
		logger.info(log);
		***/

		// Upload to SFTP Server
		String localPath = zipFileName;
		String remoteFilePath = sftpDirectoryDownload + File.separator + countryCode + "_downloaded_files_"
				+ formattedDateTime + ".zip";
		localPath = convertToPath(localPath, false);
		remoteFilePath = convertToPath(remoteFilePath, true);
		File fileInput = new File(localPath);
		InputStream inputStream = new FileInputStream(fileInput);
		try {
			channelSftp.put(inputStream, remoteFilePath);
		} catch (Exception ex) {
			String log = String.format(Constants.ERROR, ex.getMessage());
			logger.error(log);
		}
		/***
		log = String.format("Zip file %s has been uploaded): %s", localPath, remoteFilePath);
		logger.info(log);
		***/
		inputStream.close();
		zipFileName = remoteFilePath;
		return zipFileName;
	}
	
	private String downloadZipFilesEntries(ChannelSftp channelSftp, String dirOutput, ByteArrayOutputStream baos,
			String countryCode) throws IOException {
		// Get the current date and time
		LocalDateTime now = LocalDateTime.now();
		// Format the date and time as a string
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
		String formattedDateTime = now.format(formatter);
		String zipFileName = dirOutput + File.separator + "downloaded_files_" + formattedDateTime + ".zip";
		zipFileName = convertToPath(zipFileName, false);
		
		try (FileOutputStream fos = new FileOutputStream(zipFileName)) {
			fos.write(baos.toByteArray());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		// Base64
		String base64Encoded = "";
        try {
            // Read the ZIP file into a byte array
            byte[] zipFileBytes = readBytesFromFile(zipFileName);

            // Encode bytes to base64 string
            base64Encoded = encodeToBase64(zipFileBytes);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        zipFileName = base64Encoded;
        
        /***
        // Test back
        String base64EncodedString = base64Encoded;
        String testback = dirOutput + File.separator + "testback_" + formattedDateTime + ".zip";
        testback = convertToPath(testback, false);		
        try {
            // Decode the base64 string to a byte array
            byte[] zipFileBytes = decodeFromBase64(base64EncodedString);
            // Write the byte array to a file
            writeBytesToFile(zipFileBytes, testback);
            logger.info("ZIP file has been created at: " + testback);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ***/
		return zipFileName;
	}

	public boolean archiveFile(String archivedFile, String sftpArchivedFile) {
		ChannelSftp channelSftp = connectChannelSftpWithRetry();
		try {
			channelSftp.put(archivedFile, sftpArchivedFile);
			return true;
		} catch (SftpException ex) {
			String log = String.format("Error remotely archiving file %s", ex.getMessage());
			logger.error(log);
		} finally {
			disconnectChannelSftp(channelSftp);
		}
		return false;
	}

	public boolean deleteFileFromSFTP(String origSftpFile) {
		ChannelSftp channelSftp = connectChannelSftpWithRetry();
		try {
			channelSftp.rm(origSftpFile);
			return true;
		} catch (SftpException ex) {
			String log = String.format("Error remotely deleting file %s", ex.getMessage());
			logger.error(log);
		} finally {
			disconnectChannelSftp(channelSftp);
		}
		return false;
	}

	ChannelSftp connectChannelSftpWithRetry() {
		String password = "";
		String dusername = "";
		String dhost = "";
		for (int retry = 0; retry < Constants.MAX_RETRIES; retry++) {
			try {
				JSch jSch = new JSch();
				/*** jSch.addIdentity(privateKey); ***/
				Encrypter encrypter = new Encrypter(passwordKey);
				dusername = encrypter.decrypt(username);
				dhost = encrypter.decrypt(host);
				Session session = jSch.getSession(dusername, dhost, port);
				session.setConfig("StrictHostKeyChecking", "no");				
				password = encrypter.decrypt(passwordEnc);
				session.setPassword(password);
				session.connect(sessionTimeout);
				Channel channel = session.openChannel("sftp");
				channel.connect(channelTimeout);
				return (ChannelSftp) channel;
			} catch (JSchException ex) {
				String log = String.format(
						"host=%s, username=%s, password=%s, port=%s, sessionTimeout=%s, channelTimeout=%s", dhost,
						dusername, password, port, sessionTimeout, channelTimeout);
				logger.error(log);
				logger.error(String.format("Connecting to SFTP... Try #: %d (%d seconds to reconnect.)", retry + 1,
						Constants.NEXT_RETRY_WAIT_TIME / 1000));

				// Wait for a moment before the next retry
				if (retry < Constants.MAX_RETRIES - 1) {
					try {
						Thread.sleep(Constants.NEXT_RETRY_WAIT_TIME); // Adjust the wait time as needed
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						log = String.format("InterruptedException %s", e.getMessage());
						logger.error(log);
					}
				}
			}
		}
		throw new CustomException("Cannot connect to SFTP server after multiple retries.");
	}

	private void disconnectChannelSftp(ChannelSftp channelSftp) {
		try {
			if (channelSftp == null)
				return;

			if (channelSftp.isConnected())
				channelSftp.disconnect();

			if (channelSftp.getSession() != null)
				channelSftp.getSession().disconnect();

		} catch (Exception ex) {
			String log = String.format("SFTP disconnect error %s", ex.getMessage());
			logger.error(log);
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> checkSftpIfHasFiles(ChannelSftp channelSftp, String sftpDirectory) throws SftpException {
		// Check if SFTP Server has files
		List<String> list = new ArrayList<>();
		if (channelSftp != null) {
			List<LsEntry> entries = new ArrayList<>(channelSftp.ls(sftpDirectory));
			for (LsEntry entry : entries) {
				if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")
						&& !entry.getFilename().equals(Constants.RESOURCETRACKER)
						&& !entry.getFilename().equals(Constants.PROCESSED)
						&& !entry.getFilename().equals(Constants.DOWNLOAD)
						&& !entry.getFilename().equals(Constants.TEMPLATE)) {
					list.add(entry.getFilename());
				}
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public List<String> checkSftpIfHasFilesResourcetracker(ChannelSftp channelSftp, String sftpDirectoryResourcetracker)
			throws SftpException {
		// Check if SFTP Server has files
		List<String> list = new ArrayList<>();
		if (channelSftp != null) {
			List<LsEntry> entries = new ArrayList<>(channelSftp.ls(sftpDirectoryResourcetracker));
			for (LsEntry entry : entries) {
				if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
					list.add(entry.getFilename());
				}
			}
		}
		return list;
	}

	public void createPvOutputDirectory(String dirOutput) {
		File newDirOutput = new File(dirOutput);
		if (!newDirOutput.exists()) {
			newDirOutput.mkdirs();
		}
	}

	public List<String> downloadFileFromSftpServer(ChannelSftp channelSftp, List<String> list, String sftpDirectory,
			String dirOutput) throws IOException {
		String log = "";
		List<String> cvFilenames = new ArrayList<>();
		OutputStream outputStream;
		if (!list.isEmpty()) {
			for (int idx = 0; idx < list.size(); idx++) {
				String remoteFilename = list.get(idx); // Get filenames
				String remoteFilePath = sftpDirectory + File.separator + remoteFilename;
				String localPath = dirOutput + File.separator + remoteFilename;
				remoteFilePath = convertToPath(remoteFilePath, true);
				localPath = convertToPath(localPath, false);
				/***
				System.out.println("remoteFilePath: " + remoteFilePath);
				System.out.println("localPath: " + localPath);
				***/
				/***
				log = String.format("remoteFilePath: %s", remoteFilePath);
				logger.info(log);
				log = String.format("pvPath: %s", localPath);
				logger.info(log);
				***/
				File fileOutput = new File(localPath);
				outputStream = new FileOutputStream(fileOutput);
				try {
					channelSftp.get(remoteFilePath, outputStream);
				} catch (Exception ex) {
					log = String.format(Constants.ERROR, ex.getMessage());
					logger.error(log);
				}
				/***
				log = String.format("File %s has been copied): %s", remoteFilePath, localPath);
				logger.info(log);
				***/
				outputStream.close();
				cvFilenames.add(localPath);
			}
		}
		return cvFilenames;
	}
		

	
	public List<String> downloadFileFromSftpServerStaffEntries(ChannelSftp channelSftp, String sftpDirectory,
			String dirOutput, List<LsEntry> entries) throws IOException, SftpException {
		String log = "";
		List<String> cvFilenames = new ArrayList<>();
		OutputStream outputStream;		
		for (LsEntry entry : entries) {
			String remoteFilename = entry.getFilename(); // Get filenames
			String remoteFilePath = sftpDirectory + File.separator + remoteFilename;
			String localPath = dirOutput + File.separator + remoteFilename;
			remoteFilePath = convertToPath(remoteFilePath, true);
			localPath = convertToPath(localPath, false);
			File fileOutput = new File(localPath);
			outputStream = new FileOutputStream(fileOutput);
			try {
				channelSftp.get(remoteFilePath, outputStream);
			} catch (Exception ex) {
				log = String.format(Constants.ERROR, ex.getMessage());
				logger.error(log);
			}
			outputStream.close();
			cvFilenames.add(localPath);
		}
		return cvFilenames;
	}

	public void createSftpDirectory(ChannelSftp channelSftp, String sftpDirectory, String folder) throws SftpException {
		String newSftpFolder = sftpDirectory + File.separator + folder;
		newSftpFolder = convertToPath(newSftpFolder, true);
		boolean exists = directoryExists(channelSftp, sftpDirectory, folder);
		if (exists) {
			channelSftp.cd(sftpDirectory);
		} else {
			channelSftp.cd(sftpDirectory);
			channelSftp.mkdir(newSftpFolder);
		}
	}

	public void deletePvArchiveFiles() {
		// Delete all files in archive directory, archive files will be stored in SFTP
		// server
		String dirArchive = sftpDirectoryProcessed;
		File checkDirArchive = new File(dirArchive);
		if (checkDirArchive.exists()) {
			// List of all files and directories
			String fileToBeDeleted = "";
			File[] filesList = checkDirArchive.listFiles();
			for (File file : filesList) {
				fileToBeDeleted = checkDirArchive + File.separator + file.getName();
				deleteFile(fileToBeDeleted);
			}
		}
	}

	public void deleteAll(String dirOutput) {
		// Clear all files in output directory if already exists
		File checkDirOutput = new File(dirOutput);
		if (checkDirOutput.exists()) {
			// List of all files and directories
			File[] filesList = checkDirOutput.listFiles();
			for (File file : filesList) {
				String fileToBeDeleted = checkDirOutput + File.separator + file.getName();
				//System.out.println("fileToBeDeleted: "+fileToBeDeleted);
				deleteFile(fileToBeDeleted);
			}
		}
	}

	public void deleteAllExceptZip(String dirOutput) {
		// Clear all files in output directory if already exists
		File checkDirOutput = new File(dirOutput);
		if (checkDirOutput.exists()) {
			// List of all files and directories
			File[] filesList = checkDirOutput.listFiles();
			for (File file : filesList) {
				if (!file.getName().endsWith("zip")) {
					String fileToBeDeleted = checkDirOutput + File.separator + file.getName();
					deleteFile(fileToBeDeleted);
				}
			}
		}
	}

	private void deleteFile(String fileToBeDeleted) {
		Path path = Paths.get(fileToBeDeleted);
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			String log = String.format(Constants.ERROR, e.getMessage());
			logger.error(log);
		}
	}

	public boolean directoryExists(ChannelSftp channelSftp, String sftpDirectory, String folder) {
		try {
			@SuppressWarnings("unchecked")
			List<ChannelSftp.LsEntry> dirList = channelSftp.ls(sftpDirectory);
			for (ChannelSftp.LsEntry entry : dirList) {
				if (entry.getFilename().equals(".") || entry.getFilename().equals("..")
						|| entry.getFilename().endsWith(Constants.DOCX_EXTENSION)
						|| !entry.getFilename().equals(folder)) {
					continue; // Skip current and parent directory entries
				}

				if (entry.getFilename().equals(folder)) {
					return true; // Directory exists
				}
			}
			return false; // Directory not found in the listing
		} catch (SftpException e) {
			return false; // Directory does not exist or an error occurred
		}
	}

	public String readAllCVs() throws IOException {
		ChannelSftp channelSftp = connectChannelSftpWithRetry();
		// Read all files in local directory
		String log = "";
		String message = Constants.NO_FILES_FOUND;
		InputStream inputStream;
		File checkDirOutput = new File(uploadCv);
		if (checkDirOutput.exists()) {
			// List of all files and directories
			File[] filesList = checkDirOutput.listFiles();
			for (File file : filesList) {
				String localPath = checkDirOutput + File.separator + file.getName();
				String remoteFilePath = sftpDirectory + File.separator + file.getName();
				localPath = convertToPath(localPath, false);
				remoteFilePath = convertToPath(remoteFilePath, true);
				File fileInput = new File(localPath);
				inputStream = new FileInputStream(fileInput);
				try {
					channelSftp.put(inputStream, remoteFilePath);
				} catch (Exception ex) {
					log = String.format(Constants.ERROR, ex.getMessage());
					logger.error(log);
				}
				/***
				log = String.format("File %s has been uploaded): %s", localPath, remoteFilePath);
				logger.info(log);
				***/
				inputStream.close();
				message = "CVs uploaded.";

			}
		}
		return message;
	}

	public String readAllResigned() throws IOException {
		ChannelSftp channelSftp = connectChannelSftpWithRetry();
		// Read all files in local directory
		String log = "";
		String message = Constants.NO_FILES_FOUND;
		InputStream inputStream;
		File checkDirOutput = new File(uploadCvResigned);
		if (checkDirOutput.exists()) {
			// List of all files and directories
			File[] filesList = checkDirOutput.listFiles();
			for (File file : filesList) {
				String localPath = checkDirOutput + File.separator + file.getName();
				String remoteFilePath = sftpDirectoryResourcetracker + File.separator + file.getName();
				localPath = convertToPath(localPath, false);
				remoteFilePath = convertToPath(remoteFilePath, true);
				File fileInput = new File(localPath);
				inputStream = new FileInputStream(fileInput);
				try {
					channelSftp.put(inputStream, remoteFilePath);
				} catch (Exception ex) {
					log = String.format(Constants.ERROR, ex.getMessage());
					logger.error(log);
				}
				/***
				log = String.format("File %s has been uploaded): %s", localPath, remoteFilePath);
				logger.info(log);
				***/
				inputStream.close();
				message = "Resigned employees uploaded";

			}
		}
		return message;
	}

	private String convertToPath(String path, boolean isSFTPServer) {
		Path envPath = Paths.get(path);
		String correctPath = envPath.toString();
		if (isSFTPServer) {
			correctPath = correctPath.replace("\\", "/");
		}
		return correctPath;
	}
	
	public void forEntries(List<String> listAll, List<LsEntry> entries) {
		for (LsEntry entry : entries) {
			if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")
					&& !entry.getFilename().equals(Constants.RESOURCETRACKER)
					&& !entry.getFilename().equals(Constants.PROCESSED)
					&& !entry.getFilename().equals(Constants.DOWNLOAD)
					&& !entry.getFilename().equals(Constants.TEMPLATE)) {
				listAll.add(entry.getFilename());
			}
		}
	}
	
	public String uploadResumeText(String dirText) throws IOException {
		ChannelSftp channelSftp = connectChannelSftpWithRetry();
		// Read all files in local directory
		String log = "";
		String message = "No files found";
		InputStream inputStream;
		File checkDirOutput = new File(dirText);
		if (checkDirOutput.exists()) {
			// List of all files and directories
			File[] filesList = checkDirOutput.listFiles();
			for (File file : filesList) {
				String localPath = checkDirOutput + File.separator + file.getName();
				String remoteFilePath = sftpDirectoryProcessed + File.separator + file.getName();
				localPath = convertToPath(localPath, false);
				remoteFilePath = convertToPath(remoteFilePath, true);
				File fileInput = new File(localPath);
				inputStream = new FileInputStream(fileInput);
				try {
					channelSftp.put(inputStream, remoteFilePath);
				} catch (Exception ex) {
					log = String.format(Constants.ERROR, ex.getMessage());
					logger.error(log);
				}
				log = String.format("File %s has been uploaded): %s", localPath, remoteFilePath);
				logger.info(log);
				inputStream.close();
				message = "Resume text uploaded.";
			}
		}
		return message;
	}
	
	public List<String> readResumeText() {
		ChannelSftp channelSftp = connectChannelSftpWithRetry();
		List<String> resumes = new ArrayList<>();
		List<String> profiles = new ArrayList<>();
		try {
			// Check if STPT server has files
			List<String> list = checkSftpIfHasResumeTextFile(channelSftp, sftpDirectoryProcessed);
			// SFTP Server has files
			if (!list.isEmpty()) {
				String dirOutput = lpsCvpText;
				// Create output directory if not exist yet
				createPvOutputDirectory(dirOutput);
				// Download the file
				resumes = downloadResumeTextFileFromSftpServer(channelSftp, list, sftpDirectoryProcessed, dirOutput);				
				if(!resumes.isEmpty()) {
					String resume = resumes.get(0);
			        String result = "";
			        result = new String(Files.readAllBytes(Paths.get(resume)));
					profiles = Arrays.asList(result.split("\\*\\*\\*\\*\\*"));
				}
			}
		} catch (SftpException | IOException ex) {
			String log = String.format(Constants.ERROR, ex.getMessage());
			logger.error(log);
			ex.printStackTrace();
			throw new CustomException(Constants.NO_TEXT_FILES_FOUND, 500);
		} finally {
			disconnectChannelSftp(channelSftp);
		}
		return profiles;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> checkSftpIfHasResumeTextFile(ChannelSftp channelSftp, String sftpDirectory) throws SftpException {
		// Check if SFTP Server has files
		List<String> list = new ArrayList<>();
		if (channelSftp != null) {
			List<LsEntry> entries = new ArrayList<>(channelSftp.ls(sftpDirectory));
			for (LsEntry entry : entries) {
				if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")						
						&& !entry.getFilename().endsWith(Constants.DOCX_EXTENSION)) {
					list.add(entry.getFilename());
				}
			}
		}
		return list;
	}
	
	public List<String> downloadResumeTextFileFromSftpServer(ChannelSftp channelSftp, List<String> list, String sftpDirectory,
			String dirOutput) throws IOException {
		String log = "";
		List<String> resumes = new ArrayList<>();
		OutputStream outputStream;
		if (!list.isEmpty()) {
			for (int idx = 0; idx < list.size(); idx++) {
				String remoteFilename = list.get(idx); // Get filenames
				String remoteFilePath = sftpDirectory + File.separator + remoteFilename;
				String localPath = dirOutput + File.separator + remoteFilename;
				remoteFilePath = convertToPath(remoteFilePath, true);
				localPath = convertToPath(localPath, false);
				File fileOutput = new File(localPath);
				outputStream = new FileOutputStream(fileOutput);
				try {
					channelSftp.get(remoteFilePath, outputStream);
				} catch (Exception ex) {
					log = String.format(Constants.ERROR, ex.getMessage());
					logger.error(log);
				}
				outputStream.close();
				resumes.add(localPath);
			}
		}
		return resumes;
	}
	
	// Function to read bytes from a file
    private byte[] readBytesFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    // Function to encode byte array to base64 string
    private String encodeToBase64(byte[] fileBytes) {
        return Base64.getEncoder().encodeToString(fileBytes);
    }
    
    /***
    // Function to decode a base64 string to a byte array
    private byte[] decodeFromBase64(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }

    // Function to write a byte array to a file
    private void writeBytesToFile(byte[] fileBytes, String filePath) throws IOException {
        // Ensure the file path is valid and does not exceed system limits
        if (filePath.length() > 255) { // Most systems limit path length to 255 characters
            throw new IOException("File path exceeds system limit of 255 characters.");
        }        
        Path path = Paths.get(filePath);
        Files.write(path, fileBytes);
    }
    ***/

	public List<String> downloadNewCVFormat(String localFileDir, String sftpFileDir) {
		deleteAll(localFileDir);
		ChannelSftp channelSftp = connectChannelSftpWithRetry();
		List<String> fileNames = new ArrayList<>();
		try {
			// Check if SFTP server has files
			List<String> list = checkSftpIfHasFilesResourcetracker(channelSftp, sftpFileDir);
			// SFTP Server has files
			if (!list.isEmpty()) {
                // Create new cv directory if not exist yet
				createPvOutputDirectory(localFileDir);
				// Download the file
				fileNames = downloadFileFromSftpServer(channelSftp, list, sftpFileDir, localFileDir);
			}
		} catch (SftpException | IOException ex) {
			String log = String.format(Constants.ERROR, ex.getMessage());
			logger.error(log);
			throw new CustomException(Constants.NO_MSEXCEL_FILES_FOUND, 500);
		} finally {
			disconnectChannelSftp(channelSftp);
		}
		return fileNames;
	}
}