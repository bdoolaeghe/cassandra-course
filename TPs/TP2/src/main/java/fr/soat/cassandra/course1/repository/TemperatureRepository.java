package fr.soat.cassandra.course1.repository;

import com.datastax.driver.core.*;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import fr.soat.cassandra.course1.model.Temperature;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

public class TemperatureRepository {

    private final Session session;
    private final PreparedStatement savePreparedStatment;
    private final TemperatureAccessor accessor;
    private Mapper<Temperature> mapper;


    public TemperatureRepository(Session session) {
        this.session = session;
        this.savePreparedStatment = session.prepare("INSERT INTO temperature_by_city (city, date, temperature) VALUES (:city, :date, :temperature)");
        MappingManager mappingManager = new MappingManager(session);
        this.mapper = mappingManager.mapper(Temperature.class);
        this.accessor = mappingManager.createAccessor(TemperatureAccessor.class);
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

    public Temperature getByCityAndDate2(String city, LocalDate date) {
        return mapper.get(city, date);
    }

    public Temperature getLastByCity(String city) {
        return accessor.getLastByCity(city);
    }

    private Temperature toModel(Row row) {
        return (row == null) ? null : Temperature.builder()
                .city(row.getString("city"))
                .date(row.get("date", LocalDate.class))
                .temperature(row.getFloat("temperature"))
                .build();
    }

}
