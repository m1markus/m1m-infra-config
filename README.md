# m1m-infra-config

## Introduction

This is a configuration server with a REST API. It uses a PostgreSQL server to persist the data over restarts. It provides a Java client library using the REST API to let your application interact with the configuration server. There is no UI yet. The only interface is via curl.

The unique feature is that while your application is running, config changes will get pushed to your application. In reality the client library will query for updated values (using an http long poll GET request). If there are updates, it will read all config keys for your application and update all the values (this may be optimised in the future).

There is a simplistic example application shiped with these distribution. See the code under 'example-java-cli'.

## Technical stack

I develop it with an old intel mac with Ventura 13.6.7. I need a new one, your donation is more than welcome. You can send me Bitcoins or some Satoshis.

* maven 3.9.5
* jdk 21
* PostgreSQL 16
* Docker
* IntelliJ IDEA 2024.1.4 (Community Edition)

The server is build with the famous [Quarkus](https://quarkus.io/) microprofile framwork stack.

## Build the software locally on your box

1. Check out the git repo: [m1m-infra-config](https://github.com/m1markus/m1m-infra-config)
```
git clone https://github.com/m1markus/m1m-infra-config.git
```

2. Change into the new created directory 
```
cd m1m-infra-config
```

3. Start the db server
```
docker compose up
```

4. Create the database resources

Use your favorite PostgeSQL admin client to create the db objects. All the connect parameters are available in the 'docker-compose.yml'.

I'm using [DBeaver](https://dbeaver.io/).

Run the following DDL commands:

```
CREATE TABLE public.CONFIG_ITEM (
	id uuid NOT NULL,
	created_at timestamp(6) DEFAULT now() NOT NULL,
	updated_at timestamp(6) DEFAULT now() NOT NULL,
	domain varchar(255) NOT NULL,
	ou varchar(255) NOT NULL,
	application varchar(255) NOT NULL,
	key varchar(255) NOT NULL,
	value varchar(4096) NULL,
	type varchar(128) NULL,
	description varchar(4096) NULL,

	CONSTRAINT configitem_pkey PRIMARY KEY (id)
);
```

5. Build all JAR files

Run the follwoing maven command to build everything

```
mvn clean package
```

6. Start the config server

```
java -jar server/target/quarkus-app/quarkus-run.jar
```

7. Add a config item

```
curl --header "Content-Type: application/json" --request POST \
    --data '{ "id": "0190d66e-17ed-724d-a5f5-17016f7d0a21", "domain": "example.com", "application": "batch", "key": "batch.user.password", "value": "1234", "type": "password", "description": "this is my batch user pw" }' http://localhost:8080/config
```

8. Query the config item

```
curl -s "http://localhost:8080/config?domain=example.com&ou=exampleOrgUnit&application=batch" | jq
```

9. Run the example client

The client prints every second the value to the console
```
# switch directory
cd example-java-cli

mvn dependency:copy-dependencies

java -cp .:./target/classes:target/dependency/m1m-infra-config-client-1.0.0-SNAPSHOT.jar:\
./target/classes:target/dependency/slf4j-api-2.0.13.jar:\
./target/classes:target/dependency/slf4j-simple-2.0.13.jar:\
./target/classes:target/dependency/m1m-infra-config-model-1.0.0-SNAPSHOT.jar:\
./target/classes:target/dependency/jackson-annotations-2.17.2.jar:\
./target/classes:target/dependency/jackson-core-2.17.2.jar:\
./target/classes:target/dependency/jackson-databind-2.17.2.jar:\
./target/classes:target/dependency/jackson-datatype-jsr310-2.17.2.jar: ch.m1m.infra.config.example.ConfigExampleClient

```


10. Update the config value while the example client is running

```
curl --header "Content-Type: application/json" --request PUT \
    --data '{ "id": "0190d66e-17ed-724d-a5f5-17016f7d0a21", "domain":"example.com","application":"batch", "value": "8899" }' http://localhost:8080/config
```
You should see the updated value in the console where the example client is running.

Congratulation your done with the setup. You can now setup your server on a different device e.g. Raspberry Pi and start analysing the example client to use the client library in your own project.

```
 <dependency>
    <groupId>ch.m1m.infra</groupId>
    <artifactId>m1m-infra-config-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

```

Note: It's not avalable on maven central

## Using the client library

Description will follow ...

## Development

### Useful quarkus documentations

https://quarkus.io/guides/
https://quarkus.io/guides/rest
https://quarkus.io/guides/hibernate-orm

https://quarkus.io/guides/scheduler

https://smallrye.io/smallrye-mutiny/latest/guides/imperative-to-reactive/#running-blocking-code-on-subscription
https://quarkus.io/blog/mutiny-invoke-and-call/

### ToDo

Optimise the client lib, to compare old value with the received actual value. Call the "update" method only if the value is different.

OpenAPI documentation

## What you get...

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/m1m-infra-config-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- Reactive PostgreSQL client ([guide](https://quarkus.io/guides/reactive-sql-clients)): Connect to the PostgreSQL database using the reactive pattern

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
