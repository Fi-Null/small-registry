package com.small.registry.admin.model;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description 存储全量数据
 * @createDate 11/21/19 2:42 PM
 */
public class Registry {
    private Long id;
    private String biz;         // 业务标识
    private String env;         // 环境标识
    private String key;         // 注册Key
    private String data;        // 注册Value有效数据
    private int status;         // 状态：0-正常、1-锁定、2-禁用

    // plugin
    private List<String> dataList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getDataList() {
        return dataList;
    }

    public void setDataList(List<String> dataList) {
        this.dataList = dataList;
    }
}
