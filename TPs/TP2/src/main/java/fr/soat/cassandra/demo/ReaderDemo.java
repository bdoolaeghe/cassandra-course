package fr.soat.cassandra.demo;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import fr.soat.cassandra.session.SessionProvider;
import lombok.extern.log4j.Log4j;

import java.util.concurrent.TimeUnit;

import static fr.soat.cassandra.demo.WriterDemo.doInLocalQuorum;

@Log4j
public class ReaderDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("starting Demo Cassandra reader...");
        SessionProvider sessionProvider = new SessionProvider(9042, ConsistencyLevel.LOCAL_QUORUM);
        Session sSession = sessionProvider.newSession();


        while(true) {
            doInLocalQuorum(() -> {
                ResultSet resultSet = sSession.execute("SELECT the_value from demo_keyspace.key_value where the_key = 'my_key'");
                int theValue = resultSet.one().getInt("the_value");
                System.out.println("Read value for key \"my_key\": " + theValue);
            });
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
