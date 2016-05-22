package com.france.memoizer;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.france.Computable;


public class Memoizer4<A,V> implements Computable<A, V> {
	//实现用的ConcurrentHashMap而不是Map
	private final ConcurrentHashMap<A,Future<V>> cache = new ConcurrentHashMap<A,Future<V>>();
	private final Computable<A, V> c;
	@Override
	public  V compute(final A arg) throws InterruptedException {
		// TODO Auto-generated method stub
		Future<V> future=cache.get(arg);
		if(future==null){
			Callable<V> eval=new Callable<V>() {

				@Override
				public V call() throws Exception {
					// TODO Auto-generated method stub
					return c.compute(arg);
				}
			};
			FutureTask<V> ft=new FutureTask<V>(eval);
			future=ft;
			//putIfAbsent 原子方法，详情请看源码 用的是Segment控制
			cache.putIfAbsent(arg, ft);
			ft.run();
		}
		try {
			return future.get();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public Memoizer4(Computable<A, V> c) {
		this.c = c;
	}
	
	
}
