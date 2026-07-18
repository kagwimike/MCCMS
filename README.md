# MCCMS: Multi-Channel Content Management System

A production-grade **Content-Pipeline Orchestration** and **Progress-Tracking** system designed for multi-channel creators. This platform solves "Platform Blindness" by providing independent visibility into production stages across YouTube, TikTok, and Instagram from a single project view.

## 🏛️ System Architecture
- **Backend**: Java 17+, Spring Boot 3.x (Web, Data JPA, Security with JWT)
- **Database**: MariaDB/MySQL (13-Table Schema)
- **Frontend**: Decoupled Web (Semantic HTML5, Modular CSS3, Vanilla JS ES6)
- **Orchestration**: Scheduled Background Tasks for deadline monitoring and smart stage transitions.

## 🎯 Core Research Problem Solved
Content creators managing multiple channels often lose track of short-form deliverables (TikTok/Instagram) while focused on long-form edits (YouTube). This system orchestrates the pipeline by:
1.  **Independent Stage Tracking**: Monitoring every platform deliverable separately within one project.
2.  **Dependency-based Progression**: Automatically moving "Short Clips" to the active pipeline once the "YouTube" main edit is approved for final export.
3.  **Proactive Deadline Warnings**: A background "Safety Net" that generates notifications 24–48 hours before a scheduled post day if the content is not yet published.

## 📁 Project Structure
```text
MCCMS/
├── backend/                # Spring Boot REST API
│   ├── src/main/java/...   # Modular Services, Controllers, and Models
│   ├── src/main/resources/ # application.properties & SQL Schema
│   └── README_API.md       # Guide for upgrading mocks to production APIs
└── frontend/               # Vanilla JS Web UI
    ├── css/                # Structured CSS (base, layout, pages)
    ├── js/                 # Modular JS (api-modules, page-logic)
    ├── login.html          # Authentication entry point
    └── dashboard.html      # Main orchestration hub
```

## 🚀 Getting Started

### 1. Database Setup (MariaDB/XAMPP)
- Ensure MariaDB is running on `localhost:3306`.
- Create a database named `mccms_db`.
- The system will automatically generate tables and seed roles/stages on the first run.

### 2. Run the Backend
Open the terminal in Android Studio and run:
```powershell
cd backend
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\mvnw.cmd spring-boot:run
```
*Wait for the `Started MccmsApplication` message.*

### 3. Run the Frontend
Open a **new** terminal tab and run:
```powershell
cd frontend
python -m http.server 3000
```
Visit **`http://localhost:3000/index.html`** in your browser.

## 🛠️ Key Features
- **Smart Pipeline**: Automated transitions from long-form to short-form tasks with dependency-based progression.
- **Real Media Management**: Integrated physical **Video Uploads** and storage abstraction.
- **Rich Content Editing**: Full **Quill.js** integration for professional formatting of descriptions and captions.
- **Review Workflow**: Multi-round feedback system with Approve/Reject/Publish cycle.
- **Analytics**: Real-time charts showing upload consistency and platform split via Chart.js.
- **Admin Console**: Configurable "Deadline Warning Window" and User Management.
- **Full CRUD Control**: Complete lifecycle management for projects and deliverables.
- **Premium UI/UX**: High-fidelity dark mode with glassmorphism and atmospheric effects.
- **Mobile-First Design**: Fully responsive architecture optimized for touch devices and one-handed navigation.
- **Audit Logs**: Comprehensive trail of all system actions (Register, Create, Review, Publish).

## 🔌 API Integration Policy
- **YouTube**: Integrated with **YouTube Data API v3** for video publishing.
- **TikTok/Instagram**: Mocked implementations provided as architectural placeholders.
- **Future Growth**: Refer to `backend/README_API.md` for steps to upgrade to live production APIs.

## 👥 Roles & Access
- **ADMIN**: Global settings, user management, and system-wide analytics.
- **CREATOR**: Project creation, deliverable management, and personal analytics.
- **REVIEWER**: Review queue management and quality control feedback.

---
*Developed as a high-fidelity orchestration pipeline for multi-channel digital content production.*
