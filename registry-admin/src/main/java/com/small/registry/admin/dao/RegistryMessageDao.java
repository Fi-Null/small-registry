package com.small.registry.admin.dao;

import com.small.registry.admin.model.RegistryMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/21/19 2:56 PM
 */
@Mapper
public interface RegistryMessageDao {

    public int add(@Param("xxlRegistryMessage") RegistryMessage registryMessage);

    public List<RegistryMessage> findMessage(@Param("excludeIds") List<Long> excludeIds);

    public int cleanMessage(@Param("messageTimeout") int messageTimeout);

}
