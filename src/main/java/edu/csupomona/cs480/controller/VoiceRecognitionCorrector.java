package edu.csupomona.cs480.controller;

import java.io.File;
import java.util.Hashtable;
import java.util.Scanner;

public class VoiceRecognitionCorrector
{
	// single instance of the voice recognition corrector
	private static VoiceRecognitionCorrector instance;
	// hashtable that stores all the "corrections"
	// uses incorrect as key and correct as value
	private Hashtable<String, String> converter;
	// Path to the text file containing the corrections
	private static final String LOCAL_CORRECTIONS_FILE_PATH = "src/main/resources/static/documents/VoiceRecognitionCorrections.txt";
	private static final String SERVER_CORRECTIONS_FILE_PATH = "http://ec2-54-201-132-163.us-west-2.compute.amazonaws.com/documents/VoiceRecognitionCorrections.txt";
	
	/**
     * Gets the instance of the VoiceRecognitionCorrector
     * @return the instance
     */
	public static VoiceRecognitionCorrector getVoiceRecognitionCorrector()
	{
		if(instance == null)
			instance = new VoiceRecognitionCorrector();
		return instance;
	}
	
	/**
     * Constructor for the instance of the VoiceRecognitionCorrector. 
     * Uses a default path to the file of corrections
     * @return new VoiceRecognitionCorrector for instance
     */
	private VoiceRecognitionCorrector()
	{
		converter = new Hashtable<String, String>();
		try
		{
			File correctionsFile = chooseExistingFile(LOCAL_CORRECTIONS_FILE_PATH, SERVER_CORRECTIONS_FILE_PATH);
			if(correctionsFile.exists())
			{
				System.out.println("FILE FOUND FOR VRC");
				Scanner correctionsScanner = new Scanner(correctionsFile);
				while(correctionsScanner.hasNextLine())
				{
					String line = correctionsScanner.nextLine();
					if((line.length() == 0) || (!(line.contains("=>"))))
					{
						// deals with blank lines and incorrect lines
						continue;
					}
					int splitNdx = line.indexOf("=>");
					String key = line.substring(0, splitNdx).trim();
					String value = line.substring(splitNdx+2).trim();
					
					converter.put(key, value);
				}
				correctionsScanner.close();
			}
		}
		catch(Exception ex)
		{
			System.out.println("Exception thrown in VoiceRecognitionCorrector: " + ex);
		}
	}
	
	/**
     * Chooses which file exists and returns that file
     * @param filePath1 the String of the first file path
	 * @param filePath2 the String of the second file path
     * @return the existing file, if both exists then it returns filePath1, if neither exists then returns null
     */
	private File chooseExistingFile(String filePath1, String filePath2)
	{
		File file1 = new File(filePath1);
		if(file1.exists())
		{
			System.out.println("File path 1 exists: " + filePath1);
			return file1;
		}
		File file2 = new File(filePath2);
		if(file2.exists())
		{
			System.out.println("File path 2 exists: " + filePath2);
			return file2;
		}
		// neither file exists so return null
		System.out.println("Neither file path exists: " + filePath1 + " , " + filePath2);
		return null;
	}
	
	/**
     * Corrects the String by substituting when required
     * @param voice the String to be corrected
     * @return the corrected String
     */
	public String correct(String voice)
	{
	    String lowerVoice = voice.toLowerCase();
		if(!(converter.containsKey(lowerVoice)))
		{
			return voice;
		}
		return converter.get(lowerVoice);
	}
}