import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreatePod {

    private static final Logger logger = LoggerFactory.getLogger(CreatePod.class);

    public static void main(String[] args) {
        String master = "https://192.168.99.103:8443";
        Config config = new ConfigBuilder().withMasterUrl(master).build();
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            String namespace = "default";
            Pod pod = client.pods().inNamespace(namespace)
                    .load(CreatePod.class.getResourceAsStream("create-newpod.yml")).get();
            client.pods().inNamespace(namespace).create(pod);

            Pod pod2 = new PodBuilder()
                    .withNewMetadata().withName("nginx-self")
                    .withNamespace(namespace)
                    .addToLabels("app", "nginx")
                    .addToLabels("tier", "dev").endMetadata()
                    .withNewSpec()
                    .addNewContainer().withName("nginx-self-container").withImage("nginx").endContainer()
                    .endSpec().build();
            client.pods().inNamespace(namespace).create(pod2);
        }
    }
    public static void log(String action, Object obj) {
        logger.info("{} : {}", action, obj);
    }
    public static void log(String action) {
        logger.info(action);
    }
}
