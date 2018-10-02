## Dropwizard RESTful/GraphQL Example Service

### Features

- Basic resources and DAO concept
- Generic resources and DAO concept
- RESTful implementation
- GraphQL implementation
- CRUD repositories
- Updater concept
- Secured with OAuth using JWT
- ORM with Hibernate

### Description

Easy to use concept of a  RESTful service using Dropwizard. Using GraphQL as a complementary API.
Shows the usage of basic resources and DAO layers.  Includes a concept for generic handling of resources.

Adds JWT Bearer token support.
We use Gradle for building, H2 as in memory test database and Hibernate as ORM.

------------
- [Dropwizard RESTful/GraphQL Example Service](#dropwizard-restfulgraphql-example-service)
  - [Features](#features)
  - [Description](#description)
  - [Gradle Dependencies](#gradle-dependencies)
    - [Core](#core)
    - [Hibernate](#hibernate)
    - [Security](#security)
    - [Database](#database)
  - [Deployment](#deployment)
    - [Building](#building)
    - [Starting](#starting)
  - [Service Documentation](#service-documentation)
    - [Application](#application)
      - [Initialize](#initialize)
      - [Run](#run)
      - [Registration](#registration)
    - [Resources](#resources)
    - [Configuration](#configuration)
  - [Health](#health)
  - [Security](#security)
    - [User](#user)
    - [Method security](#method-security)
    - [Filter](#filter)
      - [Authentication](#authentication)
      - [Authorization](#authorization)
  - [Conclusion](#conclusion)
- [Generic Features](#generic-features)
  - [Repositories](#repositories)
  - [Resources](#resources)
  - [Updater](#updater)
  - [Registration](#registration)
  - [Conclusion](#conclusion)
- [GraphQL](#graphql)


### Gradle Dependencies
------------
#### Core 
`compile group: 'io.dropwizard', name: 'dropwizard-core', version: '1.3.5'`
#### Hibernate
`compile group: 'io.dropwizard', name: 'dropwizard-hibernate', version: '1.3.5'`
#### Security
`compile group: 'io.dropwizard', name: 'dropwizard-auth', version: '1.3.5'`

`compile group: 'com.auth0', name: 'java-jwt', version: '3.4.0'`
#### Database
`compile group: 'com.h2database', name: 'h2', version: '1.4.197`

### Deployment
------------
#### Building
To build the Gradle project type

`./gradlew build`

in your console.
#### Starting

Go to your `/build` folder and start the application server with command

`java -jar DropwizardExampleService.jar server configuration.yml`

you could specify our own configuration YML or use the default one.


### Service Documentation
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
##### Run
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
##### Registration
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

#### Resources
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
#### Configuration
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
### Health
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

### Security
#### User
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

#### Method security
We used a role based method security with the `@RolesAllowed` annotation. For `GET` requests we added a `read` role and for `POST, PUT, PATCH, DELETE` a `write` role.

#### Filter
We add the `AuthDynamicFeature` that enables with in conjunction with `RolesAllowedDynamicFeature` authentication and authorization. Dropwizard has OAuth support out of the box with `OAuthCredentialAuthFilter`.  
##### Authentication
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
##### Authorization
Now we need to authorize the user with its roles against the method roles. We added an implementation of `Authorizer` with the class `UserAuthorizer`.
```java
public class UserAuthorizer implements Authorizer<User> {

    @Override
    public boolean authorize(User principal, String role) {
        return principal.getRoles().contains(role);
    }

}
```
### Conclusion
------------

Dropwizard allows us to deploy RESTful services in an easy and fast way. The bootstraping mechanism pretty simple to understand and the performance is superior to frameworks like [Spring Boot](http://spring.io/projects/spring-boot "Spring Boot"). We should take note that Spring ecosystem is way better so its not always an easy decision between these two.

You can easly combine Dropwizard with dependency injection frameworks like [Dagger](https://google.github.io/dagger/ "Dagger"), [HK2](https://javaee.github.io/hk2/ "HK2") or [Spring](https://spring.io/ "Spring") and get the most out of it.


## Generic Features

### Repositories
Lets add some generic flavor to the service. We use our `CRUDRepository` and add a generic implementation that works for every entity. We use Hibernate directly instead of `AbstractDAO` just because it lacks target detection at runtime.

```java
public class SimpleCRUDRepository<T, S extends Serializable> implements CRUDRepository<T, S> {

    private Class<T> domainClass;
    private SessionFactory sessionFactory;

    public SimpleCRUDRepository(Class<T> domainClass, SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.domainClass = domainClass;
    }

    public List<T> getAll() {
        return requireNonNull((Query<T>) sessionFactory.getCurrentSession().createQuery("from " + domainClass.getSimpleName())).list();
    }

    public T getOne(S id) {
        return sessionFactory.getCurrentSession().get(domainClass, requireNonNull(id));
    }

    public T save(T object) {
        sessionFactory.getCurrentSession().saveOrUpdate(requireNonNull(object));
        return object;
    }

    public boolean delete(S id) {
        sessionFactory.getCurrentSession().delete(getOne(id));
        return getOne(id) == null;
    }

}
```
### Resources
For resource mappings we use Jerseys `Resource` and programmatically add resource endpoints. `CRUDResourceMapping` is a class that helps us with mapping CRUD methods to a jersey resources. We could also use a builder pattern here but thats just a simple concept and should be used with caution. Therefore we skip the use of Dropwizards `ObjectMapper` and go straight with our own one. Keep in mind that configurations did not apply  in this case.

```java
public class CRUDResourceMapping<T, S extends Serializable> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Class<T> resource;
    private CRUDRepository<T, S> crudRepository;
    private String path;
    private Updater updater;

    public CRUDResourceMapping(String path, Class<T> resource, CRUDRepository<T, S> crudRepository) {
        this.path = path;
        this.resource = resource;
        this.crudRepository = crudRepository;
        this.updater = new JacksonUpdater();
    }

    public CRUDResourceMapping(String path, Class<T> resource, CRUDRepository<T, S> crudRepository, Updater updater) {
        this.path = path;
        this.resource = resource;
        this.crudRepository = crudRepository;
        this.updater = updater;
    }

    public Resource getResource() {
        Resource.Builder builder = Resource.builder(path);
        builder.path(path);
        builder.addMethod("GET").produces(MediaType.APPLICATION_JSON).handledBy(getAll());
        builder.addChildResource("{id}").addMethod("GET").produces(MediaType.APPLICATION_JSON).handledBy(get());
        builder.addMethod("POST").produces(MediaType.APPLICATION_JSON).handledBy(post());
        builder.addChildResource("{id}").addMethod("PUT").produces(MediaType.APPLICATION_JSON).handledBy(update());
        builder.addChildResource("{id}").addMethod("PATCH").produces(MediaType.APPLICATION_JSON).handledBy(update());
        builder.addChildResource("{id}").addMethod("DELETE").produces(MediaType.APPLICATION_JSON).handledBy(delete());
        return builder.build();
    }


    private Inflector<ContainerRequestContext, Object> getAll() {
        return new Inflector<ContainerRequestContext, Object>() {
            @Override
            @UnitOfWork
            @Timed
            @RolesAllowed("read")
            public Object apply(ContainerRequestContext containerRequestContext) {
                return crudRepository.getAll();
            }
        };
    }

    private Inflector<ContainerRequestContext, Object> get() {
        return new Inflector<ContainerRequestContext, Object>() {
            @Override
            @UnitOfWork
            @Timed
            @RolesAllowed("read")
            public Object apply(ContainerRequestContext containerRequestContext) {
                return crudRepository.getOne((S) containerRequestContext.getUriInfo().getPathParameters().get("id").get(0));
            }
        };
    }

    private Inflector<ContainerRequestContext, Object> post() {
        return new Inflector<ContainerRequestContext, Object>() {
            @Override
            @UnitOfWork
            @Timed
            @RolesAllowed("write")
            public Object apply(ContainerRequestContext containerRequestContext) {
                try {
                    return crudRepository.save(objectMapper.readValue(containerRequestContext.getEntityStream(), resource));
                } catch (IOException e) {
                    throw new MappingException(resource, e);
                }
            }
        };
    }

    private Inflector<ContainerRequestContext, Object> update() {
        return new Inflector<ContainerRequestContext, Object>() {
            @Override
            @UnitOfWork
            @Timed
            @RolesAllowed("read")
            public Object apply(ContainerRequestContext containerRequestContext) {
                try {
                    return crudRepository.save((T) updater.update(crudRepository.getOne((S) containerRequestContext.getUriInfo().getPathParameters().get("id").get(0)),
                            objectMapper.readValue(containerRequestContext.getEntityStream(), Map.class)));
                } catch (IOException e) {
                    throw new MappingException(resource, e);
                }
            }
        };
    }

    private Inflector<ContainerRequestContext, Object> delete() {
        return new Inflector<ContainerRequestContext, Object>() {
            @Override
            @UnitOfWork
            public Object apply(ContainerRequestContext containerRequestContext) {
                if (crudRepository.delete((S) containerRequestContext.getUriInfo().getPathParameters().get("id").get(0))) {
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        };
    }

}
```
### Updater
The `Updater` is a interface that let you handle the mapping between the DTO and the entity. For sophisticated resources you may want your own handling. For your generic concept there is a `JacksonUpdater` that uses Jacksons merging mechanism. This should cover most cases and minds most of Jacksons annotations. Handles also embedded entities but keep in mind that its not the way to [REST](https://www.martinfowler.com/articles/richardsonMaturityModel.html "REST").

```java
public class JacksonUpdater implements Updater {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T update(T source, Map<String, Object> dto) {
        try {
            return objectMapper.readerForUpdating(source).readValue(objectMapper.writeValueAsString(dto));
        } catch (IOException e) {
            throw new UpdaterException(e);
        }
    }

}
```
#### Registration
Last but not least we register the resources in our run method.
```java
    private void configureResourcesAsGeneric(ServiceConfiguration configuration, Environment environment) {
        CRUDRepository<Address, String> addressRepository = new SimpleCRUDRepository<>(Address.class, hibernateBundle.getSessionFactory());
        environment.jersey().getResourceConfig().registerResources(new CRUDResourceMapping<>("/address", Address.class, addressRepository).getResource());
    }
```
### Conclusion
Its simple as that to add generics to resources in Dropwizard. It saves time, boiler plate code and everything is in one place so win win? Not exactly especially the merging like Jackson does is a hassle to debug and fix. It requires knowledge of the framework capabilities and should be tested to death first before even thinking of putting it in production. Every framework specific bug could break your leg and version updates are far from easy. But nonetheless its a good alternative that should be used with caution.

## GraphQL

