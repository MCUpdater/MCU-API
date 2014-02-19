package org.mcupdater;

// Heavily based on cpw.mods.fml.relauncher.FMLLogFormatter

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FMLStyleFormatter extends Formatter {
	static final String LINE_SEP = System.getProperty("line.separator");
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public String format(LogRecord record) {
		StringBuilder msg = new StringBuilder();
		msg.append(sdf.format(record.getMillis()));
		Level level = record.getLevel();
		
		String name = level.getLocalizedName();
		if (name == null) { name = level.getName(); }
		
		if ( name != null && name.length() > 0) {
			msg.append(" [").append(name).append("] ");
		} else {
			msg.append(" ");
		}
		// Heavily based on cpw.mods.fml.relauncher.FMLLogFormatter
		if (record.getLoggerName() != null) {
			msg.append("[").append(record.getLoggerName()).append("] ");
		}
		
		msg.append(record.getMessage());
		msg.append(LINE_SEP);
		Throwable thrown = record.getThrown();
		
		if (thrown != null) {
			StringWriter thrownDump = new StringWriter();
			thrown.printStackTrace(new PrintWriter(thrownDump));
			msg.append(thrownDump.toString());
		}
		return msg.toString();
	}

}
