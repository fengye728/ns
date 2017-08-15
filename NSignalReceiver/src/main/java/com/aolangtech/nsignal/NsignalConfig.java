package com.aolangtech.nsignal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.aolangtech.nsignal.constants.CommonConstants;

public class NsignalConfig {

	private static Logger logger = Logger.getLogger(NsignalConfig.class);
	
	//private String serverUrl;
	
	private String serverPort;
	
	private String dbDriverClassName;
	
	private String dbUrl;
	
	private String dbUsername;
	
	private String dbPassword;
	
	public void initProperties() {
		InputStream configInStream = null;
		Properties configProp = new Properties();
		try {
			configInStream = new FileInputStream(CommonConstants.APPLICATION_CONFIG_FILE_NAME);
			configProp.load(configInStream);
		} catch (FileNotFoundException e) {
			logger.error(CommonConstants.APPLICATION_CONFIG_FILE_NAME + " is not existed! Now will use default property values!");
		} catch(IOException e) {
			logger.error("Read file " + CommonConstants.APPLICATION_CONFIG_FILE_NAME + " failed! Now will use default property values!");
		}
		
		// get property value
		//this.remoteUrl = configProp.getProperty(CommonConstants.PropertyName.REMOTE_URL.toString(), CommonConstants.PropertyName.REMOTE_URL.getValue());
		this.serverPort = configProp.getProperty(CommonConstants.PropertyName.SERVER_PORT.toString(), CommonConstants.PropertyName.SERVER_PORT.getValue());
		
		this.dbDriverClassName = configProp.getProperty(CommonConstants.PropertyName.DB_DRIVER_CLASSNAME.toString(), CommonConstants.PropertyName.DB_DRIVER_CLASSNAME.getValue());
		this.dbUrl = configProp.getProperty(CommonConstants.PropertyName.DB_URL.toString(), CommonConstants.PropertyName.DB_URL.getValue());
		
		this.dbUsername = configProp.getProperty(CommonConstants.PropertyName.DB_USERNAME.toString(), CommonConstants.PropertyName.DB_USERNAME.getValue());
		this.dbPassword = configProp.getProperty(CommonConstants.PropertyName.DB_PASSWORD.toString(), CommonConstants.PropertyName.DB_PASSWORD.getValue());
		
		// log porperty info
		//logger.info("Property " + CommonConstants.PropertyName.REMOTE_URL.toString() + "\t:\t" + this.remoteUrl);
		logger.info("Property " + CommonConstants.PropertyName.SERVER_PORT.toString() + "\t:\t" + this.serverPort);
		logger.info("Property " + CommonConstants.PropertyName.DB_DRIVER_CLASSNAME.toString() + "\t:\t" + this.dbDriverClassName);
		logger.info("Property " + CommonConstants.PropertyName.DB_URL.toString() + "\t:\t" + this.dbUrl);
		
		// close input stream
		try {
			configInStream.close();
		} catch (IOException e) {
		}
	}

	public String getDbDriverClassName() {
		return dbDriverClassName;
	}


	public void setDbDriverClassName(String dbDriverClassName) {
		this.dbDriverClassName = dbDriverClassName;
	}


	public String getDbUrl() {
		return dbUrl;
	}


	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbUsername() {
		return dbUsername;
	}


	public void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}


	public String getDbPassword() {
		return dbPassword;
	}


	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	
}
