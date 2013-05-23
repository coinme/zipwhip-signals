package com.zipwhip.signals;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SignalServerBootstrap {

    /**
     * Order is important here. Do not change!
     */
    public static final String[] APPLICATION_DEV = new String[]{
            "development.xml",
            "datasource.xml",
            "server.xml"
    };

    /**
     * Runs the app.
     * If no param is passed it will default to DEV.
     * Otherwise pass in xml config file names to load (loads files in order)
     * ex: datasource-staging.xml utils.xml data.xml signals.xml application-staging.xml application.xml subscriptions.xml pipeline.xml
     *
     * @param args [xml config files]
     */
    public static void main(String[] args) {

        AbstractApplicationContext ctx;

        // Default to development mode
        if (args == null || args.length == 0) {
            ctx = new ClassPathXmlApplicationContext(APPLICATION_DEV);
        } else {
            ctx = new FileSystemXmlApplicationContext(args);
//            ctx = new ClassPathXmlApplicationContext(args);
        }

        // So the beans can know that they are shutting down.
        ctx.registerShutdownHook();
    }

}
