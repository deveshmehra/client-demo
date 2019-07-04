import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import me.snowdrop.servicecatalog.api.client.ServiceCatalogClient;
import me.snowdrop.servicecatalog.api.model.ClusterServiceBroker;

public class ServiceCatalogExample {
    public static void main(String[] args) {
        ServiceCatalogClient client = ClientFactory.newClient(args);
        ClusterServiceBroker broker = client.clusterServiceBrokers().createNew()
                .withNewMetadata()
                .withName("mybroker")
                .endMetadata()
                .withNewSpec()
                .withUrl("http://url.to.service.broker")
                .endSpec()
                .done();
    }
}