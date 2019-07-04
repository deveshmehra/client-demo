/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentVolumeClaimExampleShort {
  private static final Logger logger = LoggerFactory.getLogger(PersistentVolumeClaimExample.class);

  public static void main(String[] args) {
    String master = "https://192.168.99.101:8443/";
    if (args.length == 1) {
      master = args[0];
    }

    log("Using master with url ", master);
    Config config = new ConfigBuilder().withMasterUrl(master).build();
    try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
      log("Creating PersistentVolume object");
      PersistentVolume persistentVolume = client.persistentVolumes().load(PersistentVolumeClaimExample.class.getResourceAsStream("/test-pv.yml")).get();
      client.persistentVolumes().create(persistentVolume);
      log("Successfully created PersistentVolume object");

      log("Creating PersistentVolumeClaim object");
      PersistentVolumeClaim persistentVolumeClaim = client.persistentVolumeClaims().load(PersistentVolumeClaimExample.class.getResourceAsStream("/test-pvc.yml")).get();
      client.persistentVolumeClaims().create(persistentVolumeClaim);
      log("Successfully created PersistentVolumeClaim object");

      log("Creating pod");
      Pod pod = client.pods().load(PersistentVolumeClaimExample.class.getResourceAsStream("/test-pv-pod.yml")).get();
      client.pods().create(pod);
      log("Successfully created pod");

    } catch (KubernetesClientException e) {
      log("Could not create resource", e.getMessage());
    } finally {

    }
  }

  private static void log(String action, Object obj) {
    logger.info("{}: {}", action, obj);
  }

  private static void log(String action) {
    logger.info(action);
  }
}

