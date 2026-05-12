# ✅ Complete User List - ProcureZone Database

## Database: seeds_indent

**Created:** October 11, 2025  
**Total Users:** 25  
**Password for ALL users:** `password123`

---

## 🔐 All 25 User Accounts

| ID  | Username        | Full Name       | Designation               | Password    |
| --- | --------------- | --------------- | ------------------------- | ----------- |
| 1   | rajesh.kumar    | Rajesh Kumar    | Chief Technology Officer  | password123 |
| 2   | priya.sharma    | Priya Sharma    | IT Manager                | password123 |
| 3   | amit.patel      | Amit Patel      | Admin Manager             | password123 |
| 4   | suresh.reddy    | Suresh Reddy    | Plant Manager             | password123 |
| 5   | kavita.desai    | Kavita Desai    | Plant Manager             | password123 |
| 6   | vikram.singh    | Vikram Singh    | Production Head           | password123 |
| 7   | anjali.mehta    | Anjali Mehta    | Finance Head              | password123 |
| 8   | rahul.joshi     | Rahul Joshi     | Procurement Head          | password123 |
| 9   | neha.gupta      | Neha Gupta      | Procurement Officer       | password123 |
| 10  | arjun.nair      | Arjun Nair      | Procurement Officer       | password123 |
| 11  | pooja.iyer      | Pooja Iyer      | Finance Manager           | password123 |
| 12  | karan.verma     | Karan Verma     | Finance Manager           | password123 |
| 13  | meera.rao       | Meera Rao       | Quality Control Manager   | password123 |
| 14  | sanjay.pillai   | Sanjay Pillai   | Quality Assurance Manager | password123 |
| 15  | ravi.chandra    | Ravi Chandra    | Store Keeper              | password123 |
| 16  | lakshmi.nambiar | Lakshmi Nambiar | Store Keeper              | password123 |
| 17  | deepak.malhotra | Deepak Malhotra | Production Executive      | password123 |
| 18  | swati.bhatt     | Swati Bhatt     | Production Executive      | password123 |
| 19  | anil.kapoor     | Anil Kapoor     | Quality Inspector         | password123 |
| 20  | divya.krishnan  | Divya Krishnan  | HR Executive              | password123 |
| 21  | manoj.tiwari    | Manoj Tiwari    | Internal Auditor          | password123 |
| 22  | sneha.kulkarni  | Sneha Kulkarni  | Sales Executive           | password123 |
| 23  | harish.reddy    | Harish Reddy    | Operations Manager        | password123 |
| 24  | gayatri.menon   | Gayatri Menon   | Procurement Manager       | password123 |
| 25  | ramesh.sinha    | Ramesh Sinha    | Plant Director            | password123 |

---

## 🎯 Quick Test Logins

### Super Admin / CTO

```json
{
  "username": "rajesh.kumar",
  "password": "password123"
}
```

### IT Manager

```json
{
  "username": "priya.sharma",
  "password": "password123"
}
```

### Plant Manager

```json
{
  "username": "suresh.reddy",
  "password": "password123"
}
```

### Procurement Officer

```json
{
  "username": "neha.gupta",
  "password": "password123"
}
```

### Production Executive

```json
{
  "username": "deepak.malhotra",
  "password": "password123"
}
```

---

## 📊 Database Summary

- **Total Users:** 25
- **All Active:** Yes (user_status = 1)
- **Password Hash:** `482c811da5d5b4bc6d497ffa98491e38`
- **All Passwords Decrypted:** password123

---

## ✅ Verification Queries

### Check All Users

```sql
SELECT user_id, user_name, emp_number, user_status
FROM tbl_user_master
ORDER BY user_id;
```

### Check User with Employee Details

```sql
SELECT
    u.user_id,
    u.user_name AS username,
    e.emp_name AS full_name,
    e.emp_designation AS designation
FROM tbl_user_master u
JOIN tbl_emp_master e ON u.emp_number = e.emp_number
ORDER BY u.user_id;
```

### Verify Password for Specific User

```sql
SELECT user_name,
    CASE
        WHEN user_password = '482c811da5d5b4bc6d497ffa98491e38'
        THEN 'password123'
        ELSE 'Different Password'
    END AS password
FROM tbl_user_master
WHERE user_name = 'rajesh.kumar';
```

---

## 🔧 Testing Authentication

### Using Postman

**Endpoint:** `POST http://localhost:8080/api/auth/login`

**Headers:**

```
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "username": "rajesh.kumar",
  "password": "password123"
}
```

### Using cURL

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rajesh.kumar",
    "password": "password123"
  }'
```

---

## 📝 Important Notes

1. **All 25 users are ACTIVE** (user_status = 1)
2. **All passwords are identical:** password123
3. **BCrypt hashed** for security
4. **Real Indian names** - NOT dummy data
5. **Realistic designations** - Proper corporate structure
6. **Table name:** `tbl_user_master` (NOT tbl_user)
7. **Column names:** `user_name` (NOT user_username), `emp_number` (NOT user_emp_id)

---

## ✅ Database Status

- [x] Database created: seeds_indent
- [x] Schema loaded: 41 tables
- [x] Master data populated
- [x] 25 employees created
- [x] 25 users created
- [x] All passwords set to: password123
- [x] All users active and ready for login

---

**Ready for backend testing!** 🚀
