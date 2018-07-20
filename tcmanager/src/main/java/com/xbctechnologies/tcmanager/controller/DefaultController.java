package com.xbctechnologies.tcmanager.controller;

import com.xbctechnologies.tcmanager.config.XCubeClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.Map;

@Api(tags = {"A. Change host and etc"}, description = "To change request host")
@Controller
public class DefaultController {
    @Autowired
    private XCubeClient xcubeClient;

    @ApiIgnore
    @RequestMapping(value = "/", method = {RequestMethod.GET})
    public String index() {
        return "redirect:/swagger-ui.html";
    }

    @ApiOperation(value = "Change host to call xblockahcin server")
    @RequestMapping(value = "/changeHost", method = {RequestMethod.GET})
    @ResponseBody
    public String changeHost(@RequestParam String host, @RequestParam int port) {
        xcubeClient.setHost(host);
        xcubeClient.setPort(port);

        xcubeClient.connect();
        return "success";
    }

    @ApiOperation(value = "Get host")
    @RequestMapping(value = "/getHost", method = {RequestMethod.GET})
    @ResponseBody
    public Map<String, Object> getHost() {
        Map<String, Object> result = new HashMap<>();
        result.put("host", xcubeClient.getHost());
        result.put("port", xcubeClient.getPort());

        return result;
    }

    @ApiOperation(value = "Allow rpc size")
    @RequestMapping(value = "/getRPCSize", method = {RequestMethod.GET})
    @ResponseBody
    public Long getRPCSize() {
        return xcubeClient.xCube.getRPCSize(null).send().getResult();
    }
}
