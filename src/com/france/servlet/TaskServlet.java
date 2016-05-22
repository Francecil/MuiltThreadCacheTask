package com.france.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.france.Computable;
import com.france.memoizer.Memoizer1;
import com.france.memoizer.Memoizer4;
import com.france.memoizer.Memoizer5;

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
	private final Computable<Integer,Integer> cahce=new Memoizer4<Integer,Integer>(c);
	
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
