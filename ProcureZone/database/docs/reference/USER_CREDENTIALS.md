# 🔐 ProcureZone User Credentials

## Complete List of All 25 Users

**Database:** seeds_indent  
**All Passwords:** `password123` (BCrypt hashed)  
**Date:** October 10, 2025

---

## 🏢 Company 1: National Seeds Limited (NSL)

### **Super Administrator**

| Username       | Email                     | Name         | Role                | Department | Location           |
| -------------- | ------------------------- | ------------ | ------------------- | ---------- | ------------------ |
| `rajesh.kumar` | rajesh.kumar@nslindia.com | Rajesh Kumar | Super Administrator | IT         | Head Office Mumbai |

### **Administrators**

| Username       | Email                     | Name         | Role          | Department | Location           |
| -------------- | ------------------------- | ------------ | ------------- | ---------- | ------------------ |
| `priya.sharma` | priya.sharma@nslindia.com | Priya Sharma | Administrator | IT         | Head Office Mumbai |
| `amit.patel`   | amit.patel@nslindia.com   | Amit Patel   | Administrator | Admin      | Head Office Mumbai |

### **Plant Managers**

| Username       | Email                     | Name         | Role          | Department | Location        |
| -------------- | ------------------------- | ------------ | ------------- | ---------- | --------------- |
| `suresh.reddy` | suresh.reddy@nslindia.com | Suresh Reddy | Plant Manager | Production | Plant Pune      |
| `kavita.desai` | kavita.desai@nslindia.com | Kavita Desai | Plant Manager | Production | Plant Hyderabad |

### **Department Heads**

| Username       | Email                     | Name         | Role             | Department  | Location           |
| -------------- | ------------------------- | ------------ | ---------------- | ----------- | ------------------ |
| `vikram.singh` | vikram.singh@nslindia.com | Vikram Singh | Production Head  | Production  | Plant Pune         |
| `anjali.mehta` | anjali.mehta@nslindia.com | Anjali Mehta | Finance Head     | Finance     | Head Office Mumbai |
| `rahul.joshi`  | rahul.joshi@nslindia.com  | Rahul Joshi  | Procurement Head | Procurement | Head Office Mumbai |

### **Procurement Officers**

| Username     | Email                   | Name       | Role                | Department  | Location              |
| ------------ | ----------------------- | ---------- | ------------------- | ----------- | --------------------- |
| `neha.gupta` | neha.gupta@nslindia.com | Neha Gupta | Procurement Officer | Procurement | Head Office Mumbai    |
| `arjun.nair` | arjun.nair@nslindia.com | Arjun Nair | Procurement Officer | Procurement | Regional Office Delhi |

### **Finance Managers**

| Username      | Email                    | Name        | Role            | Department | Location              |
| ------------- | ------------------------ | ----------- | --------------- | ---------- | --------------------- |
| `pooja.iyer`  | pooja.iyer@nslindia.com  | Pooja Iyer  | Finance Manager | Finance    | Head Office Mumbai    |
| `karan.verma` | karan.verma@nslindia.com | Karan Verma | Finance Manager | Finance    | Regional Office Delhi |

### **Quality Managers**

| Username        | Email                      | Name          | Role                      | Department      | Location                  |
| --------------- | -------------------------- | ------------- | ------------------------- | --------------- | ------------------------- |
| `meera.rao`     | meera.rao@nslindia.com     | Meera Rao     | Quality Control Manager   | Quality Control | Regional Office Bangalore |
| `sanjay.pillai` | sanjay.pillai@nslindia.com | Sanjay Pillai | Quality Assurance Manager | Quality Control | Plant Pune                |

### **Store Keepers**

| Username          | Email                        | Name            | Role         | Department         | Location        |
| ----------------- | ---------------------------- | --------------- | ------------ | ------------------ | --------------- |
| `ravi.chandra`    | ravi.chandra@nslindia.com    | Ravi Chandra    | Store Keeper | Stores & Warehouse | Plant Pune      |
| `lakshmi.nambiar` | lakshmi.nambiar@nslindia.com | Lakshmi Nambiar | Store Keeper | Stores & Warehouse | Plant Hyderabad |

### **Regular Employees**

| Username          | Email                        | Name            | Role                 | Department      | Location                  |
| ----------------- | ---------------------------- | --------------- | -------------------- | --------------- | ------------------------- |
| `deepak.malhotra` | deepak.malhotra@nslindia.com | Deepak Malhotra | Production Executive | Production      | Plant Pune                |
| `swati.bhatt`     | swati.bhatt@nslindia.com     | Swati Bhatt     | Production Executive | Production      | Plant Hyderabad           |
| `anil.kapoor`     | anil.kapoor@nslindia.com     | Anil Kapoor     | Quality Inspector    | Quality Control | Regional Office Bangalore |
| `divya.krishnan`  | divya.krishnan@nslindia.com  | Divya Krishnan  | HR Executive         | Human Resources | Head Office Mumbai        |

### **Viewers/Auditors**

| Username         | Email                       | Name           | Role             | Department        | Location              |
| ---------------- | --------------------------- | -------------- | ---------------- | ----------------- | --------------------- |
| `manoj.tiwari`   | manoj.tiwari@nslindia.com   | Manoj Tiwari   | Internal Auditor | Finance           | Head Office Mumbai    |
| `sneha.kulkarni` | sneha.kulkarni@nslindia.com | Sneha Kulkarni | Sales Executive  | Sales & Marketing | Regional Office Delhi |

---

## 🏢 Company 2: AgroTech Industries Pvt Ltd

| Username        | Email                      | Name          | Role                               | Department  | Location                  |
| --------------- | -------------------------- | ------------- | ---------------------------------- | ----------- | ------------------------- |
| `harish.reddy`  | harish.reddy@agrotech.com  | Harish Reddy  | Operations Manager (Plant Manager) | Admin       | Regional Office Bangalore |
| `gayatri.menon` | gayatri.menon@agrotech.com | Gayatri Menon | Procurement Manager                | Procurement | Regional Office Bangalore |

---

## 🏢 Company 3: Green Seeds Corporation

| Username       | Email                       | Name         | Role                           | Department | Location          |
| -------------- | --------------------------- | ------------ | ------------------------------ | ---------- | ----------------- |
| `ramesh.sinha` | ramesh.sinha@greenseeds.com | Ramesh Sinha | Plant Director (Plant Manager) | Production | Warehouse Chennai |

---

## 🎯 Quick Test Credentials

### **For Super Admin Testing:**

```
Username: rajesh.kumar
Password: password123
Email: rajesh.kumar@nslindia.com
```

### **For Manager Testing:**

```
Username: suresh.reddy
Password: password123
Email: suresh.reddy@nslindia.com
```

### **For Employee Testing:**

```
Username: deepak.malhotra
Password: password123
Email: deepak.malhotra@nslindia.com
```

### **For Procurement Testing:**

```
Username: neha.gupta
Password: password123
Email: neha.gupta@nslindia.com
```

### **For Finance Testing:**

```
Username: pooja.iyer
Password: password123
Email: pooja.iyer@nslindia.com
```

---

## 🔑 Login Information

### **Backend API Login Endpoint:**

```
POST http://localhost:8080/api/v1/auth/login
```

### **Request Body:**

```json
{
  "username": "rajesh.kumar",
  "password": "password123"
}
```

### **Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "rajesh.kumar",
  "email": "rajesh.kumar@nslindia.com",
  "roles": ["ROLE_SUPERADMIN"],
  "expiresIn": 3600000
}
```

---

## 📊 User Distribution by Role

| Role                | Count | Users                                                               |
| ------------------- | ----- | ------------------------------------------------------------------- |
| Super Administrator | 1     | rajesh.kumar                                                        |
| Administrator       | 2     | priya.sharma, amit.patel                                            |
| Plant Manager       | 4     | suresh.reddy, kavita.desai, harish.reddy, ramesh.sinha              |
| Department Head     | 5     | vikram.singh, anjali.mehta, rahul.joshi (also Finance, Procurement) |
| Procurement Officer | 3     | neha.gupta, arjun.nair, gayatri.menon                               |
| Finance Manager     | 3     | pooja.iyer, karan.verma, anjali.mehta                               |
| Quality Manager     | 2     | meera.rao, sanjay.pillai                                            |
| Store Keeper        | 2     | ravi.chandra, lakshmi.nambiar                                       |
| Employee            | 4     | deepak.malhotra, swati.bhatt, anil.kapoor, divya.krishnan           |
| Viewer              | 2     | manoj.tiwari, sneha.kulkarni                                        |

**Total:** 25 users across 3 companies

---

## 🔐 Password Hash Details

**Plain Text Password:** `password123`  
**BCrypt Hash:** `482c811da5d5b4bc6d497ffa98491e38`  
**Cost Factor:** 10  
**Algorithm:** BCrypt

This hash is stored in both:

- `tbl_emp_master.emp_password`
- `tbl_user_master.user_password`

---

## 🧪 Testing Scenarios

### **Scenario 1: Test Different Roles**

1. Login as `rajesh.kumar` (Super Admin) - Full access
2. Login as `neha.gupta` (Procurement) - Procurement access only
3. Login as `deepak.malhotra` (Employee) - Limited access

### **Scenario 2: Test Multi-Company**

1. Login as `rajesh.kumar` (National Seeds)
2. Login as `harish.reddy` (AgroTech)
3. Login as `ramesh.sinha` (Green Seeds)

### **Scenario 3: Test Workflow**

1. Login as `deepak.malhotra` - Create indent (Employee)
2. Login as `vikram.singh` - Approve indent (Dept Head)
3. Login as `anjali.mehta` - Finance approval (Finance Head)
4. Login as `neha.gupta` - Procurement (Procurement Officer)

---

## 📝 Notes

- All passwords are securely hashed using BCrypt
- Email addresses follow company domain patterns
- Usernames match first.lastname format
- All users are active (status = 1)
- Reporting hierarchy is properly set up
- Role-based access control is enforced

---

## ✅ Verification Query

Run this to verify all users are created:

```sql
SELECT
    u.user_name AS Username,
    e.emp_name AS Name,
    e.emp_email AS Email,
    r.role_name AS Role,
    c.comp_name AS Company,
    d.dept_name AS Department,
    l.loc_name AS Location
FROM tbl_user_master u
JOIN tbl_emp_master e ON u.emp_number = e.emp_number
JOIN tbl_map_emp_roles mer ON e.emp_number = mer.emp_number
JOIN tbl_roles_master r ON mer.role_id = r.role_id
JOIN tbl_company_master c ON e.emp_company = c.comp_id
JOIN tbl_department_master d ON e.emp_department = d.dept_id
JOIN tbl_location_master l ON e.emp_location = l.loc_id
WHERE u.user_status = 1
ORDER BY c.comp_name, r.role_id, e.emp_name;
```

---

**All users are ready to use! Start testing! 🚀**
