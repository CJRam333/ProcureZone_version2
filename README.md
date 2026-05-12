# ProcureZone – Enterprise Procurement & Manufacturing Operations System

A full-stack procurement management platform for Nuziveedu Seeds India (NSL), replacing a legacy Struts 2 JSP application with a modern Spring Boot + React stack.

## Repository Structure

```
ProcureZone_version2/
├── backend/          # Spring Boot 3.2.5 / Java 21 REST API
├── frontend/         # React 19 / TypeScript / Vite SPA
├── ProcureZone/      # Legacy Struts 2 application (reference)
├── docs/             # All project documentation
└── lib/              # Shared libraries
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2.5, Java 21, Spring Security + JWT |
| Database | MySQL 8.0, Flyway migrations (V1–V40) |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS, React Query |
| Legacy | Struts 2, Java 8, JSP, jQuery |

## Quick Start

### Backend
```bash
cd backend
# Copy and configure application.yml
cp src/main/resources/application.yml.example src/main/resources/application.yml
# Edit application.yml with your DB credentials
mvn spring-boot:run
# API available at http://localhost:8080/api/v1
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# App available at http://localhost:5173
```

## Documentation

See [docs/](docs/) for all project documentation including:
- Integration guides
- Gap analysis (legacy vs new)
- Database schema
- User stories and test credentials
- Changelog

## Development Status

- **Backend:** ~92% complete (225 endpoints, 35 controllers, 56 DB tables)
- **Frontend:** ~70% complete (~70 pages, 25 API modules)
- **Overall:** ~75% complete

## Key Modules

| Module | Backend | Frontend |
|--------|---------|----------|
| Authentication | JWT + 9 roles | Login page |
| Indents (Procurement) | Complete | Complete |
| Plant Indents (R&D) | Complete | Complete |
| Purchase Orders | Complete | Complete |
| GRN (Goods Receipt) | Complete | Complete |
| Issue Notes | Complete | Complete |
| Inventory | Complete | Complete |
| Masters & Mappings | Complete | ~60% |
| Reports | Complete | ~40% |
| Email Notifications | Complete | N/A |

## Default Test Users (Development Only)

| Username | Role | Password |
|----------|------|----------|
| rajesh.kumar | SUPERADMIN | password123 |
| priya.sharma | ADMIN | password123 |
| neha.gupta | PROCUREMENT | password123 |
| lakshmi.nambiar | STOREKEEPER | password123 |

> Full test user list: [docs/testing/USER_STORIES_FOR_TESTING.md](docs/testing/USER_STORIES_FOR_TESTING.md)
