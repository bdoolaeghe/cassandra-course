package fr.soat.cassandra.course1.repository;

import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import fr.soat.cassandra.course1.model.Temperature;

@Accessor
public interface TemperatureAccessor {

    @Query("SELECT * FROM temperature_by_city WHERE city = :city LIMIT 1")
    Temperature getLastByCity(@Param("city") String city);

}
