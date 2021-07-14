package org.processmining.tbr;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.deckfour.xes.model.XLog;

public class LogStaticReadingUtils {
	public static XLog readSingleLog(String fileName) throws Exception {
		File initialFile = new File(fileName);
		InputStream inputStream = new FileInputStream(initialFile);
		org.xeslite.parser.XesLiteXmlParser parser = new
		org.xeslite.parser.XesLiteXmlParser(true);
		List<XLog> parsedLogs = parser.parse(inputStream);
		if (parsedLogs.size() > 0) {
			return parsedLogs.get(0);
		}
		return null;
	}
}
