package ecsploit.utils.debug;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public final class ToStringBuilder {
	
	private static final String PREFIX = "|    ";
	private static final String ETC = "...";
	
	private static final Deque<Integer> classHashTrace = new LinkedList<>();
	
	public static ToStringBuilder from(Object object) {
		int hashCode = object.hashCode();
		boolean bypass = false;
		if (classHashTrace.contains(hashCode)) {
			bypass = true;
		}
		classHashTrace.push(hashCode);
		return new ToStringBuilder(bypass, object.getClass().getSimpleName() + ".class:");
	}

	public static ToStringBuilder fromC(String message) {
		int hashCode = message.hashCode();
		boolean bypass = false;
		if (classHashTrace.contains(hashCode)) {
			bypass = true;
		}
		classHashTrace.push(hashCode);
		return new ToStringBuilder(bypass, message);
	}
	
	private final boolean bypass;
	
	private final StringBuilder result;
	private String maxIndentDepthCheck;
	private boolean hasEtcChain = false;
	
	private ToStringBuilder(boolean bypass, String label) {
		this.bypass = bypass;
		this.result = new StringBuilder(label + "\n");
		this.depth(100);
	}
	
	public ToStringBuilder depth(int maxDepth) {
		this.maxIndentDepthCheck = PREFIX.repeat(Math.max(0, maxDepth + 1));
		return this;
	}
	
	private void append(String line) {
		if (this.checkExceedDepthLimit(line)) {
			if (!hasEtcChain) {
				this.result.append(this.maxIndentDepthCheck).append(ETC).append("\n");
				hasEtcChain = true;
			}
		} else {
			this.result.append(line).append("\n");
			hasEtcChain = false;
		}
	}
	
	private boolean checkExceedDepthLimit(String line) {
		for (int i = 0; i < this.maxIndentDepthCheck.length(); i++) {
			if (i >= line.length()) return false;
			if (this.maxIndentDepthCheck.charAt(i) != line.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	public ToStringBuilder with(String message) {
		if (this.bypass) return this;
		this.append(PREFIX + message);
		return this;
	}
	
	public ToStringBuilder withPrim(String name, Object value) {
		if (this.bypass) return this;
		this.append(PREFIX + name + " = " + value);
		return this;
	}
	
	public ToStringBuilder withPrim(String name, String value) {
		if (this.bypass) return this;
		this.append(PREFIX + name + " = \"" + value + "\"");
		return this;
	}

	public ToStringBuilder withIntArray(String name, int[] values) {
		if (this.bypass) return this;

		StringBuilder out = new StringBuilder("[");
		for (int i = 0; i < values.length; i++) {
			out.append(values[i]);
			if (i + 1 < values.length) out.append(", ");
		}
		this.append(PREFIX + name + ":(int[].class) = " + out.toString() + "]");
		return this;
	}

	public ToStringBuilder withStringArray(String name, String[] values) {
		if (this.bypass) return this;

		StringBuilder out = new StringBuilder("[");
		for (int i = 0; i < values.length; i++) {
			out.append(values[i]);
			if (i + 1 < values.length) out.append(", ");
		}
		this.append(PREFIX + name + ":(String[].class) = " + out.toString() + "]");
		return this;
	}

	public <T> ToStringBuilder withArrayB(T[] values) {
		if (this.bypass) return this;
		for (T value: values) {
			String[] objectLines = value == null ? new String[]{"null"} : value.toString().split("\n");
			for (String objectLine: objectLines) {
				this.append(PREFIX + objectLine);
			}
		}
		return this;
	}

	public <T> ToStringBuilder withArray(String name, T[] values) {
		if (this.bypass) return this;
		this.append(PREFIX + name + ":(" + values.getClass().getComponentType().getSimpleName() + "[].class)");
		for (T value: values) {
			String[] objectLines = value == null ? new String[]{"null"} : value.toString().split("\n");
			for (String objectLine: objectLines) {
				this.append(PREFIX + PREFIX + objectLine);
			}
		}
		return this;
	}
	
	public ToStringBuilder withList(String name, Collection<?> list) {
		if (this.bypass) return this;
		this.append(PREFIX + name + ":(" + list.getClass().getSimpleName() + ".class)");
		for (Object line: list) {
			String[] objectLines = line == null ? new String[]{"null"} : line.toString().split("\n");
			for (String objectLine : objectLines) {
				this.append(PREFIX + PREFIX + objectLine);
			}
		}
		return this;
	}

	public ToStringBuilder withMap(String name, Map<?, ?> map) {
		if (this.bypass) return this;
		this.append(PREFIX + name + ":(" + map.getClass().getSimpleName() + ".class)");
		for (Entry<?, ?> line: map.entrySet()) {
			String key = line.getKey() == null ? "null" : line.getKey().toString();
			String[] valueLines = line.getValue() == null ? new String[]{"null"} : line.getValue().toString().split("\n");

			this.append(PREFIX + PREFIX + "(" + key + ")->" + valueLines[0]);
			for (int j = 1; j < valueLines.length; j++) {
				this.append(PREFIX + PREFIX + valueLines[j]);
			}
		}
		return this;
	}
	
	public ToStringBuilder withObj(String name, Object value) {
		if (this.bypass) return this;
		String[] lines = value.toString().split("\n");

		this.append(PREFIX + name + ":(" + value.getClass().getSimpleName() + ".class)");
		for (int i = 1; i < lines.length; i++) {
			this.append(PREFIX + lines[i]);
		}
		return this;
	}
	
	public String toString() {
		classHashTrace.pop();
		if (this.bypass) return "\n" + PREFIX + ETC;
		return result.substring(0, result.length() - 1);
	}
}
