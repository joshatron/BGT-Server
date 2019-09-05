package io.joshatron.bgt.server.logic;

import io.joshatron.bgt.server.logic.utils.HttpUtils;
import io.joshatron.bgt.server.logic.utils.RandomUtils;
import org.apache.http.client.HttpClient;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    public final static String ZERO_ID = "00000000-0000-0000-0000-000000000000";
    protected HttpClient client;

    @BeforeSuite(groups = {"parallel", "serial"})
    public void initializeSuite() throws Exception {
        client = HttpUtils.createHttpClient();
    }

    protected String getTest() {
        String test = RandomUtils.generateTest(10);
        System.out.println("Test Base ID: " + test);
        return test;
    }
}
