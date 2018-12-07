package com.kw.extract.main;

import java.util.ArrayList;
import java.util.List;

public class MyStack<T> {
	private List<T> list = null;
	public MyStack() {
		list = new ArrayList<T>();
	}
	
	public void push(T c) {
		list.add(c);
	}
	
	public T pop() {
		T c = list.get(list.size() - 1);
		list.remove(list.size() - 1);
		return c;
	}
	public int size() {
		return list.size();
	}
	
	public List<T> getList() {
		return list;
	}
}
