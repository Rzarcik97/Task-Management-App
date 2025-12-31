## 📊 Demo Data (Liquibase)

This document visualizes the database schema created by Liquibase changelogs,
and presents **Demo data**.

---

## 👤 **Table:** `users`

**Demo data:**

| id | username   | email                  | password             | first_name | last_name     | role  | is_deleted |
|----|------------|------------------------|----------------------|------------|---------------|-------|------------|
| 1  | admin      | admin@taskmanager.com  | (encoded "Admin123") | System     | Administrator | ADMIN | false      |
| 2  | john_doe   | john.doe@example.com   | (encoded "User123")  | John       | Doe           | USER  | false      |
| 3  | jane_smith | jane.smith@example.com | (encoded "User321")  | Jane       | Smith         | USER  | false      |
| 4  | Anna smith | anna@example.com       | (encoded "User123")  | Anna       | Smith         | USER  | false      |
---

## 📁 **Table:** `projects`

**Demo data:**

| id | name                   | description                                                                 | start_date | end_date   | status      |
|----|------------------------|-----------------------------------------------------------------------------|------------|------------|-------------|
| 1  | Task Management System | System for managing tasks and teams with labels, attachments, and comments. | 2025-01-01 | 2025-06-30 | IN_PROGRESS |
| 2  | E-commerce Platform    | Online store project with catalog, cart, and order management.              | 2024-10-01 | 2025-03-31 | INITIATED   |
| 3  | Mobile Banking App     | Cross-platform app for personal banking and transactions.                   | 2024-06-15 | NULL       | COMPLETED   |
---

## ✅ **Table:** `tasks`

**Demo data:**

| id | name                           | description                                                                            | due_date   | project_id | assignee_id | priority | status      |
|----|--------------------------------|----------------------------------------------------------------------------------------|------------|------------|-------------|----------|-------------|
| 1  | Design API Endpoints           | Create REST API for task management including authentication and task CRUD operations. | 2025-11-20 | 1          | 1           | HIGH     | IN_PROGRESS |
| 2  | Implement Notification Service | Develop email and reminder notification system.                                        | 2025-11-25 | 1          | 2           | MEDIUM   | NOT_STARTED |
| 3  | Prepare Project Documentation  | Write user guide and developer documentation.                                          | NULL       | 2          | 3           | LOW      | IN_PROGRESS |
---

## 🏷 **Table:** `labels`

**Demo data:**

| id | name     | color   |
|----|----------|---------|
| 1  | Backend  | #3498db |
| 2  | Frontend | #e67e22 |
| 3  | Bug      | #e74c3c |

---

## 🔗 **Table:** `task_labels` (Many-to-Many)

**Demo data:**

| task_id | label_id |
|--------:|---------:|
|       1 |        1 |
|       1 |        2 |
|       2 |        3 |

---

## 📎 **Table:** `attachments`|

**Demo data:**

| id | task_id | dropbox_file_id | filename       | path                    | upload_date         | uploaded_by |
|----|--------:|-----------------|----------------|-------------------------|---------------------|-------------|
| 1  |       1 | dbx_001         | design.pdf     | /docs/project-plan.pdf  | 2025-01-01T10:00:00 | 1           |
| 2  |       2 | dbx_002         | bug-report.txt | /reports/bug-report.txt | 2025-01-03T15:30:00 | 2           |
| 3  |       3 | dbx_003         | ui-design.png  | /design/ui-design.png   | 2025-01-05T09:15:00 | 3           |

---

## 💬 **Table:** `comments`

**Demo data:**

| id | task_id | user_id | text                                                    | timestamp           |
|----|--------:|--------:|---------------------------------------------------------|---------------------|
| 1  |       1 |       1 | Initial task setup completed successfully.              | 2025-01-02T09:45:00 |
| 2  |       2 |       2 | Found a potential issue with the data validation logic. | 2025-01-04T11:30:00 |
| 3  |       3 |       3 | Design assets uploaded for review.                      | 2025-01-06T16:10:00 |

---

## 👥 **Table:** `project_members`

**Demo data:**

| id | project_id | user_id | role    |
|----|------------|---------|---------|
| 1  | 1          | 1       | MANAGER |
| 2  | 1          | 2       | MEMBER  |
| 3  | 2          | 3       | VIEWER  |
| 4  | 2          | 2       | MANAGER |

---

## 🔐 **Table:** `verification_tokens`

**Demo data:**

| id | user_id | verification_code  | new_value             | expiration_time                | type            |
|----|--------:|--------------------|-----------------------|--------------------------------|-----------------|
| 1  |       1 | (encoded "123456") | (encoded "Admin1234") | (CURRENT_TIMESTAMP() + 20 min) | PASSWORD_CHANGE |
| 2  |       2 | (encoded "654321") | newemail@example.com  | (CURRENT_TIMESTAMP() + 20 min) | EMAIL_CHANGE    |

---
