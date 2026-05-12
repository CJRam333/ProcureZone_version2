# Quick Start Guide - ProcureZone Database

**Time Required:** 5 minutes  
**Difficulty:** Easy

---

## ⚡ Quick Setup

### Prerequisites

- MySQL 8.0+ installed and running
- MySQL root password ready
- Terminal/Command Prompt access

---

## 🚀 One-Command Setup

### For Linux/Mac/Git Bash:

```bash
cd /e/Net-Beans/ProcureZone/database
./setup_complete_database.sh
```

### For Windows Command Prompt:

```cmd
cd e:\Net-Beans\ProcureZone\database
setup_complete_database.bat
```

**That's it!** The script will:

1. ✅ Drop and create `seeds_indent` database
2. ✅ Create all 41 tables
3. ✅ Populate master data (companies, departments, materials)
4. ✅ Create 25 realistic employees
5. ✅ Create 25 user accounts
6. ✅ Set up role mappings and relationships
7. ✅ Add business transaction data

---

## 🔐 Login Credentials

**All 25 users have the same password:**

```
username: rajesh.kumar
password: password123
```

Other test users:

- priya.sharma / password123
- suresh.reddy / password123
- neha.gupta / password123
- deepak.malhotra / password123

**See full list:** [All Users](../reference/ALL_USERS_LIST.md)

---

## 🧪 Test Authentication

### 1. Start Backend

```bash
cd /e/Net-Beans/ProcureZone/backend
mvn spring-boot:run
```

### 2. Test Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"rajesh.kumar","password":"password123"}'
```

### 3. Expected Response

```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJ...",
  "authenticatedUser": {
    "fullName": "Rajesh Kumar",
    "roles": ["SUPER_ADMIN"]
  }
}
```

---

## ✅ Verify Setup

```bash
# Check database exists
mysql -u root -p -e "SHOW DATABASES LIKE 'seeds_indent';"

# Check user count
mysql -u root -p seeds_indent -e "SELECT COUNT(*) FROM tbl_user_master;"

# View all users
mysql -u root -p seeds_indent < verify_users.sql
```

---

## 🆘 Common Issues

### Database Connection Failed

- Check MySQL is running: `mysql -u root -p`
- Verify password in `application.yml`

### Login Returns "Invalid Credentials"

- All passwords are: `password123`
- Username is case-sensitive
- See: [Login Fix](../troubleshooting/LOGIN_FIX_APPLIED.md)

### Backend Won't Start

```bash
cd /e/Net-Beans/ProcureZone/backend
mvn clean install
mvn spring-boot:run
```

---

## 📚 More Documentation

- **[Complete Setup Guide](COMPLETE_SETUP_GUIDE.md)** - Detailed instructions
- **[Setup Success Summary](SETUP_SUCCESS_SUMMARY.md)** - Latest results
- **[All Users List](../reference/ALL_USERS_LIST.md)** - Complete credentials
- **[Database Schema](../reference/DATABASE_SCHEMA_DOCUMENTATION.md)** - Schema details

---

**Next Steps:** Start the backend and test authentication! 🚀
