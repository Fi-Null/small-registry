package com.small.registry.admin.util;

import com.small.registry.admin.model.Registry;
import com.small.registry.admin.model.RegistryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/21/19 4:41 PM
 */
public class FileUtil {

    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    // get
    public static Registry getFileRegistryData(String registryDataFilePath, RegistryData RegistryData) {

        // fileName
        String fileName = parseRegistryDataFileName(registryDataFilePath, RegistryData.getBiz(), RegistryData.getEnv(), RegistryData.getKey());

        // read
        Properties prop = PropUtil.loadProp(fileName);
        if (prop != null) {
            Registry fileRegistry = new Registry();
            fileRegistry.setData(prop.getProperty("data"));
            fileRegistry.setStatus(Integer.valueOf(prop.getProperty("status")));
            fileRegistry.setDataList(JsonUtil.readValue(fileRegistry.getData(), List.class));
            return fileRegistry;
        }
        return null;
    }

    private static String parseRegistryDataFileName(String registryDataFilePath, String biz, String env, String key) {
        // fileName
        String fileName = registryDataFilePath
                .concat(File.separator).concat(biz)
                .concat(File.separator).concat(env)
                .concat(File.separator).concat(key)
                .concat(".properties");
        return fileName;
    }

    // set
    public static String setFileRegistryData(String registryDataFilePath, Registry Registry) {

        // fileName
        String fileName = parseRegistryDataFileName(registryDataFilePath, Registry.getBiz(), Registry.getEnv(), Registry.getKey());

        // valid repeat update
        Properties existProp = PropUtil.loadProp(fileName);
        if (existProp != null
                && existProp.getProperty("data").equals(Registry.getData())
                && existProp.getProperty("status").equals(String.valueOf(Registry.getStatus()))
        ) {
            return new File(fileName).getPath();
        }

        // write
        Properties prop = new Properties();
        prop.setProperty("data", Registry.getData());
        prop.setProperty("status", String.valueOf(Registry.getStatus()));

        PropUtil.writeProp(prop, fileName);

        logger.info(">>>>>>>>>>> -registry, setFileRegistryData: biz={}, env={}, key={}, data={}"
                , Registry.getBiz(), Registry.getEnv(), Registry.getKey(), Registry.getData());

        return fileName;
    }

    // clean
    public static void cleanFileRegistryData(String registryDataFilePath, List<String> registryDataFileList) {
        filterChildPath(new File(registryDataFilePath), registryDataFileList);
    }

    public static void filterChildPath(File parentPath, final List<String> registryDataFileList) {
        if (!parentPath.exists() || parentPath.list() == null || parentPath.list().length == 0) {
            return;
        }
        File[] childFileList = parentPath.listFiles();
        for (File childFile : childFileList) {
            if (childFile.isFile() && !registryDataFileList.contains(childFile.getPath())) {
                childFile.delete();

                logger.info(">>>>>>>>>>> -registry, cleanFileRegistryData, RegistryData Path={}", childFile.getPath());
            }
            if (childFile.isDirectory()) {
                if (parentPath.listFiles() != null && parentPath.listFiles().length > 0) {
                    filterChildPath(childFile, registryDataFileList);
                } else {
                    childFile.delete();
                }

            }
        }

    }

}
