# Testing Checklist - Document Monitoring System

## 🧪 Test Plan Overview
Comprehensive testing guide for all major features before asistensi.

---

## 🔐 Authentication & Security Testing

### **User Registration**
- [ ] **Valid Registration**:
  - Input: Complete valid data
  - Expected: Success message, redirect to login
  - Verify: User created in database
  
- [ ] **Duplicate Email**:
  - Input: Existing email
  - Expected: Error message "Email already exists"
  - Verify: No duplicate user created

- [ ] **Invalid Password**:
  - Input: Weak password
  - Expected: Validation error
  - Verify: Form shows validation messages

### **User Login**
- [ ] **Valid Login**:
  - Input: Correct credentials
  - Expected: Redirect to dashboard
  - Verify: Session created, user authenticated

- [ ] **Invalid Login**:
  - Input: Wrong password
  - Expected: Error message "Invalid credentials"
  - Verify: No session created

- [ ] **Non-existent User**:
  - Input: Unregistered email
  - Expected: Error message
  - Verify: No access granted

### **Role-Based Access**
- [ ] **Admin Access**:
  - Login as admin
  - Expected: Access to /admin routes
  - Verify: Admin dashboard visible

- [ ] **User Access**:
  - Login as regular user
  - Expected: No access to /admin routes
  - Verify: Redirected with error

- [ ] **Unauthorized Access**:
  - Try accessing /admin without login
  - Expected: Redirect to login
  - Verify: Security working

---

## 📄 Document Management Testing

### **Manual Document Creation**
- [ ] **Complete Valid Data**:
  - Input: All fields filled, file uploaded
  - Expected: Success message, document saved
  - Verify: Document in database, file saved

- [ ] **Required Fields Missing**:
  - Input: Missing nama dokumen
  - Expected: Validation error
  - Verify: No document created

- [ ] **Invalid Date Range**:
  - Input: End date before start date
  - Expected: Validation error
  - Verify: Form rejected

- [ ] **Large File Upload**:
  - Input: File > 20MB
  - Expected: Error message
  - Verify: Upload rejected

### **Document Listing & Filtering**
- [ ] **User Document List**:
  - Login as user
  - Expected: Only user's documents
  - Verify: No other users' documents visible

- [ ] **Admin Document List**:
  - Login as admin
  - Expected: All documents visible
  - Verify: Complete list shown

- [ ] **Search Functionality**:
  - Input: Search keyword
  - Expected: Filtered results
  - Verify: Correct matching documents

- [ ] **Status Filter**:
  - Input: Filter by status (AKTIF/AKAN_HABIS/KADALUARSA)
  - Expected: Correct status documents
  - Verify: Filter working properly

### **Document Editing**
- [ ] **Valid Update**:
  - Edit existing document
  - Expected: Success message, data updated
  - Verify: Changes saved in database

- [ ] **File Replacement**:
  - Upload new file
  - Expected: Old file deleted, new file saved
  - Verify: File path updated

- [ ] **Invalid Update**:
  - Input invalid data
  - Expected: Validation error
  - Verify: No changes made

### **Document Deletion**
- [ ] **Soft Delete Test**:
  - Delete document
  - Expected: Success message
  - Verify: Document removed from database
  - Verify: File deleted from filesystem

---

## 🤖 AI Integration Testing

### **Document Extraction**
- [ ] **Valid Document Image**:
  - Upload clear KTP/SIM image
  - Expected: Successful extraction
  - Verify: Correct data extracted
  - Verify: Auto-categorization working

- [ ] **Blurry Image**:
  - Upload low quality image
  - Expected: Error or partial extraction
  - Verify: Graceful error handling

- [ ] **No Text Detected**:
  - Upload image without text
  - Expected: Error message
  - Verify: No document created

- [ ] **API Rate Limit**:
  - Multiple rapid uploads
  - Expected: Rate limit handling
  - Verify: App doesn't crash

### **Auto-Categorization**
- [ ] **Existing Category**:
  - Extract document with known category
  - Expected: Assigned to existing category
  - Verify: No duplicate category created

- [ ] **New Category**:
  - Extract document with new category
  - Expected: New category created
  - Verify: Category with proper description

- [ ] **Confidence Scoring**:
  - Check extraction confidence
  - Expected: Confidence score displayed
  - Verify: Low confidence handled appropriately

---

## ⏰ Scheduler & Notification Testing

### **Manual Scheduler Trigger**
- [ ] **Admin Manual Run**:
  - Click "Run Scheduler" in admin panel
  - Expected: Success message
  - Verify: Status updates applied
  - Verify: Emails sent (if configured)

- [ ] **Scheduler Logic**:
  - Documents with different expiry dates
  - Expected: Correct status updates
  - Verify: AKTIF → AKAN_HABIS → KADALUARSA

### **Email Notifications**
- [ ] **Email Configuration**:
  - Check SMTP settings
  - Expected: Connection successful
  - Verify: Test email sent

- [ ] **Expiry Notifications**:
  - Documents expiring soon
  - Expected: Emails sent to users
  - Verify: Email content correct
  - Verify: Recipients correct

- [ ] **Email Templates**:
  - Check email formatting
  - Expected: Professional layout
  - Verify: All variables populated

---

## 📊 Admin Features Testing

### **User Management**
- [ ] **User Listing**:
  - View all users
  - Expected: Complete user list
  - Verify: Pagination working (if many users)

- [ ] **User Deactivation**:
  - Deactivate active user
  - Expected: User marked inactive
  - Verify: User cannot login

- [ ] **User Statistics**:
  - View user stats
  - Expected: Correct counts
  - Verify: Active vs inactive users

### **Category Management**
- [ ] **Category Creation**:
  - Add new category
  - Expected: Success message
  - Verify: Category available in forms

- [ ] **Duplicate Category**:
  - Try creating duplicate category
  - Expected: Error message
  - Verify: No duplicate created

- [ ] **Category Deletion**:
  - Delete unused category
  - Expected: Success message
  - Verify: Category removed

- [ ] **Category with Documents**:
  - Try deleting used category
  - Expected: Error or cascade handling
  - Verify: Data integrity maintained

### **System Statistics**
- [ ] **Dashboard Stats**:
  - View admin dashboard
  - Expected: Accurate statistics
  - Verify: Total counts correct
  - Verify: Status breakdown correct

---

## 🗄️ Database Testing

### **Connection & Performance**
- [ ] **Database Connection**:
  - Start application
  - Expected: Successful connection
  - Verify: No connection errors

- [ ] **Query Performance**:
  - Test with large dataset
  - Expected: Reasonable response times
  - Verify: No timeout errors

- [ ] **Transaction Management**:
  - Test concurrent operations
  - Expected: Data consistency
  - Verify: No corrupted data

### **Data Integrity**
- [ ] **Foreign Key Constraints**:
  - Try deleting referenced data
  - Expected: Constraint violations
  - Verify: Referential integrity

- [ ] **Data Validation**:
  - Test invalid data insertion
  - Expected: Validation errors
  - Verify: No invalid data saved

---

## 🔧 Configuration Testing

### **Environment Variables**
- [ ] **Database Config**:
  - Test different database configs
  - Expected: Proper connection
  - Verify: Config overrides working

- [ ] **File Upload Config**:
  - Test different file size limits
  - Expected: Config respected
  - Verify: Limits enforced

- [ ] **Email Config**:
  - Test different SMTP settings
  - Expected: Proper email sending
  - Verify: Config changes applied

---

## 🌐 Integration Testing

### **API Endpoints**
- [ ] **REST API Calls**:
  - Test all controller endpoints
  - Expected: Proper HTTP responses
  - Verify: Status codes correct

- [ ] **Error Handling**:
  - Test error scenarios
  - Expected: Proper error responses
  - Verify: No stack traces exposed

### **External Services**
- [ ] **Gemini API Integration**:
  - Test API calls
  - Expected: Successful responses
  - Verify: Error handling for API failures

- [ ] **Email Service**:
  - Test email sending
  - Expected: Successful delivery
  - Verify: Bounce handling

---

## 📱 Cross-Browser Testing

### **Browser Compatibility**
- [ ] **Chrome**: Full functionality
- [ ] **Firefox**: Full functionality  
- [ ] **Edge**: Full functionality
- [ ] **Safari**: Full functionality (if available)

### **Responsive Design**
- [ ] **Desktop**: Proper layout
- [ ] **Tablet**: Responsive design
- [ ] **Mobile**: Mobile-friendly

---

## 🚨 Performance Testing

### **Load Testing**
- [ ] **Concurrent Users**:
  - Multiple simultaneous users
  - Expected: System remains responsive
  - Verify: No crashes or timeouts

- [ ] **Large File Upload**:
  - Upload maximum file size
  - Expected: Successful upload
  - Verify: Memory usage reasonable

### **Memory Usage**
- [ ] **Long Running**:
  - Run application for extended period
  - Expected: No memory leaks
  - Verify: Stable performance

---

## ✅ Pre-Asistensi Final Checklist

### **Application Status**
- [ ] Application starts without errors
- [ ] Database connection successful
- [ ] All pages load correctly
- [ ] No console errors
- [ ] Sample data loaded

### **Demo Preparation**
- [ ] Test users created (admin & regular)
- [ ] Sample documents uploaded
- [ ] AI extraction working
- [ ] Email configuration tested
- [ ] Scheduler manual trigger working

### **Documentation Ready**
- [ ] Architecture guide completed
- [ ] Demo script prepared
- [ ] Database setup documented
- [ ] FAQ section ready
- [ ] Troubleshooting guide available

### **Environment Setup**
- [ ] Development environment ready
- [ ] PostgreSQL database running
- [ ] All dependencies installed
- [ ] Configuration files ready
- [ ] Backup of current state

---

## 🐛 Common Issues & Solutions

### **Database Issues**
- **Problem**: Connection refused
- **Solution**: Check PostgreSQL service, credentials, firewall

### **API Issues**  
- **Problem**: Gemini API not responding
- **Solution**: Check API key, internet connection, rate limits

### **File Upload Issues**
- **Problem**: File too large
- **Solution**: Adjust multipart.max-file-size in properties

### **Email Issues**
- **Problem**: Gmail authentication failed
- **Solution**: Enable 2FA, use app password, check SMTP settings

---

## 📝 Test Results Summary

### **Passed Tests**: _____
### **Failed Tests**: _____
### **Issues Found**: _____
### **Critical Issues**: _____
### **Ready for Demo**: Yes/No

### **Notes**:
- 
- 
- 

---

*Prepared for comprehensive testing before asistensi session*
