/*
bank-core 더미데이터

user
<들어가야하는 것>
- worket user
- 실제 사용자 데이터

account 한 유저에 여러개 가능
- worket account
- user account -> 계좌 상태, 잔액 나눠서
*/

-- 1. User 데이터

-- worket 유저
INSERT INTO users (itscno, customer_name, resident_reg_number_hash, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('WK', '주식회사 워켓', 'HASH_MERCHANT_RRN', '010-1234-5678', 'CORPORATE', 'ACTIVE', NOW(), NOW());

-- 우리은행 모계좌 유저
INSERT INTO users (itscno, customer_name, resident_reg_number_hash, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('WOORI_20', '우리은행 모계좌', 'HASH_MERCHANT_RRN', '010-2222-2222', 'CORPORATE', 'ACTIVE', NOW(), NOW());

-- 일반 유저
INSERT INTO users (itscno, customer_name, resident_reg_number_hash, ci, di, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('USER_001', '신수연', 'HASH_PAYEE_RRN', 'CI_PAYEE_88CHARS', 'DI_PAYEE_64CHARS', '010-1111-1111', 'INDIVIDUAL', 'ACTIVE', NOW(), NOW());

INSERT INTO users (itscno, customer_name, resident_reg_number_hash, ci, di, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('USER_002', '김윤미', 'HASH_PAYEE_RRN', 'CI_PAYEE_88CHARS', 'DI_PAYEE_64CHARS', '010-2222-2222', 'INDIVIDUAL', 'ACTIVE', NOW(), NOW());

INSERT INTO users (itscno, customer_name, resident_reg_number_hash, ci, di, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('USER_003', '김윤서', 'HASH_PAYEE_RRN', 'CI_PAYEE_88CHARS', 'DI_PAYEE_64CHARS', '010-3333-3333', 'INDIVIDUAL', 'ACTIVE', NOW(), NOW());

INSERT INTO users (itscno, customer_name, resident_reg_number_hash, ci, di, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('USER_004', '남기연', 'HASH_PAYEE_RRN', 'CI_PAYEE_88CHARS', 'DI_PAYEE_64CHARS', '010-4444-4444', 'INDIVIDUAL', 'ACTIVE', NOW(), NOW());

INSERT INTO users (itscno, customer_name, resident_reg_number_hash, ci, di, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('USER_005', '신경남', 'HASH_PAYEE_RRN', 'CI_PAYEE_88CHARS', 'DI_PAYEE_64CHARS', '010-5555-5555', 'INDIVIDUAL', 'ACTIVE', NOW(), NOW());

INSERT INTO users (itscno, customer_name, resident_reg_number_hash, ci, di, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('USER_006', '이영은', 'HASH_PAYEE_RRN', 'CI_PAYEE_88CHARS', 'DI_PAYEE_64CHARS', '010-6666-6666', 'INDIVIDUAL', 'ACTIVE', NOW(), NOW());

-- worket 더미 클라이언트
INSERT INTO users (itscno, customer_name, resident_reg_number_hash, ci, di, phone_number, customer_type, status, registered_at, updated_at)
VALUES
    -- 1. 유정호 (worekt user 101)
    (10101, '유정호', 'e9955745d1be8e9d22f183c509b557d1b827732a3d0f0d6e60b0d39e802058b7',
    'CI_HASH_10101_DUMMY', 'DI_HASH_10101_DUMMY', '010-1111-1111', 'INDIVIDUAL', 'ACTIVE',
    DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
    -- 2. 유승한 (worekt user 102)
    (10102, '유승한', 'b5321f8c09a80e7d5f4c3b2a1e0d9c8b7a6f5e4d3c2b1a0987654321fedcba98',
    'CI_HASH_10102_DUMMY', 'DI_HASH_10102_DUMMY', '010-2222-2222', 'INDIVIDUAL', 'ACTIVE',
    DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),
    -- 3. 김하영 (worekt user 103)
    (10103, '김하영', '7d483c6f2a1e0b9d8c7b6a5f4e3d2c1b0a9f8e7d6c5b4a3928170654321fedcba',
    'CI_HASH_10103_DUMMY', 'DI_HASH_10103_DUMMY', '010-3333-3333', 'INDIVIDUAL', 'ACTIVE',
    DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),
    -- 4. 이시현 (worekt user 104)
    (10104, '이시현', 'f6c9a0b1d3e2f4g5h6i7j8k9l0m1n2o3p4q5r6s7t8u9v0w1x2y3z4a5b6c7d8e9',
    'CI_HASH_10104_DUMMY', 'DI_HASH_10104_DUMMY', '010-1111-1111', 'INDIVIDUAL', 'ACTIVE',
    DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
    -- 5. 심미경 (worekt user 105)
    (10105, '심미경', 'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2',
    'CI_HASH_10105_DUMMY', 'DI_HASH_10105_DUMMY', '010-2222-2222', 'INDIVIDUAL', 'ACTIVE',
    DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
    -- 6. 백인호 (worekt user 106)
    (10106, '백인호', '5f4e3d2c1b0a9d8c7b6a5f4e3d2c1b0a9f8e7d6c5b4a3928170654321fedcba9',
    'CI_HASH_10106_DUMMY', 'DI_HASH_10106_DUMMY', '010-1111-1111', 'INDIVIDUAL', 'ACTIVE',
    DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
    -- 7. 공윤호 (worekt user 107)
    (10107, '공윤호', '9d8c7b6a5f4e3d2c1b0a9f8e7d6c5b4a3928170654321fedcba987654321fedcba',
    'CI_HASH_10107_DUMMY', 'DI_HASH_10107_DUMMY', '010-2222-2222', 'INDIVIDUAL', 'ACTIVE',
    DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
    -- 8. 강유민 (worekt user 108)
    (10108, '강유민', '3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d',
    'CI_HASH_10108_DUMMY', 'DI_HASH_10108_DUMMY', '010-3333-3333', 'INDIVIDUAL', 'ACTIVE',
    DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());

-- 2. Account 데이터

-- 우리은행 모계좌
INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('WOORI_20', 'WOORI_20', '0200020000222', 'CHECKING', 1000000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1);

INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('PLATFORM_ACC_001', 'WK', '0201111111111', 'CHECKING', 1000000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'CUSTOMER', 1);

-- status = ACTIVE
INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('ACC_001', 'USER_001', '0100100001', 'CHECKING', 10000000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'CUSTOMER', 1);

-- status = SUSPENDED
INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('ACC_001', 'USER_001', '0100200001', 'CHECKING', 50000.00, 'SUSPENDED', NOW(), NOW(), NOW(), 'CUSTOMER', 1);

-- status = CLOSED
INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('ACC_001', 'USER_001', '0100300001', 'CHECKING', 50000.00, 'CLOSED', NOW(), NOW(), NOW(), 'CUSTOMER', 1);

-- status = ACTIVE, VAL = 0
INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('ACC_001', 'USER_001', '0100100001', 'CHECKING', 0.00, 'ACTIVE', NOW(), NOW(), NOW(), 'CUSTOMER', 1);

-- worket 더미 유저의 계좌

INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES
-- itscno 10101 (유정호) 계좌 - 0100101010101
('ACCOUNT_10101', '10101', '0100101010101', 'CHECKING', 7500000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1),
-- itscno 10102 (유승한) 계좌 - 0100102010202
('ACCOUNT_10102', '10102', '0100102010202', 'CHECKING', 5200000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1),
-- itscno 10103 (김하영) 계좌 - 0100103010303
('ACCOUNT_10103', '10103', '0100103010303', 'CHECKING', 12000000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1),
-- itscno 10104 (이시현) 계좌 - 0100104010404
('ACCOUNT_10104', '10104', '0100104010404', 'CHECKING', 6500000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1),
-- itscno 10105 (심미경) 계좌 - 0100105010505
('ACCOUNT_10105', '10105', '0100105010505', 'CHECKING', 8100000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1),
-- itscno 10106 (백인호) 계좌 - 0100106010606
('ACCOUNT_10106', '10106', '0100106010606', 'CHECKING', 4500000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1),
-- itscno 10107 (공윤호) 계좌 - 0100107010707
('ACCOUNT_10107', '10107', '0100107010707', 'CHECKING', 9300000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1),
-- itscno 10108 (강유민) 계좌 - 0100108010808
('ACCOUNT_10108', '10108', '0100108010808', 'CHECKING', 15000000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1);