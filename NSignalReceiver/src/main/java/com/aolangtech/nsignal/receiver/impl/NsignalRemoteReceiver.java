package com.aolangtech.nsignal.receiver.impl;

import org.apache.log4j.Logger;

import com.aolangtech.nsignal.Application;
import com.aolangtech.nsignal.constants.CommonConstants;
import com.aolangtech.nsignal.receiver.NSignalReceiver;
import com.aolangtech.nsignal.utils.OptionTradeHandlerUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class NsignalRemoteReceiver implements NSignalReceiver {

	private ServerBootstrap bootstrap;
	
	@Override
	public void run() {
		
		bootstrap = new ServerBootstrap();
		
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		bootstrap.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new LineBasedFrameDecoder(CommonConstants.OPTION_TRADE_RECORD_MAX_LENGTH));
					ch.pipeline().addLast(new StringDecoder());
					ch.pipeline().addLast(new NsignalHandler());
				}
		});
		
		try {
			ChannelFuture future = bootstrap.bind(Integer.valueOf(Application.config.getServerPort())).sync();
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
		
	}
	
	/**
	 * Handler for Netty inbound handler and also for handle all option trade records for each client sending data.
	 * 
	 * @author AOLANG
	 *
	 */
	class NsignalHandler extends ChannelInboundHandlerAdapter {

		private final Logger logger = org.apache.log4j.Logger.getLogger(NsignalHandler.class);
		
		private OptionTradeHandlerUtil handler;
		
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	        String record = (String) msg;
	        handler.handleOneLineRecord(record);     
	    }
	    
	    @Override
	    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	    	handler = new OptionTradeHandlerUtil();
	        logger.info("Channel is active!" );
	    }

	    @Override
	    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	    	handler.processForMap();
	    	// persist all records
	    	handler.persist();
	    	logger.info("Channel is inactive!");
	    }
		
	}

}
