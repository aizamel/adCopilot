package com.lps.cvp.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.lps.cvp.controller.CoPilotController;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lps.cvp.model.PDFSearch;

@Service
public class CoPilotService {
	private final Logger logger = LoggerFactory.getLogger(CoPilotController.class);

	private final FileTransferService fileTransferService;

	public CoPilotService(FileTransferService fileTransferService) {
		this.fileTransferService = fileTransferService;
	}

	@Value("${lps.copilot.pdf}")
	private String outputFilePath;

	public String covertToSearchedPages(PDFSearch searchInfo) throws IOException {
		logger.info("START - Service - covertToSearchedPages");
		String dirOutput = outputFilePath + File.separator + searchInfo.getFilename();
		System.out.println("dirOutput: "+dirOutput);
		// Create output directory if not exist yet
		createPvOutputDirectory(dirOutput);
		fileTransferService.deleteAll(dirOutput);
		
		byte[] decodedBytes = Base64.getDecoder().decode(searchInfo.getSearchContent());

		try (PDDocument rawDocument = Loader.loadPDF(decodedBytes); PDDocument extractedPDF = new PDDocument()) {
			int totalPages = rawDocument.getNumberOfPages();
			List<PDPage> pagesWithSearchTerm = new ArrayList<>();
			/***
			 * File newDir = new File(outputFilePath); 
			 * Path path = Paths.get(outputFilePath); 
			 * FileUtils.cleanDirectory(newDir);
			 * Files.deleteIfExists(path);
			 ***/
			
			if (searchInfo.getSearchTerm().isEmpty()) {
				int counter = 1;
				for (PDPage page : rawDocument.getPages()) {
					try (PDDocument splitDocument = new PDDocument()) {
						splitDocument.addPage(page);
						String filename = String.format("%s%s%s_page_%d.pdf", dirOutput, File.separator,
								searchInfo.getFilename(), counter++);
						filename = filename.replace("\\", "/");
						System.out.println("filename1: "+filename);
						splitDocument.save(filename);
					}
				}
			} else {
				PDFTextStripper stripper = new PDFTextStripper();
				for (int i = 1; i <= totalPages; i++) {
					stripper.setStartPage(i);
					stripper.setEndPage(i);
					String extractedText = stripper.getText(rawDocument);
					if (extractedText.isEmpty()) {
						continue;
					}
					if (extractedText.toLowerCase().contains(searchInfo.getSearchTerm().toLowerCase().trim())) {
						pagesWithSearchTerm.add(rawDocument.getPage(i));
					}
				}
				for (PDPage extractedPage : pagesWithSearchTerm) {
					extractedPDF.addPage(extractedPage);
				}
				String filename = String.format("%s%spdf_%s.pdf", outputFilePath, File.separator, "extracted");
				System.out.println("filename2: "+filename);
				extractedPDF.save(filename);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		logger.info("END - Service - covertToSearchedPages");
		String result = processFilesToZip(dirOutput);
		fileTransferService.deleteAll(dirOutput);
		return result;
	}

	private String processFilesToZip(String outputPath) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try (Stream<Path> stream = Files.list(Paths.get(outputPath));
				ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
			Set<String> fileList = stream.filter(file -> !Files.isDirectory(file)).map(Path::getFileName)
					.map(Path::toString).map(Path -> outputPath + File.separator + Path).collect(Collectors.toSet());
			for (String file : fileList) {
				File pdfFile = new File(file);
				try (InputStream inputStream = new FileInputStream(file)) {
					ZipEntry zipEntry = new ZipEntry(pdfFile.getName());
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
			zipOutputStream.finish();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return encodeZipToBase64(outputPath, byteArrayOutputStream);
	}

	private String encodeZipToBase64(String outputPath, ByteArrayOutputStream byteArrayOutputStream) {
		// Get the current date and time
		LocalDateTime now = LocalDateTime.now();
		// Format the date and time as a string
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
		String formattedDateTime = now.format(formatter);
		String base64Encoded = null;
		String zipName = outputPath + File.separator + "extracted_pdfs_" + formattedDateTime + ".zip";
		zipName = Paths.get(zipName).toString();
		try (FileOutputStream fos = new FileOutputStream(zipName)) {
			fos.write(byteArrayOutputStream.toByteArray());
			// Read the ZIP file into a byte array
			byte[] zipFileBytes = Files.readAllBytes(Paths.get(zipName));
			// Encode bytes to base64 string
			base64Encoded = Base64.getEncoder().encodeToString(zipFileBytes);
			byteArrayOutputStream.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return base64Encoded;
	}

	public void createPvOutputDirectory(String dirOutput) {
		File newDirOutput = new File(dirOutput);
		if (!newDirOutput.exists()) {
			newDirOutput.mkdirs();
		}
	}
}
