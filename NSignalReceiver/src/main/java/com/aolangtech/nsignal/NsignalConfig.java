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
	
	private String remoteUrl;
	
	private String remotePort;
	
	
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
		this.remoteUrl = configProp.getProperty(CommonConstants.PropertyName.REMOTE_URL.toString(), CommonConstants.PropertyName.REMOTE_URL.getValue());
		this.remotePort = configProp.getProperty(CommonConstants.PropertyName.REMOTE_PORT.toString(), CommonConstants.PropertyName.REMOTE_PORT.getValue());
		
		this.dbDriverClassName = configProp.getProperty(CommonConstants.PropertyName.DB_DRIVER_CLASSNAME.toString(), CommonConstants.PropertyName.DB_DRIVER_CLASSNAME.getValue());
		this.dbUrl = configProp.getProperty(CommonConstants.PropertyName.DB_URL.toString(), CommonConstants.PropertyName.DB_URL.getValue());
		
		this.dbUsername = configProp.getProperty(CommonConstants.PropertyName.DB_USERNAME.toString(), CommonConstants.PropertyName.DB_USERNAME.getValue());
		this.dbPassword = configProp.getProperty(CommonConstants.PropertyName.DB_PASSWORD.toString(), CommonConstants.PropertyName.DB_PASSWORD.getValue());
		
		// log porperty info
		logger.info("Property " + CommonConstants.PropertyName.REMOTE_URL.toString() + "\t:\t" + this.remoteUrl);
		logger.info("Property " + CommonConstants.PropertyName.REMOTE_PORT.toString() + "\t:\t" + this.remotePort);
		logger.info("Property " + CommonConstants.PropertyName.DB_DRIVER_CLASSNAME.toString() + "\t:\t" + this.dbDriverClassName);
		logger.info("Property " + CommonConstants.PropertyName.DB_URL.toString() + "\t:\t" + this.dbUrl);
		
		// close input stream
		try {
			configInStream.close();
		} catch (IOException e) {
		}
	}


	public static Logger getLogger() {
		return logger;
	}


	public static void setLogger(Logger logger) {
		NsignalConfig.logger = logger;
	}


	public String getRemoteUrl() {
		return remoteUrl;
	}


	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}


	public String getRemotePort() {
		return remotePort;
	}


	public void setRemotePort(String remotePort) {
		this.remotePort = remotePort;
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
	
}
