CREATE TABLE IF NOT EXISTS temperature_by_city (
    city text,
    date date,
    temperature float,
    PRIMARY KEY ((city), date)
)
WITH CLUSTERING ORDER BY (date desc);
