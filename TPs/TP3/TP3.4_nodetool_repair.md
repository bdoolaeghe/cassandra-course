TP3.4 - Nodetool repair
=======================

***Scenario***: *demonstrate how the "nodetool repair" can resynchronize data in the cluster*

ReStart the 3-node cluster:
```
docker-compose up -d
```
Insert a new temperature (1°C) in Bruxelles:
```
cqlsh:my_keyspace_rf3> INSERT INTO temperature_by_city (city, date, temperature) VALUES ('bruxelles', '2017-01-01', 1);
```
Let's now create a data desynchro. shutdown all nodes but node-0:
```
docker-compose stop cassandra-node-1 cassandra-node-2
```
Set temperature in Bruxelles to 0°C (use cqlsh from *node-0*):
```
cqlsh:my_keyspace_rf3> INSERT INTO temperature_by_city (city, date, temperature) VALUES ('bruxelles', '2017-01-01', 0);
```
To avoid hinted handoff from node-0 to repair node-1 and node-2, let's manually shoot the hint stored by node-0:
```
cassandra-node-0 $> nodetool truncatehints
```

Restart the whole cluster:
```
docker-compose up -d
```
*We now have in Bruxelles:*
* *0°C in node-0*
* *1°C in node-1 and node-2*

**Let's run a "nodetool repair" !** From node-1 (then from node-2), open shell and execute:
```
$> nodetool repair
```
Rad temperature in Bruxcelles from repaired nodes cassandra-node-1 (then cassandra-node-2):
```
cqlsh:my_keyspace_rf3> SELECT * FROM temperature_by_city WHERE city = 'bruxelles';
```
So, what is the read value ?