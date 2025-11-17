## PerkManager

A Spring Boot web application for managing and displaying Perks — discounts or benefits associated with specific Memberships (e.g., Visa, Student ID, CAA).
The system provides a REST API, in-memory H2 database, voting functionality, membership filtering, and a simple HTML/JS frontend.

## Tech Stack

-Java 17

-Spring Boot 3.5.7

-Spring Web

-Spring Data JPA

-H2 In-Memory Database

-Maven

-HTML / CSS / JavaScript frontend

-JUnit + MockMvc testing
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

## CI Status

[![Java CI with Maven](https://github.com/AbdullahSoboh/SYSC4806_Project_Group_7/actions/workflows/maven.yml/badge.svg)](https://github.com/AbdullahSoboh/SYSC4806_Project_Group_7/actions/workflows/maven.yml)

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
- Get all perks
  GET /api/perks


Supports:

- search (title or product)

- sortBy (title, product, upvotes, score)

- direction (asc/desc)

- Create a new perk
  POST /api/perks
  Content-Type: application/json


Example body:

{
"title": "Student Discount",
"description": "15% off",
"product": "Movie Tickets",
"membership": { "id": 1 },
"location": "Ottawa",
"expiryDate": "2026-12-31"
}

- Voting
  POST /api/perks/{id}/upvote
  POST /api/perks/{id}/downvote


Votes are stored and returned immediately.

- Membership API
  GET /api/memberships
  POST /api/memberships


Used by frontend dropdowns and filtering logic.
## Notes:

id is ignored if provided (server auto-generates).

upvotes and downvotes default to 0 if missing.

cURL test:
curl -X POST http://localhost:8080/api/perks
-H "Content-Type: application/json" -d '{"title":"Student Discount","description":"15% off","product":"Movies","membership":"University","location":"Ottawa","expiryDate":"2026-12-31"}'

curl http://localhost:8080/api/perks

## Frontend

Located in src/main/resources/static/

index.html 

- Displays all perks, search bar, sorting, and forms.

app.js Handles:

- Fetching perks

- Searching

- Sorting

- Voting

- Creating perks

style.css

- Lightweight styling for consistency

Simply open http://localhost:8080/
after running the backend.

## Tests

Run all tests with:
mvn test

Includes:

PerkControllerTest → integration tests for REST endpoints

PerkTest → unit tests for the model logic

PerkManagerApplicationTests → verifies Spring context loads

## Team Contributions

Abdullah Soboh – Team Lead & Backend Features

- Led backend architecture planning and coordinated task distribution across team members.
- Implemented the Search & Sort API (Issue #47), allowing perks to be filtered and sorted by title, 
product, or popularity.
- Added Membership dropdown integration and updated the MembershipController (Issue #54).
- Implemented application monitoring with Spring Actuator, Prometheus, and Grafana (Issue #60).
- Performed ongoing PR reviews and milestone planning.

Moesa Malik – Data Model Lead & Backend Refactoring

- Implemented the full Membership entity, repository, and refactored Perk to use ManyToOne relationships 
(Issues #40, #42, #44).
- Updated all tests to support the new relational model.
- Added the DataLoader with sample memberships and perks.
- Performed extensive code reviews (#48, #55, #57, #59, #65).
- Secured monitoring configuration (PR #61) by replacing exposed credentials with environment variables.
- Contributed to the Prometheus/Grafana monitoring presentation.


Imann Brar – Business Logic & System Documentation

- Implemented the voting counter feature for Perks (Issue #41) with both upvotes and downvotes; 
adapted solution after major model refactoring.
- Created the updated database ER diagram (Issue #62).
- Updated the UML Class Diagram to match the new project structure (Issue #63).
- Rewrote and reorganized the README.md with setup instructions, architecture descriptions, and 
contribution summaries (Issue #64).
- Completed Speaker 4 responsibilities for the team’s monitoring presentation (Issue #69).

Tommy Csete – Frontend Features & UI Enhancements

- Implemented the full Voting UI with upvote/downvote buttons, vote counters, and event handlers (Issue #52).
- Fixed expiry date validation to prevent inserting expired perks (Issue #53).
- Added full Delete Perk functionality: backend DELETE endpoint + frontend delete button with confirmation flow (Issue #56).
- Developed slides and recorded presentation section on enabling Actuator (Issue #67).

Qusai Abdulrahman – Frontend Search, Filter, and UX

- Built the complete Search & Filter UI for perks (Issues #50, #51).
- Connected frontend filters to the backend via updated fetchAndRenderPerks() with event listeners for live updates.
- Ensured sorting (including popularity) works correctly end-to-end.


## Project Structure

src/
├─ main/java/ca/carleton/s4806/perkmanager/
│  ├─ PerkManagerApplication.java
│  ├─ DataLoader.java
│  ├─ controller/
│  │   ├─ PerkController.java
│  │   └─ MembershipController.java
│  ├─ model/
│  │   ├─ Perk.java
│  │   └─ Membership.java
│  └─ repository/
│      ├─ PerkRepository.java
│      └─ MembershipRepository.java
│
├─ main/resources/
│  ├─ application.properties
│  └─ static/
│      ├─ index.html
│      ├─ style.css
│      └─ app.js
│
└─ test/java/ca/carleton/s4806/perkmanager/
├─ controller/PerkControllerTest.java
├─ model/PerkTest.java
└─ PerkManagerApplicationTests.java

docker-compose.yml  
prometheus.yml  
alertmanager.yml  
alert.rules.yml  
Database.png  
UML.png  
README.md  
pom.xml