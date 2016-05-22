## 首先给上我们的耗时任务，和简单web框架搭建
提供一个0-20的数字，计算前n项和，为了不使得计算太快 每加一次 sleep

    package com.france.servlet;
    
    import java.io.IOException;
    
    import javax.servlet.Servlet;
    import javax.servlet.ServletConfig;
    import javax.servlet.ServletException;
    import javax.servlet.ServletRequest;
    import javax.servlet.ServletResponse;
    
    public class TaskServlet implements Servlet{
    
    
    	@Override
    	public void service(ServletRequest req, ServletResponse resp)
    			throws ServletException, IOException {
    		// TODO Auto-generated method stub
    			try {
    				int num=Integer.valueOf(req.getParameter("num"));
    				if(num<=0)throw new Exception("数字<=0");
    				if(num>=20)throw new Exception("数字>=20");
    				int sum=calculateSumWithSleep(num,500);
    				System.out.println("计算得到的结果是:"+sum);
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    	}
    	private int calculateSumWithSleep(int num,long ms){
    		int sum=0;
    		for(int i=0;i<=num;i++){
    			sum+=i;
    			try {
    				Thread.sleep(ms);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    		return sum;
    	}
    
    	@Override
    	public void destroy() {
    		// TODO Auto-generated method stub
    		
    	}
    
    	@Override
    	public ServletConfig getServletConfig() {
    		// TODO Auto-generated method stub
    		return null;
    	}
    
    	@Override
    	public String getServletInfo() {
    		// TODO Auto-generated method stub
    		return null;
    	}
    
    	@Override
    	public void init(ServletConfig arg0) throws ServletException {
    		// TODO Auto-generated method stub
    		
    	}
    
    }


<!--more-->


> web.xml

    <?xml version="1.0" encoding="UTF-8"?>
    <web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" version="2.5">
      <description>MuiltThreadCacheTask</description>
      <display-name>MuiltThreadCacheTask</display-name>
      <servlet>
        <display-name>TaskServlet</display-name>
        <servlet-name>TaskServlet</servlet-name>
        <servlet-class>com.france.servlet.TaskServlet</servlet-class>
      </servlet>
      <servlet-mapping>
        <servlet-name>TaskServlet</servlet-name>
        <url-pattern>/TaskServlet</url-pattern>
      </servlet-mapping>
      
      <welcome-file-list>
        <welcome-file>/index.jsp</welcome-file>
      </welcome-file-list>
    </web-app>

> 做个测试：http://localhost:8888/MuiltThreadCacheTask/TaskServlet?num=3
> 控制台输出6

## 接下来开始搭建我们的并发缓存框架

先将我们刚刚的执行任务函数改成低耦合方式
### 定义Computable接口实现

    package com.france;
    
    public interface Computable<A, V> {
    	V compute(A arg)throws InterruptedException;
    }

**Memoizer**：用于缓存结果，提供Computable接口并在servlet实现 来执行任务
### 创建Memoizer1

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

先搭好框架，后面再说这边的弊端

修改原来的Servlet

    package com.france.servlet;
    
    import java.io.IOException;
    
    import javax.servlet.Servlet;
    import javax.servlet.ServletConfig;
    import javax.servlet.ServletException;
    import javax.servlet.ServletRequest;
    import javax.servlet.ServletResponse;
    
    import com.france.Computable;
    import com.france.memoizer.Memoizer1;
    
    public class TaskServlet implements Servlet{
    	private final long ms=500;
    	private final Computable<Integer,Integer> c =new Computable<Integer,Integer>() {
    
    		@Override
    		public Integer compute(Integer arg) throws InterruptedException {
    			// TODO Auto-generated method stub
    			return calculateSumWithSleep(arg);
    		}
    	};
    	//接口实现子类 可拔插
    	private final Computable<Integer,Integer> cahce=new Memoizer1<Integer,Integer>(c);
    	
    	@Override
    	public void service(ServletRequest req, ServletResponse resp)
    			throws ServletException, IOException {
    		// TODO Auto-generated method stub
    			try {
    				int num=Integer.valueOf(req.getParameter("num"));
    				if(num<=0)throw new Exception("数字<=0");
    				if(num>=20)throw new Exception("数字>=20");
    				int sum=this.cahce.compute(num);
    				System.out.println("计算得到的结果是:"+sum);
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    	}
    	private int calculateSumWithSleep(int num){
    		int sum=0;
    		for(int i=0;i<=num;i++){
    			sum+=i;
    			try {
    				Thread.sleep(ms);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    		return sum;
    	}
    
    	@Override
    	public void destroy() {
    		// TODO Auto-generated method stub
    		
    	}
    
    	@Override
    	public ServletConfig getServletConfig() {
    		// TODO Auto-generated method stub
    		return null;
    	}
    
    	@Override
    	public String getServletInfo() {
    		// TODO Auto-generated method stub
    		return null;
    	}
    
    	@Override
    	public void init(ServletConfig arg0) throws ServletException {
    		// TODO Auto-generated method stub
    		
    	}
    
    }

> 做测试(单机)，第一次执行很慢，第二次执行从cache中拿，很快。

那么出现的问题是什么呢？

>  首先先明白一点，`implements Servlet`后，每个线程执行`service`方法是独立的，而`cahce`定义为全局变量，所有线程访问的是**同一个引用**而不是**线程封闭**

然后，由于`HashMap`不是线程安全的，为了保证两个线程不会同时访问`HashMap`,`Memoizer1`采用方法是对整个`compute`方法进行同步
虽然能`确保线程安全`，但是带来的问题是`其他调用compute的线程被堵塞`，效果上来说比`不使用cache`更差
### **改进，使用线程安全的`ConcurrentHashMap`**


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

较之前相比，多线程可并发使用它。
但是存在一个`较严重的问题`，**两个线程同时调用compute去计算同一个值会造成重复计算**
当任务耗时更久的时候问题将更严重。

> 图示：

    A  ->f(1)不在缓存->[   计算f(1)    ]->将f(1)放入缓存
    B  ----------->f(1)不在缓存->[   计算f(1)    ]->将f(1)放入缓存

这样就是同样的任务计算了2次并放了2次缓存，对一个`高效`的缓存框架来说是糟糕的。
### **改进，采用FutureTask**
`FutureTask`表示一个计算的过程，可能已经计算完成，也可能正在进行。如果有结果可用，那么`FutureTask.get`将立即返回结果，否则会一直堵塞，直到计算结果出来再将其返回


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


较之前相比，**基本不会**出现重复计算同一个值的问题。
为什么说基本不会而不是完成不会？
原因在于`compute中if代码`块是**非原子**的`先检查再执行`操作

> 图示：

    A ->f(1)不在缓存中->将f(1)的future放入缓存->[计算f(1)]->通过get()返回结果
    B ---->f(1)不在缓存中->将f(1)的future放入缓存->[计算f(1)]->通过get()返回结果

虽然几率小，但还是有可能出现。
**复合操作（若没有则添加）**是在**底层的Map对象**执行的，这个对象`不能通过加锁`来确保原子性**(否则map堵塞将导致其他线程访问堵塞)**
### 故使用**ConcurrentMap**中的**原子方法`putIfAbsent`** 
修改处有注释

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

这样对于**`线程安全性`**和**`并发性`**来说是很完美的
但是注意
**缓存的是Future而不是值，会导致`缓存污染(Cache Pollution)问题`**：
如果计算被取消或者失败，那么**在计算这个结果时`(调用get方法时)`**将指明**计算过程被取消或者失败`(抛出异常)`**
解决：如果Memoizer发现`计算被取消`或者`出现RuntimeException`，那么**将Future从缓存中移除**，这样之后的计算才可能成功(故我们把之前的操作放在一个`while(true)循环`中去做)

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

当然，上面还是没有解决`缓存逾期`和`缓存清理`的问题
不清除是要从concurrentHashMap下手还是Future下手，求指教

一个想法是结合LruCache和concurrentHashMap实现自己的框架 待续

参考： Java并发编程第五章 
