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
        def hostSize = HOSTS.size()
        def r = new Random()

        def data = "{\"jsonrpc\":null,\"method\":\"data_getCurrentGovernance\",\"id\":1,\"params\":[\"1T\"]}"

        HTTPResponse result = request.POST(HOSTS.get(r.nextInt(hostSize)), data.getBytes(), headers)
        if (result.statusCode == 301 || result.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", result.statusCode);
        } else {
            assertThat(result.statusCode, is(200));
        }
    }
}
