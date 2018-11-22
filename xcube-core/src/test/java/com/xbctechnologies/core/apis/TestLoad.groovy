package com.xbctechnologies.core.apis

import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.plugin.http.HTTPRequest
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import java.util.Date
import java.util.List
import java.util.ArrayList

import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair

@RunWith(GrinderRunner)
class TestLoad {

    public static GTest test
    public static HTTPRequest request
    public static NVPair[] headers = [new NVPair("Content-Type", "application/json")]
    public static NVPair[] params = []
    public static Cookie[] cookies = []
    public static HOSTS = [
            "http://52.78.40.119:7979",
            "http://13.125.47.48:7979",
            "http://13.124.186.168:7979",
            "http://13.125.233.138:7979",
    ]

    public static HOLDERS = [
            "0xd09913fec8f4797b5344eddea930d2558e5d9015",
            "0x1167d0b1f1194a473691287dd3d886518a70b911",
            "0x5d74f2b7024c2258e1213cffdd983b068bbfade1",
            "0xe66cf2bc13cd7e607d4d619befdd6a51dbcd3adc",
            "0xfefffa046afb0aa030a6633a3976fcefe6791fbf",
            "0x60bcb2b65d1f086fc34aeb8f00a1f3794eed7771",
            "0x96a76d177a4b361d2ebec4ca3dfdf8fd330a80c5",
            "0xa642f33ec1a951eceded3cf9e51edea1a806105b",
            "0xaa0efb5946698728720e508a7029e539ecfa399a",
            "0xf652d4681058865cebfc25d2ed7934fa03005c6b",
    ]

    @BeforeProcess
    public static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 6000
        test = new GTest(1, "XNode")
        request = new HTTPRequest()
        grinder.logger.info("before process.");
    }

    @BeforeThread
    public void beforeThread() {
        test.record(this, "test")
        grinder.statistics.delayReports = true;
        grinder.logger.info("before thread.");
    }

    @Before
    public void before() {
        request.setHeaders(headers)
        cookies.each { CookieModule.addCookie(it, HTTPPluginControl.getThreadHTTPClientContext()) }
        grinder.logger.info("before thread. init headers and cookies");
    }

    @Test
    public void test() {
        def holderSize = HOLDERS.size()
        def hostSize = HOSTS.size()
        def r = new Random()
        def isSync = "true"

        def data = "{\"jsonrpc\":null,\"method\":\"tx_sendTransaction\",\"id\":1,\"params\":[{\"isSync\":${isSync},\"targetChainId\":\"1T\",\"sender\":\"${HOLDERS.get(r.nextInt(holderSize))}\",\"receiver\":\"${HOLDERS.get(r.nextInt(holderSize))}\",\"fee\":\"1000000000000000000\",\"amount\":\"10000000000000000000\",\"time\":\"0\",\"v\":0,\"r\":null,\"s\":null,\"payloadType\":1,\"payloadBody\":\"eyJpbnB1dCI6bnVsbH0=\",\"sync\":true}]}"

        HTTPResponse result = request.POST(HOSTS.get(r.nextInt(hostSize)), data.getBytes(), headers)
        if (result.statusCode == 301 || result.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", result.statusCode);
        } else {
            assertThat(result.statusCode, is(200));
        }
    }
}
