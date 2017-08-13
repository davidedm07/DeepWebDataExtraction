package bp;

import java.util.logging.*;

public class FileFormatter extends Formatter {

	 public String format(LogRecord rec) {
         StringBuffer buf = new StringBuffer(1000);
         // Bold any levels >= WARNING
         if (rec.getLevel().intValue() >= Level.INFO.intValue()) {
             buf.append(rec.getLevel());
             buf.append(' ');
//             buf.append(new Date(rec.getMillis()));
//             buf.append(' ');
         }
         buf.append(formatMessage(rec));
         buf.append('\n');
         return buf.toString();
     }

}
