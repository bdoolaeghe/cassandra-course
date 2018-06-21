TP3.1) Masterless architecture
==============================

***Scenario***: *experiment the serverless architecture, and connect from any node*

Create a keyspace 
```
cqlsh> SOURCE '/TPs/TP1/create_keyspace.cql'
cqlsh> USE my_keyspace;
cqlsh:my_keyspace2> SOURCE '/TPs/TP1/create_table_temperature_by_city.cql'
cqlsh:my_keyspace> SOURCE '/TPs/TP1/insert_dataset_for_temperature_by_city.cql'
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

[>> Next (TP3.2_data_availability.md)](TP3.2_data_availability.md)
