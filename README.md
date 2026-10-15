# Online Exam System

An online examination platform with a question bank and **intelligent test paper
assembly**. Teachers build reusable question banks and either assemble papers by hand
or let the system generate them automatically from rules; students sit timed exams in
the browser and are graded automatically.

Built with Spring Boot 2.7.18, MyBatis-Plus and MySQL 8, rendered server-side with
Thymeleaf.

---

## Features

### For teachers

- **Question bank** — manage questions across four types (`SINGLE_CHOICE`,
  `MULTIPLE_CHOICE`, `TRUE_FALSE`, `FILL_BLANK`), each tagged with a category and a
  difficulty level (`EASY` / `MEDIUM` / `HARD`)
- **Intelligent paper assembly** — generate a paper automatically from the question
  bank by category, difficulty and score constraints, or assemble one manually
- **Paper lifecycle** — drafts are edited freely, then published to students and
  eventually closed; students can only see `PUBLISHED` papers
- **Excel import** — bulk-load questions from a spreadsheet (Apache POI)
- **Result analytics** — score distribution per paper, per-question correct rates, and
  cross-class comparison, exportable to Excel

### For students

- **Timed online exams** — server-side timing with answers auto-saved as you go
- **Automatic grading** — objective answers are scored on submission
- **Wrong-answer notebook** — every mistake is recorded automatically, with the
  ability to mark an item as mastered once it's understood
- **Score history** — review all past results and per-paper breakdowns

### For administrators

- **User management** — create, edit and disable accounts, with bulk import
- **Activity log** — audit trail of system events
- **Paper overview** — monitor all papers across the system

### System-wide

- **Role-based access control** — `ADMIN`, `TEACHER` and `STUDENT`, enforced by a
  session interceptor
- **BCrypt password hashing** — via `spring-security-crypto`
- **Teacher notifications** — teachers are notified when a student submits an exam
- **Bean validation** on all form input, with a global exception handler
- **Light and dark themes** — CSS custom properties, toggle persisted client-side

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 11 |
| Framework | Spring Boot 2.7.18 — Web, Thymeleaf, Validation |
| Persistence | MyBatis-Plus 3.5.5 |
| Database | MySQL 8.0 |
| Security | BCrypt (`spring-security-crypto`), session interceptor |
| Excel | Apache POI 5.2.5 |
| Templating | Thymeleaf (server-rendered) |
| Build | Maven |
| Boilerplate | Lombok |

---

## Screenshots

<!-- Add images here — see CONTRIBUTING notes below. Suggested layout: -->

| Login | Teacher — question bank |
|---|---|
| _screenshot_ | _screenshot_ |

| Student — taking an exam | Teacher — score analysis |
|---|---|
| _screenshot_ | _screenshot_ |

> To add these: create a `docs/screenshots/` folder, drop your PNGs in, and replace the
> placeholders with `![alt text](docs/screenshots/your-file.png)`.

---

## Getting Started

### Prerequisites

- JDK 11 or 17
- Maven 3.8+
- MySQL 8 running on `localhost:3306`

### 1. Create the database

The repository ships the full schema and a set of demo data.

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/test-data.sql
```

`sql/schema.sql` creates the database and all eight tables.
`sql/test-data.sql` loads demo users, questions, papers and exam records.

> Upgrading an existing installation instead? `sql/migration-add-notifications.sql`
> adds the notifications table without touching your data.

### 2. Configure the database password

Credentials are read from **environment variables** — no secrets are stored in the
repository.

`src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/online_exam_system?...}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
```

`DB_URL` and `DB_USERNAME` have sensible local defaults, so you only need to supply
the password.

**Linux / macOS / Git Bash:**

```bash
export DB_PASSWORD="your-mysql-password"
```

**Windows (PowerShell)** — sets it permanently for your user:

```powershell
[Environment]::SetEnvironmentVariable('DB_PASSWORD','your-mysql-password','User')
```

> `DB_PASSWORD` deliberately has **no default**. If it isn't set the application fails
> immediately with a clear error, rather than silently attempting a blank password.

### 3. Run

```bash
mvn spring-boot:run
```

Or build a runnable jar:

```bash
mvn clean package -DskipTests
java -jar target/online-exam-system-1.0.0.jar
```

Then open **<http://localhost:8080/exam>**

### Demo accounts

The demo data creates these accounts. **All use the password `123456`.**

| Username | Role | Name |
|---|---|---|
| `admin` | Administrator | System Admin |
| `teacher1` | Teacher | Professor Wang |
| `teacher2` | Teacher | Professor Li |
| `student1` | Student | Zhang San |
| `student2` | Student | Li Si |
| `student3` | Student | Wang Wu |

---

## Project Structure

```
src/main/java/com/exam/
├── ExamApplication.java      Entry point
├── controller/               HTTP endpoints
│   ├── LoginController       Authentication and sessions
│   ├── AdminController       User management, activity log
│   ├── TeacherController     Question bank, papers, assembly, import/export
│   ├── StudentController     Exam-taking, results, wrong answers
│   ├── StatisticsController  Score and correct-rate analytics
│   └── NotificationController
├── service/                  Business logic
│   ├── ExamPaperService      Manual and rule-based paper assembly
│   ├── ExamService           Exam sessions, timing, answer saving
│   ├── GradingService        Automatic scoring
│   ├── StatisticsService     Aggregation and analytics
│   ├── QuestionService
│   ├── UserService           Registration, BCrypt verification
│   └── NotificationService
├── mapper/                   MyBatis-Plus data access
├── entity/                   Domain models
├── config/                   MyBatis-Plus, MVC, metadata fill, error handling
├── interceptor/              Login/role guard
└── util/ExcelUtil.java       Apache POI import/export

src/main/resources/
├── templates/                Thymeleaf views
│   ├── admin/  teacher/  student/  common/  fragments/
│   └── login.html
├── static/css/               Stylesheets (incl. theming)
├── static/js/                Client-side behaviour
└── application.properties

sql/                          Schema, demo data, migrations
```

---

## Data Model

Eight tables, related as follows:

| Table | Purpose |
|---|---|
| `users` | Accounts, with a `role` of `ADMIN` / `TEACHER` / `STUDENT` |
| `questions` | The question bank — type, category, difficulty, score, options, answer |
| `exam_papers` | Papers, with `assembly_type` of `MANUAL` or `RULE_BASED` |
| `paper_questions` | Join table placing questions into papers, with per-paper score |
| `exam_records` | One attempt per student per paper |
| `answer_records` | The student's answer to each question, and whether it was correct |
| `wrong_answer_notebook` | Aggregated mistakes per student, with a mastered flag |
| `notifications` | In-app messages, primarily exam-submission alerts |

Two status lifecycles drive the workflow:

- **Paper:** `DRAFT` → `PUBLISHED` → `CLOSED` — students only ever see published papers
- **Attempt:** `IN_PROGRESS` → `SUBMITTED` → `GRADED`

---

## Configuration Reference

Optional environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `DB_PASSWORD` | *(none — required)* | Database password |
| `DB_USERNAME` | `root` | Database user |
| `DB_URL` | local `online_exam_system` | Full JDBC URL |
| `PORT` | `8080` | HTTP port — set automatically by hosting platforms |

Application settings such as the context path (`/exam`) and multipart upload limits live
in `src/main/resources/application.properties`.

---

## Deployment

The app ships with a `Dockerfile`, so it runs on any container host. The notes below
cover a free-tier deployment on **Render** with a **free MySQL** database from Aiven.

### Why a container is required

Render has no native Java runtime, so the project is built and run inside Docker. The
`Dockerfile` is a multi-stage build: Maven compiles the jar, then a slim JRE image runs it.

> **Important:** on Render, leave **Build Command** and **Start Command empty**. Setting
> them makes Render use its native build environment, which fails with
> `JAVA_HOME is not set`. Both commands live in the `Dockerfile` instead.

### 1. Create a free MySQL database

Aiven's free MySQL plan (1 CPU / 1 GB RAM / 5 GB storage) is real MySQL, so no code
changes are needed. TiDB Cloud Serverless is a MySQL-compatible alternative.

From the Aiven console, note the **host**, **port**, **user**, **password** and
**database name**, then create your schema:

```bash
mysql -h <host> -P <port> -u <user> -p --ssl-mode=REQUIRED < sql/schema.sql
mysql -h <host> -P <port> -u <user> -p --ssl-mode=REQUIRED < sql/test-data.sql
```

### 2. Create the Render web service

1. **New → Web Service**, and connect this GitHub repository
2. **Language: Docker** (this is what makes Render read the `Dockerfile`)
3. **Instance type: Free**
4. Leave Build Command and Start Command **empty**
5. Add these environment variables under **Environment**:

| Key | Value |
|---|---|
| `DB_URL` | `jdbc:mysql://<host>:<port>/<database>?ssl-mode=REQUIRED&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | your database user |
| `DB_PASSWORD` | your database password |

6. Deploy

Your app will be live at `https://<service-name>.onrender.com/exam` — note the
**`/exam`** context path, which is easy to forget when linking from a portfolio.

### Free-tier behaviour

Render's free instances **sleep after about 15 minutes of inactivity**, and the next
visitor waits roughly **30–60 seconds** for it to wake. If you link this from a
portfolio, add a short note such as *"free tier — the demo may take a moment to load."*

The container is capped at **512 MB RAM**, which is why the `Dockerfile` sets
`-XX:MaxRAMPercentage=70` — it keeps the JVM heap comfortably inside the limit.

---

## License

Released under the MIT License. See `LICENSE` if included.

---

*Originally built as coursework for Web Application System Design (student ID
2442710013L).*
