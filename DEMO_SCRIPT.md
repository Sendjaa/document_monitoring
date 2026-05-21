# Demo Script - Document Monitoring System

## 🎯 Demo Objectives
- Show core functionality: document management + AI extraction
- Demonstrate business value: automated document processing
- Highlight technical features: Spring Boot, JPA, AI integration

---

## 📋 Demo Scenario Script

### **Scenario 1: User Registration & Login (2 minutes)**

**Presenter**: "Mari kita mulai dengan registrasi user baru"

**Steps**:
1. **Buka**: `http://localhost:8081/auth/register`
2. **Input**: 
   - Name: "John Doe"
   - Email: "john@example.com"
   - Password: "Password123"
   - Phone: "08123456789"
3. **Submit** → Success message
4. **Login**: `http://localhost:8081/auth/login`
5. **Input**: john@example.com / Password123
6. **Result**: Dashboard dengan statistics

**Talking Points**:
- "Spring Security handles authentication"
- "Password encrypted with BCrypt"
- "Role-based access control"

---

### **Scenario 2: Manual Document Entry (3 minutes)**

**Presenter**: "Sekarang kita tambahkan dokumen secara manual"

**Steps**:
1. **Navigate**: Dashboard → "Tambah Dokumen"
2. **Form Input**:
   - Nama Dokumen: "SIM Card"
   - Tanggal Mulai: Today
   - Tanggal Berakhir: 2025-12-31
   - Kategori: "Identitas"
   - File: Upload sample document
3. **Submit** → Success message
4. **View**: Document list dengan new entry

**Talking Points**:
- "JPA handles database operations"
- "File upload dengan multipart"
- "Automatic reminder creation"
- "Status calculation based on expiry date"

---

### **Scenario 3: AI Document Extraction (5 minutes)**

**Presenter**: "Ini adalah fitur unggulan - AI extraction dari foto"

**Steps**:
1. **Navigate**: "Upload Foto Dokumen"
2. **Upload**: Sample document image (KTP/SIM/Ijazah)
3. **Processing**: Show loading animation
4. **Result**: Extracted data displayed
   - Nama Dokumen: Auto-extracted
   - Tanggal: Auto-extracted
   - Kategori: Auto-created if not exists
5. **Confirm**: Save to database
6. **View**: Document dengan AI-extracted data

**Talking Points**:
- "Gemini Vision API untuk OCR"
- "LLM processes extracted text"
- "Auto-categorization dengan confidence scoring"
- "Error handling untuk failed extractions"

---

### **Scenario 4: Admin Dashboard (3 minutes)**

**Presenter**: "Sekarang kita lihat dari perspective admin"

**Steps**:
1. **Login**: Admin credentials
2. **Dashboard**: Global statistics
   - Total users, documents, categories
   - Document status breakdown
3. **User Management**: List all users
4. **Category Management**: Add new category
5. **Manual Scheduler**: Trigger reminder check

**Talking Points**:
- "Admin sees all data vs user sees own data"
- "Bulk operations for management"
- "Manual override for automated systems"

---

### **Scenario 5: Email Notification Demo (2 minutes)**

**Presenter**: "Sistem otomatis mengirim email reminder"

**Steps**:
1. **Show**: Email configuration in properties
2. **Trigger**: Manual scheduler run
3. **Check**: Email inbox for notifications
4. **Explain**: Cron job schedule (08:00 daily)

**Talking Points**:
- "SMTP integration dengan Gmail"
- "Automated scheduling dengan Spring @Scheduled"
- "Template-based email content"

---

## 🔧 Technical Demo Points

### **Database Integration**
```sql
-- Show tables created by JPA
\dt document_monitoring_db

-- Show sample data
SELECT * FROM dokumen JOIN user ON dokumen.user_id = user.user_id;
```

### **API Integration**
```bash
# Show Gemini API call (in logs)
curl -X POST "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
```

### **Security Features**
- Password encryption demo
- Role-based access control
- CSRF protection

---

## 📊 Demo Data Preparation

### **Sample Documents**
1. **KTP Image**: For AI extraction demo
2. **SIM Card**: For manual entry demo
3. **Ijazah**: For category auto-creation

### **Test Users**
1. **Admin**: admin@docmonitor.com / Admin@123
2. **User**: user@example.com / User@123

### **Sample Categories**
- Identitas (KTP, SIM, Passport)
- Pendidikan (Ijazah, Sertifikat)
- Keuangan (NPWP, Rekening Koran)

---

## ⚡ Live Demo Tips

### **Preparation**
- [ ] Test all scenarios beforehand
- [ ] Prepare sample images
- [ ] Check internet connection for API calls
- [ ] Verify email configuration
- [ ] Clear browser cache

### **Backup Plans**
- **API fails**: Show manual entry fallback
- **Database issues**: Switch to H2 for demo
- **Network issues**: Use cached responses
- **Time constraints**: Focus on AI extraction feature

### **Engagement Questions**
- "How would you use this in your organization?"
- "What documents would you automate first?"
- "How important are the reminder features?"

---

## 🎥 Demo Flow Summary

```
1. Registration (2m) → 2. Manual Entry (3m) → 3. AI Extraction (5m) 
→ 4. Admin Panel (3m) → 5. Email Demo (2m) → Q&A (5m)
```

**Total Time**: ~20 minutes
**Key Focus**: AI extraction automation
**Value Proposition**: Reduce manual data entry by 80%

---

## 🚨 Common Demo Issues & Solutions

### **Technical Issues**
- **Slow API response**: Have cached examples ready
- **Database connection**: Switch to H2 instantly
- **File upload fails**: Use smaller sample files
- **Email not received**: Check spam folder

### **Presentation Issues**
- **Too technical**: Focus on business benefits
- **Time running out**: Prioritize AI extraction
- **Audience questions**: Prepare FAQ section
- **Demo fails**: Have screenshots ready

---

*Prepared for live demonstration - Document Monitoring System*
