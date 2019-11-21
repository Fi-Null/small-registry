package com.small.registry.admin;

import java.io.Serializable;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/21/19 2:50 PM
 */
public class Response<T> implements Serializable {

    public static final Integer SUCCESS_CODE = 200;

    public static final Integer FAIL_CODE = 500;

    public static final Response<String> SUCCESS = new Response<String>(null);
    public static final Response<String> FAIL = new Response<String>(FAIL_CODE, null);

    private Integer code;
    private String msg;
    private T data;

    public Response() {
    }

    public Response(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Response(T data) {
        this.code = SUCCESS_CODE;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Boolean isSucess() {
        return this.getCode().equals(SUCCESS_CODE);
    }

    @Override
    public String toString() {
        return "ReturnT [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }
}
