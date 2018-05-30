CREATE KEYSPACE IF NOT EXISTS my_keyspace_rf3 WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 3 };

USE my_keyspace_rf3;
