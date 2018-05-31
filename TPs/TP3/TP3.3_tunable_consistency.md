TP3.3) Tunable consistency
==========================

In a cqlsh, display the current consistency level (CL):
```
cqlsh:my_keyspace_rf2> CONSISTENCY;
Current consistency level is ONE.
```

Start up the clister but cassandra-node-2:
```
docker-compose stop
docker-compose up -d cassandra-node-0 cassandra-node-1
```

Query the number of temperatures in CL=ALL:
```
cqlsh:my_keyspace_rf2> CONSISTENCY ALL
Consistency level set to ALL.

cqlsh:my_keyspace_rf2> SELECT count(*) from temperature_by_city ;
ReadTimeout: Error from server: code=1200 [Coordinator node timed out waiting for replica nodes' responses] message="Operation timed out - received only 1 responses." info={'received_responses': 1, 'required_responses': 2, 'consistency': 'ALL'}
```

Query the number of temperatures, after downgrading CL to ONE:
```
cqlsh:my_keyspace_rf2> CONSISTENCY ONE
Consistency level set to ONE.

cqlsh:my_keyspace_rf2> SELECT count(*) from temperature_by_city ;

 count
-------
    38

(1 rows)
```
*As you can see, when a node is fallen, the client applicatin may accept to downgrade the consistency level to make the data available (but maybe not the last  version !)*

TP3.3) Fail over
----------------
***Scenario***: *try the cassandra data "resynchronizatoin" mecanisms after a node failure*

Let's hava a look to the different "repair" mecanism after desynchronization of cassandra nodes...

Start up a 3-node-cluster:
```
docker-compose stop
docker-compose up -d
```

To setup a proper context for following scenarios, let's create a keyspace with RF=3 *(my_keyspace_rf3)*, and insert data:
```
cqlsh> SOURCE '/TPs/TP3/create_keyspace_rf3.cql';
cqlsh> USE my_keyspace_rf3;
cqlsh:my_keyspace_rf3> SOURCE '/TPs/TP1/create_table_temperature_by_city.cql'
cqlsh:my_keyspace_rf3> SOURCE '/TPs/TP1/insert_dataset_for_temperature_by_city.cql'
```


### Hinted handoff
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

### Read repair
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

 city   | date       | temperature
--------+------------+-------------
 dublin | 2017-01-01 |           0

(1 rows)
```
*We got 0°C because it's the last written value.* 

To check now the value has been fixed to 0°C on node-1 and node-2 (thanks to *'read repair'*), Shutdown all nodes but node-1, and read value on node-1:
```
docker-compose stop cassandra-node-0 cassandra-node-2
```
from node-1:
```
cqlsh:my_keyspace_rf3> SELECT * from temperature_by_city where city = 'dublin';

 city   | date       | temperature
--------+------------+-------------
 dublin | 2017-01-01 |           0

(1 rows)
```
*You can also check value on node-2. As you can see, the last value has been propagated with* "read repair" *!*


### Anti antropy
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

 city      | date       | temperature
-----------+------------+-------------
 bruxelles | 2017-01-01 |           0

(1 rows)
```
*You can see the last value 0°C has been fixed on node-1 and node-2* !

[>> Next (TP3.4_fail_over.md)](TP3.4_fail_over.md)