package ecsploit.utils.debug;

import java.util.HashMap;
import java.util.Map;

public final class Console {
	
	private static final class DefaultConsoleException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;

		public DefaultConsoleException(String string) {
			super(string);
		}
	}
	
	public enum LogLevel {
		ERROR(0),
		WARN(1),
		INFO(2),
		DEBUG(3),
		DETAIL(4);
		
		private final int priority;
		
		LogLevel(int priority) {
			this.priority = priority;
		}
		
		public int getPriority() {
			return priority;
		}
	}
	
	private static final Map<Class<?>, Console> consoleList = new HashMap<>();
	
	public static Console getConsole(Class<?> consoleClass) {
		if (!consoleList.containsKey(consoleClass)) {
			consoleList.put(consoleClass, new Console());
		}
		return consoleList.get(consoleClass);
	}
	
	private LogLevel logLevel;
	
	private Console() {
		this.logLevel = LogLevel.DETAIL;
	}
	
	public Console withLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
		return this;
	}
	
	private static final int DEFAULT_TRACE_BACK_AMOUNT = 2;
	
	public void detail(String message) {
		if (this.logLevel.getPriority() < LogLevel.DETAIL.getPriority()) return;
		synchronized (Console.class) {
			System.out.println("\t=> " + message);
		}
	}
	
	public void debug(String message) {
		if (this.logLevel.getPriority() < LogLevel.DEBUG.getPriority()) return;
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		synchronized (Console.class) {
			System.err.println("[Debug in " + elements[DEFAULT_TRACE_BACK_AMOUNT] + "] " + message);
		}
	}
	
	public void info(String message) {
		if (this.logLevel.getPriority() < LogLevel.INFO.getPriority()) return;
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		synchronized (Console.class) {
			System.out.println("[Info in (" + elements[DEFAULT_TRACE_BACK_AMOUNT].getFileName() + ":" + elements[DEFAULT_TRACE_BACK_AMOUNT].getLineNumber() + ")] " + message);
		}
	}
	
	public void warn(String message) {
		if (this.logLevel.getPriority() < LogLevel.WARN.getPriority()) return;
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		synchronized (Console.class) {
			System.err.println("[Warning in " + elements[DEFAULT_TRACE_BACK_AMOUNT] + "] " + message);
			traceLog(elements);
		}
	}
	
	public void err(String message) {
		if (this.logLevel.getPriority() < LogLevel.ERROR.getPriority()) return;
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		synchronized (System.out) {
			throw new DefaultConsoleException("\n[Error in " + elements[DEFAULT_TRACE_BACK_AMOUNT] + "] " + message);
		}
	}
	
	private void traceLog(StackTraceElement[] elements) {
		synchronized (System.out) {
			for (int i = DEFAULT_TRACE_BACK_AMOUNT + 1; i < elements.length; i++) {
				System.err.println("\tin " + elements[i]);
			}	
		}
	}

}
