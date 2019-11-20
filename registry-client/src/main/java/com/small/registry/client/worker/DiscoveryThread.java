package com.small.registry.client.worker;

import com.small.registry.client.RegistryBaseClient;
import com.small.registry.client.RegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName DiscoveryThread
 * @Description TODO
 * @Author xiangke
 * @Date 2019/11/20 23:48
 * @Version 1.0
 **/
public class DiscoveryThread extends Thread {

    private static Logger logger = LoggerFactory.getLogger(DiscoveryThread.class);

    private RegistryClient registryClient;

    public DiscoveryThread(RegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    @Override
    public void run() {
        boolean registryThreadStop = registryClient.isRegistryThreadStop();
        RegistryBaseClient registryBaseClient = registryClient.getRegistryBaseClient();
        ConcurrentMap<String, TreeSet<String>> discoveryData = registryClient.getDiscoveryData();

        while (!registryThreadStop) {
            if (discoveryData.size() == 0) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    if (!registryThreadStop) {
                        logger.error(">>>>>>>>>>> small-registry, discoveryThread error.", e);
                    }
                }
            } else {
                try {
                    // monitor
                    boolean monitorRet = registryBaseClient.monitor(discoveryData.keySet());

                    // avoid fail-retry request too quick
                    if (!monitorRet) {
                        TimeUnit.SECONDS.sleep(10);
                    }

                    // refreshDiscoveryData, all
                    registryClient.refreshDiscoveryData(discoveryData.keySet());
                } catch (Exception e) {
                    if (!registryThreadStop) {
                        logger.error(">>>>>>>>>>> small-registry, discoveryThread error.", e);
                    }
                }
            }

        }
        logger.info(">>>>>>>>>>> small-registry, discoveryThread stoped.");
    }
}
