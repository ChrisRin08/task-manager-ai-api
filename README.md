# task-manager-ai-api

## 1. Project Overview
`task-manager-ai-api` is a Java 17 Spring Boot REST API for managing personal tasks.  
It was built for a Backend Engineering Intern take-home assessment, with focus on clean layering, testability, and practical AI integration.

## 2. Features
- Task CRUD API:
  - `POST /tasks`
  - `GET /tasks`
  - `GET /tasks/{id}`
  - `PUT /tasks/{id}`
  - `DELETE /tasks/{id}`
- AI-powered endpoints:
  - `POST /tasks/suggest`
  - `POST /tasks/{id}/summarize`
  - `POST /tasks/{id}/breakdown`
- Global exception handling with consistent JSON error responses
- Simple browser UI for CRUD + AI actions at `http://localhost:8081`
- H2 in-memory database and H2 console

## 3. Tech Stack
- Java 17
- Spring Boot
- Maven (Wrapper)
- Spring Web
- Spring Data JPA
- H2 in-memory database
- Bean Validation
- JUnit 5
- Mockito
- MockMvc
- Plain HTML/CSS/JavaScript frontend
- Google Gemini API integration (optional at runtime)

## 4. Project Structure
```text
src/
  main/
    java/com/eulerity/task_manager/
      controller/
      dto/
      exception/
      model/
      repository/
      service/
    resources/
      application.properties
      static/index.html
  test/
    java/com/eulerity/task_manager/
      controller/
      service/
```

## 5. Requirements
- Java 17 installed
- Internet only needed if you want real Gemini responses
- No API key is required for local fallback mode

## 6. How to Run the Project
```bash
git clone <your-repo-url>
cd task-manager-ai-api
./mvnw spring-boot:run
```

App URL: `http://localhost:8081`

## 7. Optional Gemini Setup
No real API key is committed in this repository.

To enable real Gemini responses:
```bash
export GEMINI_API_KEY="your_gemini_api_key_here"
export GEMINI_MODEL="gemini-2.5-flash-lite"
./mvnw spring-boot:run
```

Notes:
- `GEMINI_MODEL` is optional.
- If `GEMINI_API_KEY` is missing or Gemini fails (invalid key, quota, provider error, invalid AI response), the app automatically falls back to `LocalFallbackAiClient`.

## 8. How to Run Tests
```bash
./mvnw test
```

## 9. H2 Database Console
- URL: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:taskdb`
- Username: `sa`
- Password: *(leave blank)*

## 10. API Endpoints
| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/tasks` | Create task |
| GET | `/tasks` | List all tasks |
| GET | `/tasks/{id}` | Get single task |
| PUT | `/tasks/{id}` | Update task |
| DELETE | `/tasks/{id}` | Delete task |
| POST | `/tasks/suggest` | AI task suggestion from prompt |
| POST | `/tasks/{id}/summarize` | AI summary of existing task |
| POST | `/tasks/{id}/breakdown` | AI subtask breakdown of existing task |

## 11. Task CRUD Examples
### Create task
```bash
curl -X POST http://localhost:8081/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Finish internship project",
    "description": "Complete the backend API, tests, AI integration, and README",
    "dueDate": "2026-05-01",
    "priority": "HIGH",
    "status": "TODO"
  }'
```

### Get all tasks
```bash
curl http://localhost:8081/tasks
```

### Get one task
```bash
curl http://localhost:8081/tasks/1
```

### Update task
```bash
curl -X PUT http://localhost:8081/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Finish internship project (updated)",
    "description": "Finalize and submit",
    "dueDate": "2026-05-02",
    "priority": "HIGH",
    "status": "IN_PROGRESS"
  }'
```

### Delete task
```bash
curl -X DELETE http://localhost:8081/tasks/1
```

## 12. AI Endpoint Examples
### Suggest task from prompt
```bash
curl -X POST http://localhost:8081/tasks/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "remind me to submit my internship project before Friday"
  }'
```

Example response:
```json
{
  "title": "Submit internship project",
  "description": "Prepare and submit the internship project before Friday.",
  "dueDate": "2026-05-01",
  "priority": "HIGH",
  "status": "TODO"
}
```

### Summarize saved task
```bash
curl -X POST http://localhost:8081/tasks/1/summarize
```

### Break down saved task
```bash
curl -X POST http://localhost:8081/tasks/1/breakdown
```

## 13. Simple UI
Open: `http://localhost:8081`

The UI supports:
- Viewing all tasks
- Creating a task
- Deleting a task
- AI task suggestion from prompt
- AI summarize by task ID
- AI breakdown by task ID

## 14. Testing Summary
- `TaskServiceTest` (unit tests, Mockito, no full Spring context)
  - Happy paths for:
    - `createTask`
    - `getAllTasks`
    - `getTaskById`
    - `updateTask`
    - `deleteTask`
- `TaskControllerIntegrationTest` (Spring context + MockMvc)
  - End-to-end coverage for all CRUD endpoints
- `AiTaskControllerTest`
  - AI endpoint behavior with mocked service
  - No real Gemini call in controller tests
- `GeminiAiClientTest`
  - Validates fallback behavior without requiring real provider access

## 15. AI Design Notes
- AI behavior is isolated behind `AiClient` for clean abstraction and testability.
- `GeminiAiClient` is used when configured and provider calls succeed.
- `LocalFallbackAiClient` keeps the app runnable without secrets or external AI availability.
- AI responses are stateless and **not persisted** automatically.
  - Data is saved only when calling `POST /tasks`.
- If AI returns a past `dueDate` for task suggestion, it is normalized to `null`.

## 16. Future Improvements
- Add pagination and filtering for large task lists
- Add sorting options (due date, priority, status)
- Add authentication/authorization
- Add CI workflow and code quality gates
- Add containerization for deployment environments
