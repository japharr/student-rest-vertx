# A Simple Web API with Vert.x 4

This project is a very simple Vert.x 4 application and contains some explanation on how this application is built
and tested.

## Building

You build the project using:

```
mvn clean package
```

## Testing

The application is tested using [vertx-Junit5](https://vertx.io/docs/vertx-junit5/java/).

## Packaging

The application is packaged as a _fat jar_, using the
[Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/).

## Running

Once packaged, just launch the _fat jar_ as follows:

```
java -jar target/student-rest-1.0.0-SNAPSHOT-fat.jar
```

Then, open a browser to http://localhost:8080.
