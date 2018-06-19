TP3.4 - READ REPAIR
===================

***Scenario***: *demonstrate a node resynchronization with 'read repair'*

ReStart the 3-node cluster:
```
docker-compose up -d
```
Insert a new temperature (1°C) in Dublin:
```
cqlsh:my_keyspace_rf3> INSERT INTO temperature_by_city (city, date, temperature) VALUES ('dublin', '2017-01-01', 1);
```
Let's now create a data desynchro. shutdown all nodes but node-0:
```
docker-compose stop cassandra-node-1 cassandra-node-2
```
Set temperature in Dublin to 0°C (use cqlsh from *node-0*):
```
cqlsh:my_keyspace_rf3> INSERT INTO temperature_by_city (city, date, temperature) VALUES ('dublin', '2017-01-01', 0);
```
To avoid hinted handoff from node-0 to repair node-1 and node-2, let's manually shoot the hint stored by node-0:
```
cassandra-node-0 $> nodetool truncatehints
```

Restart the whole cluster:
```
docker-compose up -d
```
*We now have in Dublin:*
* *0°C in node-0*
* *1°C in node-1 and node-2*

**Let's make a read repair !** Read the Dublin temperature in CL=ALL from node-0:
```
cqlsh:my_keyspace_rf3> CONSISTENCY ALL;
cqlsh:my_keyspace_rf3> SELECT * from temperature_by_city where city = 'dublin';
```
*We got 0°C because it's the last written value.* 

To check now the value has been fixed to 0°C on node-1 and node-2 (thanks to *'read repair'*), Shutdown all nodes but node-1, and read value on node-1:
```
docker-compose stop cassandra-node-0 cassandra-node-2
```
from node-1:
```
cqlsh:my_keyspace_rf3> SELECT * from temperature_by_city where city = 'dublin';
```
So, what is the read temperature ??

*You can also check value on node-2*

[>> Next (TP3.4_nodetool_repair.md](TP3.4_nodetool_repair.md)