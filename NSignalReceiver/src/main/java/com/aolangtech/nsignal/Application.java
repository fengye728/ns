package com.aolangtech.nsignal;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.log4j.PropertyConfigurator;

import com.aolangtech.nsignal.constants.CommonConstants;
import com.aolangtech.nsignal.mappers.OptionTradeMapper;
import com.aolangtech.nsignal.models.OptionTradeModel;
import com.aolangtech.nsignal.receiver.NSignalReceiver;
import com.aolangtech.nsignal.receiver.impl.NSignalFileReceiver;

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
		
		SqlSession session = sqlSessionFactory.openSession();
		
		OptionTradeMapper otMapper = session.getMapper(OptionTradeMapper.class);
		
		//OptionTradeModel obj = otMapper.selectByPrimaryKey("option_trade_163", 1L);
		
		List<OptionTradeModel> recordList = new ArrayList<>();
		
		OptionTradeModel t1 = new OptionTradeModel();
		t1.setStockSymbol("Test1");
		
		recordList.add(t1);
		
		otMapper.insertList("option_trade_163", recordList);
		
	} 
	
}
