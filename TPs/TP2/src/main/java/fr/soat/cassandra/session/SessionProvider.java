package fr.soat.cassandra.session;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.extras.codecs.jdk8.LocalDateCodec;

public class SessionProvider {

    private Cluster cluster;

    public SessionProvider() {
        this.cluster = createCluster();
    }

    private Cluster createCluster() {
        Cluster.Builder clusterBuilder = Cluster.builder()
                .addContactPoints("localhost")
                .withPort(9142);
        Cluster cluster = clusterBuilder.build();

        // register type codecs
        cluster.getConfiguration().getCodecRegistry()
                .register(LocalDateCodec.instance);

        return cluster;
//        throw new RuntimeException("implement me !");
    }

    public Session newSession(String keyspace) {
        return cluster.connect(keyspace);
    }

    public Session newSession() {
        return cluster.connect();
    }

}
