## 📊 Database Structure & Example Data (Liquibase)

This document visualizes the database schema created by Liquibase changelogs  
and presents **example rows** to help understand table structure and relations.

---

## 👤 **Table:** `users`

| Column     | Type         | Description      |
|------------|--------------|------------------|
| id         | BIGINT (PK)  | User identifier  |
| username   | VARCHAR(255) | Unique username  |
| password   | VARCHAR(255) | BCrypt hash      |
| email      | VARCHAR(255) | Unique email     |
| first_name | VARCHAR(255) | First name       |
| last_name  | VARCHAR(255) | Last name        |
| role       | VARCHAR(50)  | USER / ADMIN     |
| is_deleted | BIT(1)       | Soft delete flag |

**Example data:**

| id | username | email                 | first_name | last_name     | role  | is_deleted |
|----|----------|-----------------------|------------|---------------|-------|------------|
| 1  | admin    | admin@taskmanager.com | System     | Administrator | ADMIN | false      |

---

## 📁 **Table:** `projects`

| Column      | Type         | Description          |
|-------------|--------------|----------------------|
| id          | BIGINT (PK)  | Project ID           |
| name        | VARCHAR(255) | Unique project name  |
| description | CLOB         | Optional description |
| start_date  | DATE         | Project start        |
| end_date    | DATE         | Project end          |
| status      | VARCHAR(50)  | Project status       |

**Example data:**

| id | name           | description       | start_date | end_date   | status     |
|----|----------------|-------------------|------------|------------|------------|
| 1  | Task Manager   | Demo project      | 2025-01-01 | 2025-06-01 | ACTIVE     |

---

## ✅ **Table:** `tasks`

| Column      | Type         | Description               |
|-------------|--------------|---------------------------|
| id          | BIGINT (PK)  | Task ID                   |
| name        | VARCHAR(255) | Task name                 |
| description | CLOB         | Optional                  |
| due_date    | DATE         | Deadline                  |
| project_id  | BIGINT (FK)  | Project                   |
| assignee_id | BIGINT (FK)  | Assigned user             |
| priority    | VARCHAR(50)  | LOW / MEDIUM / HIGH       |
| status      | VARCHAR(50)  | TODO / IN_PROGRESS / DONE |

**Example data:**

| id | name           | project_id | assignee_id | priority | status |
|----|----------------|------------|-------------|----------|--------|
| 1  | Create backend | 1          | 1           | HIGH     | TODO   |

---

## 🏷 **Table:** `labels`

| Column | Type         | Description       |
|--------|--------------|-------------------|
| id     | BIGINT (PK)  | Label ID          |
| name   | VARCHAR(255) | Unique label      |
| color  | VARCHAR(50)  | Color name or hex |

**Example data:**

| id | name     | color |
|----|----------|-------|
| 1  | Backend  | blue  |
| 2  | Urgent   | red   |

---

## 🔗 **Table:** `task_labels` (Many-to-Many)

| Column   | Type   | Description |
|----------|--------|-------------|
| task_id  | BIGINT | FK → tasks  |
| label_id | BIGINT | FK → labels |

**Primary Key:** `(task_id, label_id)`

**Example data:**

| task_id | label_id |
|--------:|---------:|
|       1 |        1 |
|       1 |        2 |

---

## 📎 **Table:** `attachments`

| Column          | Type         | Description        |
|-----------------|--------------|--------------------|
| id              | BIGINT (PK)  | Attachment ID      |
| task_id         | BIGINT (FK)  | Task               |
| dropbox_file_id | VARCHAR(255) | Dropbox identifier |
| filename        | VARCHAR(255) | Original filename  |
| path            | VARCHAR(255) | Storage path       |
| upload_date     | TIMESTAMP    | Upload time        |
| uploaded_by     | BIGINT (FK)  | User               |

**Example data:**

| id | task_id | filename   | uploaded_by |
|----|--------:|------------|-------------|
| 1  |       1 | design.pdf | 1           |

---

## 💬 **Table:** `comments`

| Column    | Type        | Description |
|----------|-------------|-------------|
| id       | BIGINT (PK) | Comment ID |
| task_id  | BIGINT (FK) | Task |
| user_id  | BIGINT (FK) | Author |
| text     | TINYTEXT    | Comment |
| timestamp| TIMESTAMP   | Creation time |

**Example data:**

| id | task_id | user_id | text                    |
|----|--------:|--------:|-------------------------|
| 1  |       1 |       1 | Initial backend setup   |

---

## 👥 **Table:** `project_members`

| Column     | Type        | Description      |
|------------|-------------|------------------|
| id         | BIGINT (PK) | Entry ID         |
| project_id | BIGINT (FK) | Project          |
| user_id    | BIGINT (FK) | User             |
| role       | VARCHAR(50) | MEMBER / MANAGER |

**Example data:**

| id | project_id | user_id | role     |
|----|------------|---------|----------|
| 1  | 1          | 1       | MANAGER  |

---

## 🔐 **Table:** `verification_tokens`

| Column            | Type         | Description        |
|-------------------|--------------|--------------------|
| id                | BIGINT (PK)  | Token ID           |
| user_id           | BIGINT (FK)  | User               |
| verification_code | VARCHAR(255) | Code               |
| new_value         | VARCHAR(255) | New email/password |
| expiration_time   | TIMESTAMP    | Expiration         |
| type              | VARCHAR(50)  | Token type         |

**Example data:**

| id | user_id | verification_code | type             |
|----|--------:|-------------------|------------------|
| 1  |       1 | 123456            | PASSWORD_CHANGE  |

---

## 🔗 Entity Relationships Overview
````
    User
    ├── ProjectMember ── Project ── Task ──┬── Comment
    └── VerificationToken                  ├── Attachment
                                           └── Label
    
````

