package com.small.registry.client.model;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/20/19 8:14 PM
 */
public class RegistryParamVO {

    private String accessToken;
    private String biz;
    private String env;

    private List<RegistryDataParamVO> registryDataList;
    private List<String> keys;

    public String getBiz() {
        return biz;
    }

    public void setBiz(String biz) {
        this.biz = biz;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<RegistryDataParamVO> getRegistryDataList() {
        return registryDataList;
    }

    public void setRegistryDataList(List<RegistryDataParamVO> registryDataList) {
        this.registryDataList = registryDataList;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
}
