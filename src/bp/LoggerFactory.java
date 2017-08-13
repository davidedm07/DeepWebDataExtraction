package bp;

import java.util.logging.*;

public class LoggerFactory {
	static final Logger logger = Logger.getLogger("bpm.LoggerFactory");
	static {
		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new ConsoleFormatter());
		ch.setLevel(Level.WARNING);
		logger.setLevel(Level.WARNING);
		logger.addHandler(ch);
		logger.setUseParentHandlers(false);
		FileHandler fh;
		try {
			fh = new FileHandler("BPLog.txt");
			fh.setFormatter(new FileFormatter());
			fh.setLevel(Level.ALL);
			logger.addHandler(fh);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Logger getLogger() {
		return logger;
	}
}
