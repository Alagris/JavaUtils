package net.comn.src.util;

import java.io.PrintStream;

public class Timer {
	
	private static final long timeMeasuringFunctionDelay;
	private static long startingTime;
	
	static{
		
		long time0=System.nanoTime();
		/*long time1=*/System.nanoTime();
		/*long time2=*/System.nanoTime();
		long time3=System.nanoTime();
		timeMeasuringFunctionDelay = (time3-time0)/3;
	}
	
	public static long getTimeWithoutFunctionDelay(){
		return System.nanoTime()-timeMeasuringFunctionDelay;
	}
	
	public static void start(){
		startingTime = getTimeWithoutFunctionDelay();
	}
	
	public static long end(){
		return getTimeWithoutFunctionDelay()-startingTime;
	}
	
	public static void endAndPrintRecordedTime(PrintStream str){
		str.println(getTimeWithoutFunctionDelay()-startingTime);
	}
	
	public static void endAndPrintRecordedTime(){
		System.out.println(getTimeWithoutFunctionDelay()-startingTime);
	}
}
