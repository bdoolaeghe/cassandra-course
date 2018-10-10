package fr.soat.cassandra.course1.repository;

import com.datastax.driver.core.Session;
import fr.soat.cassandra.course1.model.Temperature;
import fr.soat.cassandra.session.SessionProvider;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TemperatureRepositoryTest {

    private static Session session;
    public static final String KEYSPACE = "my_keyspace";

    private final LocalDate firstJanuary = LocalDate.of(2017,1,1);
    private final LocalDate secondJanuary = LocalDate.of(2017,1,2);

    private static TemperatureRepository repository;


    @BeforeClass
    public static void startup() throws InterruptedException, IOException, TTransportException {
        // startup embeded cassandra
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        // create session
        SessionProvider sessionProvider = new SessionProvider();
        Session initSession = sessionProvider.newSession();
        // create keyspace
        new CQLDataLoader(initSession).load(new ClassPathCQLDataSet("cql/create_keyspace.cql"));
        initSession.close();
        // connect to my_keyspace
        session = sessionProvider.newSession(KEYSPACE);
        // create tables
        new CQLDataLoader(session).load(new ClassPathCQLDataSet("cql/create_table_temperature_by_city.cql", false));
        // instanciate service and repos
        repository = new TemperatureRepository(session);
    }


    @AfterClass
    public static void shutdownEmbededCassandra() {
        if (session != null)
            session.close();
    }

    @Before
    public void setUp() {
        session.execute("TRUNCATE TABLE temperature_by_city");
    }

    @Test
    public void should_be_able_to_load_all_temperatures() throws Exception {
        // Given some sample temperatures are present in temperature_by_city
        new CQLDataLoader(session).load(new ClassPathCQLDataSet("cql/insert_temperatures.cql", false));

        // when I load all temperatures
        List<Temperature> temperatures = repository.getAll();

        // Then
        assertThat(temperatures.size(), is(4));
    }

    @Test
    public void should_be_able_to_load_a_single_temperature() throws Exception {
        // Given some sample temperatures are present in temperature_by_city
        new CQLDataLoader(session).load(new ClassPathCQLDataSet("cql/insert_temperatures.cql", false));

        // When I load by city and date
        Temperature loadedTemperature = repository.getByCityAndDate("paris", firstJanuary);

        // Then
        Temperature expectedTemperature = Temperature.builder().city("paris").date(firstJanuary).temperature(1.1f).build();
        assertEquals(expectedTemperature, loadedTemperature);
    }

    @Test
    public void should_save_a_single_temperature() throws Exception {
        // Given a saved temperature
        Temperature temperature = Temperature.builder()
                .city("paris")
                .date(firstJanuary)
                .temperature(15).build();
        repository.save(temperature);

        // when I reload temperature
        int temperaturesInDb = session.execute("SELECT * FROM temperature_by_city").all().size();
        assertEquals(1, temperaturesInDb);
    }

    @Test
    public void should_be_able_to_load_a_single_temperature2() throws Exception {
        // Given some sample temperatures are present in temperature_by_city
        new CQLDataLoader(session).load(new ClassPathCQLDataSet("cql/insert_temperatures.cql", false));

        // When I load by city and date
        Temperature loadedTemperature = repository.getByCityAndDate2("paris", firstJanuary);

        // Then
        Temperature expectedTemperature = Temperature.builder().city("paris").date(firstJanuary).temperature(1.1f).build();
        assertEquals(expectedTemperature, loadedTemperature);
    }

    @Test
    public void should_be_able_to_load_last_temperature_in_a_city() throws Exception {
        // Given some sample temperatures are present in temperature_by_city
        new CQLDataLoader(session).load(new ClassPathCQLDataSet("cql/insert_temperatures.cql", false));

        // When I load the last temperature in paris
        Temperature lastFoundParisTemperature = repository.getLastByCity("paris");

        // Then
        Temperature expectedTemperature = Temperature.builder().city("paris").date(secondJanuary).temperature(2.2f).build();
        assertEquals(expectedTemperature, lastFoundParisTemperature);
    }


}
