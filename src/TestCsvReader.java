import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.processmining.csv.CustomCsvReader;
import org.processmining.csv.OCELConverter;
import org.processmining.csv.OCELExporterCSV;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestCsvReader {
	public static void main(String[] args) throws IOException {
		String fileContent = new String(Files.readAllBytes(Paths.get("C:\\example_log.csv")), StandardCharsets.UTF_8);
		List<List<String>> parsedCsv = CustomCsvReader.parseContent(fileContent, "\r\n", ',', '"');
		Map<Integer, String> columns = OCELConverter.getDefaultMapping(parsedCsv);
		OcelEventLog ocel = OCELConverter.getOCELfromParsedCSV(parsedCsv, columns, "classic");
		String ret = OCELExporterCSV.exportCsv(ocel, "\r\n", ',', '\"');
		System.out.println(ret);
	}
}
