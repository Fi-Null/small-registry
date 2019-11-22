package com.small.registry.admin.service;

import com.small.registry.admin.Response;
import com.small.registry.admin.model.Registry;
import com.small.registry.admin.model.RegistryData;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/21/19 2:47 PM
 */
public interface RegistryService {
    // admin
    Map<String, Object> pageList(int start, int length, String biz, String env, String key);

    Response<String> delete(Long id);

    Response<String> update(Registry registry);

    Response<String> add(Registry registry);


    // ------------------------ remote registry ------------------------

    /**
     * refresh registry-value, check update and broacase
     */
    Response<String> registry(String accessToken, String biz, String env, List<RegistryData> registryDataList);

    /**
     * remove registry-value, check update and broacase
     */
    Response<String> remove(String accessToken, String biz, String env, List<RegistryData> registryDataList);

    /**
     * discovery registry-data, read file
     */
    Response<Map<String, List<String>>> discovery(String accessToken, String biz, String env, List<String> keys);

    /**
     * monitor update
     */
    DeferredResult<Response<String>> monitor(String accessToken, String biz, String env, List<String> keys);

}
