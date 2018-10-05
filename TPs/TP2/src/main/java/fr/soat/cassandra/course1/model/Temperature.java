package fr.soat.cassandra.course1.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Temperature {
    private LocalDate date;
    private String city;
    private float temperature;
}
