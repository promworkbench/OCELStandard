import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.processmining.ocel.exporters.OCEL2XMLExporter;
import org.processmining.ocel.importers.OCEL2XMLImporter;
import org.processmining.ocel.ocelobjects.OcelEventLog;

public class TestOCEL2XML {
	public static void importExport(String inputPath, String outputPath) {
		File file = new File(inputPath);
		InputStream is0 = null;
		try {
			is0 = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OCEL2XMLImporter ocelImporter = new OCEL2XMLImporter();
		OcelEventLog ocel = ocelImporter.doImportFromStream(is0);
		
		System.out.println("ciao");
		
		OCEL2XMLExporter ocelExporter = new OCEL2XMLExporter(ocel);
		
		try (OutputStream outputStream = new FileOutputStream(outputPath)) {
			ocelExporter.exportLogToStream(outputStream);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
		System.out.println("ciao2");
	}
	
	public static void main(String[] args) throws Exception {
		importExport("C:\\ocel20_example.xmlocel", "C:\\Users\\berti\\xmlocel1.xmlocel");
	}
}
