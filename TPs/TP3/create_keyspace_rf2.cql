CREATE KEYSPACE IF NOT EXISTS my_keyspace_rf2 WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 2 };

USE my_keyspace_rf2;