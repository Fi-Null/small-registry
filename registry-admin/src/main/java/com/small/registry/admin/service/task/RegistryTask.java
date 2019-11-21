package com.small.registry.admin.service.task;

import com.small.registry.admin.Response;
import com.small.registry.admin.dao.RegistryDao;
import com.small.registry.admin.dao.RegistryDataDao;
import com.small.registry.admin.dao.RegistryMessageDao;
import com.small.registry.admin.model.Registry;
import com.small.registry.admin.model.RegistryData;
import com.small.registry.admin.model.RegistryMessage;
import com.small.registry.admin.service.impl.RegistryServiceImpl;
import com.small.registry.admin.util.FileUtil;
import com.small.registry.admin.util.JsonUtil;
import com.small.registry.admin.util.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/21/19 6:03 PM
 */
@Component
public class RegistryTask implements InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(RegistryServiceImpl.class);

    @Resource
    private RegistryDao registryDao;
    @Resource
    private RegistryDataDao registryDataDao;
    @Resource
    private RegistryMessageDao registryMessageDao;

    @Value("${small.registry.data.filepath}")
    private String registryDataFilePath;

    private ExecutorService executorService = ThreadPoolUtil.createThreadPool(10, "registry-worker");
    private static volatile boolean executorStoped = false;
    private static volatile List<Long> readedMessageIds = Collections.synchronizedList(new ArrayList<>());

    private static volatile LinkedBlockingQueue<RegistryData> registryQueue = new LinkedBlockingQueue<>();
    private static volatile LinkedBlockingQueue<RegistryData> removeQueue = new LinkedBlockingQueue<>();
    private static Map<String, List<DeferredResult>> registryDeferredResultMap = new ConcurrentHashMap<>();
    private int registryBeatTime = 10;

    public static void addRegistryQueue(List<RegistryData> registryDataList) {
        registryQueue.addAll(registryDataList);
    }

    public static void addRemoveQueue(List<RegistryData> registryDataList) {
        removeQueue.addAll(registryDataList);
    }

    @Override
    public void destroy() throws Exception {
        executorStoped = true;
        executorService.shutdownNow();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /**
         * registry registry data         (client-num/10 s)
         */
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                while (!executorStoped) {
                    try {
                        RegistryData registryData = registryQueue.take();
                        if (registryData != null) {

                            // refresh or add
                            int ret = registryDataDao.refresh(registryData);
                            if (ret == 0) {
                                registryDataDao.add(registryData);
                            }

                            // valid file status
                            Registry fileRegistry = FileUtil.getFileRegistryData(registryDataFilePath, registryData);
                            if (fileRegistry == null) {
                                // go on
                            } else if (fileRegistry.getStatus() != 0) {
                                continue;     // "Status limited."
                            } else {
                                if (fileRegistry.getDataList().contains(registryData.getValue())) {
                                    continue;     // "Repeated limited."
                                }
                            }

                            // checkRegistryDataAndSendMessage
                            checkRegistryDataAndSendMessage(registryData);
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            });
        }

        /**
         * remove registry data         (client-num/start-interval s)
         */
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                while (!executorStoped) {
                    try {
                        RegistryData registryData = removeQueue.take();
                        if (registryData != null) {

                            // delete
                            registryDataDao.deleteDataValue(registryData.getBiz(), registryData.getEnv(), registryData.getKey(), registryData.getValue());

                            // valid file status
                            Registry fileRegistry = FileUtil.getFileRegistryData(registryDataFilePath, registryData);
                            if (fileRegistry == null) {
                                // go on
                            } else if (fileRegistry.getStatus() != 0) {
                                continue;   // "Status limited."
                            } else {
                                if (!fileRegistry.getDataList().contains(registryData.getValue())) {
                                    continue;   // "Repeated limited."
                                }
                            }

                            // checkRegistryDataAndSendMessage
                            checkRegistryDataAndSendMessage(registryData);
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            });
        }

        /**
         * broadcase new one registry-data-file     (1/1s)
         *
         * clean old message   (1/10s)
         */
        executorService.execute(() -> {
            while (!executorStoped) {
                try {
                    // new message, filter readed
                    List<RegistryMessage> messageList = registryMessageDao.findMessage(readedMessageIds);
                    if (messageList != null && messageList.size() > 0) {
                        for (RegistryMessage message : messageList) {
                            readedMessageIds.add(message.getId());

                            if (message.getType() == 0) {   // from registry、add、update、deelete，ne need sync from db, only write

                                Registry registry = JsonUtil.readValue(message.getData(), Registry.class);

                                // process data by status
                                if (registry.getStatus() == 1) {
                                    // locked, not updated
                                } else if (registry.getStatus() == 2) {
                                    // disabled, write empty
                                    registry.setData(JsonUtil.toJson(new ArrayList<String>()));
                                } else {
                                    // default, sync from db （aready sync before message, only write）
                                }

                                // sync file
                                setFileRegistryData(registry);
                            }
                        }
                    }

                    // clean old message;
                    if ((System.currentTimeMillis() / 1000) % registryBeatTime == 0) {
                        registryMessageDao.cleanMessage(registryBeatTime);
                        readedMessageIds.clear();
                    }
                } catch (Exception e) {
                    if (!executorStoped) {
                        logger.error(e.getMessage(), e);
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    if (!executorStoped) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });

        /**
         *  clean old registry-data     (1/10s)
         *
         *  sync total registry-data db + file      (1+N/10s)
         *
         *  clean old registry-data file
         */
        executorService.execute(() -> {
            while (!executorStoped) {

                // align to beattime
                try {
                    long sleepSecond = registryBeatTime - (System.currentTimeMillis() / 1000) % registryBeatTime;
                    if (sleepSecond > 0 && sleepSecond < registryBeatTime) {
                        TimeUnit.SECONDS.sleep(sleepSecond);
                    }
                } catch (Exception e) {
                    if (!executorStoped) {
                        logger.error(e.getMessage(), e);
                    }
                }

                try {
                    // clean old registry-data in db
                    registryDataDao.cleanData(registryBeatTime * 3);

                    // sync registry-data, db + file
                    int offset = 0;
                    int pagesize = 1000;
                    List<String> registryDataFileList = new ArrayList<>();

                    List<Registry> registryList = registryDao.pageList(offset, pagesize, null, null, null);
                    while (registryList != null && registryList.size() > 0) {

                        for (Registry registryItem : registryList) {

                            // process data by status
                            if (registryItem.getStatus() == 1) {
                                // locked, not updated
                            } else if (registryItem.getStatus() == 2) {
                                // disabled, write empty
                                String dataJson = JsonUtil.toJson(new ArrayList<String>());
                                registryItem.setData(dataJson);
                            } else {
                                // default, sync from db
                                List<RegistryData> RegistryDataList = registryDataDao.findData(registryItem.getBiz(), registryItem.getEnv(), registryItem.getKey());
                                List<String> valueList = new ArrayList<>();
                                if (RegistryDataList != null && RegistryDataList.size() > 0) {
                                    for (RegistryData dataItem : RegistryDataList) {
                                        valueList.add(dataItem.getValue());
                                    }
                                }
                                String dataJson = JsonUtil.toJson(valueList);

                                // check update, sync db
                                if (!registryItem.getData().equals(dataJson)) {
                                    registryItem.setData(dataJson);
                                    registryDao.update(registryItem);
                                }
                            }

                            // sync file
                            String registryDataFile = setFileRegistryData(registryItem);

                            // collect registryDataFile
                            registryDataFileList.add(registryDataFile);
                        }


                        offset += 1000;
                        registryList = registryDao.pageList(offset, pagesize, null, null, null);
                    }

                    // clean old registry-data file
                    FileUtil.cleanFileRegistryData(registryDataFilePath, registryDataFileList);

                } catch (Exception e) {
                    if (!executorStoped) {
                        logger.error(e.getMessage(), e);
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(registryBeatTime);
                } catch (Exception e) {
                    if (!executorStoped) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    // set
    public String setFileRegistryData(Registry registry) {
        String fileName = FileUtil.setFileRegistryData(registryDataFilePath, registry);

        // brocast monitor client
        List<DeferredResult> deferredResultList = registryDeferredResultMap.get(fileName);
        if (deferredResultList != null) {
            registryDeferredResultMap.remove(fileName);
            for (DeferredResult deferredResult : deferredResultList) {
                deferredResult.setResult(new Response<>(Response.SUCCESS_CODE, "Monitor key update."));
            }
        }

        return new File(fileName).getPath();
    }

    /**
     * update Registry And Message
     */
    private void checkRegistryDataAndSendMessage(RegistryData registryData) {
        // data json
        List<RegistryData> registryDataList = registryDataDao.findData(registryData.getBiz()
                , registryData.getEnv()
                , registryData.getKey());
        List<String> valueList = new ArrayList<>();
        if (registryDataList != null && registryDataList.size() > 0) {
            for (RegistryData dataItem : registryDataList) {
                valueList.add(dataItem.getValue());
            }
        }
        String dataJson = JsonUtil.toJson(valueList);

        // update registry and message
        Registry registry = registryDao.load(registryData.getBiz()
                , registryData.getEnv()
                , registryData.getKey());
        boolean needMessage = false;
        if (registry == null) {
            registry = new Registry();
            registry.setBiz(registryData.getBiz());
            registry.setEnv(registryData.getEnv());
            registry.setKey(registryData.getKey());
            registry.setData(dataJson);
            registryDao.add(registry);
            needMessage = true;
        } else {

            // check status, locked and disabled not use
            if (registry.getStatus() != 0) {
                return;
            }

            if (!registry.getData().equals(dataJson)) {
                registry.setData(dataJson);
                registryDao.update(registry);
                needMessage = true;
            }
        }

        if (needMessage) {
            // sendRegistryDataUpdateMessage (registry update)
            sendRegistryDataUpdateMessage(registry);
        }

    }

    /**
     * send RegistryData Update Message
     */
    private void sendRegistryDataUpdateMessage(Registry registry) {
        String registryUpdateJson = JsonUtil.toJson(registry);

        RegistryMessage registryMessage = new RegistryMessage();
        registryMessage.setType(0);
        registryMessage.setData(registryUpdateJson);
        registryMessageDao.add(registryMessage);
    }

    public static Map<String, List<DeferredResult>> getRegistryDeferredResultMap() {
        return registryDeferredResultMap;
    }
}
