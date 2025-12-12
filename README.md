# ⭐ Task Manager – Spring Boot Application

Task Manager is a robust task and project management backend built with **Spring Boot**.  
It supports:

- Project and task management
- User roles (USER / ADMIN)
- Project roles (MANAGER / MEMBER / VIEWER)
- Comments, labels, attachments (Dropbox)
- JWT authentication
- Email/password change with verification flow
- Task reminders with email sender
- Full CI pipeline with GitHub Actions

---

# 📚 Table of Contents

1. [Technologies](#technologies-used)
2. [Architecture](#system-architecture-overview)
3. [Features](#features--api-overview)
4. [How to Run](#how-to-run)
5. [Database & Liquibase](#database--liquibase)
6. [Testing](#testing)
7. [CI/CD](#cicd)


---

# <h1 id="technologies-used">🛠 Technologies Used</h1>

| Technology           | Description                      |
|----------------------|----------------------------------|
| Java 17              | Core language                    |
| Spring Boot 3.4.4    | Main framework                   |
| Spring Security      | Authentication & authorization   |
| Spring Data JPA      | Data persistence                 |
| JWT (jjwt)           | Token-based authentication       |
| MapStruct            | DTO mapping                      |
| Liquibase            | DB schema management             |
| Hibernate Validator  | Input validation                 |
| Testcontainers       | Integration tests with Docker    |
| MySQL / H2           | Databases (prod/test)            |
| Swagger (springdoc)  | API documentation                |
| Lombok               | Less boilerplate                 |
| Checkstyle           | Code quality                     |
| Log4J                | Logger                           |

# <h1 id="system-architecture-overview">🏗️ System Architecture Overview</h1>


The system is built as a layered Spring Boot backend with a role–based security model.
User permissions exist on two levels:

1. **Global Roles** – assigned to the user account
    - `ADMIN` – full system access
    - `USER` – standard user with access only to their own data

2. **Project Roles** – assigned per project
    - `VIEWER` – read–only access
    - `MEMBER` – can update tasks assigned to them
    - `MANAGER` – manages the entire project (tasks, members, labels)

Below is the architecture flow:
````
                        ┌──────────────────────────────┐
                        │          Client Side         │
                        │  (Frontend / Postman / API)  │
                        └──────────────┬───────────────┘
                                       │ HTTP Requests
                                       ▼
                        ┌──────────────────────────────┐
                        │      Authentication Layer    │
                        │    Spring Security + JWT     │
                        ├──────────────────────────────┤
                        │  Global Roles:               │
                        │    • ADMIN                   │
                        │    • USER                    │
                        └──────────────┬───────────────┘
                                       │ Authenticated Principal
                                       ▼
               ┌─────────────────────────────────────────────────┐
               │                 REST Controllers                │
               │  /projects, /tasks, /labels, /users, /auth      │
               └───────────────────────┬─────────────────────────┘
                                       │ Pass validated data
                                       ▼
                     ┌─────────────────────────────────┐
                     │            Service Layer        │
                     │ Business logic + role checks    │
                     │                                 │
                     │ • Global role checks:           │
                     │     ADMIN vs USER               │
                     │                                 │
                     │ • Project role checks:          │
                     │     VIEWER / MEMBER / MANAGER   │
                     └──────────────┬──────────────────┘
                                    │ Repository Access
                                    ▼
                     ┌─────────────────────────────────┐
                     │         Persistence Layer       │
                     │   JPA Repositories + Entities   │
                     └──────────────┬──────────────────┘
                                    │
                                    ▼
                     ┌─────────────────────────────────┐
                     │              Database           │
                     │   Projects / Tasks / Users      │
                     │   Labels / Members / Tokens     │
                     └─────────────────────────────────┘
````

---

# <h1 id="features--api-overview">📘 Features & API Overview</h1>

## 👤 User Permissions (ROLE_USER)

### 🔐 Authentication & Profile

| Action                       | Method | Endpoint                    |
|------------------------------|--------|-----------------------------|
| Register                     | POST   | `/auth/register`            |
| Log in                       | POST   | `/auth/login`               |
| Get my profile               | GET    | `/users/me`                 |
| Update my profile            | PATCH  | `/users/me`                 |
| Change email                 | PATCH  | `/users/me/change-email`    |
| Change password              | PATCH  | `/users/me/change-password` |
| Verify email/password change | POST   | `/users/verify`             |

### 🏷 Label

| Action         | Method | Endpoint  |
|----------------|--------|-----------|
| Get all labels | GET    | `/labels` |

---

## 👤 User Permissions (ROLE_VIEWER)


### 📁 Projects

| Action                   | Method | Endpoint         |
|--------------------------|--------|------------------|
| Get projects I belong to | GET    | `/projects`      |
| Get project by ID        | GET    | `/projects/{id}` |

---

### 📋 Tasks

| Action                    | Method | Endpoint                |
|---------------------------|--------|-------------------------|
| Get tasks for project     | GET    | `/tasks?projectId={id}` |
| Get task by ID            | GET    | `/tasks/{id}`           |
| Update task (if assigned) | PATCH  | `/tasks/{taskId}`       |

---

### 💬 Comments

| Action                | Method | Endpoint             |
|-----------------------|--------|----------------------|
| Add comment           | POST   | `/comments`          |
| Get comments for task | GET    | `/comments/{taskId}` |
| Update my comment     | PUT    | `/comments/{id}`     |
| Delete my comment     | DELETE | `/comments/{id}`     |

---

### 🗃️Attachment

| Action              | Method | Endpoint                               |
|---------------------|--------|----------------------------------------|
| Get Attachment      | GET    | `/attachments/{taskId}`                |
| Download Attachment | GET    | `/attachments/{attachmentId}/download` |

---
## 👤 User Permissions (ROLE_MEMBER) additional capabilities

### 📋 Tasks

| Action                    | Method | Endpoint          |
|---------------------------|--------|-------------------|
| Update task (if assigned) | PATCH  | `/tasks/{taskId}` |

---

### 🗃️Attachment

| Action                          | Method | Endpoint                               |
|---------------------------------|--------|----------------------------------------|
| Upload Attachment (if assigned) | POST   | `/attachments/{taskId}`                |
| Delete Attachment (if assigned) | DELETE | `/attachments/{attachmentId}/download` |

---

## 🛠 Project Manager (ROLE_MANAGER) additional capabilities

### 📁 Projects

| Action                     | Method | Endpoint                       |
|----------------------------|--------|--------------------------------|
| Add member to project      | POST   | `/projects/{projectId}/member` |
| Remove member from project | DELETE | `/projects/{projectId}/member` |
| Update projects            | PATCH  | `/projects/{projectId}`        |
| Delete projects            | DELETE | `/projects/{projectId}`        |
---

### 📋 Tasks

| Action          | Method | Endpoint          |
|-----------------|--------|-------------------|
| Create task     | POST   | `/tasks`          |
| Update all task | PATCH  | `/tasks/{taskId}` |
| Delete task     | DELETE | `/tasks/{taskId}` |

---

### 🗃️Attachment

| Action            | Method | Endpoint                               |
|-------------------|--------|----------------------------------------|
| Upload Attachment | POST   | `/attachments/{taskId}`                |
| Delete Attachment | DELETE | `/attachments/{attachmentId}/download` |

## 🔑 Administrator Permissions (ROLE_ADMIN) additional capabilities

### 👥 User Management

| Action           | Method | Endpoint               |
|------------------|--------|------------------------|
| Change user role | PUT    | `/users/{userId}/role` |

---

### 📁 Project Management

| Action         | Method | Endpoint    |
|----------------|--------|-------------|
| Create project | POST   | `/projects` |

---

### 🏷 Label Management

| Action         | Method | Endpoint            |
|----------------|--------|---------------------|
| Create label   | POST   | `/labels`           |
| Update label   | PATCH  | `/labels/{id}`      |
| Delete label   | DELETE | `/labels/{labelId}` |
---

# <h1 id="how-to-run">🚀 How to Run</h1>


## ✅ Prerequisites

- Java 17+

- Maven

- MySQL (Docker Container)

**Clone the repository:**

	git clone https://github.com/Rzarcik97/Task-Management-App.git

    cd Task-Management-App


## 🔐 Environment Variables (.env)

The application uses environment variables to securely store sensitive configuration such as database credentials, JWT keys, mail settings, and API tokens.
This prevents leaking secrets into GitHub and allows easy configuration per environment (local, test, production).

Create a file named .env in the project root, copy the contents of the file .env.example into created .env

```bash
cp .env.example .env
```
Then edit `.env` with your actual configuration values.


##  Run application

By default, the project uses MySQL for production and Test container for tests.
For test application need installed docker

**Run application at docker:**

```bash
docker compose up
```

This will start:
- MySQL database
- Spring Boot application
- MailHog (email testing)

**Access the application at:**
- API: `http://localhost:8080`
- MailHog UI: `http://localhost:8025`

**or run locally with Maven use command:**

Prerequisites: Docker must be running (for MySQL and MailHog)

Start only the database and mail server:
```bash
docker compose up mysqldb mailhog -d
```


build project

```bash
mvn clean package
```
Run the application:

```bash
mvn spring-boot:run
```


**Access the API at**  ```http://localhost:8080```

# <h1 id="database--liquibase">🗄 Database & Liquibase</h1>


Liquibase automatically:
- creates tables
- sets relationships
- create Admin user to manage application
- inserts seed/test data (users, tasks, projects, tokens)

### 👥 Default Users

| Role  | Email             | Password   |
|-------|-------------------|------------|
| ADMIN | `admin@taskmanager.com` | `Admin123` |

Files are located in:
```
src/main/resources/db/changelog/
src/test/resources/db/changelog/
```
---
# <h1 id="testing">🧪 Testing</h1>


The project includes:

### ✔ Unit tests (Mockito)
- service logic
- error handling
- token verification

### ✔ Integration tests (MockMvc + Testcontainers)
- full REST API
- security checks
- validation rules
- repository integration

Run all tests:
```bash
mvn test
```

---

# <h1 id="cicd">🚀 CI/CD</h1>

The project includes a full GitHub Actions pipeline that automatically:
- checks out the project
- installs JDK 17
- runs full test suite
- builds the JAR artifact

### `.github/workflows/ci.yml`
```yaml
name: Java CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'adopt'
      - run: mvn -B verify
```

Artifacts can be downloaded directly from GitHub Actions.

---
