import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerBuilder;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.ResourceQuotaBuilder;
import io.fabric8.kubernetes.client.APIGroupNotAvailableException;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){
        String master = "https://localhost:8443/";
        if (args.length == 1) {
            master = args[0];
        }

        Config config = new ConfigBuilder().withMasterUrl(master).build();
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            try (Watch watch = client.replicationControllers().inNamespace("thisisatest").withResourceVersion("0").watch(new Watcher<ReplicationController>() {
                @Override
                public void eventReceived(Action action, ReplicationController resource) {
                    logger.info("{}: {}", action, resource);
                }

                @Override
                public void onClose(KubernetesClientException e) {
                    if (e != null) {
                        e.printStackTrace();
                        logger.error(e.getMessage(), e);
                    }
                }
            })) {
                // Create a namespace for all our stuff
                Namespace ns = new NamespaceBuilder().withNewMetadata().withName("thisisatest").addToLabels("this", "rocks").endMetadata().build();
                log("Created namespace", client.namespaces().create(ns));

                // Get the namespace by name
                log("Get namespace by name", client.namespaces().withName("thisisatest").get());
                // Get the namespace by label
                log("Get namespace by label", client.namespaces().withLabel("this", "rocks").list());

                ResourceQuota quota = new ResourceQuotaBuilder().withNewMetadata().withName("pod-quota").endMetadata().withNewSpec().addToHard("pods", new Quantity("10")).endSpec().build();
                log("Create resource quota", client.resourceQuotas().inNamespace("thisisatest").create(quota));

                try {
                    log("Get jobs in namespace", client.batch().jobs().inNamespace("thisisatest").list());
                } catch (APIGroupNotAvailableException e) {
                    log("Skipping jobs example - extensions API group not available");
                }

                // Create an RC
                ReplicationController rc = new ReplicationControllerBuilder()
                        .withNewMetadata().withName("nginx-controller").addToLabels("server", "nginx").endMetadata()
                        .withNewSpec().withReplicas(3)
                        .withNewTemplate()
                        .withNewMetadata().addToLabels("server", "nginx").endMetadata()
                        .withNewSpec()
                        .addNewContainer().withName("nginx").withImage("nginx")
                        .addNewPort().withContainerPort(80).endPort()
                        .endContainer()
                        .endSpec()
                        .endTemplate()
                        .endSpec().build();

                log("Created RC", client.replicationControllers().inNamespace("thisisatest").create(rc));

                log("Created RC with inline DSL",
                        client.replicationControllers().inNamespace("thisisatest").createNew()
                                .withNewMetadata().withName("nginx2-controller").addToLabels("server", "nginx").endMetadata()
                                .withNewSpec().withReplicas(0)
                                .withNewTemplate()
                                .withNewMetadata().addToLabels("server", "nginx2").endMetadata()
                                .withNewSpec()
                                .addNewContainer().withName("nginx").withImage("nginx")
                                .addNewPort().withContainerPort(80).endPort()
                                .endContainer()
                                .endSpec()
                                .endTemplate()
                                .endSpec().done());

                client.replicationControllers().inNamespace("thisisatest").withName("nginx-controller").scale(8);

                Thread.sleep(1000);

            } finally {
                client.namespaces().withName("thisisatest").delete();
                log("Deleted namespace");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            Throwable[] suppressed = e.getSuppressed();
            if (suppressed != null) {
                for (Throwable t : suppressed) {
                    logger.error(t.getMessage(), t);
                }
            }
        }
    }

    private static void log(String action, Object obj) {
        logger.info("{}: {}", action, obj);
    }

    private static void log(String action) {
        logger.info(action);
    }
}
