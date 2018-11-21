package cn.jerry.test.lang;

public class CharTester {
	public static void main(String[] args) {
		char a = 'a', b = 'A';
		int i1 = a, i2 = b;
		System.out.println(i1 + "," + i2 + "," + (i1 - i2));
		char c = (char) (a-32);
		System.out.println(c);
		System.out.println("%abcd__".replaceAll("[%,_]", ""));
		System.out.println((int)'0');
		System.out.println((int)'9');
	}
}
