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
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.aolangtech.nsignal.constants.CommonConstants;
import com.aolangtech.nsignal.receiver.NSignalReceiver;
import com.aolangtech.nsignal.receiver.impl.NSignalFileReceiver;
import com.aolangtech.nsignal.receiver.impl.NsignalRemoteReceiver;

public class Application {
	
	private static Logger logger = Logger.getLogger(Application.class);
	
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
		
		if(args.length > 1 || args.length < 1) {
			logger.error("Argument Error: too much or too little arguments!");
			return ;
		}
		boolean isRemote = false;
		for(String arg : args) {
			// -r means get data from remote, otherwise from local file.
			if(arg.equals("-r"))
			{
				isRemote = true;
			}
		}
		
		NSignalReceiver receiver;
		if(isRemote) {
			receiver = new NsignalRemoteReceiver();
		} else {
			receiver = new NSignalFileReceiver(args[0]);
		}

		// run receiver
		receiver.run();
	} 
	
}
