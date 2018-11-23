package com.xbctechnologies.core.apis

import HTTPClient.Cookie
import HTTPClient.CookieModule
import HTTPClient.HTTPResponse
import HTTPClient.NVPair
import net.grinder.plugin.http.HTTPPluginControl
import net.grinder.plugin.http.HTTPRequest
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static net.grinder.script.Grinder.grinder
import static org.hamcrest.Matchers.is

// import static net.grinder.util.GrinderUtils.* // You can use this if you're using nGrinder after 3.2.3

import static org.junit.Assert.assertThat

@RunWith(GrinderRunner)
class TestLoad {

    public static GTest test
    public static HTTPRequest request
    public static NVPair[] headers = [new NVPair("Content-Type", "application/json")]
    public static NVPair[] params = []
    public static Cookie[] cookies = []
    public static FEES = [
            "1000000000000000000", "2000000000000000000", "3000000000000000000",
            "4000000000000000000", "5000000000000000000", "6000000000000000000",
            "7000000000000000000", "8000000000000000000", "9000000000000000000",
            "10000000000000000000", "11000000000000000000", "12000000000000000000",
            "13000000000000000000", "14000000000000000000", "15000000000000000000",
    ]
    public static HOSTS = [
            "http://52.78.40.119:7979",
            "http://13.125.47.48:7979",
            "http://13.124.186.168:7979",
            "http://13.125.233.138:7979",
    ]

    //ls | head -100 | egrep -o '\--(.*)'
    public static HOLDERS = [
            "0xa4f39f99f293d9588ed9ed4fcd917ada6d2ab8df",
            "0x92141ef54df1403ac872d1ef582b3b043596a8b9",
            "0xbffa85d9a7ba93669be80bab277999251e323a80",
            "0xcc0760d6da605d250fdd486d2628b3dbdaeb8ee6",
            "0x7fd884a2ccc301a806a17be44617ae0fe125ef85",
            "0xb4eee2fb49756a43403e465dc02bac88ab68d544",
            "0x00462d62c5e84738014eb730554bcffdf57c140b",
            "0x584ae6d5c2ec4649d5ae571891b9c5a4988d99c8",
            "0x4d258c49e32bd40413dc16235ab0d4ac6a620173",
            "0xe658b9746c34168f5292c05e0d6ce8ced7f1e891",
            "0x4d30463545faa263ea20d0cec185f68f48476ae3",
            "0xd69206769ef0537019319e43be1c89e5beebbedd",
            "0xf8556f46b75ff79ccaa17a03d5b82a78d363d795",
            "0xe27f13829ef902662554d70b90eef496bfb15907",
            "0x44cc6f41486d2b11a8f230f14fb9588c23c4f81c",
            "0xcfd69ef6237799949e1a456f3e7dd0c8e34525f4",
            "0x85796267213fb295b1bc4952745ff7eddab06fb6",
            "0x20205e362318fc7ff100853aefe9fb0716172963",
            "0x7c50f0b6e3c45869a4cfd147f727ed287271d2e4",
            "0xc949f4da2e1fb8134edea0dc9db5e6742cd4b25f",
            "0xb05068fedf5b759696d5cbd31337024af6b06f77",
            "0x6c6a579841ce209ebb211f33fe33a6c54869b4a8",
            "0xc26ab8a86fcf5acbd88ad82acac6b5e3d984e6a7",
            "0x3dc5739f8faa0b7ce9ba34741baadb6e7383cb67",
            "0x4fe640cd1a13ec1da16abb32504c1a5e12bc1212",
            "0x2d293be50979af840437596d6d92e3d98bd9625a",
            "0x833693dfd0f7fd5d18f7070c314e44adb1425fb2",
            "0x7e4333c81b57e049af3164a894916a48d405a7b0",
            "0xf1ee09df6de50084db09b5db59f4c64558319fb3",
            "0x07f45375dcc4990201f9fa76c8851d48d7220697",
            "0xb4be309060691b3a4b99eee7dd58da8a6dcc72d1",
            "0xae7c1771491d0f9d42705f26ec130bbd08c80268",
            "0x386c930854a49b54e744986bd7737d6e69fe33d7",
            "0x97c05564844e240c2e7d90c526cf0568c119f6e2",
            "0x7f563a3f9027d82d8c95c74aefbd8b957c9acdee",
            "0xd06b6afbd72d611af93b67b35aefe81a99546ea6",
            "0x14e6aa7965a01006127cdbb3f6f6fbd5cc6b0205",
            "0xc49285f2d04cbd16675267dff359f2719a5da173",
            "0xf8ebbc6fad9d619b7b1b11ac26a6610af894c2e6",
            "0x566e1cd35e61e3ac891d9583bf527dadc830dd1f",
            "0xa0f9804a58ad4d2c8c7755046c3115f9481eff21",
            "0x53f0236e3c646ef6c4cfe2b0c8c5f0ff02423aef",
            "0xe895b93bfc067ee7b3572df62e3921db06e4af7b",
            "0x82615d2b8c6b706024282716ff414ec2547bb869",
            "0xc55267368987a97278c9e9d960314d349efa7fd5",
            "0x8b5d04c6fabf4537b9866ed2171feb5b3d384e70",
            "0xc07bf930e4ed4bbb4edb970297356354676d93e2",
            "0x8de617b5b97c0242441274ceddbaeb5c68a1c71a",
            "0xdf8116cd8f7350243b1bbfe9bdd5181b6c5e1dc5",
            "0x296d90e8326092abbb6b7394d1619ace015b8cea",
            "0x66b2a1830f06a9f1774152b60018b3e7afa220eb",
            "0x758920bcdb07eaa8df05e52a68129a43acf92c73",
            "0x3d0a8a4499430fb9eeeb1616895fd13fee658cec",
            "0xbce9896a5ba5a0c6eb0579f712b204f9e44ca63b",
            "0x38ad3a0f45fba3e54bd4ffd73a336ab464b8fab9",
            "0x6435db0c3463fac518519639033567a526814349",
            "0xe3756e3b80cb4599a58ecc1784c048998704e7b0",
            "0xd487b320e16a7504b44baf55ff9ffb11db93340f",
            "0x30474c73f1a6c170adc9ac007135349edbde10a2",
            "0xa4ec3fd12fec60843801f14a821b429f6127c03c",
            "0x7628a3a4822c294c792ba0d8f58b31eaecefec99",
            "0xb5bcfdd87d29d1e735167cac7694d522019fcc4f",
            "0xa7723a2fa19506c9364badf8d57caf9bab2f2624",
            "0xcaba5fba6d3a6e3c80bdc41ef0230241b0060f2e",
            "0x41c743011f54822a7fd8f694662a86101994db46",
            "0x75e3e81c194599d0feab560bd5c71407c9393212",
            "0x65b233e82192d0c321c829265a9386a93515ec8c",
            "0xa93be2bbe9af3425f0a5bd6eb7128f2a80c6e441",
            "0xae8bbb437706c177deebe2a92425166f4f0483f7",
            "0x4ae56c0925059cd9695fad8b356f60023b473050",
            "0x79b92b278a7d9b5cc0d16abddda3004c47cd7ad9",
            "0x59b485956ef65657131faa06668baf0f77574702",
            "0x0185ae9ba78ff10871d0908d586e9d4d3d62d87c",
            "0xe16f9e6e5ffe73f22ecb54d2050bc243fb6ecf37",
            "0x04483d630bce7d5bcc82f98be9ff4d223d0cb0bf",
            "0xa9be0a3d7b0d2c86087172f48dbf32280e00ce96",
            "0x57c46f9a6c11a9ad5eea014c921a2cc45d247187",
            "0x9711ff4421467dde492da31894982464fac25ab4",
            "0xd4c1e6fe9997af52cef688c86df25e9bf37a648c",
            "0xdc5797f6590e602c11cd0a1a309a7e0241c35f4b",
            "0x9cb910e6861bed272b79647f01f4e1d235a9d755",
            "0x5a09c7bd90d6c8a7e9225cd1659751887791c537",
            "0x7a0be3ac8a5d058013c5fdff951f332eb8cddffc",
            "0xeaadd270779a4e1d4fc77c874cd9d6d85d4536d3",
            "0xdbca97a2b5ebc02e02933d4f203412f7fc2d2de1",
            "0x1bfe121ec5744e131c5ba9c3391d4d28f49b7f6d",
            "0xee8b7e94ff6f5d7442bcee718d3cc139a14e8268",
            "0xad9f2fe4ca30e913827c709581b57f951dc2799d",
            "0x93b87359c4632ffaf6aeb6fbc1d01388cfae941d",
            "0x5639ed2dccc8d0ba2ec6a28f906b21313957a028",
            "0xe26a7fbfaf7de61ef4c7110d01566789c8e00c01",
            "0x433200e7de859e05d7da5d9e0fc582c8bfa21db3",
            "0x565fc30f64492b7f577bf583d233702fbfdaae80",
            "0x325064ab0c7248f4d33f354072e55c931db5dbfd",
            "0x4e7ae88f9b174e1004d2e2739e273f2c051ccedd",
            "0xdf708b1f42e3826853f1f5aa676a3e5725e0858e",
            "0x91b2dde380a0b68eb748685057ef5f59b4aa54db",
            "0x7dbc3fde61d2f4637e518ec1c291917e12245e42",
            "0xfb7359ce594f4631ac699c25b4c695b0892ec077",
            "0x6c0311d652c2e0b3a4f8c441c2344367194e486c",
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
        def feeSize = FEES.size()
        def r = new Random()
        def isSync = "true"

        def data = "{\"jsonrpc\":null,\"method\":\"tx_sendTransaction\",\"id\":1,\"params\":[{\"isSync\":${isSync},\"targetChainId\":\"1T\",\"sender\":\"${HOLDERS.get(r.nextInt(holderSize))}\",\"receiver\":\"${HOLDERS.get(r.nextInt(holderSize))}\",\"fee\":\"${FEES.get(r.nextInt(feeSize))}\",\"amount\":\"10000000000000000000\",\"time\":\"0\",\"v\":0,\"r\":null,\"s\":null,\"payloadType\":1,\"payloadBody\":\"eyJpbnB1dCI6bnVsbH0=\",\"sync\":true}]}"

        HTTPResponse result = request.POST(HOSTS.get(r.nextInt(hostSize)), data.getBytes(), headers)
        if (result.statusCode == 301 || result.statusCode == 302) {
            grinder.logger.warn("Warning. The response may not be correct. The response code was {}.", result.statusCode);
        } else {
            assertThat(result.statusCode, is(200));
        }
    }
}
