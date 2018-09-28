![alt text](https://linkurio.us/wp-content/uploads/2016/06/datastax_logo-600x140.jpg "TP2")

TP2 - Practice the java Datastax driver
=======================================
Clone this git repository (if not done yet) and import TP2 Maven project in your favorite IDE. 

*Nota: the project depends on [lombok](https://projectlombok.org/). You may have to install lombok plugin in your IDE to build successfully the cassandra-course TP:*
* [IntelliJ lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin)
* [eclipse install](https://projectlombok.org/setup/eclipse)

You can see the project is partly imphttps://projectlombok.org/lemented, and some [JUnit tests]((src/test/java/fr/soat/cassandra/course1/) are implemented, but failing. At the end of TP all tests should be green !

Now, write in [TemperatureByCityRepository](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java) the following methods:
* **[Q2.1]** [getById(city, date)](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java#L46), returning the temperature **in a city at a given date** (hint: use the _Mapper<TemperatureByCity>_)
* **[Q2.2]** [getByIdAsync(city, date)](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java#L52), same -- **but asynchronous** -- mehtod as _getById(city, date)_  (hint: also use the Mapper<TemperatureByCity>)
* **[Q2.3]** [getLastByCity(city)](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java#L67), giving the **last temperature in a given city** (hint: use _TemperatureByCityAccessor_)
* **[Q2.4]** [getLastByCityAsync(city)](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java#L72), same -- **but asynchronous** -- method as _getLastByCity(city)_  (hint: use same accessor)
* **[Q2.5]** [getFirstByCity(city)](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java#L77), giving the **oldest temperature in a given city** (hint: use the accessor)
* **[Q2.6]** [saveBatch(temperatures)](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java#L82), **saving a list of temperatures** at some dates in some cities (hint: don't use cassandra [BatchStatement](https://docs.datastax.com/en/cql/3.3/cql/cql_reference/cqlBatch.html), but apply save operations in paralllel)


Extra
-----
* **[Q2.7]** [getByCityUntil(city, boudnDate)](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java#L87), returning all the temperatures **in a given city before a given bound date, in reverse chronological order** (that is, the latest temperature first)
* **[Q2.8]** [getByCityUntilAsc(city, boudnDate)](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java#L92), same as [getByCityUntil(city, boudnDate)](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java#L87) but **in chronological order** (that is, the latest temperature in last)

Extra bis !
-----------
* **[Q2.9]** complete the [TemperatureByDate](src/main/java/fr/soat/cassandra/course1/model/TemperatureByDate.java) bean to map new table [temperature_by_date](src/main/resources/cql/create_table_temperature_by_date.cql) and complete the repository class [TemperatureByDateRepository](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByDateRepository.java) the method _getByDate(date)_, to save and get the temperatures at a given date **in every city** (hint: use table temperature_by_date, with [create_table_temperature_by_date.cql](src/main/resources/cql/create_table_temperature_by_date.cql) and [insert_dataset_for_temperature_by_date.cql](src/main/resources/cql/insert_dataset_for_temperature_by_date.cql) )
* **[Q2.10]** create an **aggregating service** [TemperatureService](src/main/java/fr/soat/cassandra/course1/service/TemperatureService.java), to:
 * save a temperature **in a city at a given date** (hint: delegate to the 2 underlying repositories [TemperatureByCityRepository](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByCityRepository.java) and [TemperatureByDateRepository](src/main/java/fr/soat/cassandra/course1/repository/TemperatureByDateRepository.java))
 * get the temperatures in a **given city**
 * get the temperatures at a **given date**

