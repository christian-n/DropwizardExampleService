##Dropwizard RESTful/GraphQL Example Service

###Features

- Basic resources and DAO concept
- Generic resources and DAO concept
- RESTful implementation
- GraphQL implementation
- CRUD repositories
- Updater concept
- Secured with OAuth using JWT
- ORM with Hibernate

###Description

Easy to use concept of a  RESTful service using Dropwizard. Using GraphQL as a complementary API.
Shows the usage of basic resources and DAO layers.  Includes a concept for generic handling of resources.

Adds JWT Bearer token support.
We use Gradle for building, H2 as in memory test database and Hibernate as ORM.

------------



**Table of Contents**

[TOCM]

[TOC]

###Gradle Dependencies
------------
####Core 
`compile group: 'io.dropwizard', name: 'dropwizard-core', version: '1.3.5'`
####Hibernate
`compile group: 'io.dropwizard', name: 'dropwizard-hibernate', version: '1.3.5'`
####Security
`compile group: 'io.dropwizard', name: 'dropwizard-auth', version: '1.3.5'`

`compile group: 'com.auth0', name: 'java-jwt', version: '3.4.0'`
####Database
`compile group: 'com.h2database', name: 'h2', version: '1.4.197`

###Deployment
------------
####Building
To build the Gradle project type

`./gradlew build`

in your console.
####Starting

Go to your `/build` folder and start the application server with command

`java -jar DropwizardExampleService.jar server configuration.yml`

you could specify our own configuration YML or use the default one.


###Service Documentation
------------
####Application
Lets start with the application. In the `ServiceStarter` we extend `Application` with our specific configuration class `ServiceConfiguration`. Add a `main(String[] args)` method as usual that initiates the service.

##### Initialize
First we need to bootstap the application and add the hibernate bundle. Lets add `Include.NON_NULL` to ignore null fields in serialization. Instantiate  the `HibernateBundle` with our example `@Entity` Person and Address. We use our configuration to provide a datasource.
```java

    private final HibernateBundle<ServiceConfiguration> hibernateBundle =
            new HibernateBundle<ServiceConfiguration>(Person.class, Address.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(ServiceConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(path -> getClass().getResourceAsStream("/" + path));
        bootstrap.addBundle(hibernateBundle);
        bootstrap.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
```
#####Run
Now we take a look at the run method. This is where we register our resources and to the Jersey environment and add auth support for JWT token.

```java
    @Override
    public void run(ServiceConfiguration configuration, Environment environment) {
        configureResourcesAsBasic(configuration, environment);
        configureResourcesAsGeneric(configuration, environment);
        configureSecurity(configuration, environment);
        configureHealth(configuration, environment);
    }
```
#####Registration
Register the resource and its repository in the `ServiceStarter` and add the repository to our database health.
```java
    private void configureResourcesAsBasic(ServiceConfiguration configuration, Environment environment) {
        PersonRepository personRepository = new PersonRepository(hibernateBundle.getSessionFactory());
        environment.jersey().register(new PersonResource(personRepository));
        dbHealth.addRepository(personRepository);
    }
```
Add a `OAuthCredentialAuthFilter` with an `JwtAuthenticator` for authentication and a simple `UserAuthorizer` for authorization.
```java
    private void configureSecurity(ServiceConfiguration configuration, Environment environment) {
        environment.jersey()
                .register(new AuthDynamicFeature(new OAuthCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(new JwtAuthenticator(configuration.getJwtFactory(), new InMemoryUserProvider()))
                        .setAuthorizer(new UserAuthorizer()).setPrefix("bearer").buildAuthFilter()));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
    }
```
And last but not least we gather a little bit of metric with `DbHealth`.
```java
    private void configureHealth(ServiceConfiguration configuration, Environment environment) {
        environment.healthChecks().register("Database Health", dbHealth);
    }
```

####Resources
First we create a `PersonRepository` as a DAO for our entity `Person`. Dropwizard has a simple dao class `AbstractDAO` for that. We use a interface better abstraction of DAO and Entity called `CRUDRepository` that implements basic CreateReadUpdateDelete methods. 

```java
public class PersonRepository extends AbstractDAO<Person> implements CRUDRepository<Person, String> {

    public PersonRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Person> getAll() {
        return list((Query<Person>) currentSession().createQuery("from Person"));
    }

    public Person getOne(String id) {
        return get(id);
    }

    public Person save(Person person) {
        return persist(person);
    }

    public boolean delete(String id) {
        currentSession().delete(get(id));
        return get(id) == null;
    }

}

```
Then we add a resource class for request handling in CRUD style. 
- `GET /person` Get all persons. 

- `GET /person/{id}` Get one person.

- `POST /person` Creates a person.

- `PUT/PATCH /person/{id}` Update a existing person.

- `DELETE /person/{id}` Delete a person and return Status.204.

We annotate mappings with `@Path`, add method security with `@RolesAllowed` and Hibernate session management with `@UnitOfWork`. Lets add some metrics with `@Timed`.

```java
@Path("/person")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

    private PersonRepository personRepository;

    public PersonResource(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GET
    @Timed
    @UnitOfWork
    @RolesAllowed("read")
    public List<Person> getAll() {
        return personRepository.getAll();
    }

    @GET
    @Path("{id}")
    @Timed
    @UnitOfWork
    @RolesAllowed("read")
    public Person get(@PathParam("id") String id) {
        return personRepository.getOne(id);
    }

    @POST
    @Timed
    @UnitOfWork
    @RolesAllowed("write")
    public Person create(@NotNull @Valid Person person) {
        return personRepository.save(person);
    }

    @PUT
    @Path("{id}")
    @Timed
    @UnitOfWork
    @RolesAllowed("write")
    public Person updatePut(@PathParam("id") String id, @NotNull @Valid Map<String, Object> body) {
        return update(id, body);
    }


    @PATCH
    @Path("{id}")
    @Timed
    @UnitOfWork
    @RolesAllowed("write")
    public Person updatePatch(@PathParam("id") String id, @NotNull @Valid Map<String, Object> body) {
        return update(id, body);
    }

    @DELETE
    @Path("{id}")
    @Timed
    @UnitOfWork
    @RolesAllowed("write")
    public Response delete(@PathParam("id") String id) {
        if (personRepository.delete(id)) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

```
The reason inject `Map<String, Object>` instead of `Person` (whats to totally possible) for easier merging. Now we know the fields that are missing instead of just `Null` fields. Because of that the client can `Null` fields now.
```java
    private Person update(String id, Map<String, Object> body) {
        Person source = personRepository.getOne(id);
        source.setForename(body.containsKey("forename") ? body.get("forename").toString() : source.getForename());
        source.setSurname(body.containsKey("surname") ? body.get("surname").toString() : source.getSurname());
        source.setBirthDate(body.containsKey("birthDate") ? body.get("birthDate").toString() : source.getBirthDate());
        return personRepository.save(source);
    }
```
####Configuration
Our configuration contains a `DataSourceFactory` for the database connection properties and a `JwtFactory` for token secret and issuer.

```java
public class ServiceConfiguration extends Configuration {

    @Valid
    @NotNull
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @Valid
    @NotNull
    private JwtFactory jwtFactory;


    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @JsonProperty("jwt")
    public JwtFactory getJwtFactory() {
        return jwtFactory;
    }

    @JsonProperty("jwt")
    public void setJwtFactory(JwtFactory jwtFactory) {
        this.jwtFactory = jwtFactory;
    }
}

```
###Health
Metrics are an important factor for service deployment so we add a simple database health `DbHealth` class that extends `HealthCheck` and takes all `CRUDRepository` instances (therefore the abstraction) and checks them with a `SELECT * FROM PERSON` statement.

```java
public class DbHealth extends HealthCheck {

    private List<CRUDRepository<?, ?>> repositories = new ArrayList<>();

    public DbHealth() {
    }

    public DbHealth(List<CRUDRepository<?, ?>> repositories) {
        this.repositories = repositories;
    }

    public void addRepository(CRUDRepository<?, ?> repository) {
        this.repositories.add(repository);
    }

    @Override
    protected Result check() throws Exception {
        repositories.forEach(CRUDRepository::getAll);
        return Result.healthy();
    }
}
```

###Security
####User
A simple `Principle` representation with roles.

```java
public class User implements Principal {

    private String username;
    private List<String> roles;

    public User(String username, String... roles) {
        this.username = username;
        this.roles = Arrays.asList(roles);
    }

    @Override
    public String getName() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }
}


```
And an in memory user provider. The `UserProvider` interface is a simple abstraction. The in memory implementation should be for testing purpose only.
```java
public class InMemoryUserProvider implements UserProvider {

    private static final Map<String, User> users = users();

    @Override
    public User getUser(String username) {
        return users.get(username);
    }

    private static Map<String, User> users() {
        Map<String, User> users = new HashMap<>();
        users.put("simple-user", new User("simple-user", "read"));
        users.put("simple-admin", new User("simple-admin", "read", "write"));
        return users;
    }
}


```

####Method security
We used a role based method security with the `@RolesAllowed` annotation. For `GET` requests we added a `read` role and for `POST, PUT, PATCH, DELETE` a `write` role.

####Filter
We add the `AuthDynamicFeature` that enables with in conjunction with `RolesAllowedDynamicFeature` authentication and authorization. Dropwizard has OAuth support out of the box with `OAuthCredentialAuthFilter`.  
#####Authentication
JWT is a common way for authentication. There is a lack of support but we use the handy [auth0-jwt](https://auth0.com/docs/jwt "auth0-jwt") library to verify and decode token. We implement a `JwtAuthenticator` class and extend `Authenticator`.

```java
public class JwtAuthenticator implements Authenticator<String, User> {

    private JwtFactory jwtFactory;
    private UserProvider userProvider;

    public JwtAuthenticator(JwtFactory jwtFactory, UserProvider userProvider) {
        this.jwtFactory = jwtFactory;
        this.userProvider = userProvider;
    }

    @Override
    public Optional<User> authenticate(String token) throws AuthenticationException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtFactory.getSecret());
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(jwtFactory.getIssuer()).build();
            return Optional.ofNullable(userProvider.getUser(verifier.verify(token).getSubject()));
        } catch (JWTVerificationException exception) {
            throw new AuthenticationException(exception);
        }
    }
}
```
#####Authorization
Now we need to authorize the user with its roles against the method roles. We added an implementation of `Authorizer` with the class `UserAuthorizer`.
```java
public class UserAuthorizer implements Authorizer<User> {

    @Override
    public boolean authorize(User principal, String role) {
        return principal.getRoles().contains(role);
    }

}
```
###Conclusion
------------

Dropwizard allows us to deploy RESTful services in an easy and fast way. The bootstraping mechanism pretty simple to understand and the performance is superior to frameworks like [Spring Boot](http://spring.io/projects/spring-boot "Spring Boot"). We should take note that Spring ecosystem is way better so its not always an easy decision between these two.

You can easly combine Dropwizard with dependency injection frameworks like [Dagger](https://google.github.io/dagger/ "Dagger"), [HK2](https://javaee.github.io/hk2/ "HK2") or [Spring](https://spring.io/ "Spring") and get the most out of it.


##Generic Features
## GraphQL

