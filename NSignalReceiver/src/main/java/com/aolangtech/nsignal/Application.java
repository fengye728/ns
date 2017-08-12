package com.aolangtech.nsignal;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.log4j.PropertyConfigurator;

import com.aolangtech.nsignal.constants.CommonConstants;
import com.aolangtech.nsignal.receiver.NSignalReceiver;
import com.aolangtech.nsignal.receiver.OptionTradeHandler;
import com.aolangtech.nsignal.receiver.impl.NSignalFileReceiver;
import com.aolangtech.nsignal.receiver.impl.OptionTradeHandlerImpl;

public class Application {
	
	// Config properties
	public static NsignalConfig config = null;
	public static SqlSessionFactory sqlSessionFactory = null;
	

	private static void init() {
		// init logger
		PropertyConfigurator.configure(CommonConstants.LOGGER_PROPERTY_PATH);
		
		// init properties
		config = new NsignalConfig();
		config.initProperties();
		
		// initialize database
		DataSource ds = new PooledDataSource(config.getDbDriverClassName(), config.getDbUrl(), config.getDbUsername(), config.getDbPassword());
		TransactionFactory txnFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("devlopmente", txnFactory, ds);
		
		Configuration configuration = new Configuration(environment);
		
		configuration.setCacheEnabled(true);
		configuration.addMappers(CommonConstants.MAPPERS_PATH);
		
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
	}
	
	public static void main(String[] args) throws SQLException {	
		// Init
		init();
		
		NSignalReceiver receiver = new NSignalFileReceiver("D:\\test\\out24");
		
		OptionTradeHandler handler = new OptionTradeHandlerImpl(receiver);
		
		handler.run();
		
	} 
	
}
