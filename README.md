## PerkManager

A Spring Boot web application for managing and displaying “perks”; discounts or benefits associated with memberships and products.
Includes a RESTful API, an H2 in-memory database, and a simple HTML/JS frontend.

## Tech Stack

- Java 17

- Spring Boot 3.5.7 (Web + JPA)

- H2 Database (in-memory)

- Maven

- HTML, CSS, JavaScript frontend

## Quick Start

Clone and run:
mvn spring-boot:run

The backend will start at:
http://localhost:8080

To serve the frontend automatically, place the following files inside:
src/main/resources/static/

- index.html

- style.css

- app.js

Then open http://localhost:8080/
in your browser.

## Configuration

Located in src/main/resources/application.properties

spring.application.name=PerkManager
spring.datasource.url=jdbc:h2:mem:perkdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

Access the H2 console at:
http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:perkdb

## REST API Overview
Get all perks

GET /api/perks
Accept: application/json
Response: 200 OK → [] or an array of Perk objects.

Create a new perk

POST /api/perks
Content-Type: application/json

Example body:
{
"title": "Student Discount",
"description": "15% off with student ID",
"product": "Movie Tickets",
"membership": "University",
"location": "Ottawa, ON",
"expiryDate": "2026-12-31"
}

## Notes:

id is ignored if provided (server auto-generates).

upvotes and downvotes default to 0 if missing.

cURL test:
curl -X POST http://localhost:8080/api/perks
-H "Content-Type: application/json" -d '{"title":"Student Discount","description":"15% off","product":"Movies","membership":"University","location":"Ottawa","expiryDate":"2026-12-31"}'

curl http://localhost:8080/api/perks

## Frontend

index.html – Form to add a new perk and view the list

app.js – Fetches and posts data to /api/perks

style.css – Basic layout and styling

Simply open http://localhost:8080/
after running the backend.

## Tests

Run all tests with:
mvn test

Includes:

PerkControllerTest → integration tests for REST endpoints

PerkTest → unit tests for the model logic

PerkManagerApplicationTests → verifies Spring context loads

## Project Structure

src/
├─ main/java/ca/carleton/s4806/perkmanager/
│ ├─ PerkManagerApplication.java
│ ├─ controller/PerkController.java
│ ├─ model/Perk.java
│ └─ repository/PerkRepository.java
├─ test/java/ca/carleton/s4806/perkmanager/
│ ├─ controller/PerkControllerTest.java
│ ├─ model/PerkTest.java
│ └─ PerkManagerApplicationTests.java
└─ main/resources/
├─ application.properties
└─ static/
├─ index.html
├─ style.css
└─ app.js
pom.xml