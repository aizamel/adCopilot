package com.lps.cvp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.lps.cvp.model.PDFSearch;
import com.lps.cvp.response.ResponseMessage;
import com.lps.cvp.service.CoPilotService;
import com.lps.cvp.service.CoPilotServiceExtend;
import com.lps.cvp.service.FileTransferService;
import com.lps.cvp.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "CoPilot", description = "Endpoints for CoPilot")
@RestController
@RequestMapping("/api/copilot")
public class CoPilotController {
	private final Logger logger = LoggerFactory.getLogger(CoPilotController.class);

	private final FileTransferService fileTransferService;
	private final CoPilotService coPilotService;
	private final CoPilotServiceExtend coPilotServiceExtend;

	public CoPilotController(FileTransferService fileTransferService, CoPilotService coPilotService,
			CoPilotServiceExtend coPilotServiceExtend) {
		this.fileTransferService = fileTransferService;
		this.coPilotService = coPilotService;
		this.coPilotServiceExtend = coPilotServiceExtend;
	}

	@Value("${lps.copilot.pdf}")
	private String lpsCopilotPdf;

	@Operation(summary = "Given a search query, convert the binary PDF file to workable File then get pages from it based on the search query, return to it as PDF", tags = {
			"copilotPDFPages" })
	@ApiResponse(responseCode = "200", description = "Success", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = {
					@ExampleObject(name = "SuccessResponse", value = "{\"status\": 200, \"success\": true, \"message\": \"Upload successful\"}") }) })
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = {
					@ExampleObject(name = "ErrorResponse", value = "{\"status\": 500, \"success\": false, \"message\": \"Internal server error\"}") }) })
	@PostMapping("/getPages")
	public ResponseEntity<ResponseMessage> producePDFBasedOnQuery(@RequestBody String payload) {
		logger.info("START - Controller - producePDFBasedOnQuery");
		Gson gson = new Gson();
		PDFSearch searchData = gson.fromJson(payload, PDFSearch.class);
		try {
			String result = coPilotService.covertToSearchedPages(searchData);
			logger.info("END - Controller - producePDFBasedOnQuery");
			fileTransferService.deleteAll(lpsCopilotPdf);
			return new ResponseEntity<>(new ResponseMessage(HttpStatus.OK.value(), true, result), HttpStatus.OK);
		} catch (Exception e) {
			logger.info("ERROR - Controller - producePDFBasedOnQuery");
			logger.info("ERROR - Controller - {}", e.getMessage());
			return new ResponseEntity<>(
					new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), false, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Operation(summary = "Split PDF into pages.", tags = { "splitPDF" })
	@ApiResponse(responseCode = "200", description = "Success", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = {
					@ExampleObject(name = "SuccessResponse", value = "{\"status\": 200, \"success\": true, \"message\": \"Split successful\"}") }) })
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = {
					@ExampleObject(name = "ErrorResponse", value = "{\"status\": 500, \"success\": false, \"message\": \"Internal server error\"}") }) })
	@PostMapping("/splitPDF")
	public ResponseEntity<ResponseMessage> splitPDF(@RequestParam String filename, @RequestBody String payload) {
		try {
			ResponseMessage successMessage = new ResponseMessage(200, true, "Split successful");
			System.out.println("FILENAME: "+filename);
			String result = coPilotServiceExtend.convertToPDFPages(filename, payload);
			if(result.equals("success")) {
				logger.info("Split PDF into pages successful");
				successMessage = new ResponseMessage(200, true, result);
			}
			return new ResponseEntity<>(successMessage, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			ResponseMessage errorMessage = new ResponseMessage(500, false, Constants.INTERNAL_SERVER_ERROR);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Operation(summary = "Convert PDF pages to Base 64 String.", tags = { "convertPDF" })
	@ApiResponse(responseCode = "200", description = "Success", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = {
					@ExampleObject(name = "SuccessResponse", value = "{\"status\": 200, \"success\": true, \"message\": \"Convert successful\"}") }) })
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = {
					@ExampleObject(name = "ErrorResponse", value = "{\"status\": 500, \"success\": false, \"message\": \"Internal server error\"}") }) })
	@PostMapping("/convertPDF")
	public ResponseEntity<ResponseMessage> convertPDF(@RequestParam String filename) {
		try {
			ResponseMessage successMessage = new ResponseMessage(200, true, "Convert successful");
			String result = coPilotServiceExtend.convertToBase64String(filename);
			logger.info("Convert PDF pages to Base 64 String successful");
			successMessage = new ResponseMessage(200, true, result);
			return new ResponseEntity<>(successMessage, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			ResponseMessage errorMessage = new ResponseMessage(500, false, Constants.INTERNAL_SERVER_ERROR);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
