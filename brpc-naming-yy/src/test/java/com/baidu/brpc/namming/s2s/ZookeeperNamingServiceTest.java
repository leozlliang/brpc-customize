/*
 * Copyright (c) 2019 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.brpc.namming.s2s;

import com.baidu.brpc.client.instance.ServiceInstance;
import com.baidu.brpc.naming.BrpcURL;
import com.baidu.brpc.naming.NotifyListener;
import com.baidu.brpc.naming.RegisterInfo;
import com.baidu.brpc.naming.SubscribeInfo;
import com.yy.lite.brpc.namming.s2s.S2sNamingService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ZookeeperNamingServiceTest {
//    private TestingServer zkServer;
    private BrpcURL namingUrl;
    private S2sNamingService namingService;

    private final static String TEST_S2S_URL="s2s://14.17.106.94:2001";
    private final static String DEFAULT_S2S_NAME="yylitesvinfo";
    private final static int DEFAULT_PORT=18005;

    public void setUp() throws Exception {
//        zkServer = new TestingServer(2087, true);
        namingUrl = new BrpcURL(TEST_S2S_URL);
        namingUrl.addParameter("serviceId",DEFAULT_S2S_NAME);
        namingUrl.addParameter("accessAccount","yylitesvinfo");
        namingUrl.addParameter("accessKey","7c08831cc39ca6c14cc203620546de3196740869a0d0f7009f7bcc934dcefe9c");
        namingUrl.addParameter("protocol","yyp");
        namingService = new S2sNamingService(namingUrl);
    }

    public void tearDown() throws Exception {
//        zkServer.stop();
    }

    protected RegisterInfo createRegisterInfo(String host, int port) {
        RegisterInfo registerInfo = new RegisterInfo();
        registerInfo.setHost(host);
        registerInfo.setPort(port);
        registerInfo.setInterfaceName(EchoService.class.getName());
        registerInfo.setServiceId(DEFAULT_S2S_NAME);
        return registerInfo;
    }

    protected SubscribeInfo createSubscribeInfo(boolean ignoreFail) {
        SubscribeInfo subscribeInfo = new SubscribeInfo();
        subscribeInfo.setInterfaceName(EchoService.class.getName());
        subscribeInfo.setServiceId(DEFAULT_S2S_NAME);
        subscribeInfo.setIgnoreFailOfNamingService(ignoreFail);
        return subscribeInfo;
    }

    @Test
    public void testLookup() throws Exception {
        setUp();
        SubscribeInfo subscribeInfo = createSubscribeInfo(true);
        List<ServiceInstance> instances = namingService.lookup(subscribeInfo);
        Assert.assertTrue(instances.size() == 0);

        RegisterInfo registerInfo = createRegisterInfo("127.0.0.1", DEFAULT_PORT);
        namingService.register(registerInfo);
        instances = namingService.lookup(subscribeInfo);
        Assert.assertTrue(instances.size() == 1);
        Assert.assertTrue(instances.get(0).getIp().equals("127.0.0.1"));
        Assert.assertTrue(instances.get(0).getPort() == DEFAULT_PORT);
        namingService.unregister(registerInfo);
        tearDown();
    }

    @Test
    public void testSubscribe() throws Exception {
        setUp();
        final List<ServiceInstance> adds = new ArrayList<ServiceInstance>();
        final List<ServiceInstance> deletes = new ArrayList<ServiceInstance>();
        SubscribeInfo subscribeInfo = createSubscribeInfo(false);
        namingService.subscribe(subscribeInfo, new NotifyListener() {
            @Override
            public void notify(Collection<ServiceInstance> addList, Collection<ServiceInstance> deleteList) {
                System.out.println("receive new subscribe info time:" + System.currentTimeMillis());
                System.out.println("add size:" + addList.size());
                for (ServiceInstance instance : addList) {
                    System.out.println(instance);
                }
                adds.addAll(addList);

                System.out.println("delete size:" + deleteList.size());
                for (ServiceInstance instance : deleteList) {
                    System.out.println(instance);
                }
                deletes.addAll(deleteList);
            }
        });
        RegisterInfo registerInfo = createRegisterInfo("127.0.0.1", DEFAULT_PORT);
        namingService.register(registerInfo);
        System.out.println("register time=" + System.currentTimeMillis());
        Thread.sleep(1000);
        Assert.assertTrue(adds.size() == 1);
        Assert.assertTrue(deletes.size() == 0);
        Assert.assertTrue(adds.get(0).getIp().equals("127.0.0.1"));
        Assert.assertTrue(adds.get(0).getPort() == DEFAULT_PORT);
        adds.clear();
        deletes.clear();

        namingService.unregister(registerInfo);
        System.out.println("unregister time=" + System.currentTimeMillis());
        Thread.sleep(1000);
        Assert.assertTrue(adds.size() == 0);
        Assert.assertTrue(deletes.size() == 1);
        Assert.assertTrue(deletes.get(0).getIp().equals("127.0.0.1"));
        Assert.assertTrue(deletes.get(0).getPort() == DEFAULT_PORT);

        namingService.unsubscribe(subscribeInfo);
        tearDown();
    }

    /**
     * This test must test under actual zookeeper server, Not the TestingServer of Curator
     */
    @Test
    @Ignore
    public void testSubscribeWhenZookeeperDownAndUp() throws Exception {
        namingUrl = new BrpcURL(TEST_S2S_URL);
        namingService = new S2sNamingService(namingUrl);

        final List<ServiceInstance> adds = new ArrayList<ServiceInstance>();
        final List<ServiceInstance> deletes = new ArrayList<ServiceInstance>();
        SubscribeInfo subscribeInfo = createSubscribeInfo(false);
        namingService.subscribe(subscribeInfo, new NotifyListener() {
            @Override
            public void notify(Collection<ServiceInstance> addList, Collection<ServiceInstance> deleteList) {
                System.out.println("receive new subscribe info time:" + System.currentTimeMillis());
                System.out.println("add size:" + addList.size());
                for (ServiceInstance instance : addList) {
                    System.out.println(instance);
                }
                adds.addAll(addList);

                System.out.println("delete size:" + deleteList.size());
                for (ServiceInstance instance : deleteList) {
                    System.out.println(instance);
                }
                deletes.addAll(deleteList);
            }
        });
        RegisterInfo registerInfo = createRegisterInfo("127.0.0.1", DEFAULT_PORT);
        namingService.register(registerInfo);
        System.out.println("register time=" + System.currentTimeMillis());
        Thread.sleep(1000);
        Assert.assertTrue(adds.size() == 1);
        Assert.assertTrue(deletes.size() == 0);
        Assert.assertTrue(adds.get(0).getIp().equals("127.0.0.1"));
        Assert.assertTrue(adds.get(0).getPort() == 8014);
        adds.clear();
        deletes.clear();

        // sleep for restarting zookeeper
        Thread.sleep(30 * 1000);

        List<ServiceInstance> instances = namingService.lookup(subscribeInfo);
        Assert.assertTrue(instances.size() == 1);
        Assert.assertTrue(instances.get(0).getIp().equals("127.0.0.1"));
        Assert.assertTrue(instances.get(0).getPort() == 8014);

        namingService.unregister(registerInfo);
        System.out.println("unregister time=" + System.currentTimeMillis());
        Thread.sleep(1000);
        Assert.assertTrue(adds.size() == 0);
        Assert.assertTrue(deletes.size() == 1);
        Assert.assertTrue(deletes.get(0).getIp().equals("127.0.0.1"));
        Assert.assertTrue(deletes.get(0).getPort() == 8014);

        namingService.unsubscribe(subscribeInfo);
    }

    /**
     * This test must test under actual zookeeper server, Not the TestingServer of Curator
     */
    @Test
    @Ignore
    public void testRegisterWhenZookeeperDownAndUp() throws Exception {
        setUp();
        namingService = new S2sNamingService(namingUrl);

        RegisterInfo registerInfo = createRegisterInfo("127.0.0.1", DEFAULT_PORT);
        namingService.register(registerInfo);
        SubscribeInfo subscribeInfo = createSubscribeInfo(false);
        List<ServiceInstance> instances = namingService.lookup(subscribeInfo);
        Assert.assertTrue(instances.size() == 1);
        Assert.assertTrue(instances.get(0).getIp().equals("127.0.0.1"));
        Assert.assertTrue(instances.get(0).getPort() == DEFAULT_PORT);

        // sleep for restarting zookeeper
        Thread.sleep(30 * 1000);
        instances = namingService.lookup(subscribeInfo);
        Assert.assertTrue(instances.size() == 1);
        System.out.println(instances.get(0));
        Assert.assertTrue(instances.get(0).getIp().equals("127.0.0.1"));
        Assert.assertTrue(instances.get(0).getPort() == DEFAULT_PORT);
        namingService.unregister(registerInfo);
    }
}
