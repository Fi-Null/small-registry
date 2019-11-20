package com.small.registry.client;

import com.small.registry.client.model.RegistryDataParamVO;
import com.small.registry.client.util.JsonUtil;
import com.small.registry.client.worker.DiscoveryThread;
import com.small.registry.client.worker.RegistryThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName RegistryClient
 * @Description TODO
 * @Author xiangke
 * @Date 2019/11/20 23:26
 * @Version 1.0
 **/
public class RegistryClient {
    private static Logger logger = LoggerFactory.getLogger(RegistryClient.class);


    private volatile Set<RegistryDataParamVO> registryData = new HashSet<>();
    private volatile ConcurrentMap<String, TreeSet<String>> discoveryData = new ConcurrentHashMap<>();

    private Thread registryThread;
    private Thread discoveryThread;
    private volatile boolean registryThreadStop = false;


    private RegistryBaseClient registryBaseClient;

    public RegistryClient(String adminAddress, String accessToken, String biz, String env) {
        registryBaseClient = new RegistryBaseClient(adminAddress, accessToken, biz, env);
        logger.info(">>>>>>>>>>> small-registry, RegistryClient init .... [adminAddress={}, accessToken={}, biz={}, env={}]", adminAddress, accessToken, biz, env);

        // registry thread
        registryThread = new RegistryThread(this);
        registryThread.setName("small-registry, RegistryClient registryThread.");
        registryThread.setDaemon(true);
        registryThread.start();

        // discovery thread
        discoveryThread = new DiscoveryThread(this);
        discoveryThread.setName("small-registry, RegistryClient discoveryThread.");
        discoveryThread.setDaemon(true);
        discoveryThread.start();

        logger.info(">>>>>>>>>>> small-registry, RegistryClient init success.");
    }


    public void stop() {
        registryThreadStop = true;
        if (registryThread != null) {
            registryThread.interrupt();
        }
        if (discoveryThread != null) {
            discoveryThread.interrupt();
        }
    }

    /**
     * registry
     *
     * @param registryDataList
     * @return
     */
    public boolean registry(List<RegistryDataParamVO> registryDataList) {

        // valid
        if (registryDataList == null || registryDataList.size() == 0) {
            throw new RuntimeException("small-registry registryDataList empty");
        }
        for (RegistryDataParamVO registryParam : registryDataList) {
            if (registryParam.getKey() == null || registryParam.getKey().trim().length() < 4 || registryParam.getKey().trim().length() > 255) {
                throw new RuntimeException("small-registry registryDataList#key Invalid[4~255]");
            }
            if (registryParam.getValue() == null || registryParam.getValue().trim().length() < 4 || registryParam.getValue().trim().length() > 255) {
                throw new RuntimeException("small-registry registryDataList#value Invalid[4~255]");
            }
        }

        // cache
        registryData.addAll(registryDataList);

        // remote
        registryBaseClient.registry(registryDataList);

        return true;
    }


    /**
     * remove
     *
     * @param registryDataList
     * @return
     */
    public boolean remove(List<RegistryDataParamVO> registryDataList) {
        // valid
        if (registryDataList == null || registryDataList.size() == 0) {
            throw new RuntimeException("small-registry registryDataList empty");
        }
        for (RegistryDataParamVO registryParam : registryDataList) {
            if (registryParam.getKey() == null || registryParam.getKey().trim().length() < 4 || registryParam.getKey().trim().length() > 255) {
                throw new RuntimeException("small-registry registryDataList#key Invalid[4~255]");
            }
            if (registryParam.getValue() == null || registryParam.getValue().trim().length() < 4 || registryParam.getValue().trim().length() > 255) {
                throw new RuntimeException("small-registry registryDataList#value Invalid[4~255]");
            }
        }

        // cache
        registryData.removeAll(registryDataList);

        // remote
        registryBaseClient.remove(registryDataList);

        return true;
    }


    /**
     * discovery
     *
     * @param keys
     * @return
     */
    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        if (keys == null || keys.size() == 0) {
            return null;
        }

        // find from local
        Map<String, TreeSet<String>> registryDataTmp = new HashMap<>();
        for (String key : keys) {
            TreeSet<String> valueSet = discoveryData.get(key);
            if (valueSet != null) {
                registryDataTmp.put(key, valueSet);
            }
        }

        // not find all, find from remote
        if (keys.size() != registryDataTmp.size()) {

            // refreshDiscoveryData, some, first use
            refreshDiscoveryData(keys);

            // find from local
            for (String key : keys) {
                TreeSet<String> valueSet = discoveryData.get(key);
                if (valueSet != null) {
                    registryDataTmp.put(key, valueSet);
                }
            }

        }

        return registryDataTmp;
    }

    /**
     * refreshDiscoveryData, some or all
     */
    public void refreshDiscoveryData(Set<String> keys) {
        if (keys == null || keys.size() == 0) {
            return;
        }

        // discovery mult
        Map<String, TreeSet<String>> updatedData = new HashMap<>();

        Map<String, TreeSet<String>> keyValueListData = registryBaseClient.discovery(keys);
        if (keyValueListData != null) {
            for (String keyItem : keyValueListData.keySet()) {

                // list > set
                TreeSet<String> valueSet = new TreeSet<>();
                valueSet.addAll(keyValueListData.get(keyItem));

                // valid if updated
                boolean updated = true;
                TreeSet<String> oldValSet = discoveryData.get(keyItem);
                if (oldValSet != null && JsonUtil.toJson(oldValSet).equals(JsonUtil.toJson(valueSet))) {
                    updated = false;
                }

                // set
                if (updated) {
                    discoveryData.put(keyItem, valueSet);
                    updatedData.put(keyItem, valueSet);
                }

            }
        }

        if (updatedData.size() > 0) {
            logger.info(">>>>>>>>>>> small-registry, refresh discovery data finish, discoveryData(updated) = {}", updatedData);
        }
        logger.debug(">>>>>>>>>>> small-registry, refresh discovery data finish, discoveryData = {}", discoveryData);
    }


    public TreeSet<String> discovery(String key) {
        if (key == null) {
            return null;
        }

        Map<String, TreeSet<String>> keyValueSetTmp = discovery(new HashSet<String>(Arrays.asList(key)));
        if (keyValueSetTmp != null) {
            return keyValueSetTmp.get(key);
        }
        return null;
    }


    public Set<RegistryDataParamVO> getRegistryData() {
        return registryData;
    }

    public void setRegistryData(Set<RegistryDataParamVO> registryData) {
        this.registryData = registryData;
    }

    public ConcurrentMap<String, TreeSet<String>> getDiscoveryData() {
        return discoveryData;
    }

    public void setDiscoveryData(ConcurrentMap<String, TreeSet<String>> discoveryData) {
        this.discoveryData = discoveryData;
    }

    public boolean isRegistryThreadStop() {
        return registryThreadStop;
    }

    public void setRegistryThreadStop(boolean registryThreadStop) {
        this.registryThreadStop = registryThreadStop;
    }

    public RegistryBaseClient getRegistryBaseClient() {
        return registryBaseClient;
    }

    public void setRegistryBaseClient(RegistryBaseClient registryBaseClient) {
        this.registryBaseClient = registryBaseClient;
    }
}
