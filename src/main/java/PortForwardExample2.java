import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.*;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class PortForwardExample2 {
    private static final Logger logger = LoggerFactory.getLogger(PortForwardExample2.class);

    public static void main(String args[]) {
        String master = "https://192.168.99.101:8443";

        ConfigBuilder builder = new ConfigBuilder();
        if (args.length > 1) {
            builder.withOauthToken(args[1]);
        }
        Config config = builder.build();
        config.setWebsocketPingInterval(5 * 1000);
        try (final DefaultKubernetesClient client = new DefaultKubernetesClient(config)) {
            LocalPortForward localPortForward =
                    client.pods().inNamespace("default").withName("web-0").portForward(80, 10001);
            SocketChannel channel = SocketChannel.open();
            channel.connect(new InetSocketAddress("localhost", 10001));
            System.out.println(channel.getLocalAddress());

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int read;
            do {
                read = channel.read(buffer);
            } while (read >= 0);
            buffer.flip();
            String data = ByteString.of(buffer.array()).utf8();
            System.out.println(data);
        } catch (Exception e) {
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
