TP3 - HINTED HANDOFF
====================

***Scenario***: *write in CL=ALL with one fallen node. When the fallen node comes back in the ring, the coordinator should notify it to resynchronize (hinted handoff)*

Shutdown cassandra-node-2:
```
docker-compose stop cassandra-node-2
```
Open a cqlsh on node-1, and insert data for Madrid:
```
cqlsh:> USE my_keyspace_rf3;
cqlsh:my_keyspace_rf3> INSERT INTO temperature_by_city (city, date, temperature) VALUES ('madrid', '2017-01-01', 10);
```

Now, shutdown node-0 and node-1 and then startup node-2 only:
```
docker-compose stop
docker-compose start cassandra-node-2
```
Open a cqlsh on node-2, and read data for Madrid:
```
cqlsh:> USE my_keyspace_rf3;
cqlsh:my_keyspace_rf3> SELECT * from temperature_by_city where city = 'madrid' ;

 city | date | temperature
------+------+-------------

(0 rows)
```
*As expected, cassandra-node-2 is desynchronized*

Now, restart the whole cluster:
```
docker-compose up -d
```
*We expect the coordinator node (cassandra-node-1) has so notified cassandra-node-2 of missed Madrid row*

Shutdown node-0 and node-1:
```
docker-compose stop cassandra-node-0 cassandra-node-1
```
Request Madrid data onto node-2:
```
cqlsh:my_keyspace_rf3> SELECT * from temperature_by_city where city = 'madrid';

 city   | date       | temperature
--------+------------+-------------
 madrid | 2017-01-01 |          10

(1 rows)
```
*As you can see, the madrid row has been resynchronized on cassandra-node-2 ! (hinted handoff)*

[>> Next (TP3.4_read_repair.md](TP3.4_read_repair.md)
