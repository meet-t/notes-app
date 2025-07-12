# 🗝️ Secret Notes - Kotlin Spring Boot Application

Note-taking backend application built with **Kotlin + Spring Boot**, supporting JWT-based user authentication and per-user rate limiting

---

## 🏗️ Architectural Decisions

### Database: H2 for current development, PostgreSQL for Production

For this assignment, I have used **H2 in-memory database** for simplicity and to avoid external dependencies during development and testing.

In a production setting, would prefer **PostgreSQL** due to relational capabilities, transactional integrity, and 
future extensibility (e.g., storing note metadata in JSONB columns)

### Authentication: JWT-Based Auth

JWTs are issued during login and validated on each secured API call. The authenticated user's ID is extracted and injected into the request context for downstream access control.
The implemented approach is not secure enough for production use, as it lacks proper token expiration and refresh mechanisms. 


### Rate Limiting: In-Memory via Bucket4J

For this assignment, have used **Bucket4J with in-memory storage** to enforce per-user rate limiting policies. It's environment-profile driven:
- `dev/test`: in-memory buckets
- `prod`: Redis-backed implementation (pluggable via interface, not implemented here)

Rate limits are applied at a per-user and per-endpoint level (e.g., stricter for `/latest` endpoint). Strategy pattern and path-based resolution ensure flexibility.

### Caching: Caffeine

To improve performance, have used **Caffeine Cache** for caching frequently accessed data like:
- `/api/v1/notes/latest` — retrieves the latest 1000 notes for a user

---

### 🔧 Components

- `AuthService` – handles user registration/login and JWT token issuing
- `NoteService` – business logic for note CRUD
- `RateLimiter` – abstract strategy for request limiting (in-memory now, Redis for prod)
- `CacheConfig` – uses Spring Cache abstraction with Caffeine
- `NoteController` – secured CRUD endpoints with request validation and expiry filtering

## 🗄️ Data Model

### User
| Field     | Type      | Description        |
|-----------|-----------|--------------------|
| id        | UUID      | Primary key        |
| name      | String    | Display name       |
| email     | String    | Unique email       |
| password  | String    | Encrypted password |
| createdOn | Timestamp | Created timestamp  |
| updatedOn  | Timestamp | Last update timestamp|

### Note
| Field      | Type      | Description                     |
|------------|-----------|---------------------------------|
| id         | UUID      | Primary key                     |
| userId     | UUID      | FK to User                      |
| title      | String    | Note title                      |
| content    | String    | Note body                       |
| createdOn  | Timestamp | When note was created           |
| updatedOn  | Timestamp | Last update timestamp           |
| expiresAt  | Timestamp | When note should expire (soft)  |

## 🧑‍💻 Technologies Used

- **Kotlin** + **Spring Boot**
- **JWT** for authentication
- **H2** for persistence
- **JPA/Hibernate**
- **Caffeine** for in-memory caching
- **Gradle Kotlin DSL**
- **JUnit** + **MockK** for testing

---

## 📦 Getting Started

### Prerequisites

- Java 17+
- Gradle

### 🛠️ Run Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/meet-t/notes-app.git
   cd notes-app
   ```


2.Run the app:
   ```bash
   ./gradlew bootRun
   ```

3. Swagger UI (if enabled):
   ```
   http://localhost:8080/swagger-ui.html
   ```
4. DataLoaderService class
    - This class is used to 1000 records data into the Notes table, you can modify the field **numberOfNotes**
    - You can modify the **data.sql** file in the **src/main/resources** directory to add your own test data.
   
---
## Postman Collection

I've added a Postman collection that you can import to easily test the API endpoints.

**Step 1**: Download the collection from the following link:

- [Download Postman Collection (JSON)](https://github.com/meet-t/notes-app/blob/825d4c6c918582735d432f222fca1b5432346d6a/postman/Noter%20App%20API.postman_collection.json)

**Step 2**: Import the collection into Postman:
1. Open Postman.
2. Click on **Import** in the top-left corner.
3. Select **Upload Files** and choose the `.json` file you downloaded.
4. The collection will now appear in your Postman app, and you can start testing the endpoints.

## 📚 API Endpoints

### 🔐 Auth

| Method | Endpoint              | Description         |
|--------|------------------------|---------------------|
| POST   | `/api/v1/auth/register` | Register new user   |
| POST   | `/api/v1/auth/login`    | Login + get JWT     |

### 🗒️ Notes

| Method | Endpoint                  | Description                  |
|--------|----------------------------|------------------------------|
| POST   | `/api/v1/notes`            | Create a note                |
| GET    | `/api/v1/notes/{id}`       | Get note by ID               |
| PUT    | `/api/v1/notes/{id}`       | Update note by ID            |
| DELETE | `/api/v1/notes/{id}`       | Delete note by ID            |
| GET    | `/api/v1/notes/latest`     | Get latest 1000 valid notes  |

---

## ✅ Testing

Run unit tests with:

```bash
./gradlew test
```

---


