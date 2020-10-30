package ecsploit.main.test;

import ecsploit.utils.collections.BitString;

import java.util.Random;

public class BitStringTest {
	
	public static void main(String[] args) {
		BitString a = new BitString();
		BitString b = new BitString();
		
		System.out.println(a.equals(b));
		
		a.set(96);
		System.out.println(a.equals(b));
		
		b.set(4);
		System.out.println(a.equals(b));
		
		a.clear(96);
		a.set(4);
		System.out.println(a.equals(b));

		System.out.println("---------");

		BitString c = new BitString();

		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			int value = random.nextInt(30);
			System.out.println(value);
			c.set(value);
		}
		System.out.println(c);
		while(!c.isEmpty()) {
			int index = c.indexOfLSB();
			System.out.println(c + "::" + c.indexOfLSB());
			c.clear(index);
		}
	}

}
