package com.small.registry.client;

import com.small.registry.client.model.RegistryDataParamVO;
import com.small.registry.client.model.RegistryParamVO;
import com.small.registry.client.util.HttpUtil;
import com.small.registry.client.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/20/19 8:16 PM
 */
public class RegistryBaseClient {
    private static Logger logger = LoggerFactory.getLogger(RegistryBaseClient.class);

    private String adminAddress;
    private String accessToken;
    private String biz;
    private String env;

    private List<String> adminAddressArr;

    public RegistryBaseClient(String adminAddress, String accessToken, String biz, String env) {
        this.adminAddress = adminAddress;
        this.accessToken = accessToken;
        this.biz = biz;
        this.env = env;

        // valid
        if (adminAddress == null || adminAddress.trim().length() == 0) {
            throw new RuntimeException("small-registry adminAddress empty");
        }
        if (biz == null || biz.trim().length() < 4 || biz.trim().length() > 255) {
            throw new RuntimeException("small-registry biz empty Invalid[4~255]");
        }
        if (env == null || env.trim().length() < 2 || env.trim().length() > 255) {
            throw new RuntimeException("small-registry biz env Invalid[2~255]");
        }

        // parse
        adminAddressArr = new ArrayList<>();
        if (adminAddress.contains(",")) {
            adminAddressArr.addAll(Arrays.asList(adminAddress.split(",")));
        } else {
            adminAddressArr.add(adminAddress);
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

        // pathUrl
        String pathUrl = "/api/registry";

        // param
        RegistryParamVO registryParamVO = new RegistryParamVO();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setBiz(this.biz);
        registryParamVO.setEnv(this.env);
        registryParamVO.setRegistryDataList(registryDataList);

        String paramsJson = JsonUtil.toJson(registryParamVO);

        // result
        Map<String, Object> respObj = requestRegistryServer(pathUrl, paramsJson, 5);
        return respObj != null ? true : false;
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

        // pathUrl
        String pathUrl = "/api/remove";

        // param
        RegistryParamVO registryParamVO = new RegistryParamVO();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setBiz(this.biz);
        registryParamVO.setEnv(this.env);
        registryParamVO.setRegistryDataList(registryDataList);

        String paramsJson = JsonUtil.toJson(registryParamVO);

        // result
        Map<String, Object> respObj = requestRegistryServer(pathUrl, paramsJson, 5);
        return respObj != null ? true : false;
    }

    /**
     * discovery
     *
     * @param keys
     * @return
     */
    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        // valid
        if (keys == null || keys.size() == 0) {
            throw new RuntimeException("small-registry keys empty");
        }

        // pathUrl
        String pathUrl = "/api/discovery";

        // param
        RegistryParamVO registryParamVO = new RegistryParamVO();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setBiz(this.biz);
        registryParamVO.setEnv(this.env);
        registryParamVO.setKeys(new ArrayList(keys));

        String paramsJson = JsonUtil.toJson(registryParamVO);

        // result
        Map<String, Object> respObj = requestRegistryServer(pathUrl, paramsJson, 5);

        // parse
        if (respObj != null && respObj.containsKey("data")) {
            Map<String, TreeSet<String>> data = (Map<String, TreeSet<String>>) respObj.get("data");
            return data;
        }

        return null;
    }

    /**
     * discovery
     *
     * @param keys
     * @return
     */
    public boolean monitor(Set<String> keys) {
        // valid
        if (keys == null || keys.size() == 0) {
            throw new RuntimeException("small-registry keys empty");
        }

        // pathUrl
        String pathUrl = "/api/monitor";

        // param
        RegistryParamVO registryParamVO = new RegistryParamVO();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setBiz(this.biz);
        registryParamVO.setEnv(this.env);
        registryParamVO.setKeys(new ArrayList(keys));

        String paramsJson = JsonUtil.toJson(registryParamVO);

        // result
        Map<String, Object> respObj = requestRegistryServer(pathUrl, paramsJson, 60);
        return respObj != null ? true : false;
    }


    private Map<String, Object> requestRegistryServer(String pathUrl, String requestBody, int timeout) {

        /** requestRegistryServer from any regisrty server **/
        for (String adminAddressUrl : adminAddressArr) {
            String finalUrl = adminAddressUrl + pathUrl;

            // request
            String responseData = HttpUtil.postBody(finalUrl, requestBody, timeout);
            if (responseData == null) {
                return null;
            }

            // parse resopnse
            Map<String, Object> resopnseMap = null;
            try {
                resopnseMap = JsonUtil.parseMap(responseData);
            } catch (Exception e) {

            }

            // valid resopnse
            if (resopnseMap == null
                    || !resopnseMap.containsKey("code")
                    || !"200".equals(String.valueOf(resopnseMap.get("code")))
                    ) {
                logger.warn("RegistryBaseClient response fail, responseData={}", responseData);
                return null;
            }

            return resopnseMap;
        }


        return null;
    }

}
