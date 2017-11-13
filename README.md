# Log Parser

Parse the **access.log** file, fill a database and block IPs.

## Dependencies

* Java 8
* Maven
* Docker
* Docker-compose

## Build

### Source

To build the source file, just execute: 

```
./build.sh
```

This will compile and package the tool creating the `parser.jar` file.

### Infrastructure 

To create the database infrastructure, just execute:

```
docker-compose up log-migrator
```

This command will create two docker containers:

* **log-db**: MySQL database container
* **log-db-migrator**: Container to create and update **log-db** schema. Using [flyway](https://flywaydb.org/). This container will update the schema and finish.

## Usage
To run the tool, just execute:

```
java -jar parser.jar --startDate=2017-01-01.13:00:00 --duration=daily --threshold=250
```

The command is a little different from the specification. We just execute as an executable jar.
