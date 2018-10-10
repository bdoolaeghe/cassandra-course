package fr.soat.cassandra.course1.repository;

import com.datastax.driver.core.Session;
import fr.soat.cassandra.course1.model.Temperature;

import java.time.LocalDate;
import java.util.List;

public class TemperatureRepository {

    private final Session session;

    public TemperatureRepository(Session session) {
        this.session = session;
    }

    /**
     * @return all saved temperatures
     */
    public List<Temperature> getAll() {
        throw new RuntimeException("implement me !");
    }

    /**
     * upsert the temperature in a city at a given date
     * @param temperature
     */
    public void save(Temperature temperature) {
        throw new RuntimeException("implement me !");
    }

    /**
     * load temperature in a city at a given date
     * @param city
     * @param date
     * @return the temperature
     */
    public Temperature getByCityAndDate(String city, LocalDate date) {
        throw new RuntimeException("implement me !");
    }

    public Temperature getByCityAndDate2(String city, LocalDate date) {
        throw new RuntimeException("implement me !");
    }

    public Temperature getLastByCity(String city) {
        throw new RuntimeException("implement me !");
    }

}
