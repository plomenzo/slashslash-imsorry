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
	private static final String CORRECTIONS_FILE_PATH = "src/main/resources/static/documents/VoiceRecognitionCorrections.txt";
	
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
			File correctionsFile = new File(CORRECTIONS_FILE_PATH);
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