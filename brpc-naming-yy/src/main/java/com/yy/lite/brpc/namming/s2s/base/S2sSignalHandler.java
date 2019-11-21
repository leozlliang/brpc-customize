/*
 * @(#) S2sSignalHandler.java 
 * Copyright(c) 欢聚时代科技有限公司
*/
package com.yy.lite.brpc.namming.s2s.base;

import com.yy.ent.clients.daemon.DaemonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * <pre>
 * This class is used for ... shutdown gracefully to destroy from s2s firstly by listening signal command kill -15
 *
 * &#64;version 1.0
 * &#64;author zhangfeng@yy.com
 * &#64;time 2019年5月20日 下午2:11:47
 *
 * &#64;versionChangeList
　* version:
 * date：
 * author:
　* desc:
 * auditor：
 * </pre>
 */
@SuppressWarnings("restriction")
public class S2sSignalHandler implements SignalHandler {
	private DaemonClient daemonClient;
	private volatile boolean exiting = false;
	private final static Logger logger = LoggerFactory.getLogger(S2sSignalHandler.class);

	public S2sSignalHandler(DaemonClient daemonClient) {
		this.daemonClient = daemonClient;
	}

	@Override
	public void handle(Signal signal) {
		if (exiting) {
			logger.info("shutdown gracefully, exiting receive signal again");
			return;
		}
		logger.info("shutdown gracefully, receive kill signal,delMine from s2s");
		exiting = true;
		try {
			daemonClient.delMine();
			// 删掉之后延迟5秒
			Thread.sleep(5000);
		} catch (Exception e) {
			logger.error("shutdown gracefully, delMine from s2s encounter error:", e);
		} finally {
			logger.info("shutdown gracefully, done delMine from s2s and begin to exit jvm");
			System.exit(0);
		}
	}

	public void registerSignal() {
		// 注册监听 kill -15
		Signal.handle(new Signal("TERM"), this);
	}

}
