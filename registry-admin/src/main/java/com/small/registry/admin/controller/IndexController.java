package com.small.registry.admin.controller;

import com.small.registry.admin.Response;
import com.small.registry.admin.controller.annotation.PermessionLimit;
import com.small.registry.admin.controller.interceptor.PermissionInterceptor;
import com.small.registry.admin.dao.RegistryDao;
import com.small.registry.admin.dao.RegistryDataDao;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 11/22/19 4:04 PM
 */
@Controller
public class IndexController {

    @Resource
    private RegistryDao registryDao;
    @Resource
    private RegistryDataDao registryDataDao;


    @RequestMapping("/")
    public String index(Model model, HttpServletRequest request) {

        int registryNum = registryDao.pageListCount(0, 1, null, null, null);
        int registryDataNum = registryDataDao.count();

        model.addAttribute("registryNum", registryNum);
        model.addAttribute("registryDataNum", registryDataNum);

        return "index";
    }

    @RequestMapping("/toLogin")
    @PermessionLimit(limit = false)
    public String toLogin(Model model, HttpServletRequest request) {
        if (PermissionInterceptor.ifLogin(request)) {
            return "redirect:/";
        }
        return "login";
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    @PermessionLimit(limit = false)
    public Response<String> loginDo(HttpServletRequest request, HttpServletResponse response, String userName, String password, String ifRemember) {
        // valid
        if (PermissionInterceptor.ifLogin(request)) {
            return Response.SUCCESS;
        }

        // param
        if (userName == null || userName.trim().length() == 0 || password == null || password.trim().length() == 0) {
            return new Response<String>(500, "请输入账号密码");
        }
        boolean ifRem = (ifRemember != null && "on".equals(ifRemember)) ? true : false;

        // do login
        boolean loginRet = PermissionInterceptor.login(response, userName, password, ifRem);
        if (!loginRet) {
            return new Response<String>(500, "账号密码错误");
        }
        return Response.SUCCESS;
    }

    @RequestMapping(value = "logout", method = RequestMethod.POST)
    @ResponseBody
    @PermessionLimit(limit = false)
    public Response<String> logout(HttpServletRequest request, HttpServletResponse response) {
        if (PermissionInterceptor.ifLogin(request)) {
            PermissionInterceptor.logout(request, response);
        }
        return Response.SUCCESS;
    }

    @RequestMapping("/help")
    public String help() {
        return "help";
    }


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

}
