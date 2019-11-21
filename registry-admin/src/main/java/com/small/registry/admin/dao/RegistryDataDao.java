package com.small.registry.admin.dao;

import com.small.registry.admin.model.RegistryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegistryDataDao {

    public int refresh(@Param("xxlRegistryData") RegistryData registryData);

    public int add(@Param("xxlRegistryData") RegistryData registryData);


    public List<RegistryData> findData(@Param("biz") String biz,
                                       @Param("env") String env,
                                       @Param("key") String key);

    public int cleanData(@Param("timeout") int timeout);

    public int deleteData(@Param("biz") String biz,
                          @Param("env") String env,
                          @Param("key") String key);

    public int deleteDataValue(@Param("biz") String biz,
                               @Param("env") String env,
                               @Param("key") String key,
                               @Param("value") String value);

    public int count();

}
