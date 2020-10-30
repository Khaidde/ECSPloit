package ecsploit.utils.collections;

import java.util.Arrays;

//Inspired by Artemis Framework
public class BitString {
	
	private long[] words; //a word is 8 bytes = 64 bits
	
	public BitString() {
		this(0);
	}
	
	public BitString(int numBits) {
		this.words = new long[numBits];
	}
	
	public BitString(BitString other) {
		words = Arrays.copyOf(other.words, other.words.length);
	}
	 
	private void assertSize(int wordIndex) {
		words = Arrays.copyOf(words, wordIndex + 1);
	}

	public boolean isEmpty() {
		for (long word: this.words) {
			if (word != 0) return false;
		}
		return true;
	}
	
	public void set(int index) {
		int wordIndex = index >>> 6;
		if (wordIndex >= words.length) this.assertSize(wordIndex);
		this.words[wordIndex] = this.words[wordIndex] | (1L << index);
	}
	
	public void clear(int index) {
		int wordIndex = index >>> 6;
		if (wordIndex >= words.length) this.assertSize(wordIndex);
		this.words[wordIndex] = this.words[wordIndex] & ~(1L << index);
	}
	
	public boolean get(int index) {
		int wordIndex = index >>> 6;
		if (wordIndex >= words.length) return false;
		return (this.words[wordIndex] & (1L << index)) != 0;
	}

	public int indexOfLSB() {
		for (long word: this.words) {
			if (word == 0) continue;
			long b = word - 1;
			long c = (word | b) ^ b;

			int index = 0;
			while (c > 1) {
				c = c >> 1;
				index++;
			}
			return index;
		}
		return -1;
	}
	
	public boolean includes(BitString other) {
		int minLen = Math.min(this.words.length, other.words.length);
		for (int i = 0; i < minLen; i++) {
			if ((other.words[i] & this.words[i]) != other.words[i]) return false;
		}
		
		if (this.words.length > other.words.length) return true;
		
		for (int i = this.words.length; i < other.words.length; i++) {
			if (other.words[i] != 0) return false;
		}
		return true;
	}
	
	public int hashCode() {
		return Arrays.hashCode(this.words);
	}
	
	public boolean equals(Object object) {
		if (object != null && this.getClass() == object.getClass()) {
			long[] otherWords = ((BitString) object).words;
			
			long[] maxWords;
			long[] minWords;
			if (this.words.length > otherWords.length) {
				maxWords = this.words;
				minWords = otherWords;
			} else {
				maxWords = otherWords;
				minWords = this.words;
			}
			for (int i = 0; i < minWords.length; i++) {
				if (words[i] != otherWords[i]) return false;
			}
			
			for (int i = minWords.length; i < maxWords.length; i++) {
				if (maxWords[i] != 0) return false;
			}
			return true;
		}
		return false;
	}
	
	public String toString() {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
			String line = Long.toBinaryString(words[i]);
			out.insert(0, line);
			if (i + 1 < words.length) {
				for (int j = 0; j < 64 - line.length(); j++) {
					out = new StringBuilder("0" + line);
				}
			}
		}
		return out.toString();
	}
}