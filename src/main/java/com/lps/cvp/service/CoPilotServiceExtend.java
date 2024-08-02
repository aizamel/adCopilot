package com.lps.cvp.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lps.cvp.util.Constants;

@Service
public class CoPilotServiceExtend {
	private final Logger logger = LoggerFactory.getLogger(CoPilotServiceExtend.class);

	@Value("${copilot.dir.split}")
	private String copilotDirSplit;

	@Value("${copilot.pages.per.loop}")
	private int copilotPagesPerLoop;	
	
	private final FileTransferService fileTransferService;

	public CoPilotServiceExtend(FileTransferService fileTransferService) {
		this.fileTransferService = fileTransferService;
	}

	public String convertToPDFPages(String filename, String payload) throws IOException {
		byte[] decodedBytes = Base64.getDecoder().decode(payload);

		String dirOutput = copilotDirSplit + File.separator + filename;
		// Create output directory if not exist yet
		createPvOutputDirectory(dirOutput);
		fileTransferService.deleteAll(dirOutput);
		try (PDDocument rawDocument = Loader.loadPDF(decodedBytes); PDDocument extractedPDF = new PDDocument()) {
			int totalPages = rawDocument.getNumberOfPages();
			List<PDPage> pages = new ArrayList<>();
			int counter = 1;
			for (PDPage page : rawDocument.getPages()) {
				try (PDDocument splitDocument = new PDDocument()) {
					splitDocument.addPage(page);

					String saveFilename = String.format("%s%s%s_page_%d.pdf", dirOutput, File.separator, filename,
							counter++);
					saveFilename = saveFilename.replace("\\", "/");
					/** System.out.println("saveFilename: " + saveFilename); **/
					splitDocument.save(saveFilename);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			return e.getMessage();
		}
		return "success";
	}

	public String convertToBase64StringX(String filename) throws IOException {
		String dirOutput = copilotDirSplit + File.separator + filename;
		// Create output directory if not exist yet
		createPvOutputDirectory(dirOutput);

		StringBuilder sb = new StringBuilder();
		try {
			List<Path> filePaths = Files.walk(Paths.get(dirOutput)).filter(Files::isRegularFile)
					.collect(Collectors.toList());
			int ctr = 0;
			if (filePaths.size() > 0) {
				for (Path path : filePaths) {
					ctr++;
					if(ctr > 20) {
						break;
					}
					String base64String = convertFileToBase64(path);
					sb.append(base64String);
					sb.append(";");
					try {
						Files.deleteIfExists(path);
					} catch (IOException e) {
						String log = String.format(Constants.ERROR, e.getMessage());
						logger.error(log);
					}
				}
				// Remove the last character if it is a semicolon
				if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ';') {
					sb.deleteCharAt(sb.length() - 1);
				}				
			} else {
				fileTransferService.deleteAll(dirOutput);
				File directory = new File(dirOutput);
				try {
					FileUtils.deleteDirectory(directory);
				} catch (IOException e) {
					logger.error(e.getMessage());
					return e.getMessage();
				}
				return "done";
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			return e.getMessage();
		}
		return sb.toString();
	}
	
	public String convertToBase64String(String filename) throws IOException {
	    String dirOutput = copilotDirSplit + File.separator + filename;
	    // Create output directory if not exist yet
	    createPvOutputDirectory(dirOutput);

	    StringBuilder sb = new StringBuilder();
	    try {
	        List<Path> filePaths = Files.walk(Paths.get(dirOutput))
	                .filter(Files::isRegularFile)
	                .sorted(new Comparator<Path>() {
	                    @Override
	                    public int compare(Path p1, Path p2) {
	                        String fileName1 = p1.getFileName().toString();
	                        String fileName2 = p2.getFileName().toString();
	                        return extractPageNumber(fileName1) - extractPageNumber(fileName2);
	                    }

	                    private int extractPageNumber(String fileName) {
	                        String[] parts = fileName.split("_page_");
	                        if (parts.length > 1) {
	                            String[] subParts = parts[1].split("\\.");
	                            if (subParts.length > 0) {
	                                try {
	                                    return Integer.parseInt(subParts[0]);
	                                } catch (NumberFormatException e) {
	                                    return 0; // or some default value
	                                }
	                            }
	                        }
	                        return 0; // or some default value
	                    }
	                })
	                .collect(Collectors.toList());

	        int ctr = 0;
	        if (filePaths.size() > 0) {
	            for (Path path : filePaths) {
	                ctr++;
	                if (ctr > copilotPagesPerLoop) {
	                    break;
	                }
	                String base64String = convertFileToBase64(path);
	                sb.append(base64String);
	                sb.append(";");
	                try {
	                    Files.deleteIfExists(path);
	                } catch (IOException e) {
	                    String log = String.format(Constants.ERROR, e.getMessage());
	                    logger.error(log);
	                }
	            }
	            // Remove the last character if it is a semicolon
	            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ';') {
	                sb.deleteCharAt(sb.length() - 1);
	            }
	        } else {
	            fileTransferService.deleteAll(dirOutput);
	            File directory = new File(dirOutput);
	            try {
	                FileUtils.deleteDirectory(directory);
	            } catch (IOException e) {
	                logger.error(e.getMessage());
	                return e.getMessage();
	            }
	            return "done";
	        }
	    } catch (IOException e) {
	        logger.error(e.getMessage());
	        return e.getMessage();
	    }
	    return sb.toString();
	}

	private String convertFileToBase64(Path path) throws IOException {
		byte[] fileContent = Files.readAllBytes(path);
		return Base64.getEncoder().encodeToString(fileContent);
	}

	public void createPvOutputDirectory(String dirOutput) {
		File newDirOutput = new File(dirOutput);
		if (!newDirOutput.exists()) {
			newDirOutput.mkdirs();
		}
	}
}
