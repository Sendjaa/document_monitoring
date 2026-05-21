-- Database Setup & Sample Data for Document Monitoring System
-- PostgreSQL Schema

-- ============================================================
-- 1. DATABASE CREATION
-- ============================================================

-- Create database (run as postgres user)
-- CREATE DATABASE document_monitoring_db;

-- Connect to database
-- \c document_monitoring_db;

-- ============================================================
-- 2. TABLE SCHEMAS (Auto-created by JPA)
-- ============================================================

-- User table
-- CREATE TABLE user (
--     user_id BIGSERIAL PRIMARY KEY,
--     name VARCHAR(255) NOT NULL,
--     email VARCHAR(255) UNIQUE NOT NULL,
--     password VARCHAR(255) NOT NULL,
--     phone VARCHAR(20),
--     role VARCHAR(20) DEFAULT 'USER',
--     active BOOLEAN DEFAULT TRUE
-- );

-- Kategori Dokumen table
-- CREATE TABLE kategori_dokumen (
--     kategori_id BIGSERIAL PRIMARY KEY,
--     nama_kategori VARCHAR(255) UNIQUE NOT NULL,
--     deskripsi TEXT
-- );

-- Dokumen table
-- CREATE TABLE dokumen (
--     dokumen_id BIGSERIAL PRIMARY KEY,
--     nama_dokumen VARCHAR(255) NOT NULL,
--     tanggal_mulai DATE,
--     tanggal_berakhir DATE,
--     file_path VARCHAR(500),
--     status VARCHAR(20) DEFAULT 'AKTIF',
--     user_id BIGINT REFERENCES user(user_id),
--     kategori_id BIGINT REFERENCES kategori_dokumen(kategori_id)
-- );

-- Sistem Pengingat table
-- CREATE TABLE sistem_pengingat (
--     pengingat_id BIGSERIAL PRIMARY KEY,
--     dokumen_id BIGINT UNIQUE REFERENCES dokumen(dokumen_id),
--     interval_hari INTEGER DEFAULT 30
-- );

-- ============================================================
-- 3. SAMPLE DATA INSERTION
-- ============================================================

-- Insert Sample Users
INSERT INTO user (name, email, password, phone, role, active) VALUES
('Admin User', 'admin@docmonitor.com', '$2a$10$YourHashedPasswordHere', '08123456789', 'ADMIN', TRUE),
('John Doe', 'john.doe@example.com', '$2a$10$YourHashedPasswordHere', '08123456788', 'USER', TRUE),
('Jane Smith', 'jane.smith@example.com', '$2a$10$YourHashedPasswordHere', '08123456787', 'USER', TRUE),
('Bob Johnson', 'bob.johnson@example.com', '$2a$10$YourHashedPasswordHere', '08123456786', 'USER', TRUE);

-- Insert Sample Categories
INSERT INTO kategori_dokumen (nama_kategori, deskripsi) VALUES
('Identitas', 'Dokumen identitas pribadi seperti KTP, SIM, Passport'),
('Pendidikan', 'Dokumen pendidikan seperti Ijazah, Sertifikat'),
('Keuangan', 'Dokumen keuangan seperti NPWP, Rekening Koran'),
('Kesehatan', 'Dokumen kesehatan seperti BPJS, Asuransi'),
('Legal', 'Dokumen legal seperti Akta, Kontrak'),
('Kendaraan', 'Dokumen kendaraan seperti STNK, BPKB'),
('Properti', 'Dokumen properti seperti Sertifikat Tanah, IMB');

-- Insert Sample Documents
INSERT INTO dokumen (nama_dokumen, tanggal_mulai, tanggal_berakhir, file_path, status, user_id, kategori_id) VALUES
-- John Doe's documents
('SIM Card', '2023-01-15', '2028-01-15', './uploads/documents/sim_john.jpg', 'AKTIF', 2, 1),
('KTP Elektronik', '2022-06-20', '2032-06-20', './uploads/documents/ktp_john.jpg', 'AKTIF', 2, 1),
('Ijazah S1', '2019-07-01', '2099-12-31', './uploads/documents/ijazah_john.pdf', 'AKTIF', 2, 2),
('NPWP', '2021-03-10', '2099-12-31', './uploads/documents/npwp_john.jpg', 'AKTIF', 2, 3),

-- Jane Smith's documents
('Passport', '2023-09-01', '2028-09-01', './uploads/documents/passport_jane.jpg', 'AKTIF', 3, 1),
('Sertifikat TOEFL', '2022-11-15', '2025-11-15', './uploads/documents/toefl_jane.pdf', 'AKAN_HABIS', 3, 2),
('BPJS Kesehatan', '2020-01-01', '2024-12-31', './uploads/documents/bpjs_jane.jpg', 'AKAN_HABIS', 3, 4),

-- Bob Johnson's documents
('STNK Mobil', '2022-05-15', '2024-05-15', './uploads/documents/stnk_bob.jpg', 'KADALUARSA', 4, 6),
('BPKB Mobil', '2022-05-15', '2099-12-31', './uploads/documents/bpkb_bob.jpg', 'AKTIF', 4, 6),
('Sertifikat Tanah', '2021-08-20', '2099-12-31', './uploads/documents/sertifikat_bob.pdf', 'AKTIF', 4, 7),

-- Documents with different statuses for testing
('SIM Card Expired', '2018-03-01', '2023-03-01', './uploads/documents/sim_expired.jpg', 'KADALUARSA', 2, 1),
('Ijazah SMA', '2015-06-01', '2099-12-31', './uploads/documents/ijazah_sma.pdf', 'AKTIF', 3, 2);

-- Insert Sample Reminders
INSERT INTO sistem_pengingat (dokumen_id, interval_hari) VALUES
(1, 30),  -- SIM Card
(2, 30),  -- KTP
(3, 60),  -- Ijazah
(4, 90),  -- NPWP
(5, 30),  -- Passport
(6, 30),  -- TOEFL
(7, 30),  -- BPJS
(8, 30),  -- STNK
(9, 60),  -- BPKB
(10, 90), -- Sertifikat Tanah
(11, 30), -- SIM Expired
(12, 60); -- Ijazah SMA

-- ============================================================
-- 4. INDEXES FOR PERFORMANCE
-- ============================================================

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_dokumen_user_id ON dokumen(user_id);
CREATE INDEX IF NOT EXISTS idx_dokumen_kategori_id ON dokumen(kategori_id);
CREATE INDEX IF NOT EXISTS idx_dokumen_status ON dokumen(status);
CREATE INDEX IF NOT EXISTS idx_dokumen_tanggal_berakhir ON dokumen(tanggal_berakhir);
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);
CREATE INDEX IF NOT EXISTS idx_kategori_nama ON kategori_dokumen(nama_kategori);
CREATE INDEX IF NOT EXISTS idx_pengingat_dokumen_id ON sistem_pengingat(dokumen_id);

-- ============================================================
-- 5. VIEWS FOR COMMON QUERIES
-- ============================================================

-- View for document statistics
CREATE OR REPLACE VIEW v_document_stats AS
SELECT
    u.user_id,
    u.name as user_name,
    COUNT(d.dokumen_id) as total_documents,
    COUNT(CASE WHEN d.status = 'AKTIF' THEN 1 END) as active_documents,
    COUNT(CASE WHEN d.status = 'AKAN_HABIS' THEN 1 END) as expiring_documents,
    COUNT(CASE WHEN d.status = 'KADALUARSA' THEN 1 END) as expired_documents
FROM user u
LEFT JOIN dokumen d ON u.user_id = d.user_id
GROUP BY u.user_id, u.name;

-- View for expiring documents
CREATE OR REPLACE VIEW v_expiring_documents AS
SELECT
    d.dokumen_id,
    d.nama_dokumen,
    d.tanggal_berakhir,
    d.status,
    u.name as user_name,
    u.email as user_email,
    kd.nama_kategori,
    sp.interval_hari,
    CASE
        WHEN d.tanggal_berakhir < CURRENT_DATE THEN 'EXPIRED'
        WHEN d.tanggal_berakhir <= CURRENT_DATE + INTERVAL '30 days' THEN 'EXPIRING_SOON'
        ELSE 'ACTIVE'
    END as urgency_status
FROM dokumen d
JOIN user u ON d.user_id = u.user_id
LEFT JOIN kategori_dokumen kd ON d.kategori_id = kd.kategori_id
LEFT JOIN sistem_pengingat sp ON d.dokumen_id = sp.dokumen_id
WHERE d.tanggal_berakhir IS NOT NULL
ORDER BY d.tanggal_berakhir ASC;

-- ============================================================
-- 6. SAMPLE QUERIES FOR DEMO
-- ============================================================

-- Query 1: All documents with user and category info
SELECT
    d.nama_dokumen,
    d.tanggal_berakhir,
    d.status,
    u.name as pemilik,
    kd.nama_kategori
FROM dokumen d
JOIN user u ON d.user_id = u.user_id
LEFT JOIN kategori_dokumen kd ON d.kategori_id = kd.kategori_id
ORDER BY d.tanggal_berakhir DESC;

-- Query 2: Documents expiring in next 30 days
SELECT
    d.nama_dokumen,
    d.tanggal_berakhir,
    u.name as pemilik,
    u.email,
    d.tanggal_berakhir - CURRENT_DATE as days_until_expiry
FROM dokumen d
JOIN user u ON d.user_id = u.user_id
WHERE d.tanggal_berakhir BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days'
AND d.status != 'KADALUARSA';

-- Query 3: Document statistics by category
SELECT
    kd.nama_kategori,
    COUNT(d.dokumen_id) as total_docs,
    COUNT(CASE WHEN d.status = 'AKTIF' THEN 1 END) as active,
    COUNT(CASE WHEN d.status = 'AKAN_HABIS' THEN 1 END) as expiring,
    COUNT(CASE WHEN d.status = 'KADALUARSA' THEN 1 END) as expired
FROM kategori_dokumen kd
LEFT JOIN dokumen d ON kd.kategori_id = d.kategori_id
GROUP BY kd.nama_kategori
ORDER BY total_docs DESC;

-- Query 4: User document summary
SELECT
    u.name,
    u.email,
    COUNT(d.dokumen_id) as total_documents,
    COUNT(CASE WHEN d.status = 'AKTIF' THEN 1 END) as active_docs,
    COUNT(CASE WHEN d.status = 'AKAN_HABIS' THEN 1 END) as expiring_docs,
    COUNT(CASE WHEN d.status = 'KADALUARSA' THEN 1 END) as expired_docs
FROM user u
LEFT JOIN dokumen d ON u.user_id = d.user_id
GROUP BY u.user_id, u.name, u.email
ORDER BY total_documents DESC;

-- ============================================================
-- 7. CLEANUP SCRIPTS (Optional)
-- ============================================================

-- To reset all data (uncomment to use)
-- TRUNCATE TABLE sistem_pengingat CASCADE;
-- TRUNCATE TABLE dokumen CASCADE;
-- TRUNCATE TABLE kategori_dokumen CASCADE;
-- TRUNCATE TABLE "user" CASCADE;

-- To reset and re-insert sample data (uncomment to use)
-- \i DATABASE_SETUP.sql

-- ============================================================
-- 8. VERIFICATION QUERIES
-- ============================================================

-- Verify data insertion
SELECT 'Users' as table_name, COUNT(*) as record_count FROM user
UNION ALL
SELECT 'Categories', COUNT(*) FROM kategori_dokumen
UNION ALL
SELECT 'Documents', COUNT(*) FROM dokumen
UNION ALL
SELECT 'Reminders', COUNT(*) FROM sistem_pengingat;

-- Check document status distribution
SELECT status, COUNT(*) as count FROM dokumen GROUP BY status;

-- Check category distribution
SELECT kd.nama_kategori, COUNT(d.dokumen_id) as doc_count
FROM kategori_dokumen kd
LEFT JOIN dokumen d ON kd.kategori_id = d.kategori_id
GROUP BY kd.nama_kategori
ORDER BY doc_count DESC;

-- ============================================================
-- NOTES:
-- 1. Passwords should be properly hashed with BCrypt in production
-- 2. File paths should be adjusted based on actual upload directory
-- 3. Dates can be modified for testing different scenarios
-- 4. This script assumes PostgreSQL database
-- 5. JPA will create tables automatically, but this shows expected structure
-- ============================================================
