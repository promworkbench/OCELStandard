package org.processmining.ocel.importers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.processmining.csv.CustomCsvReader;
import org.processmining.csv.OCELConverter;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class CSVInputParameters {
	InputStream input;
	Charset charset;
	String newLineChars;
	char separator;
	char quotechar;
	String objListSeparatorWay;
	List<List<String>> parsedCsv;
	Map<Integer, String> columns;
	
	public CSVInputParameters() {
		this.charset = StandardCharsets.UTF_8;
		this.newLineChars = "\r\n";
		this.separator = ',';
		this.quotechar = '"';
		this.objListSeparatorWay = "classic";
	}
	
	public CSVInputParameters(InputStream input) {
		this.charset = StandardCharsets.UTF_8;
		this.newLineChars = "\r\n";
		this.separator = ',';
		this.quotechar = '"';
		this.objListSeparatorWay = "classic";
		this.input = input;
	}
	
	public String getFileContent() {
	    StringBuilder textBuilder = new StringBuilder();
	    try (Reader reader = new BufferedReader(new InputStreamReader
	      (input, Charset.forName(this.charset.name())))) {
	        int c = 0;
	        while ((c = reader.read()) != -1) {
	            textBuilder.append((char) c);
	        }
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return textBuilder.toString();
	}
	
	public void compute() {
		this.parsedCsv = CustomCsvReader.parseContent(this.getFileContent(), this.newLineChars, this.separator, this.quotechar);
		this.columns = OCELConverter.getDefaultMapping(this.parsedCsv);
	}
	
	public OcelEventLog obtainEventLog() {
		return OCELConverter.getOCELfromParsedCSV(this.parsedCsv, this.columns, this.objListSeparatorWay);
	}
}
