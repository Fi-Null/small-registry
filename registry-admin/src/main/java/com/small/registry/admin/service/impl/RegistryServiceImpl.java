package com.small.registry.admin.service.impl;

import com.small.registry.admin.Response;
import com.small.registry.admin.model.Registry;
import com.small.registry.admin.model.RegistryData;
import com.small.registry.admin.service.RegistryService;
import com.small.registry.admin.service.task.RegistryTask;
import com.small.registry.admin.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/21/19 2:48 PM
 */
@Service
public class RegistryServiceImpl implements RegistryService {
    private static Logger logger = LoggerFactory.getLogger(RegistryServiceImpl.class);

    @Value("${small.registry.data.filepath}")
    private String registryDataFilePath;
    @Value("${small.registry.accessToken}")
    private String accessToken;

    @Override
    public Map<String, Object> pageList(int start, int length, String biz, String env, String key) {
        return null;
    }

    @Override
    public Response<String> delete(int id) {
        return null;
    }

    @Override
    public Response<String> update(Registry Registry) {
        return null;
    }

    @Override
    public Response<String> add(Registry Registry) {
        return null;
    }

    @Override
    public Response<String> registry(String accessToken, String biz, String env, List<RegistryData> registryDataList) {
        // valid
        Response response = checkParam(accessToken, biz, env, registryDataList);
        if (!response.isSucess()) {
            return response;
        }

        // fill properties
        fillProperties(biz, env, registryDataList);

        //add queue
        RegistryTask.addRegistryQueue(registryDataList);

        return Response.SUCCESS;
    }

    @Override
    public Response<String> remove(String accessToken, String biz, String env, List<RegistryData> registryDataList) {
        Response response = checkParam(accessToken, biz, env, registryDataList);
        if (!response.isSucess()) {
            return response;
        }

        fillProperties(biz, env, registryDataList);

        RegistryTask.addRemoveQueue(registryDataList);

        return Response.SUCCESS;
    }

    @Override
    public Response<Map<String, List<String>>> discovery(String accessToken, String biz, String env, List<String> keys) {
        // valid
        if (this.accessToken != null && this.accessToken.trim().length() > 0 && !this.accessToken.equals(accessToken)) {
            return new Response<>(Response.FAIL_CODE, "AccessToken Invalid");
        }
        if (biz == null || biz.trim().length() < 2 || biz.trim().length() > 255) {
            return new Response<>(Response.FAIL_CODE, "Biz Invalid[2~255]");
        }
        if (env == null || env.trim().length() < 2 || env.trim().length() > 255) {
            return new Response<>(Response.FAIL_CODE, "Env Invalid[2~255]");
        }
        if (keys == null || keys.size() == 0) {
            return new Response<>(Response.FAIL_CODE, "keys Invalid.");
        }
        for (String key : keys) {
            if (key == null || key.trim().length() < 4 || key.trim().length() > 255) {
                return new Response<>(Response.FAIL_CODE, "Key Invalid[4~255]");
            }
        }

        Map<String, List<String>> result = new HashMap<>();
        for (String key : keys) {
            RegistryData registryData = new RegistryData();
            registryData.setBiz(biz);
            registryData.setEnv(env);
            registryData.setKey(key);

            List<String> dataList = new ArrayList();
            Registry fileRegistry = FileUtil.getFileRegistryData(registryDataFilePath, registryData);
            if (fileRegistry != null) {
                dataList = fileRegistry.getDataList();
            }

            result.put(key, dataList);
        }

        return new Response(result);
    }

    /**
     * DeferredResult使用
     * http://www.mamicode.com/info-detail-2682470.html
     * 当一个请求到达API接口，如果该API接口的return返回值是DeferredResult，
     * 在没有超时或者DeferredResult对象设置setResult时，接口不会返回，但是
     * Servlet容器线程会结束，DeferredResult另外会有线程来进行结果处理，并
     * setResult，如此以来这个请求不会占用服务连接池太久，如果超时或设置
     * setResult，接口会立即返回。
     *
     * @param accessToken
     * @param biz
     * @param env
     * @param keys
     * @return
     */
    @Override
    public DeferredResult<Response<String>> monitor(String accessToken, String biz, String env, List<String> keys) {
        // init
        DeferredResult deferredResult = new DeferredResult(30 * 1000L, new Response<>(Response.SUCCESS_CODE, "Monitor timeout, no key updated."));

        // valid
        if (this.accessToken != null && this.accessToken.trim().length() > 0 && !this.accessToken.equals(accessToken)) {
            deferredResult.setResult(new Response<>(Response.FAIL_CODE, "AccessToken Invalid"));
            return deferredResult;
        }
        if (biz == null || biz.trim().length() < 4 || biz.trim().length() > 255) {
            deferredResult.setResult(new Response<>(Response.FAIL_CODE, "Biz Invalid[4~255]"));
            return deferredResult;
        }
        if (env == null || env.trim().length() < 2 || env.trim().length() > 255) {
            deferredResult.setResult(new Response<>(Response.FAIL_CODE, "Env Invalid[2~255]"));
            return deferredResult;
        }
        if (keys == null || keys.size() == 0) {
            deferredResult.setResult(new Response<>(Response.FAIL_CODE, "keys Invalid."));
            return deferredResult;
        }
        for (String key : keys) {
            if (key == null || key.trim().length() < 4 || key.trim().length() > 255) {
                deferredResult.setResult(new Response<>(Response.FAIL_CODE, "Key Invalid[4~255]"));
                return deferredResult;
            }
        }

        // monitor by client
        for (String key : keys) {
            String fileName = registryDataFilePath
                    .concat(File.separator).concat(biz)
                    .concat(File.separator).concat(env)
                    .concat(File.separator).concat(key)
                    .concat(".properties");
            List<DeferredResult> deferredResultList = RegistryTask.getRegistryDeferredResultMap().get(fileName);
            if (deferredResultList == null) {
                deferredResultList = new ArrayList<>();
                RegistryTask.getRegistryDeferredResultMap().put(fileName, deferredResultList);
            }

            deferredResultList.add(deferredResult);
        }

        return deferredResult;
    }

    private Response checkParam(String accessToken, String biz, String env, List<RegistryData> registryDataList) {
        // valid
        if (this.accessToken != null && this.accessToken.trim().length() > 0 && !this.accessToken.equals(accessToken)) {
            return new Response<String>(Response.FAIL_CODE, "AccessToken Invalid");
        }
        if (biz == null || biz.trim().length() < 4 || biz.trim().length() > 255) {
            return new Response<String>(Response.FAIL_CODE, "Biz Invalid[4~255]");
        }
        if (env == null || env.trim().length() < 2 || env.trim().length() > 255) {
            return new Response<String>(Response.FAIL_CODE, "Env Invalid[2~255]");
        }
        if (registryDataList == null || registryDataList.size() == 0) {
            return new Response<String>(Response.FAIL_CODE, "Registry DataList Invalid");
        }
        for (RegistryData registryData : registryDataList) {
            if (registryData.getKey() == null || registryData.getKey().trim().length() < 4 || registryData.getKey().trim().length() > 255) {
                return new Response<String>(Response.FAIL_CODE, "Registry Key Invalid[4~255]");
            }
            if (registryData.getValue() == null || registryData.getValue().trim().length() < 4 || registryData.getValue().trim().length() > 255) {
                return new Response<String>(Response.FAIL_CODE, "Registry Value Invalid[4~255]");
            }
        }

        return Response.SUCCESS;
    }

    private void fillProperties(String biz, String env, List<RegistryData> registryDataList) {
        for (RegistryData registryData : registryDataList) {
            registryData.setBiz(biz);
            registryData.setEnv(env);
        }
    }
}
