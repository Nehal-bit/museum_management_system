# Museum Management System

A full-stack web application built using **Spring Boot** to manage museum operations such as exhibits, bookings, ticketing, guide assignments, feedback, notifications, and analytics.

This project follows the **MVC (Model-View-Controller)** architecture and demonstrates a structured backend system with REST APIs.

---

## Features

### Admin
- Manage exhibits (add, update, delete)
- Approve / reject bookings
- Assign guides to bookings
- View reports and analytics

### Visitor
- Browse exhibits
- Book museum visits
- Cancel bookings
- Submit feedback
- Receive notifications

### Guide
- View assigned bookings
- Accept / reject assignments
- View schedule

---

## Tech Stack

- **Backend:** Spring Boot (Java 17)
- **Database:** MySQL
- **Build Tool:** Maven
- **Architecture:** MVC (REST API)
- **Frontend:** HTML + JavaScript

---

## ⚙️ How to Run

### 1. Clone the Repository
```bash
git clone https://github.com/Nehal-bit/museum_management_system.git
cd museum_management_system
```

### 2. Create MySQL Database
```sql
CREATE DATABASE museum_db;
```

### 3. Configure Database

Edit the file:

```
src/main/resources/application.properties
```

Update with:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/museum_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

OR run the main class:
```
MuseumAppApplication.java
```

### 6. Open in Browser
```
http://localhost:8080
```

### 7. Default Admin Login
```
Email: admin@museum.com  
Password: admin123
```
## Notes
- Developed as part of an **OOAD Mini Project**
- Focuses on **clean architecture** and **modular backend design**

## Authors
- **Nehal G**
- **Nevin Mathew Thomas**
- **Pinisetti Sudhiksha**
