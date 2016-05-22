package com.france.memoizer;

import java.util.HashMap;
import java.util.Map;

import com.france.Computable;

public class Memoizer1<A,V> implements Computable<A, V> {
	private final Map<A,V> cache = new HashMap<A,V>();
	private final Computable<A, V> c;
	@Override
	public synchronized V compute(A arg) throws InterruptedException {
		// TODO Auto-generated method stub
		V result=cache.get(arg);
		if(result==null){
			result=c.compute(arg);
			cache.put(arg, result);
		}
		return result;
	}
	public Memoizer1(Computable<A, V> c) {
		this.c = c;
	}
	
	
}
