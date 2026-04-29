# task-manager-ai-api

## 1. Project Overview
`task-manager-ai-api` is a Java 17 Spring Boot REST API for a personal task manager.

Primary take-home goal: a reviewer should be able to clone the repo, run one command, and have the API running locally.

## 2. Quick Start
### Prerequisites
- Java 17
- Git

### Clone repo
```bash
git clone https://github.com/christianrincon/task-manager-ai-api
cd task-manager-ai-api
```

### Run with one command
```bash
./mvnw spring-boot:run
```

### Optional Gemini setup
No real API key is committed to this repository.

To enable real Gemini responses:
```bash
export GEMINI_API_KEY="your_gemini_api_key_here"
export GEMINI_MODEL="gemini-2.5-flash-lite"
./mvnw spring-boot:run
```

### App URL
- API + UI: `http://localhost:8081`

## 3. How to Run Tests
```bash
./mvnw test
```

## 4. AI-Powered Endpoint
### Description
Main AI endpoint:
- `POST /tasks/suggest`

It accepts a plain-language prompt and returns a structured task object.

### Example request
```bash
curl -X POST http://localhost:8081/tasks/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "remind me to submit my internship project before Friday"
  }'
```

### Example response
```json
{
  "title": "Submit internship project",
  "description": "Prepare and submit the internship project before Friday.",
  "dueDate": "2026-05-01",
  "priority": "HIGH",
  "status": "TODO"
}
```

Notes:
- `GEMINI_MODEL` is optional.
- If you do not configure `GEMINI_API_KEY`, the app still runs.

### Fallback behavior
- If `GEMINI_API_KEY` is missing, invalid, quota-limited, or Gemini fails, the app uses `LocalFallbackAiClient`.
- AI suggestions are not automatically saved to the database.

Additional AI endpoints:
- `POST /tasks/{id}/summarize`
- `POST /tasks/{id}/breakdown`

## 5. CRUD API Endpoints
- `POST /tasks`
- `GET /tasks`
- `GET /tasks/{id}`
- `PUT /tasks/{id}`
- `DELETE /tasks/{id}`

## 6. H2 Console
- URL: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:taskdb`
- Username: `sa`
- Password: blank

## 7. Simple UI
- URL: `http://localhost:8081`
- Capabilities:
  - View tasks
  - Create tasks
  - Delete tasks
  - AI task suggestion
  - AI task summary by task ID
  - AI task breakdown by task ID

## 8. Testing Summary
- Unit tests for `TaskService` happy paths:
  - `createTask`
  - `getAllTasks`
  - `getTaskById`
  - `updateTask`
  - `deleteTask`
- CRUD integration tests with Spring context + MockMvc:
  - `POST /tasks`
  - `GET /tasks`
  - `GET /tasks/{id}`
  - `PUT /tasks/{id}`
  - `DELETE /tasks/{id}`
- AI controller tests mock AI dependency, so tests do not call real Gemini.

## 9. AI Collaboration Notes
AI was used to help design, implement, review, debug, and test the project in incremental steps:
- layered architecture (controller/service/repository)
- DTO mapping and validation
- exception handling
- AI integration design with fallback
- test coverage and review readiness

## 10. Future Improvements
- Add pagination/filtering for large task lists
- Add authentication/authorization
- Add CI workflow and quality checks
- Add containerization for deployment environments
