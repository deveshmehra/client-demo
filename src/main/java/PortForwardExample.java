import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class PortForwardExample {
    private static final Logger logger = LoggerFactory.getLogger(PortForwardExample.class);

    public static void main(String args[]) {
        String master = "https://localhost:8443";

        Config config = new ConfigBuilder().withMasterUrl(master).build();
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            String namespace = "default";
            log("namespace", namespace);
            Pod pod = client.pods().inNamespace(namespace).load(PortForwardExample.class.getResourceAsStream("/portforward-example.yml")).get();
            client.pods().inNamespace(namespace).create(pod);
            log("Pod created");

            int containerPort =  pod.getSpec().getContainers().get(0).getPorts().get(0).getContainerPort();

            LocalPortForward portForward = client.pods().withName("testpod").portForward(containerPort, 8080);
            log("Port forwarded");

            int localPort = portForward.getLocalPort();

            System.out.println(localPort);

            SocketChannel channel = SocketChannel.open();
            channel.connect(new InetSocketAddress("localhost", localPort));
            log(channel.getRemoteAddress().toString());
        } catch (Exception e) {
            log("Exception occurred: ", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void log(String action, Object obj) {
        logger.info("{}: {}", action, obj);
    }

    private static void log(String action) {
        logger.info(action);
    }
}
