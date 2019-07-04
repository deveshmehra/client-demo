import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.settings.PodPreset;
import io.fabric8.kubernetes.api.model.settings.PodPresetBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class PodPresetExamples {
    private static final Logger logger = LoggerFactory.getLogger(PodPresetExamples.class);

    public static void main(String args[]) {
        String master = "https://192.168.99.101:8443";
        if (args.length == 1) {
            master = args[0];
        }
        
        Config config = new ConfigBuilder().withMasterUrl(master).build();
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            String namespace = "default";
            log("namespace", namespace);
            Pod pod = client.pods().inNamespace(namespace).load(PodPresetExamples.class.getResourceAsStream("/pod-preset-example.yml")).get();
            log("Pod created");
            client.pods().inNamespace(namespace).create(pod);

            PodPreset podPreset = new PodPresetBuilder()
                    .withNewMetadata().withName("allow-database").endMetadata()
                    .withNewSpec()
                    .withNewSelector().withMatchLabels(Collections.singletonMap("role", "frontend")).endSelector()
                    .withEnv(new EnvVarBuilder().withName("DB_PORT").withValue("6379").build())
                    .withVolumeMounts(new VolumeMountBuilder().withMountPath("/cache").withName("cache-volume").build())
                    .withVolumes(new VolumeBuilder().withName("cache-volume").withEmptyDir(new EmptyDirVolumeSourceBuilder().build()).build())
                    .endSpec()
                    .build();

            log("Creating Pod Preset : " + podPreset.getMetadata().getName());
            client.settings().podPresets().inNamespace(namespace).create(podPreset);

            pod = client.pods().inNamespace(namespace).withName(pod.getMetadata().getName()).get();
            log("Updated pod: ");
            log(SerializationUtils.dumpAsYaml(pod));
        } catch (Exception e) {
            log("Exception occurred: ", e.getMessage());
        }
    }

    private static void log(String action, Object obj) {
        logger.info("{}: {}", action, obj);
    }

    private static void log(String action) {
        logger.info(action);
    }
}

