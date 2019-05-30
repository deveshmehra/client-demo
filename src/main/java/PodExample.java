import io.fabric8.kubernetes.api.model.EmptyDirVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.settings.PodPresetBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class PodExample {
    private static final Logger logger = LoggerFactory.getLogger(PodExample.class);

    public static void main(String args[]) {
        String master = "https://192.168.99.100:8443";

        Config config = new ConfigBuilder().withMasterUrl(master).build();
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            String namespace = "default";
            log("namespace", namespace);
            Pod pod = client.pods().inNamespace(namespace).load(PodExample.class.getResourceAsStream("/pod-preset-example.yml")).get();
            log("Pod created");
            client.pods().inNamespace(namespace).create(pod);

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
