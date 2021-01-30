# Ecommerce Order Processing

## Problem Definition
Migrate monolithic ecommerce platform from legacy database engine to modern database engine and extend schema and implment enhacements

## Requirements

1. Use Java8 (or higher version of Java), or Kotlin for the programming language.
2. Spring Boot, Spring Data JPA, and other Spring modules for the microservices.
3. Design new schema keeping PostgreSQL in the mind.
4. For the RESTful API, use Swagger to document your endpoints. Use appropriate HTTP verbs and status codes. Assume no AuthN or AuthZ is required for the API.
5. Implement unit and/or integration tests.
6. Containerize your services using Docker. Use docker-compose if required.
7. If you are thinking about async communication patterns, use Apache Kafka.
8. See, if you can follow few of the 12 Factor App Principles (https://12factor.net).

## Pre-requisites
1. Install java 1.8, apache maven
2. Install any IDE of your choice to view the entire codebase supported by Java
3. Install Docker Desktop `https://www.docker.com/products/docker-desktop`
4. Install any API testing tool, for instance Postman

## Steps to run
1. To build and install java application `mvn install -DskipTests`
2. Build docker using `docker build . --file Dockerfile --tag <docker-username/application-name>`
3. Run the docker compose file using `docker-compose up -d`
4. To check is services are up and running run `docker ps` and in Postman run the `/apiTestCheck`

## Assumptions
1. No AuthN or AuthZ is required for the API
2. The application is accessed by valid user and registration and sign-in is all taken care of

## Technologies and Features used
1. Spring Boot for building java application
2. Docker for containerization
3. Swagger for API documentation
4. Mockito and JUnit for Unit testing
5. Prometheus, Grafana, Spring Boot logger for loggin, telemetry and monitoring
6. Apache Kafka for asynchronous communication pattern and Kafkadrop for viewing topics
7. PostgreSQL for presisting data
8. Git for source code version control
9. Postman for API testing
10. Zookeeper for Centralized service for maintaining configuration information and Kafka Cluster

## API URL's
1. Swagger documentation: Hit url `http://localhost:8080/swagger-ui/` then click on an api spec and 'Try it out' button
2. Application: `http://localhost:8080/api/v1/orders`
3. Grafana: `http://localhost:3000`
