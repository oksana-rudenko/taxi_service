# Taxi Service

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)

Project allows you to create your own driver for taxi service
and to use Data Base for managing all data.

## Description

Need to operate taxi Data Base and provide service for convenient using taxi?
Welcome to Taxi Service Project! You can creat your own driver. 
Information about all accounts, different car's manufacturers and cars, 
which are used in Taxi Service, is stored in Data Base. Use the project
to input new information and to output all necessary data to the web page. 

## Structure

1. **_[src/main/resources/init_db.sql](src/test/java)_** script for creating Taxi Service DataBase.
2. **_[src/main/java/taxi](src/test/java)_** project logic. 
3. **_[src/main/webapp](src/test/java)_** project web-structure.
4. **_[src/test/java/taxi](src/test/java)_** project's tests.

## Technologies 

1. [x] JDK 11
2. [x] Tomcat 9.0.50
3. [x] JDBC
4. [x] SQL
5. [x] MySQL
6. [x] Servlet API
7. [x] CSS
8. [x] JSP
9. [x] JUnit 5
10. [x] Log4j2

## Instructions

* Fork this repository
* Clone your forked repository
* Add your contribution
* Commit and push
* Create a pull request
* Wait for pull request to merge

## Using the project 

To get the actual parameters of the database tables, 
run script from the [resources/init_db.sql](resources/init_db.sql) file in the Workbench. 
Run your Tomcat version. Add and login your driver. After logging-in
you can use service for your purpose: add new manufacturers to the DB, 
add new cars, check the list of cars, manufacturers and driver, 
add or remove cars to your own list.

## Project testing

Run tests from [src/test/java/taxi/service](src/test/java/taxi/service) for testing project.