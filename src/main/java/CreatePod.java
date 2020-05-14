import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreatePod {

    private static final Logger logger = LoggerFactory.getLogger(CreatePod.class);

    public static void main(String[] args) {

        ConfigBuilder builder = new ConfigBuilder();

        Config config = builder.build();
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            String namespace = "default";
            log("namespace", namespace);
            Pod pod = client.pods().inNamespace(namespace)
                    .load(CreatePod.class.getResourceAsStream("create-newpod.yml")).get();
            //System.out.println(pod.getMetadata());
            //System.out.println(pod.toString());
            log("Pod Created");
            client.pods().inNamespace(namespace).updateStatus(pod);
        }
    }
    public static void log(String action, Object obj) {
        logger.info("{} : {}", action, obj);
    }
    public static void log(String action) {
        logger.info(action);
    }
}
