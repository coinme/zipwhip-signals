package com.zipwhip.framework;

import com.zipwhip.util.CollectionUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Michael
 * Date: 5/5/11
 * Time: 5:31 PM
 */
public class SpringApplication<TConfiguration extends Configuration> extends Application<TConfiguration> implements InitializingBean, DisposableBean {

    private List<Object> plugins;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!CollectionUtil.isNullOrEmpty(plugins)) {
            for (Object object : plugins) {
                addPlugin((Plugin) object);
            }
        }

        init(null);

        getBroker().publish("/app/started");
    }

    public List<Object> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<Object> plugins) {
        this.plugins = plugins;
    }

}
