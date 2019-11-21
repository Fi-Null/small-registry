package com.small.registry.admin.model;

import java.util.Date;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/21/19 2:46 PM
 */
public class RegistryMessage {
    private Long id;
    private int type;         // 消息类型：0-注册更新
    private String data;      // 消息内容
    private Date addTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

}
