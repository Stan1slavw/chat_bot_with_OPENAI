A Telegram bot for **anonymous feedback** collection from auto service staff (mechanics, electricians, managers). Messages are analyzed OpenAI, stored in PostgreSQL, mirrored to Google Docs, and—optionally—escalated to Trello cards for critical reports. Includes a lightweight admin page with filters.

## Features
- Onboarding: ask user **role** (mechanic/electrician/manager) and **branch** on first run
- Free-form feedback any time (complaints, suggestions, requests)
- Analysis per message:
  - **Sentiment:** `NEGATIVE` / `NEUTRAL` / `POSITIVE`
  - **Criticality:** integer from **1** to **5**
  - **Resolution:** short Ukrainian guidance on how to solve
  - Modes: **OpenAI API** 
- Persistence in **PostgreSQL** (JPA/Hibernate, Flyway migrations)
- Mirror to **Google Docs** (append to the end of the doc)
- **Trello**: auto-create a card when criticality ≥ 4
- Admin UI `/admin/feedback` (Thymeleaf) with filters by branch/role/criticality

## Tech Stack
Java 21 • Spring Boot (Web, Data JPA, Thymeleaf, Security, WebFlux) • PostgreSQL • Flyway • Telegram Bot API (long polling) • Google Docs API • Trello REST • Lombok • Jackson • Maven

## Requirements
- JDK **21+**
- Maven **3.9+**
- Docker (for PostgreSQL)
- Telegram bot token `TELEGRAM_BOT_TOKEN`
- OpenAI API Key `OPENAI_API_KEY`
- Google Docs: service account `credentials.json` + `GOOGLE_DOC_ID`
- Trello: `TRELLO_KEY`, `TRELLO_TOKEN`, `TRELLO_LIST_ID`

## Quick Start

### 1) Environment variables
Define in your IDE (Run → Edit Configurations) or OS shell:
```bash
TELEGRAM_BOT_TOKEN=123456789:ABC...

# OpenAI (optional)
OPENAI_API_KEY=sk-...          # or disable OpenAI entirely

# Google Docs
GOOGLE_DOC_ID=1AbC...xyz       # Document ID from the URL
GOOGLE_CREDENTIALS_PATH=C:/path/to/credentials.json

# Trello (optional)
TRELLO_ENABLED=true
TRELLO_KEY=...
TRELLO_TOKEN=...
TRELLO_LIST_ID=...

### 3) Run the app
```bash
mvn spring-boot:run
```
Open chat with the bot → send `/start`.  
Admin UI: `http://localhost:8080/admin/feedback` (see `ADMIN_USER`/`ADMIN_PASS`).

## Integrations

### Telegram
1) Create a bot in **@BotFather**, get `TELEGRAM_BOT_TOKEN`.  
2) For long polling, clear webhook (just in case):
```bash
curl "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/deleteWebhook?drop_pending_updates=true"
```

### OpenAI (optional)
If you want cloud-based analysis, set `OPENAI_API_KEY` and `OPENAI_ENABLED=true`.  

### Google Docs
1) Enable **Google Docs API** in your GCP project.  
2) Create a **Service Account** and download `credentials.json` (`"type":"service_account"`).  
3) In the **target Google Doc**, press **Share** and add the service account `client_email` as **Editor**.  
4) Set `GOOGLE_DOC_ID` and `GOOGLE_CREDENTIALS_PATH`.  
Append uses `EndOfSegmentLocation` to write to the end of the document.

### Trello (optional)
Create API key/token, set `TRELLO_LIST_ID` for the target list. Cards are created automatically for criticality ≥ 4.

## Configuration (snippet from `application.properties`)
```properties
spring.application.name=vgt_chat_bot

# ========================
# Spring DataSource
# ========================
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=username
spring.datasource.password=password

# ========================
# JPA
# ========================
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=true

# ========================
# Thymeleaf
# ========================
spring.thymeleaf.cache=false

# ========================
# Telegram Bot
# ========================
telegram.bot.username=vgt_test_bot
telegram.bot.token=${TELEGRAM_BOT_TOKEN}

# ========================
# OpenAI
# ========================
openai.apiKey=${OPENAI_API_KEY}
openai.model=gpt-4o-mini

# ========================
# Google Docs
# ========================
google.docs.documentId=${GOOGLE_DOCS_DOCUMENT_ID}

# ========================
# Trello
# ========================
trello.enabled=true
trello.key=${TRELLO_KEY:dummy}
trello.token=${TRELLO_TOKEN:dummy}
trello.listId=${TRELLO_LIST_ID:dummy}
trello.createThreshold=4


logging.level.org.telegram=INFO
logging.level.com.vgr.feedbackbot=DEBUG

```

## Troubleshooting
- **`/start` does nothing** → remove webhook (`.../deleteWebhook`), verify token via `getMe`, ensure only one app instance is running.  
- **Always `NEUTRAL / 3`** → OpenAI key missing or quota exceeded; set a valid `OPENAI_API_KEY`.  
- **Google Docs 403 `SERVICE_DISABLED`** → enable Docs API in the same GCP project as your service account.  
- **Google Docs 403 `PERMISSION_DENIED`** → share the Doc with your service account `client_email` as **Editor**.  
- **`Index must be less than end index`** → use `EndOfSegmentLocation` (already configured).  
- **Windows paths** → use `C:/Users/.../credentials.json` for `GOOGLE_CREDENTIALS_PATH`.


