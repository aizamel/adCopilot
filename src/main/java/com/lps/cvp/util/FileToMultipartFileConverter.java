/**
 * FileToMultipartFileConverter class
 * Purpose: For CV Profiling Project
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-01-01
 */
package com.lps.cvp.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.lps.cvp.service.DocumentIntelligenceService;

public class FileToMultipartFileConverter implements MultipartFile {

	private Logger logger = LoggerFactory.getLogger(FileToMultipartFileConverter.class);
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public FileToMultipartFileConverter(File file) throws IOException {
        this.name = file.getName();
        this.originalFilename = file.getName();
        this.contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"; // content type for .docx
        this.content = loadFileContent(file);
    }

    private byte[] loadFileContent(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            int bytesRead = input.read(buffer);
            if (bytesRead < 0) {
                throw new IOException("Failed to read file");
            }
            return buffer;
        }
    }

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getOriginalFilename() {
		return this.originalFilename;
	}

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return this.content.length == 0;
    }

    @Override
    public long getSize() {
        return this.content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return this.content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
    	try (FileOutputStream fos = new FileOutputStream(dest)) {
    	    fos.write(this.content);
    	} catch (IOException e) {
    		logger.error(e.getMessage());
    	}

    }
}