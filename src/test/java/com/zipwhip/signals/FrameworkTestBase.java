package com.zipwhip.signals;

import com.zipwhip.framework.Application;
import com.zipwhip.framework.pubsub.TestCallback;
import org.apache.curator.test.TestingServer;
import org.cassandraunit.CassandraUnit;
import org.cassandraunit.dataset.xml.ClassPathXmlDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * Date: 5/14/13
 * Time: 6:17 PM
 *
 * @author Michael
 * @version 1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:development.xml",
        "classpath:test.xml"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class FrameworkTestBase {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    Application<SignalServerConfiguration> application;

    @Rule
    public CassandraUnit cassandraUnit = new CassandraUnit(new ClassPathXmlDataSet("dataSet.xml"), "cassandra.yaml", "localhost");

    static TestingServer staticTestingServer;

    @Autowired
    TestingServer testingServer;

    protected TestCallback subscribe(String uri) {
        TestCallback callback = new TestCallback();

        application.getBroker().subscribe(uri, callback);

        return callback;
    }

    @Before
    public void before() throws Exception {
        staticTestingServer = testingServer;
    }

    @After
    public void tearDown() throws Exception {

    }

    @AfterClass
    public static void afterClass() throws IOException {
        EmbeddedCassandraServerHelper.stopEmbeddedCassandra();
        if (staticTestingServer != null) {
            staticTestingServer.stop();
        }
    }
}
