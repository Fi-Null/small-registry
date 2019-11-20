package com.small.registry.client.worker;

import com.small.registry.client.RegistryBaseClient;
import com.small.registry.client.RegistryClient;
import com.small.registry.client.model.RegistryDataParamVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName RegistryThread
 * @Description TODO
 * @Author xiangke
 * @Date 2019/11/20 23:30
 * @Version 1.0
 **/
public class RegistryThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(RegistryThread.class);

    private RegistryClient registryClient;

    public RegistryThread(RegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    @Override
    public void run() {
        boolean registryThreadStop = registryClient.isRegistryThreadStop();
        RegistryBaseClient registryBaseClient = registryClient.getRegistryBaseClient();
        Set<RegistryDataParamVO> registryData = registryClient.getRegistryData();

        while (!registryClient.isRegistryThreadStop()) {
            try {
                if (registryClient.getRegistryData().size() > 0) {

                    boolean ret = registryBaseClient.registry(new ArrayList<>(registryData));
                    logger.debug(">>>>>>>>>>> small-registry, refresh registry data {}, registryData = {}", ret ? "success" : "fail", registryData);
                }
            } catch (Exception e) {
                if (!registryThreadStop) {
                    logger.error(">>>>>>>>>>> small-registry, registryThread error.", e);
                }
            }
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (Exception e) {
                if (!registryThreadStop) {
                    logger.error(">>>>>>>>>>> small-registry, registryThread error.", e);
                }
            }
        }
        logger.info(">>>>>>>>>>> small-registry, registryThread stoped.");
    }

}
