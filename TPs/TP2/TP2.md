![alt text](https://linkurio.us/wp-content/uploads/2016/06/datastax_logo-600x140.jpg "TP2")

TP2 - Train with the java Datastax driver
=========================================
Clone this git repository (if not done yet), checkout branch *booster_camp* and import TP2 Maven project in your favorite IDE. 

*Nota: the project depends on [lombok](https://projectlombok.org/). You may have to install lombok plugin in your IDE to build successfully the cassandra-course TP:*
* *[IntelliJ lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin)*
* *[eclipse install](https://projectlombok.org/setup/eclipse)*

The project is already setup as a cassandra java project. As you can see in the [pom.xml](pom.xml), we have declared dependencies to the java [DataStax driver](https://docs.datastax.com/en/developer/java-driver/3.4/):

```
        <!-- cassandra driver -->
        <dependency>
            <groupId>com.datastax.cassandra</groupId>
            <artifactId>cassandra-driver-core</artifactId>
            <version>3.3.0</version>
        </dependency>
```
These jars will provide all classes required to create a connection to a cassandra cluster, and execute queries on it. As we want to use the new *java.util.LocalDate* type to map the temperature date from cassandra, we also need an additional jar:
```
        <!-- extras for LocalDate type support-->
        <dependency>
            <groupId>com.datastax.cassandra</groupId>
            <artifactId>cassandra-driver-extras</artifactId>
            <version>3.3.0</version>
        </dependency>
```

We also use *cassandra-unit* for testing, to start an embeded cassandra node when running tests:
```
        <dependency>
            <groupId>org.cassandraunit</groupId>
            <artifactId>cassandra-unit</artifactId>
            <version>3.0.0.1</version>
            <scope>test</scope>
        </dependency>
```
As you will see in *TemperatureRepositoryTest* test class,  we will start an embeded single-node-Cassandra cluster (started in the same JVM, on default port **9142**):
```
    @BeforeClass
    public static void startup() {
        // startup embeded cassandra
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        ...
    }
```

Setup a SessionProvider
-----------------------
_First, we need to setup a **SessionProvider**, that will provide us a *Session* object, to connect and query the Cassandra clsuter._

open class *SessionProvider*, and implement the method *createCluster()* that should create, configure and return a *Cluster*, representing the cassandra cluster we want to connect through a *Session* to:
```
    private Cluster createCluster() {
        throw new RuntimeException("implement me !");
    }

```
Use a *Cluster.builder()* to build and configurea a *Cluster* instance in *createCluster()*.

----
:+1: A *Cluster* configuration can have many options ; but in this training, we'll setup a simple configuration, giving:
* the *contact points* (list of cassandra node the driver can contact as *coordinators*). In our simple case, there is just one node: **"localhost"**
* the connection port: 9142
* the support of java.util.LocalDate type:
```
        cluster.getConfiguration().getCodecRegistry().register(LocalDateCodec.instance);
```
----

Once *createCluster()* has been implemented, we'll be able to get a *Session* object to connect and query cassandra:
```
    public Session newSession() {
        return cluster.connect();
    }

```


Implement simple Repository
---------------------------
*The goal of this part is to use the DataStax driver to write the [TemperatureRepository](src/main/java/fr/soat/cassandra/course1/repository/TemperatureRepository.java) 5 methods, demonstrating the different way we can execute cassandra queries:*
```
    public List<Temperature> getAll() {
        throw new RuntimeException("implement me !");
    }

    public void save(Temperature temperature) {
        throw new RuntimeException("implement me !");
    }

    public Temperature getByCityAndDate(String city, LocalDate date) {
        throw new RuntimeException("implement me !");
    }


    public Temperature getByCityAndDate2(String city, LocalDate date) {
        throw new RuntimeException("implement me !");
    }

    public Temperature getLastByCity(String city) {
        throw new RuntimeException("implement me !");
    }

```

To check our implementation is fine, we'll use the already written test class [TemperatureRepositoryTest](src/main/java/fr/soat/cassandra/course1/repository/TemperatureRepositoryTest.java). As you can see, this test is based on *cassandra-unit*. At setup, it will:
* start an embeded cassandra 1-noide cluster:
```
		// startup embeded cassandra
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
```
* create a "my_keyspace" keyspace, and *temperature_by_city* table:
```
        // create keyspace
        new CQLDataLoader(initSession).load(new ClassPathCQLDataSet("cql/create_keyspace.cql"));
        ...
        // create tables
        new CQLDataLoader(session).load(new ClassPathCQLDataSet("cql/create_table_temperature_by_city.cql", false));

```
* create a *Session* with *SessionProvider* to be used in the [TemperatureRepository](src/main/java/fr/soat/cassandra/course1/repository/TemperatureRepository.java):
```
		session = sessionProvider.newSession(KEYSPACE);
		...
		repository = new TemperatureRepository(session);
```

Enough talk ! It's now time to implement the repository...

### Implement *getAll()*
_We are now to implement a simple quering repository method using **session.execute(String cql)**_

Implement *TemperatureRepository.getAll()*, expecting to return every temperature in table *temperature_by_city*.

----
:+1: The simplest way to execute a simple CQL query with DataStax driver, is to use  **session.execute(*<CQL query as string>*)**. 

----

*After that, use* fr.soat.cassandra.course1.repository.TemperatureRepositoryTest#should_be_able_to_load_all_temperatures *to test your implementation !*

###  Implement *save(temperature)*
_The goal here is to train with **PreparedStatment** and **BoundStatment**_

Implement *TemperatureRepository.save(temperature)*, expecting to save in table *temperature_by_city* a single temperature in a city at a given date.

----
:+1: To execute the save, we will use a [PreparedStatement](https://docs.datastax.com/en/drivers/java/3.0/com/datastax/driver/core/PreparedStatement.html). Datastax PreparedStatement are very similar to JDBC PreapredSTatement. You can use it to prepare a parametered stetement, and then bind some parameters to values. Follow the [DataStax PreparedStatement documentation](https://docs.datastax.com/en/developer/java-driver/3.0/manual/statements/prepared/) to implement the *save(temperature)* method.

----

*Use* fr.soat.cassandra.course1.repository.TemperatureRepositoryTest#should_save_a_single_temperature *to test your implementation !*

### implement *getByCityAndDate()*
_We will here use the **QueryBuilder** to build cassandra queries._

Implement now the method *getByCityAndDate()*, finding a temparature by city and date. 

----
:+1: we could use here a [PreparedStatement](https://docs.datastax.com/en/drivers/java/3.0/com/datastax/driver/core/PreparedStatement.html) also. But, another way to execute *dynamic* queries is to Use DataStax [QueryBuilder](https://docs.datastax.com/en/drivers/java/2.0/com/datastax/driver/core/querybuilder/QueryBuilder.html) API. You can follow an [exisintg exemple](https://docs.datastax.com/en/developer/java-driver/3.6/manual/statements/built/#specifying-conditions) to write your own for *getByCityAndDate()*.

----

*Once your implementation is over, use* fr.soat.cassandra.course1.repository.TemperatureRepositoryTest#should_be_able_to_load_a_single_temperature *for testing !*


### implement *getByCityAndDate2()*

_The Datastax driver provides some more advanced object mapping mecanism (as you could find with [Hibernate](https://hibernate.org/))._

We'll experiment here the *cassandra mappers*, to implement (in a different way) **getByCityAndDate2(city)**.

First, add in the [pom.xml](pom.xml) a dependency to *cassandra-driver-mapping*, to get access to **com.datastax.driver.mapping.Mapper<T>** class: 

```
       <dependency>
            <groupId>com.datastax.cassandra</groupId>
            <artifactId>cassandra-driver-mapping</artifactId>
            <version>3.3.0</version>
        </dependency>
```

Then, you can use an instance of **Mapper<Temperature>** in **TemeratureRepository** to automaticly apply CRUD operations in *temperature_by_city*, and map ResultSet to your **Temperature** type ! 
* First, annotate the *Temperature* class de define the object / table mapping:
```
@Table(name = "temperature_by_city")
public class Temperature {

    @PartitionKey
    private String city;

    @ClusteringColumn
    private LocalDate date;

    @Column
    private float temperature;
}
```
* Create a mapper instance in *TemperatureRepository*:
```
    private Session session;
    private Mapper<Temperature> mapper;    

    public TemperatureByCityRepository(Session session) {
        this.session = session;
        MappingManager mappingManager = new MappingManager(session);
        this.mapper = mappingManager.mapper(Temperature.class);
    }
```
After that, you can use method *mapper.get(...)* to load a temperature by city and date, and map result to *Temperature* instance !

*Once your implementation is over, use *fr.soat.cassandra.course1.repository.TemperatureRepositoryTest#should_be_able_to_load_a_single_temperature2* for testing !*


### Implement getLastByCity()

_We will here use the Datastax **Accessor** type to build custom cassandra queries (not available in *Mapper*), but still use automatic resultset / object mapping._

To implement **getLastByCity(city)**, we will use the custom CQL query:
```
SELECT * FROM temperature_by_city WHERE city = :city LIMIT 1;
```
Create a **TemperatureAccessor** inteface, annotated with **@Accessor**. In this interface, you can declare a **getLastByCity()** method, annotated with a corresponding query:
```
@Accessor
public interface TemperatureAccessor {

    @Query("SELECT * FROM temperature_by_city WHERE city = :city LIMIT 1")
    Temperature getLastByCity(@Param("city") String city);

}
```
In **TemperatrureRepository**, You can then create an accessor of type **TemperatureAccessor**, to execute the custom query:
```
    private TemperatureAccessor accessor;

    public TemperatureByCityRepository(Session session) {
        ...
        MappingManager mappingManager = new MappingManager(session);
	this.accessor = mappingManager.createAccessor(TemperatureByCityAccessor.class);
    }

    public Temperature getLastByCity(String city) {
        // return this.accessor.get..
    }
```

*Once your implementation is over, use* fr.soat.cassandra.course1.repository.TemperatureRepositoryTest#should_be_able_to_load_last_temperature_in_a_city *for testing !*
