package com.france.memoizer;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.france.Computable;


public class Memoizer3<A,V> implements Computable<A, V> {
	//将V改成Future<V> Future<V>表示一个得到V的过程
	private final Map<A,Future<V>> cache = new ConcurrentHashMap<A,Future<V>>();
	private final Computable<A, V> c;
	@Override
	public  V compute(final A arg) throws InterruptedException {
		// TODO Auto-generated method stub
		//获取Future
		Future<V> future=cache.get(arg);
		if(future==null){
			Callable<V> eval=new Callable<V>() {

				@Override
				public V call() throws Exception {
					// TODO Auto-generated method stub
					return c.compute(arg);
				}
			};
			//定义Callable并传递给Future
			FutureTask<V> ft=new FutureTask<V>(eval);
			future=ft;
			//将执行过程Future存在cache后开始run
			cache.put(arg, ft);
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
	public Memoizer3(Computable<A, V> c) {
		this.c = c;
	}
	
	
}
