package com.small.registry.admin.controller;

import com.small.registry.admin.Response;
import com.small.registry.admin.model.Registry;
import com.small.registry.admin.service.RegistryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/22/19 4:10 PM
 */
@Controller
@RequestMapping("/registry")
public class RegistryController {

    @Resource
    private RegistryService registryService;


    @RequestMapping("")
    public String index(Model model) {
        return "registry/registry.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String biz,
                                        String env,
                                        String key) {
        return registryService.pageList(start, length, biz, env, key);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public Response<String> delete(Long id) {
        return registryService.delete(id);
    }

    @RequestMapping("/update")
    @ResponseBody
    public Response<String> update(Registry Registry) {
        return registryService.update(Registry);
    }

    @RequestMapping("/add")
    @ResponseBody
    public Response<String> add(Registry Registry) {
        return registryService.add(Registry);
    }


}
