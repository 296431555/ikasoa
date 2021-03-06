package com.ikasoa.zk;

import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ikasoa.core.loadbalance.ServerInfo;
import com.ikasoa.core.utils.StringUtil;

/**
 * Zookeeper基础操作类
 * 
 * @author <a href="mailto:larry7696@gmail.com">Larry</a>
 * @version 0.1
 */
public class ZkBase {

	private static final Logger LOG = LoggerFactory.getLogger(ZkBase.class);

	private ZkClient zkClient;

	private String zkNode;

	private List<String> nodeList;

	public final String ZK_ROOT_NODE = "/";

	public ZkBase(String zkServerString, String zkNode) {
		if (StringUtil.isEmpty(zkServerString)) {
			throw new RuntimeException("zkServerString is null !");
		} else {
			zkClient = new ZkClient(zkServerString);
		}
		if (StringUtil.isEmpty(zkNode)) {
			this.zkNode = ZK_ROOT_NODE;
		} else {
			this.zkNode = zkNode;
		}
		zkClient.subscribeDataChanges(zkNode, new IZkDataListener() {

			@Override
			public void handleDataChange(String nodePath, Object nodeObj) throws Exception {
				LOG.debug("handleDataChange (nodePath : " + nodePath + ", nodeObj : " + nodeObj + ")");
			}

			@Override
			public void handleDataDeleted(String nodePath) throws Exception {
				LOG.warn("handleDataDeleted (nodePath : " + nodePath + ")");
			}

		});

		zkClient.subscribeStateChanges(new IZkStateListener() {

			@Override
			public void handleNewSession() throws Exception {
				LOG.debug("handleNewSession");
				nodeList = getChildren();
			}

			@Override
			public void handleSessionEstablishmentError(Throwable t) throws Exception {
				LOG.error(t.getMessage());
			}

			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				LOG.debug("handleStateChanged (state : " + state + ")");
			}

		});

		zkClient.subscribeChildChanges(this.zkNode, new IZkChildListener() {

			@Override
			public void handleChildChange(String parentPath, List<String> currentChildList) throws Exception {
				LOG.debug("handleChildChange (parentPath : " + parentPath + ", currentChildList" + currentChildList
						+ ")");
				nodeList = currentChildList;
			}

		});

	}

	public List<ServerInfo> getServerInfoList() {
		List<ServerInfo> serverInfoList = new ArrayList<>();
		List<String> nList = zkClient.getChildren(zkNode);
		for (String n : nList) {
			ZkServerNode zksn = (ZkServerNode) zkClient
					.readData(new StringBuilder(zkNode).append("/").append(n).toString());
			serverInfoList.add(new ServerInfo(zksn.getServerHost(), zksn.getServerPort()));
		}
		LOG.debug("serverInfoList is : " + serverInfoList);
		return serverInfoList;
	}

	public boolean isExistNode(String serverName, String serverHost, int serverPort) {
		if (nodeList == null || nodeList.isEmpty()) {
			nodeList = getChildren();
		}
		for (String n : nodeList) {
			if (n.contains(new StringBuilder(serverName).append("-").append(serverHost).append("-").append(serverPort)
					.toString())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public List<String> getChildren() {
		return zkClient.getChildren(zkNode);
	}

	public ZkClient getZkClient() {
		return zkClient;
	}

	public void setZkClient(ZkClient zkClient) {
		this.zkClient = zkClient;
	}

	public String getZkNode() {
		return zkNode;
	}

	public void setZkNode(String zkNode) {
		this.zkNode = zkNode;
	}

}
