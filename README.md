# Stock Manager Application

This is a Spring Boot application for managing stocks, assets, and orders in a financial system. It includes services for customer management, asset management, and order processing.

## Summirize
 Hello , 
 - I implemented the project with Bonus 1 and Bonus 2 parts(user authentication and match endpoints)
 - The admin user is created default with username: admin , password: password (see the data.sql file)
 - User register endpoint just creates non admin users (with role: CUSTOMER)
 - You can use the example postman collection (postmanCollection/StockManager.postman_collection.json) while calling 
   endpoints 
 - I used Basic authentication method, username and password should be passed on endpoint request headers(example 
   postman 
   collection contains it) : https://swagger.io/docs/specification/v3_0/authentication/basic-authentication/

## Improvement Points
  - Domain Driven Design could be used to have more flexible and read friendly project structure
  - order, withdraw, deposit and match endpoints could be implemented as an async endpoints with messaging platform 
    like Kafka. I used pessimistic locks to avoid concurrent modifications on assets but an async messaging protocol 
    would be beneficial to handle concurrent requests also. 
  - JWT Authentication method would be more secure instead of basic authentication
  - Password in db could be encoded in db (ex: BCryptPasswordEncoder) 

## Table of Contents

- [Requirements](#requirements)
- [Setup and Installation](#setup-and-installation)
- [Build and Run](#build-and-run)
- [Testing](#running-tests)
- [Access Embedded H2 Database](#database-management-console)
- [Project Structure](#project-structure)

## Requirements

- **Docker**: https://www.docker.com
- Gradle and Java version 17 to run locally with gradle.

## Setup and Installation

Clone the repository:
   ```bash
   git clone https://github.com/sbturan/StockManager.git
   cd stock-manager
   ```

## Build and Run

Using Docker

1. Build the application:
   ```bash
   docker build -t stock-manager . 
   ```   
2. Run the application:
   ```bash
   docker run -d --name stock-manager -p 8080:8080 stock-manager
   ```
3. Stop Application
   ```bash
   docker stop  stock-manager 
   ```

## Running Tests
Run tests to verify the functionality:
   ```bash
   ./gradlew test
   ```

## Building a JAR file
To build a standalone JAR file:
   ```bash
   ./gradlew clean build
   ```

## Database Management Console

After Running the application, to access embedded H2 db:\
Visit url: http://localhost:8080/h2-console
```
JDBC URL: jdbc:h2:mem:testdb 
USERNAME : sa 
Password : password
```




## Project Structure

- src/main/java: Contains main application code
  - controller: REST controllers for HTTP requests.
  - service: Business logic and service classes.
  - repository: Spring Data JPA repositories.
  - model: Entity models and enums.
  - dto: Data Transfer Objects (DTOs).
  - exception: Custom exception handling.
  - config: Security and application configuration.
- src/test: Contains unit and integration tests.

