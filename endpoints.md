# VisionTwin AI Backend — API Endpoints

**Base URL:** `http://localhost:8080`  
**Server Port:** `8080` (configured in `application.yml`)  
**Context Path:** None (all endpoints are prefixed with `/api/`)

---

## Table of Contents

1. [Health](#1-health)
2. [Machines](#2-machines)
3. [Analysis](#3-analysis)
4. [Chat](#4-chat)
5. [Knowledge Base](#5-knowledge-base)
6. [Admin](#6-admin)
7. [H2 Console (Dev Tool)](#7-h2-console-dev-tool)

---

## 1. Health

### `GET /api/health`

Check if the backend service is up and running.

**Response `200 OK`:**
```json
{
  "status": "UP",
  "service": "VisionTwin AI Intelligent Assistant Backend",
  "timestamp": 1719500000000
}
```

**Full URL:** `http://localhost:8080/api/health`

---

## 2. Machines

All machine-related CRUD and file-serving endpoints.

### `GET /api/machines`

Retrieve a list of all registered machines (including their reference images).

**Response `200 OK`:** Array of [`Machine`](#machine-entity) objects.

**Full URL:** `http://localhost:8080/api/machines`

---

### `GET /api/machines/{id}`

Retrieve a single machine by its UUID.

**Path Parameters:**

| Name | Type | Description |
|------|------|-------------|
| `id` | `UUID` | The machine's unique identifier |

**Response `200 OK`:** [`Machine`](#machine-entity) object.  
**Response `404 Not Found`:** If the machine does not exist.

**Full URL:** `http://localhost:8080/api/machines/{id}`  
**Example:** `http://localhost:8080/api/machines/a1b2c3d4-...`

---

### `POST /api/machines`

Create a new machine with optional file uploads (multipart/form-data).

**Request (multipart/form-data):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | `String` | ✅ Yes | Machine name |
| `manufacturer` | `String` | ✅ Yes | Manufacturer name |
| `model` | `String` | ✅ Yes | Model number/name |
| `thumbnail` | `File` | ❌ No | Thumbnail image file |
| `manual` | `File` | ❌ No | PDF manual file |
| `userGuide` | `File` | ❌ No | PDF user guide file |

**Response `200 OK`:** The created [`Machine`](#machine-entity) object.  
**Response `500 Internal Server Error`:** If file processing fails.

**Full URL:** `http://localhost:8080/api/machines`

---

### `POST /api/machines/{id}/ref-image`

Add a reference image to a machine (multipart/form-data).

**Path Parameters:**

| Name | Type | Description |
|------|------|-------------|
| `id` | `UUID` | The machine's unique identifier |

**Request (multipart/form-data):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `partName` | `String` | ✅ Yes | Name of the part shown in the image (e.g. "Main Shaft") |
| `circleX` | `Float` | ✅ Yes | Normalized X coordinate of the highlight circle (0.0–1.0) |
| `circleY` | `Float` | ✅ Yes | Normalized Y coordinate of the highlight circle (0.0–1.0) |
| `circleRadius` | `Float` | ✅ Yes | Normalized radius of the highlight circle (0.0–1.0) |
| `image` | `File` | ✅ Yes | The reference image file |

**Response `200 OK`:** The created [`ReferenceImage`](#referenceimage-entity) object.  
**Response `500 Internal Server Error`:** If processing fails.

**Full URL:** `http://localhost:8080/api/machines/{id}/ref-image`  
**Example:** `http://localhost:8080/api/machines/a1b2c3d4-.../ref-image`

---

### `GET /api/machines/{id}/ref-images`

Retrieve all reference images for a machine.

**Path Parameters:**

| Name | Type | Description |
|------|------|-------------|
| `id` | `UUID` | The machine's unique identifier |

**Response `200 OK`:** Array of [`ReferenceImage`](#referenceimage-entity) objects.

**Full URL:** `http://localhost:8080/api/machines/{id}/ref-images`  
**Example:** `http://localhost:8080/api/machines/a1b2c3d4-.../ref-images`

---

### `GET /api/machines/files/{folderType}/{filename}`

Serve uploaded files (thumbnails, manuals, user guides, reference images).

**Path Parameters:**

| Name | Type | Description |
|------|------|-------------|
| `folderType` | `String` | Storage subdirectory (e.g. `thumbnails`, `manuals`, `userguides`, `refimages`) |
| `filename` | `String` | The file name (with extension) |

**Response `200 OK`:** The file content with the correct `Content-Type` and `Content-Disposition: inline`.  
**Response `404 Not Found`:** If the file does not exist.  
**Response `500 Internal Server Error`:** If an error occurs reading the file.

**Full URL:** `http://localhost:8080/api/machines/files/{folderType}/{filename}`  
**Example:** `http://localhost:8080/api/machines/files/thumbnails/my-machine.jpg`

---

## 3. Analysis

### `POST /api/analysis/diagnose`

Run an AI-powered diagnosis on a machine problem using an uploaded image (multipart/form-data).

**Request (multipart/form-data):**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `machineId` | `UUID` | ✅ Yes | The machine to diagnose |
| `problemDescription` | `String` | ✅ Yes | Description of the problem/fault |
| `image` | `File` | ✅ Yes | Image of the faulty part/situation |

**Response `200 OK`:** [`DiagnosisReport`](#diagnosisreport-entity) object containing the AI-generated diagnosis.  
**Response `500 Internal Server Error`:** If the analysis pipeline fails.

**Full URL:** `http://localhost:8080/api/analysis/diagnose`

---

## 4. Chat

### `POST /api/chat/{reportId}`

Send a follow-up message on an existing diagnosis report and get an AI reply.

**Path Parameters:**

| Name | Type | Description |
|------|------|-------------|
| `reportId` | `UUID` | The diagnosis report's unique identifier |

**Request Body (JSON):**
```json
{
  "message": "What does this diagnosis mean in plain English?"
}
```

**Response `200 OK`:** [`ChatMessage`](#chatmessage-entity) object (sender will be `AI`).  
**Response `500 Internal Server Error`:** If the AI service fails.

**Full URL:** `http://localhost:8080/api/chat/{reportId}`  
**Example:** `http://localhost:8080/api/chat/d1e2f3g4-...`

---

### `GET /api/chat/{reportId}/history`

Retrieve the full chat history for a diagnosis report.

**Path Parameters:**

| Name | Type | Description |
|------|------|-------------|
| `reportId` | `UUID` | The diagnosis report's unique identifier |

**Response `200 OK`:** Array of [`ChatMessage`](#chatmessage-entity) objects, ordered chronologically.

**Full URL:** `http://localhost:8080/api/chat/{reportId}/history`  
**Example:** `http://localhost:8080/api/chat/d1e2f3g4-.../history`

---

## 5. Knowledge Base

### `POST /api/knowledge/generate/{machineId}`

Generate/regenerate the two-layer knowledge base (Layer 1 = structured JSON, Layer 2 = vector embeddings) for a machine. This processes the machine's manuals, user guides, and reference images via the configured AI provider.

**Path Parameters:**

| Name | Type | Description |
|------|------|-------------|
| `machineId` | `UUID` | The machine's unique identifier |

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Knowledge base layers generated successfully"
}
```

**Response `500 Internal Server Error`:**
```json
{
  "success": false,
  "message": "Failed to generate knowledge base: <error details>"
}
```

**Full URL:** `http://localhost:8080/api/knowledge/generate/{machineId}`  
**Example:** `http://localhost:8080/api/knowledge/generate/a1b2c3d4-...`

---

## 6. Admin

### `POST /api/admin/login`

Authenticate as an admin user. (Uses hardcoded credentials — `admin` / `pass123`.)

**Request Body (JSON):**
```json
{
  "username": "admin",
  "password": "pass123"
}
```

**Response `200 OK` (success):**
```json
{
  "success": true,
  "token": "mock-admin-session-token"
}
```

**Response `401 Unauthorized` (failure):**
```json
{
  "success": false,
  "message": "Invalid username or password"
}
```

**Full URL:** `http://localhost:8080/api/admin/login`

---

### `GET /api/admin/dashboard`

Get aggregate statistics across the system.

**Response `200 OK`:**
```json
{
  "totalMachines": 5,
  "totalReports": 12,
  "totalLayer1Datastores": 5,
  "totalLayer2Vectors": 47
}
```

**Full URL:** `http://localhost:8080/api/admin/dashboard`

---

### `GET /api/admin/reports`

Retrieve all diagnosis reports, ordered by most recent first.

**Response `200 OK`:** Array of [`DiagnosisReport`](#diagnosisreport-entity) objects.

**Full URL:** `http://localhost:8080/api/admin/reports`

---

### `GET /api/admin/reports/{id}`

Retrieve the details of a specific diagnosis report.

**Path Parameters:**

| Name | Type | Description |
|------|------|-------------|
| `id` | `UUID` | The diagnosis report's unique identifier |

**Response `200 OK`:** [`DiagnosisReport`](#diagnosisreport-entity) object.  
**Response `404 Not Found`:** If the report does not exist.

**Full URL:** `http://localhost:8080/api/admin/reports/{id}`  
**Example:** `http://localhost:8080/api/admin/reports/d1e2f3g4-...`

---

## 7. H2 Console (Dev Tool)

### `GET /h2-console`

H2 Database web console for development. Enabled in `application.yml`.

**URL:** `http://localhost:8080/h2-console`

| JDBC Setting | Value |
|-------------|-------|
| JDBC URL | `jdbc:h2:file:./data/visiontwin` |
| Username | `sa` |
| Password | `password` |

---

## Entity Schemas

### Machine

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Auto-generated unique identifier |
| `name` | `String` | Machine name |
| `manufacturer` | `String` | Manufacturer name |
| `model` | `String` | Model number/name |
| `thumbnailPath` | `String` | Server path to thumbnail image |
| `manualPdfPath` | `String` | Server path to manual PDF |
| `userGuidePdfPath` | `String` | Server path to user guide PDF |
| `createdAt` | `LocalDateTime` | Timestamp of creation |
| `updatedAt` | `LocalDateTime` | Timestamp of last update |
| `referenceImages` | `ReferenceImage[]` | Associated reference images |

### ReferenceImage

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Auto-generated unique identifier |
| `filename` | `String` | Image file name (e.g. `Main_Shaft.jpg`) |
| `partName` | `String` | Name of the part (e.g. `Main Shaft`) |
| `circleX` | `Float` | Normalized X coordinate of highlight circle (0.0–1.0) |
| `circleY` | `Float` | Normalized Y coordinate of highlight circle (0.0–1.0) |
| `circleRadius` | `Float` | Normalized radius of highlight circle (0.0–1.0) |
| `filePath` | `String` | Server path to the image file |

### DiagnosisReport

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Auto-generated unique identifier |
| `machineId` | `UUID` | The machine being diagnosed |
| `machineName` | `String` | Machine name (denormalized) |
| `problemDescription` | `String` | User-provided problem description |
| `uploadedImagePath` | `String` | Server path to the uploaded diagnostic image |
| `diagnosisProblem` | `String` | AI-identified problem summary |
| `diagnosisSolution` | `String` | AI-provided solution/recommendation (max 2000 chars) |
| `highlightX` | `Float` | Optional highlight X coordinate (from matched ref image) |
| `highlightY` | `Float` | Optional highlight Y coordinate |
| `highlightRadius` | `Float` | Optional highlight radius |
| `timestamp` | `LocalDateTime` | Report creation timestamp |
| `chatHistory` | `ChatMessage[]` | Follow-up chat messages |

### ChatMessage

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Auto-generated unique identifier |
| `sender` | `String` | `"USER"` or `"AI"` |
| `messageText` | `String` | Message content (max 4000 chars) |
| `timestamp` | `LocalDateTime` | Message timestamp |

### KnowledgeBaseLayer1

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Auto-generated unique identifier |
| `machineId` | `UUID` | Associated machine (unique per machine) |
| `contentJson` | `String` | Large JSON blob of structured knowledge (up to ~1M chars) |

### KnowledgeBaseLayer2

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Auto-generated unique identifier |
| `machineId` | `UUID` | Associated machine |
| `contentChunk` | `String` | A text chunk/snippet |
| `embedding` | `double[]` | Vector embedding of the chunk (comma-separated) |
| `source` | `String` | Origin: `MANUAL`, `USER_GUIDE`, or `REF_IMAGE` |

---

## Summary Table

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 1 | `GET` | `/api/health` | Health check |
| 2 | `GET` | `/api/machines` | List all machines |
| 3 | `GET` | `/api/machines/{id}` | Get machine by ID |
| 4 | `POST` | `/api/machines` | Create machine (multipart) |
| 5 | `POST` | `/api/machines/{id}/ref-image` | Add reference image (multipart) |
| 6 | `GET` | `/api/machines/{id}/ref-images` | List reference images |
| 7 | `GET` | `/api/machines/files/{folderType}/{filename}` | Serve uploaded files |
| 8 | `POST` | `/api/analysis/diagnose` | Run AI diagnosis (multipart) |
| 9 | `POST` | `/api/chat/{reportId}` | Send follow-up chat message |
| 10 | `GET` | `/api/chat/{reportId}/history` | Get chat history |
| 11 | `POST` | `/api/knowledge/generate/{machineId}` | Generate knowledge base |
| 12 | `POST` | `/api/admin/login` | Admin login |
| 13 | `GET` | `/api/admin/dashboard` | Dashboard statistics |
| 14 | `GET` | `/api/admin/reports` | List all diagnosis reports |
| 15 | `GET` | `/api/admin/reports/{id}` | Get report details |
| — | `GET` | `/h2-console` | H2 Database web console |
