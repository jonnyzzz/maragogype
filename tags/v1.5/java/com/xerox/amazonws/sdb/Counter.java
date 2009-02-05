
package com.xerox.amazonws.sdb;

// a simple mutable integer
public class Counter {
	private int value;

	public Counter(int initialVal) {
		value = initialVal;
	}

	public void decrement() {
		value--;
	}

	public void increment() {
		value++;
	}

	public int getValue() {
		return value;
	}
}
