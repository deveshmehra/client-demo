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

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamespaceQuotaExample {
  private static final Logger logger = LoggerFactory.getLogger(NamespaceQuotaExample.class);

  public static void main(String[] args) throws InterruptedException {
    String master = "https://localhost:8443";

    if (args.length == 1) {
      master = args[0];
    }

    Config config = new ConfigBuilder().withMasterUrl(master).build();
    try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
      try  {
        // Creating namespace
        Namespace ns = new NamespaceBuilder().withNewMetadata().withName("namespacetest").addToLabels("hello", "world").endMetadata().build();
        log("Created namespace", client.namespaces().create(ns));

        // Get namespace by name
        log("Get namespace by name", client.namespaces().withName("namespacetest").get());
        // Get namespace by label
        log("Get namespace by label", client.namespaces().withLabel("hello", "world").list());
        
        ResourceQuota quota = new ResourceQuotaBuilder().withNewMetadata().withName("quota-example").endMetadata().withNewSpec().addToHard("pods", new Quantity("5")).endSpec().build();
        log("Create resource quota", client.resourceQuotas().inNamespace("namespacetest").create(quota));

        try {
          log("Get jobs in namespace", client.batch().jobs().inNamespace("namespacetest").list());
        } catch (APIGroupNotAvailableException e) {
          log("Skipping jobs example - extensions API group not available");
        }
      } finally {
        // Delete namespace
        log("Deleted namespace", client.namespaces().withName("namespacetest").delete());
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
