package com.zipwhip.signals.discovery;

import com.zipwhip.framework.SignalServerDetails;
import com.zipwhip.signals.address.ServerAddress;
import com.zipwhip.util.CollectionUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Date: 5/19/13
 * Time: 12:03 AM
 *
 * @author Michael
 * @version 1
 */
public class ClusterDiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterDiscoveryService.class);
    private static final String PATH = "/servers";

    private final ServerAddress serverAddress;
    private final ServiceDiscovery<SignalServerDetails> discovery;
    private final ServiceInstance<SignalServerDetails> instance;
    private final int port;

    public ClusterDiscoveryService(CuratorFramework curator, ServerAddress serverAddress, int port) throws Exception {
        this.port = port;
        this.serverAddress = serverAddress;

        this.instance = ServiceInstance.<SignalServerDetails>builder()
                .name(serverAddress.getName())
                .payload(new SignalServerDetails())
                .port(port)
                .uriSpec(new UriSpec("{scheme}://zipwhip.com:{port}/signals-server"))
                .build();

        this.discovery = ServiceDiscoveryBuilder.builder(SignalServerDetails.class)
                .client(curator)
                .serializer(new JsonInstanceSerializer<SignalServerDetails>(SignalServerDetails.class))
                .basePath(PATH)
                .thisInstance(instance)
                .build();

        this.discovery.start();
    }

    public boolean isActive(ServerAddress serverAddress) throws Exception {
        Collection<String> names = discovery.queryForNames();

        if (CollectionUtil.isNullOrEmpty(names)) {
            return false;
        }

        return names.contains(serverAddress.getName());
    }

    public int getPort() {
        return port;
    }

    public ServiceInstance<SignalServerDetails> getInstance() {
        return instance;
    }

    public ServerAddress getServerAddress() {
        return serverAddress;
    }

}
