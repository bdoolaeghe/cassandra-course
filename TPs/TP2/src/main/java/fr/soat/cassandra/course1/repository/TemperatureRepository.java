package fr.soat.cassandra.course1.repository;

import com.datastax.driver.core.*;
import fr.soat.cassandra.course1.model.Temperature;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

public class TemperatureRepository {

    private final Session session;
    private final PreparedStatement savePreparedStatment;

    public TemperatureRepository(Session session) {
        this.session = session;
        this.savePreparedStatment = session.prepare("INSERT INTO temperature_by_city (city, date, temperature) VALUES (:city, :date, :temperature)");
    }

    /**
     * @return all saved temperatures
     */
    public List<Temperature> getAll() {
        ResultSet rows = session.execute("SELECT * FROM temperature_by_city");
        return rows.all().stream().map(this::toModel).collect(Collectors.toList());
    }

    /**
     * upsert the temperature in a city at a given date
     * @param temperature
     */
    public void save(Temperature temperature) {
        BoundStatement boundStatement = savePreparedStatment.bind(temperature.getCity(), temperature.getDate(), temperature.getTemperature());
        session.execute(boundStatement);
    }

    /**
     * load temperature in a city at a given date
     * @param city
     * @param date
     * @return the temperature
     */
    public Temperature getByCityAndDate(String city, LocalDate date) {
        Statement stmt = select()
                .all()
                .from("temperature_by_city")
                .where(eq("city", city))
                .and(eq("date", date));
        ResultSet rows = session.execute(stmt);
        return toModel(rows.one());
    }

    private Temperature toModel(Row row) {
        return (row == null) ? null : Temperature.builder()
                .city(row.getString("city"))
                .date(row.get("date", LocalDate.class))
                .temperature(row.getFloat("temperature"))
                .build();
    }

}
