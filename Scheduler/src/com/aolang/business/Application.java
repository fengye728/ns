package com.aolang.business;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {
	
	private final static String PROPERTIES_URL = "tasks.properties";

	public static List<ScheduledTask> generateTaskFromProperties() {
		
		List<ScheduledTask> tasks = new ArrayList<ScheduledTask>();
		File file = new File(PROPERTIES_URL);  
	    BufferedReader reader = null;  
	    Map<String, String> map = new HashMap<String, String>();
	    int tasknum = 0;

	       try {         
	            reader = new BufferedReader(new FileReader(file));  
	            String tempString = null;   
	            
	            while ((tempString = reader.readLine()) != null) {
	            	
	            	System.out.println(tempString);
	        	 	Matcher matcher1 = Pattern.compile("([\\s\\S]+)=([\\s\\S]+)").matcher(tempString);
	        	 	while(matcher1.find()){
	        	 			        	 		
	        	 		map.put(matcher1.group(1).trim(),matcher1.group(2).trim());	        
	        	 		tasknum++;
	        	 	}
	            }  
	            reader.close();  
	        } catch (IOException e) {  
	            e.printStackTrace();
	        } finally {  
	        	System.out.println();
	            if (reader != null) {  
	                try {  
	                    reader.close();  
	                } catch (IOException e1) {  
	                }  
	            }  
	        }
	    
	     
	 
	   for(int j = 1;j<= tasknum/2;j++) {
		   
		   String task = "task" + j;
		   String temptime = map.get(task+".time");
		   String tempcommand = map.get(task+".command");
		   String startDate[] = temptime.split(" ");
		   ScheduledTask tempTask = new ScheduledTask(startDate[0]+" "+startDate[1], tempcommand , Integer.parseInt(startDate[2]));
		   tasks.add(tempTask);
		  		   
	   }
	      
		
		
		
		return tasks;
	}
	
		
	
	public static Calendar dealWithTaskStartTime(ScheduledTask task) {
		Calendar startDate = Calendar.getInstance();
		String startTime = task.getTargetTime();
		int hour = Integer.parseInt(startTime.substring(0,2));
		int minute = Integer.parseInt(startTime.substring(3,5));
        startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DATE),hour, minute, 0);
       

        return startDate;
	}
	
	public static void scheduleTask(List<ScheduledTask> tasks) {
		for(ScheduledTask task : tasks) {
			Timer timer = new Timer();
			Calendar startDt = dealWithTaskStartTime(task);
			timer.scheduleAtFixedRate(task, startDt.getTime(), task.getInterval());
		}
	}
	
	public static void main(String[] args) {
		List<ScheduledTask> tasks = generateTaskFromProperties();
		scheduleTask(tasks);
		
	}
	
}
