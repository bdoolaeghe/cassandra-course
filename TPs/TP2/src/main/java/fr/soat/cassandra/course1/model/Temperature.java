package fr.soat.cassandra.course1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Temperature {

    private String city;

    private LocalDate date;

    private float temperature;
}
