TP3 - FAULT TOLERANCE
=====================

Installation
------------
Let's install and start a 3-node cassandra cluster (using docker-compose)...

### Install scope


[Weave scope](https://www.weave.works/oss/scope/) will help to manage your cassandra cluster docker containers.

* download and start weave scope:
```
curl -L git.io/scope -o ~/bin/scope &&
chmod +x ~/bin/scope &&
~/bin/scope launch
```

* [open](http://localhost:4040) scope in a web browser.

### Install a 3-node cassandra cluster

A 3-node cassandra cluster is already configured in [docker-compose.yml](docker-compose.yml)

* startup the cluster with docker-compose:
```
cd TPS/TP3/
docker-compose -d
```

_NB: you can check logs with:_
```
docker-compose logs -f
```
Once the cluster is up, you should [see the 3 connected containers on weave scope](http://localhost:4040). Now, you'll need in the next steps to open a terminal on node containers (click on a node to select) and execute a cqlsh with button ">\_" (Execute shell). For convinience, the TPs sources are mounted in _/TPs/_ in each node container. 


TP3.1) Masterless architecture
------------------------------
***Scenario***: *experiment the serverless architecture, and connect from any node*

Create a keyspace 
```
cqlsh> SOURCE '/TPs/TP1/create_keyspace.cql'
cqlsh> USE my_keyspace;
cqlsh:my_keyspace_rf2> SOURCE '/TPs/TP1/create_table_temperature_by_city.cql'
cqlsh:my_keyspace_rf2> SOURCE '/TPs/TP1/insert_dataset_for_temperature_by_city.cql'
```

Let's try to query from each node: if you open a cqlsh from any cassandra node _(cassandra-node-0, cassandra-node-1, cassandra-node-2)_, you should be able to read the same inserted data:
```
cqlsh> use my_keyspace ;
cqlsh:my_keyspace> select count(*) from temperature_by_city ;

 count
-------
    38

(1 rows)

```
*Any node can be contacted to query any data !*

TP3.2) Data availability
------------------------
***Scenario***: *play with data replication*

### Cluster with no replication

The keyspace we created has been setup with a replication factor set to 1 (i.e. no replicatoin). 

Let's suppose one node is fallen...

* stop the node-2 docker (from a host terminal):
```
docker stop cassandra-node-2 
```
_NB: You can also stop a node with "square" (stop) button on [scope](http://localhost:4040). Then, you should see the last node has disappeared._

* Open a cqlsh from cassandra-node-1 (in [scope](http://localhost:4040)) , and query the data from 'paris': 
```
cqlsh> use my_keyspace ;
cqlsh:my_keyspace> SELECT * from temperature_by_city where city = 'paris' ;
```
* Now,  query the data from 'berlin':
```
cqlsh:my_keyspace> SELECT * from temperature_by_city where city = 'berlin' ;
```
What's happening ?

*The partition for 'berlin' can't be queried, because the node hosting the data is fallen. With no replication, when a node is lost, some data is unavailable.*

### Cluster with replication (RF=2)
***Scenario***: *use replication factor (RF) and consistency level (CL)*

Let's now have some data replication. 

* From any node create a new *my_keyspace_rf2* keyspace with replication (RF=2):
```
cqlsh> SOURCE '/TPs/TP3/create_keyspace_rf2.cql'
cqlsh> USE my_keyspace_rf2;
cqlsh> SOURCE '/TPs/TP1/create_table_temperature_by_city.cql'
cqlsh> SOURCE '/TPs/TP1/insert_dataset_for_temperature_by_city.cql'
```

* Again, shutdown a node, and query the data from 'paris' and 'berlin' as we earlier did [with no replication](#user-content-cluster-with-no-replication). Conclusion ?

*Thanks to replication, when a node is fallen, we can 
still get the data from a replica.*

TP3.3) Tunable consistency
--------------------------
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


### hinted handoff
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

### read repair
***Scenario***: *demonstrate a node resynchronizatio with 'read repair'*

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

**Let's make a read repair !** Read the Dublin temperature in CL=ALL from any node:
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
 dublin | 2017-01-01 |           1

(1 rows)
```
*You can also check value on node-2. As you can see, the last value has been propagated with *'read repair'* !*


### anti antropy
