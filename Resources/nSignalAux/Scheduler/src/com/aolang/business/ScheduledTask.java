package com.aolang.business;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TimerTask;

public class ScheduledTask extends TimerTask{

	public final static int INTERVAL_UNIT_A_DAY = 1000* 60 * 60 * 24;
	private String targetTime;
	private int intervalDays;
	private String command;
	
	public ScheduledTask(String targetTime, String command, int intervalDays) {
		this.targetTime = targetTime;
		this.command = command;
		this.intervalDays = intervalDays;
	}
	@Override
	public void run() {
		 System.out.println("Start:" ); 
		output("Execute Program:" + command);
		BufferedReader reader = null;
		try {
			// execute command
			Process processor = Runtime.getRuntime().exec(command);
			processor.waitFor();
			
			// get output of command
			InputStream in = processor.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in));
		
			String line = null;
			
			while((line = reader.readLine()) != null) {
				output(line);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private void output(String content) {
		System.out.println(content);
	}
	
	public String getTargetTime() {
		return targetTime;
	}

	public Integer getInterval() {
		return this.intervalDays * INTERVAL_UNIT_A_DAY;
	}
}
