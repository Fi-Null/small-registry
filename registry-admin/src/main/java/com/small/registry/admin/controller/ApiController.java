package com.small.registry.admin.controller;

import com.small.registry.admin.Response;
import com.small.registry.admin.controller.annotation.PermessionLimit;
import com.small.registry.admin.model.RegistryData;
import com.small.registry.admin.service.RegistryService;
import com.small.registry.admin.util.JsonUtil;
import com.small.registry.client.model.RegistryDataParamVO;
import com.small.registry.client.model.RegistryParamVO;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/22/19 3:22 PM
 */
@Controller
@RequestMapping("/api")
public class ApiController {

    @Resource
    private RegistryService registryService;

    /**
     * 服务注册 & 续约 API
     * <p>
     * 说明：新服务注册上线1s内广播通知接入方；需要接入方循环续约，否则服务将会过期（三倍于注册中心心跳时间）下线；
     * <p>
     * ------
     * 地址格式：{服务注册中心跟地址}/registry
     * <p>
     * 请求参数说明：
     * 1、accessToken：请求令牌；
     * 2、biz：业务标识
     * 2、env：环境标识
     * 3、registryDataList：服务注册信息
     * <p>
     * 请求数据格式如下，放置在 RequestBody 中，JSON格式：
     * <p>
     * {
     * "accessToken" : "xx",
     * "biz" : "xx",
     * "env" : "xx",
     * "registryDataList" : [{
     * "key" : "service01",
     * "value" : "address01"
     * }]
     * }
     *
     * @param data
     * @return
     */
    @RequestMapping("/registry")
    @ResponseBody
    @PermessionLimit(limit = false)
    public Response<String> registry(@RequestBody(required = false) String data) {

        // parse data
        RegistryParamVO registryParamVO = null;
        try {
            registryParamVO = JsonUtil.readValue(data, RegistryParamVO.class);
        } catch (Exception e) {

        }

        //check
        Assert.notNull(registryParamVO, "registryParamVO is null");
        Assert.hasText(registryParamVO.getAccessToken(), "accessToken is null");
        Assert.hasText(registryParamVO.getBiz(), "biz is null");
        Assert.hasText(registryParamVO.getEnv(), "env is null");
        Assert.notEmpty(registryParamVO.getRegistryDataList(), "registryDataList is empty");

        return registryService.registry(registryParamVO.getAccessToken(),
                registryParamVO.getBiz(),
                registryParamVO.getEnv(),
                convertRegistryList(registryParamVO));
    }

    /**
     * 服务摘除 API
     * <p>
     * 说明：新服务摘除下线1s内广播通知接入方；
     * <p>
     * ------
     * 地址格式：{服务注册中心跟地址}/remove
     * <p>
     * 请求参数说明：
     * 1、accessToken：请求令牌；
     * 2、biz：业务标识
     * 2、env：环境标识
     * 3、registryDataList：服务注册信息
     * <p>
     * 请求数据格式如下，放置在 RequestBody 中，JSON格式：
     * <p>
     * {
     * "accessToken" : "xx",
     * "biz" : "xx",
     * "env" : "xx",
     * "registryDataList" : [{
     * "key" : "service01",
     * "value" : "address01"
     * }]
     * }
     *
     * @param data
     * @return
     */
    @RequestMapping("/remove")
    @ResponseBody
    @PermessionLimit(limit = false)
    public Response<String> remove(@RequestBody(required = false) String data) {

        // parse data
        RegistryParamVO registryParamVO = null;
        try {
            registryParamVO = JsonUtil.readValue(data, RegistryParamVO.class);
        } catch (Exception e) {
        }

        //check
        Assert.notNull(registryParamVO, "registryParamVO is null");
        Assert.hasText(registryParamVO.getAccessToken(), "accessToken is null");
        Assert.hasText(registryParamVO.getBiz(), "biz is null");
        Assert.hasText(registryParamVO.getEnv(), "env is null");
        Assert.notEmpty(registryParamVO.getRegistryDataList(), "registryDataList is empty");

        return registryService.remove(registryParamVO.getAccessToken(),
                registryParamVO.getBiz(),
                registryParamVO.getEnv(),
                convertRegistryList(registryParamVO));
    }


    /**
     * 服务发现 API
     * <p>
     * 说明：查询在线服务地址列表；
     * <p>
     * ------
     * 地址格式：{服务注册中心跟地址}/discovery
     * <p>
     * 请求参数说明：
     * 1、accessToken：请求令牌；
     * 2、biz：业务标识
     * 2、env：环境标识
     * 3、keys：服务注册Key列表
     * <p>
     * 请求数据格式如下，放置在 RequestBody 中，JSON格式：
     * <p>
     * {
     * "accessToken" : "xx",
     * "biz" : "xx",
     * "env" : "xx",
     * "keys" : [
     * "service01",
     * "service02"
     * ]
     * }
     *
     * @param data
     * @return
     */
    @RequestMapping("/discovery")
    @ResponseBody
    @PermessionLimit(limit = false)
    public Response<Map<String, List<String>>> discovery(@RequestBody(required = false) String data) {

        // parse data
        RegistryParamVO registryParamVO = null;
        try {
            registryParamVO = JsonUtil.readValue(data, RegistryParamVO.class);
        } catch (Exception e) {
        }

        //check
        Assert.notNull(registryParamVO, "registryParamVO is null");
        Assert.hasText(registryParamVO.getAccessToken(), "accessToken is null");
        Assert.hasText(registryParamVO.getBiz(), "biz is null");
        Assert.hasText(registryParamVO.getEnv(), "env is null");
        Assert.notEmpty(registryParamVO.getKeys(), "keys is empty");


        return registryService.discovery(registryParamVO.getAccessToken(),
                registryParamVO.getBiz(),
                registryParamVO.getEnv(),
                registryParamVO.getKeys());
    }

    /**
     * 服务监控 API
     * <p>
     * 说明：long-polling 接口，主动阻塞一段时间（三倍于注册中心心跳时间）；直至阻塞超时或服务注册信息变动时响应；
     * <p>
     * ------
     * 地址格式：{服务注册中心跟地址}/monitor
     * <p>
     * 请求参数说明：
     * 1、accessToken：请求令牌；
     * 2、biz：业务标识
     * 2、env：环境标识
     * 3、keys：服务注册Key列表
     * <p>
     * 请求数据格式如下，放置在 RequestBody 中，JSON格式：
     * <p>
     * {
     * "accessToken" : "xx",
     * "biz" : "xx",
     * "env" : "xx",
     * "keys" : [
     * "service01",
     * "service02"
     * ]
     * }
     *
     * @param data
     * @return
     */
    @RequestMapping("/monitor")
    @ResponseBody
    @PermessionLimit(limit = false)
    public DeferredResult monitor(@RequestBody(required = false) String data) {

        // parse data
        RegistryParamVO registryParamVO = null;
        try {
            registryParamVO = JsonUtil.readValue(data, RegistryParamVO.class);
        } catch (Exception e) {
        }

        //check
        Assert.notNull(registryParamVO, "registryParamVO is null");
        Assert.hasText(registryParamVO.getAccessToken(), "accessToken is null");
        Assert.hasText(registryParamVO.getBiz(), "biz is null");
        Assert.hasText(registryParamVO.getEnv(), "env is null");
        Assert.notEmpty(registryParamVO.getKeys(), "keys is empty");


        return registryService.monitor(registryParamVO.getAccessToken(),
                registryParamVO.getBiz(),
                registryParamVO.getEnv(),
                registryParamVO.getKeys());
    }

    private List<RegistryData> convertRegistryList(RegistryParamVO registryParamVO) {
        List<RegistryData> registryDataList = null;
        if (registryParamVO.getRegistryDataList() != null) {
            registryDataList = new ArrayList<>();
            for (RegistryDataParamVO dataParamVO : registryParamVO.getRegistryDataList()) {
                RegistryData dateItem = new RegistryData();
                dateItem.setKey(dataParamVO.getKey());
                dateItem.setValue(dataParamVO.getValue());
                registryDataList.add(dateItem);
            }
        }

        return registryDataList;
    }
}
