/**
 * ExtractTextFromWord class
 * Purpose: Extract contents of resume text file per number of resumes
 *
 * @author Angelito O. Ulaye
 * @version 1.0
 * @since 2024-06-14
 */
package com.lps.cvp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;

public class ExtractTextFromWord {

	@Value("${number.of.resumes}")
	private int numberOfResumes;

	public ExtractTextFromWord() {
		// Nothing to code
	}

	public List<String> extractTextFromResumes(List<String> quotedWords, List<String> processedResumesText) {
		List<String> extractedTexts = new ArrayList<>();
		StringBuilder bob = new StringBuilder();
		boolean found = false;
		int resumeCtr = 0;
		int foundCtr = 0;
		for (String resume : processedResumesText) {
			String profileInfo = resume.trim().toLowerCase();
			for (String skill : quotedWords) {
				String skillInfo = skill.trim().toLowerCase();				
				if (profileInfo.contains(skillInfo)) {
					found = true;
					break;
				}								
			}
			if (found) {
				foundCtr++;
				bob.append("-\n");
				bob.append(resume.trim());
				found = false;
			}
			resumeCtr++;
			if (resumeCtr > numberOfResumes) {
				resumeCtr = 1;
				extractedTexts.add(bob.toString().trim());
				bob = new StringBuilder();
			}
		}
		extractedTexts.add(bob.toString().trim());
		List<String> filter = new ArrayList<>();
		for (String s : extractedTexts) {
			if (!s.trim().equals("")) {
				filter.add(s.trim());
			}
		}
		//System.out.println("foundCtr: " + foundCtr);
		return filter;
	}
}