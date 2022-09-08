import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.processmining.csv.CustomCsvReader;

public class TestCsvReader {
	public static void main(String[] args) throws IOException {
		String fileContent = new String(Files.readAllBytes(Paths.get("C:\\example_log.csv")), StandardCharsets.UTF_8);
		List<List<String>> parsedCsv = CustomCsvReader.parseContent(fileContent, "\r\n", ',', '"');
		for (List<String> row : parsedCsv) {
			System.out.println(row);
		}
	}
}
