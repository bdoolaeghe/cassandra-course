CREATE TABLE IF NOT EXISTS my_keyspace.temperature_by_city (
    city text,
    date date,
    temperature float,
    PRIMARY KEY ((city), date)
)
WITH CLUSTERING ORDER BY (date desc);
