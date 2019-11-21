package com.small.registry.admin.model;

import java.util.Date;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/21/19 2:45 PM
 */
public class RegistryData {
    private Long id;
    private String biz;         // 业务标识
    private String env;         // 环境标识
    private String key;         // 注册Key
    private String value;       // 注册Value
    private Date updateTime;    // 更新时间

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
