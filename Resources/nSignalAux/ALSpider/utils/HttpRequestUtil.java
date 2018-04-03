/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.HttpException;

public class HttpRequestUtil {
	
	private final static int REQUEST_TIMEOUT = 60000; 
	
	private static String commonMethod(String url, Map<String, String> propertyMap, String data, String method) throws IOException {
		String result = "";
		
        BufferedReader in = null;
        PrintWriter out = null;
		try {
			URL realUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
			conn.setConnectTimeout(REQUEST_TIMEOUT);
			
			conn.setRequestMethod(method);
			if(propertyMap != null) {
				for(String key : propertyMap.keySet()) {
					conn.setRequestProperty(key, propertyMap.get(key));
				}
			}
			
			// common property
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");  
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");
			conn.setInstanceFollowRedirects(false);
			
			if(data != null) {
				conn.setDoOutput(true);
				conn.setDoInput(true);
				
				out = new PrintWriter(conn.getOutputStream());
				
				out.print(data);
				out.flush();
				
			} else {
				conn.connect();
			}
  
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            char[] buffer = new char[CommonConstants.BUFFER_SIZE_OF_READER];
            int countOnce = 0;
            while ((countOnce = in.read(buffer)) != -1) {
                result += String.copyValueOf(buffer, 0, countOnce);
            }
            return result;
            
		} catch (IOException e) {
			throw e;
		}
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if(out != null) {
                	out.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        
	}
	
	public static String getMethod(String url, Map<String, String> propertyMap, int retryTime) throws HttpException {
		boolean requestSuccess = false;
		String response = null;
		for(int i = 0; i < retryTime && !requestSuccess; i++) {
			try {
				response = HttpRequestUtil.commonMethod(url, propertyMap, null, "GET");
				requestSuccess = true;
			} catch (IOException e1) {
			}
		}
		if(!requestSuccess){
			throw new HttpException(url, "GET", retryTime);
		}
		return response;
	}
	
	public static String postMethod(String url, Map<String, String> propertyMap, String data, int retryTime) throws HttpException {
		boolean requestSuccess = false;
		String response = null;
		for(int i = 0; i < retryTime && !requestSuccess; i++) {
			try {
				response = HttpRequestUtil.commonMethod(url, propertyMap, data, "POST");
				requestSuccess = true;
			} catch (IOException e1) {
			}
		}
		if(response == null || response == "" || !requestSuccess){
			throw new HttpException(url, "POST", retryTime);
		}
		
		return response;
	}
}