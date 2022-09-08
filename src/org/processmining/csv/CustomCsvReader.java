package org.processmining.csv;

import java.util.ArrayList;
import java.util.List;

public class CustomCsvReader {
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DOUBLE_QUOTES = '"';
    private static final char DEFAULT_QUOTE_CHAR = DOUBLE_QUOTES;
    private static final String NEW_LINE = "\n";
    private static boolean isMultiLine = false;
    private static String pendingField = "";
    private static String[] pendingFieldLine = new String[]{};
    
    public static List<List<String>> parseContent(String fileContent, String newline, char separator, char quoteChar) {
    	String[] rows = fileContent.split(newline);
    	List<List<String>> ret = new ArrayList<List<String>>();
    	for (String row : rows) {
    		try {
				ret.add(CustomCsvReader.parse(row, separator, quoteChar));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return ret;
    }
    
    public static List<String> parse(String line, char separator, char quoteChar)
            throws Exception {

            List<String> result = new ArrayList<>();

            boolean inQuotes = false;
            boolean isFieldWithEmbeddedDoubleQuotes = false;

            StringBuilder field = new StringBuilder();

            for (char c : line.toCharArray()) {

                if (c == DOUBLE_QUOTES) {               // handle embedded double quotes ""
                    if (isFieldWithEmbeddedDoubleQuotes) {

                        if (field.length() > 0) {       // handle for empty field like "",""
                            field.append(DOUBLE_QUOTES);
                            isFieldWithEmbeddedDoubleQuotes = false;
                        }

                    } else {
                        isFieldWithEmbeddedDoubleQuotes = true;
                    }
                } else {
                    isFieldWithEmbeddedDoubleQuotes = false;
                }

                if (isMultiLine) {                      // multiline, add pending from the previous field
                    field.append(pendingField).append(NEW_LINE);
                    pendingField = "";
                    inQuotes = true;
                    isMultiLine = false;
                }

                if (c == quoteChar) {
                    inQuotes = !inQuotes;
                } else {
                    if (c == separator && !inQuotes) {  // if find separator and not in quotes, add field to the list
                        result.add(field.toString());
                        field.setLength(0);             // empty the field and ready for the next
                    } else {
                        field.append(c);                // else append the char into a field
                    }
                }

            }

            //line done, what to do next?
            if (inQuotes) {
                pendingField = field.toString();        // multiline
                isMultiLine = true;
            } else {
                result.add(field.toString());           // this is the last field
            }

            return result;

        }
}
