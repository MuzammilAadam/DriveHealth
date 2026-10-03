# 🩺 DriveHealth

> **Google Drive File & Folder Hygiene Manager** — Scan, analyze, and clean up your Google Drive with intelligent, privacy-first insights.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Configuration](#-configuration)
- [API Reference](#-api-reference)
- [Privacy Policy](#-privacy-policy)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌐 Overview

**DriveHealth** is a full-stack web application that connects to your Google Drive via OAuth 2.0, indexes your file metadata, and runs a suite of hygiene analyzers to surface actionable findings. It helps you reclaim storage space, reduce data sprawl, and stay in control of file permissions — all without ever accessing your actual file contents.

Whether your Drive is cluttered with years of duplicates, massive video files, or publicly shared documents you have forgotten about, DriveHealth gives you a clear, prioritized action plan to clean it up.

---

## ✨ Features

### 🔍 Drive Analysis
| Finding Type        | Description |
|---------------------|-------------|
| **Duplicates**      | Exact duplicate detection using MD5 checksums — grouped and ranked by wasted space |
| **Possible Duplicates** | Near-duplicate detection by filename pattern matching |
| **Old Files**       | Files untouched beyond a configurable threshold (default: 2 years) |
| **Large Files**     | Files exceeding a configurable size limit (default: 500 MB) |
| **External Shares** | Files shared with users outside your domain |
| **Public Files**    | Files accessible to anyone with a link |
| **Empty Folders**   | Orphaned folder structures that can be safely removed |

### 📊 Dashboard and Reporting
- **Storage snapshot timeline** — track quota usage over time
- **Findings hub** — filterable, sortable list of all hygiene issues with severity ratings (LOW / MEDIUM / HIGH / CRITICAL)
- **Scan history** — every scan run is logged with timestamps and finding counts
- **Reports page** — exportable summaries of your Drive health status

### 🗂 Files Explorer
- Browse and search all indexed Drive files
- Filter by MIME type, size, age, or sharing permissions
- Deep-link directly to files in Google Drive

### ⚙️ Custom Rules Engine
Configure per-account hygiene rules:
- **Ignore Folder** — exclude specific folders from all analysis
- **Ignore MIME Type** — skip certain file types (e.g., Google Docs)
- **Large File Threshold** — set your own size limit in bytes
- **Old File Threshold** — define what "old" means in years

### 🔄 Sync Modes
- **Full Scan** — complete re-index of all Drive files
- **Incremental Sync** — delta sync using Google Drive Change Tokens (fast, efficient)
- **Scheduled Scans** — automatic daily scans at 2 AM (configurable via cron expression)

### 🔐 Authentication
- Google OAuth 2.0 — single sign-on, no password stored
- Multi-account support — connect and manage multiple Google accounts per user
- Session management with secure token refresh

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| **Java** | 21 | Core language |
| **Spring Boot** | 3.4.3 | Application framework |
| **Spring Data JPA** | — | ORM and database persistence |
| **Hibernate** | — | JPA implementation |
| **MySQL** | 8.x | Relational database |
| **Spring Scheduler** | — | Cron-based background scan jobs |
| **RestTemplate** | — | Google Drive and OAuth API calls |
| **Maven** | — | Build and dependency management |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| **React** | 19 | UI framework |
| **React Router DOM** | 7 | Client-side routing |
| **Vite** | 8 | Build tool and dev server |
| **Tailwind CSS** | 4 | Utility-first styling |
| **Axios** | 1.x | HTTP client |
| **Lucide React** | — | Icon library |

### External APIs
| API | Usage |
|---|---|
| **Google Drive API v3** | File metadata indexing, change tracking, quota info |
| **Google OAuth 2.0** | User authentication and Drive permission scopes |

---

## 🏗 Architecture

```
DriveHealth/
├── backend/          # Spring Boot REST API
│   └── src/
│       └── main/java/com/example/drivehealth/
│           ├── config/       # CORS, RestTemplate beans
│           ├── controller/   # REST API controllers
│           ├── service/      # Business logic and analyzers
│           ├── repository/   # Spring Data JPA repositories
│           ├── entity/       # JPA entities and enums
│           ├── dto/          # Request/response DTOs
│           └── exception/    # Custom exception classes
│
└── frontend/         # React + Vite SPA
    └── src/
        ├── pages/        # Route-level page components
        ├── components/   # Reusable UI components
        ├── services/     # Axios API service layer
        ├── context/      # React context providers
        └── assets/       # Static assets
```

### Data Flow

```
User → Google OAuth 2.0 → Backend
          ↓
    Google Drive API v3
    (metadata only — no file content)
          ↓
    MySQL (DriveFile, Permission, etc.)
          ↓
    Hygiene Analysis Services
    (Duplicate / Old / Large / Permission)
          ↓
    AnalysisFinding records
          ↓
    REST API → React Frontend
```

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:

- **Java 21** — [Download](https://adoptium.net/)
- **Maven 3.9+** — [Download](https://maven.apache.org/)
- **MySQL 8.x** — [Download](https://dev.mysql.com/downloads/)
- **Node.js 20+** — [Download](https://nodejs.org/)
- **npm 10+** — Included with Node.js
- A **Google Cloud Project** with the [Drive API enabled](https://console.cloud.google.com/apis/library/drive.googleapis.com)

---

### Backend Setup

#### 1. Create a Google OAuth 2.0 Client

1. Go to [Google Cloud Console → Credentials](https://console.cloud.google.com/apis/credentials)
2. Create an **OAuth 2.0 Client ID** (type: Web Application)
3. Add the following **Authorized Redirect URI**:
   ```
   http://localhost:8080/api/auth/google/callback
   ```
4. Note your **Client ID** and **Client Secret**

#### 2. Configure `application.properties`

Open `backend/src/main/resources/application.properties` and fill in your values:

```properties
# MySQL Database
spring.datasource.url=jdbc:mysql://localhost:3306/drivehealth?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD

# Google OAuth 2.0
google.client.id=YOUR_GOOGLE_CLIENT_ID
google.client.secret=YOUR_GOOGLE_CLIENT_SECRET
google.redirect.uri=http://localhost:8080/api/auth/google/callback
```

> ⚠️ **Security Note:** Never commit real credentials to version control. Use environment variables or a secrets manager in production.

#### 3. Run the Backend

```bash
cd backend
mvn spring-boot:run
```

The API server will start on **http://localhost:8080**.

The database schema is managed automatically by Hibernate (`ddl-auto=update`).

---

### Frontend Setup

#### 1. Install Dependencies

```bash
cd frontend
npm install
```

#### 2. Start the Dev Server

```bash
npm run dev
```

The frontend will be available at **http://localhost:5173**.

#### 3. Build for Production

```bash
npm run build
```

The optimized output will be in `frontend/dist/`.

---

## ⚙️ Configuration

All backend configuration lives in `backend/src/main/resources/application.properties`:

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | Backend server port |
| `spring.datasource.url` | — | MySQL JDBC connection URL |
| `google.client.id` | — | Google OAuth Client ID |
| `google.client.secret` | — | Google OAuth Client Secret |
| `google.redirect.uri` | `http://localhost:8080/api/auth/google/callback` | OAuth callback URL |
| `analysis.old-file.years` | `2` | Threshold (years) for flagging old files |
| `analysis.large-file.bytes` | `524288000` | Threshold (bytes) for flagging large files (500 MB) |
| `scheduler.scan.enabled` | `true` | Enable/disable background scans |
| `scheduler.scan.cron` | `0 0 2 * * ?` | Cron expression for scheduled scans (default: 2 AM daily) |

---

## 📡 API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/auth/google/url` | Get Google OAuth authorization URL |
| `GET` | `/api/auth/google/callback` | OAuth 2.0 callback handler |
| `GET` | `/api/auth/me` | Get current user profile |
| `GET` | `/api/auth/users` | List all registered users |
| `POST` | `/api/drive/{accountId}/scan` | Trigger a full Drive scan |
| `POST` | `/api/drive/{accountId}/sync` | Trigger an incremental sync |
| `GET` | `/api/drive/{accountId}/files` | List indexed Drive files (paginated) |
| `POST` | `/api/analysis/{accountId}/duplicates` | Run duplicate detection |
| `POST` | `/api/analysis/{accountId}/old-files` | Run old file analysis |
| `POST` | `/api/analysis/{accountId}/large-files` | Run large file analysis |
| `POST` | `/api/analysis/{accountId}/permissions` | Run permission/sharing analysis |
| `GET` | `/api/findings` | Get all hygiene findings |
| `PATCH` | `/api/findings/{id}/status` | Update a finding status |
| `GET` | `/api/dashboard` | Get dashboard summary stats |
| `GET` | `/api/rules` | Get user-configured rules |
| `POST` | `/api/rules` | Create a new hygiene rule |
| `DELETE` | `/api/rules/{id}` | Delete a rule |
| `GET` | `/api/health` | Application health check |

---

## 🔒 Privacy Policy

DriveHealth is built with a **privacy-first architecture**:

- ✅ **Metadata only** — DriveHealth only reads file names, sizes, dates, MIME types, MD5 checksums, and sharing permissions from the Drive API.
- ❌ **No file content** — Your documents, images, videos, and other files are **never downloaded, read, or stored** by DriveHealth.
- ✅ **Local storage** — All indexed metadata is stored in your own MySQL database instance, not on any external server.
- ✅ **Your credentials** — OAuth tokens are stored only in your local database.
- ❌ **No third-party analytics** — No telemetry or usage data is sent anywhere.

---

## 🤝 Contributing

Contributions are welcome! Here is how to get started:

1. **Fork** the repository
2. **Create** a feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Commit** your changes with a clear message:
   ```bash
   git commit -m "feat: add support for shared drive scanning"
   ```
4. **Push** to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```
5. **Open a Pull Request** against the `main` branch

### Code Style
- **Backend:** Follow standard Java/Spring Boot conventions. Use constructor injection.
- **Frontend:** Follow the ESLint/OxLint rules defined in `.oxlintrc.json`.

---

## 📄 License

This project is licensed under the **MIT License**.

---

<div align="center">
  <p>Built with ❤️ using Spring Boot and React</p>
</div>
