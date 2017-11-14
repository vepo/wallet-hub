#!/bin/bash
mvn clean package
cp target/parser-jar-with-dependencies.jar parser.jar
