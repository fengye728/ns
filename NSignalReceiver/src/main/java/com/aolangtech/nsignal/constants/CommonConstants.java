package com.aolangtech.nsignal.constants;

import java.text.SimpleDateFormat;

public class CommonConstants {
	// --------------------- Date format ---------------------
	public final static String DAY_DATE_FORMAT = "yyMMdd";
	
	public final static SimpleDateFormat DAY_DATE_FORMATTER = new SimpleDateFormat(CommonConstants.DAY_DATE_FORMAT);
	
	// --------------------- Record Type ----------------------
	public final static int RECORD_TYPE_TRADE = 1;
	
	public final static int RECORD_TYPE_OI = 2;
	
	// --------------------- Trading Condition ----------------
	public final static int TRADE_CONDITION_SPREAD = 35;
	
	public final static int TRADE_CONDITION_STRADDLE = 36;
	
	public final static int TRADE_CONDITION_BUYWRITE = 37;
	
	public final static int TRADE_CONDITION_COMBO = 38;
	
	

	public final static String OPTION_TRADE_RECORD_SEPARATOR = ",";
	
	public final static int OPTION_TRADE_RECORD_FIELD_NUMBER = 14;
	
	public final static int OPTION_OI_RECORD_FIELD_NUMBER = 3;
	
	public final static String LOGGER_PROPERTY_PATH = "log4j.properties";
	
	public final static String APPLICATION_CONFIG_FILE_NAME = "nsignal.properties";
	
	public final static String MAPPERS_PATH = "com.aolangtech.nsignal.mappers";
	
	public final static char OPTION_TRADE_OPTION_TYPE_CALL = 'C';
	
	public final static char OPTION_TRADE_OPTION_TYPE_PUT	= 'P';
	
	public final static int OPTION_TRADE_RECORD_MAX_LENGTH = 1024;
	
	/**
	 * The enumeration of property name and default value of configuration.
	 * 
	 * @author AOLANG
	 *
	 */
	public enum PropertyName {
		//REMOTE_URL("remote.url", "localhost"), 
		SERVER_PORT("server.port", "9899"),
		DB_DRIVER_CLASSNAME("db.driverClassName", "org.postgresql.Driver"),DB_URL("db.url", "jdbc:postgresql://localhost:5432/aolang"), DB_USERNAME("db.username", "postgres"), DB_PASSWORD("db.password", "AL");
		
		private String name;
		
		private String value;
		
		private PropertyName(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		public static PropertyName parse(String name) {
			for(PropertyName item : PropertyName.values()) {
				if(item.toString().equals(name)) {
					return item;
				}
			}
			return null;
		}
		
		public String getValue() {
			return this.value;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
	
	public enum TickTestTradeCategory {
		UNKNOWN(0),DOWNTICK(1), ZERO_DOWNTICK(2), ZERO_UPTICK(3), UPTICK(4);
		
		private int index;
		
		private TickTestTradeCategory(int index) {
			this.index = index;
		}
		
		public int getIndex() {
			return this.index;
		}
	}
}
