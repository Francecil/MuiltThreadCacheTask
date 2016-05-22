package com.france.memoizer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.france.Computable;

public class Memoizer2<A,V> implements Computable<A, V> {
	//将
	private final Map<A,V> cache = new ConcurrentHashMap<A,V>();
	private final Computable<A, V> c;
	//由于线程安全，这里不再使用synchronized
	@Override
	public  V compute(A arg) throws InterruptedException {
		// TODO Auto-generated method stub
		V result=cache.get(arg);
		if(result==null){
			result=c.compute(arg);
			cache.put(arg, result);
		}
		return result;
	}
	public Memoizer2(Computable<A, V> c) {
		this.c = c;
	}
	
	
}
