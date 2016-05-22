package com.france.memoizer;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.france.Computable;


public class Memoizer5<A,V> implements Computable<A, V> {
	private final ConcurrentHashMap<A,Future<V>> cache = new ConcurrentHashMap<A,Future<V>>();
	private final Computable<A, V> c;
	@Override
	public  V compute(final A arg) throws InterruptedException {
		// TODO Auto-generated method stub
		//放在一个循环中去操作 抛出异常时 移除cache 重新操作
		while(true){
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
				cache.putIfAbsent(arg, ft);
				ft.run();
			}
			try {
				return future.get();
			}catch(CancellationException e){
				//出现异常 删除缓存
				cache.remove(arg,future);
			}catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public Memoizer5(Computable<A, V> c) {
		this.c = c;
	}
	
	
}
