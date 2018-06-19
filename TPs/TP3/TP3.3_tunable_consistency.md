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
```

Query the number of temperatures, after downgrading CL to ONE:
```
cqlsh:my_keyspace_rf2> CONSISTENCY ONE
Consistency level set to ONE.

cqlsh:my_keyspace_rf2> SELECT count(*) from temperature_by_city ;
```
Any change ?

[>> Next (TP3.4_fail_over.md)](TP3.4_fail_over.md)

