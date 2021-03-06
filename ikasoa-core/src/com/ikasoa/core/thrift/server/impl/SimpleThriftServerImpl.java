package com.ikasoa.core.thrift.server.impl;

import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerTransport;
import com.ikasoa.core.thrift.server.ThriftServerConfiguration;

/**
 * 单线程Thrift服务器实现
 * <p>
 * 此实现一般用于调试,正式生产环境请慎用.
 * 
 * @author <a href="mailto:larry7696@gmail.com">Larry</a>
 * @version 0.1
 */
public class SimpleThriftServerImpl extends AbstractThriftServerImpl {

	public SimpleThriftServerImpl() {
		// Do nothing
	}

	public SimpleThriftServerImpl(String serverName, int serverPort, ThriftServerConfiguration configuration,
			TProcessor processor) {
		setServerName(serverName);
		setServerPort(serverPort);
		setThriftServerConfiguration(configuration);
		setProcessor(processor);
	}

	protected void initServer(TServerTransport serverTransport) {
		TServer.Args args = new TServer.Args(serverTransport);
		ThriftServerConfiguration configuration = getThriftServerConfiguration();
		args.transportFactory(configuration.getTransportFactory());
		args.protocolFactory(configuration.getProtocolFactory());
		args.processor(getProcessor());
		server = new TSimpleServer(configuration.getServerArgsAspect().TServerArgsAspect(args));
		if (configuration.getServerEventHandler() != null) {
			server.setServerEventHandler(configuration.getServerEventHandler());
		}
	}

}
