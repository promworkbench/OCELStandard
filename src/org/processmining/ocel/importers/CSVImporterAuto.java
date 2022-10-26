package org.processmining.ocel.importers;

import java.io.InputStream;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.ocel.ocelobjects.OcelEventLog;

@Plugin(name = "Import OCEL from CSV", parameterLabels = { "Filename" }, returnLabels = {
"Object-Centric Event Log" }, returnTypes = { OcelEventLog.class })
@UIImportPlugin(description = "Import OCEL from CSV (auto mapping)", extensions = { "csvocel", "csv" })
public class CSVImporterAuto extends AbstractImportPlugin {

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		// TODO Auto-generated method stub
		CSVInputParameters csvInputParameters = new CSVInputParameters(input);
		csvInputParameters.compute();
		return csvInputParameters.obtainEventLog();
	}

}
