package fr.soat.cassandra.session;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.extras.codecs.jdk8.LocalDateCodec;

public class SessionProvider {

    private Cluster cluster;

    public SessionProvider(int port, ConsistencyLevel cl) {
        this.cluster = createCluster(port, cl);
    }

    public SessionProvider() {
        this.cluster = createCluster();
    }

    private Cluster createCluster() {
        return createCluster(9142, ConsistencyLevel.ONE);
    }

    private Cluster createCluster(int port, ConsistencyLevel one) {
        Cluster.Builder clusterBuilder = Cluster.builder()
                .withQueryOptions(new QueryOptions()
                        .setConsistencyLevel(one))
                .addContactPoints("localhost")
                .withPort(port);
        Cluster cluster = clusterBuilder.build();

        // register type codecs
        cluster.getConfiguration().getCodecRegistry()
                .register(LocalDateCodec.instance);

        return cluster;
    }

    public Session newSession(String keyspace) {
        return cluster.connect(keyspace);
    }

    public Session newSession() {
        return cluster.connect();
    }

}
