package bp;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ConsoleFormatter extends Formatter {

	public String format(LogRecord rec) {
		StringBuffer buf = new StringBuffer(1000);
		// Bold any levels >= WARNING
		buf.append(rec.getLevel());
		buf.append(": ");
		buf.append(formatMessage(rec));
		buf.append('\n');
		return buf.toString();
	}

}
