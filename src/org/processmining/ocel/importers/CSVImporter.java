package org.processmining.ocel.importers;

import java.io.InputStream;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

@Plugin(name = "Import OCEL from CSV", parameterLabels = { "Filename" }, returnLabels = {
"OCEL CSV (half imported)" }, returnTypes = { CSVInputParameters.class })
@UIImportPlugin(description = "Import OCEL from CSV", extensions = { "csvocel", "csv" })
public class CSVImporter extends AbstractImportPlugin {

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		// TODO Auto-generated method stub
		CSVInputParameters csvInputParameters = new CSVInputParameters(input);
		return csvInputParameters;
	}

}
