TP3.4 - FAIL OVER
=================

***Scenario***: *try the cassandra data "resynchronizatoin" mecanisms after a node failure:*
* *Hinted handoff*
* *read repair*
* *nodetool repair*

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

You can now practice the 3 repair mecanisms...:

[>> Next (TP3.4_hinted_handoff.md)](TP3.4_hinted_handoff.md)