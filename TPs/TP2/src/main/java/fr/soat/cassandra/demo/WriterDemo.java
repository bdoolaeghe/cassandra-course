package fr.soat.cassandra.demo;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import fr.soat.cassandra.session.SessionProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WriterDemo {


    private static final String CREATE_KEYSPACE = "CREATE KEYSPACE IF NOT EXISTS demo_keyspace WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 3 };";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS demo_keyspace.key_value (\n" +
            "    the_key text,\n" +
            "    the_value int,\n" +
            "    PRIMARY KEY (the_key)\n" +
            ")";
    public static final String INSERT_CQL = "INSERT INTO demo_keyspace.key_value (the_key, the_value) VALUES (:key, :value)";

    public static void main(String[] args) throws InterruptedException {
        System.out.println("starting Demo Cassandra writer...");
        SessionProvider sessionProvider = new SessionProvider(9042, ConsistencyLevel.LOCAL_QUORUM);
        Session session = sessionProvider.newSession();
        doInLocalQuorum(() -> {
            session.execute(CREATE_KEYSPACE);
            session.execute(CREATE_TABLE);
        });
        PreparedStatement insertStatement = session.prepare(INSERT_CQL);

        AtomicInteger counter = new AtomicInteger(0);
        while(true) {
            doInLocalQuorum(() -> {
                int count = counter.getAndIncrement();
                BoundStatement boundStatement = insertStatement.bind("my_key", count);
                session.execute(boundStatement);
                System.out.println("writing for key \"my_key\" the value: " + count);
            });
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public static void doInLocalQuorum(Runnable toDo) {
        try {
            toDo.run();
        } catch (NoHostAvailableException e) {
            if (!e.getErrors().values().isEmpty()) {
                System.err.println(e.getErrors().values().iterator().next().getMessage());
            } else {
                System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

}
