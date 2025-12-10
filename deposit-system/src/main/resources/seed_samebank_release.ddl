

-- 1. User 데이터
INSERT INTO users (itscno, customer_name, customer_type, status, registered_at, updated_at)
VALUES ('BANK_CORP_001', '우리은행 에스크로 관리법인', 'CORPORATE', 'ACTIVE', NOW(), NOW());

INSERT INTO users (itscno, customer_name, resident_reg_number_hash, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('MERCHANT_001', '주식회사 워켓', 'HASH_MERCHANT_RRN', '02-1234-5678', 'INDIVIDUAL', 'ACTIVE', NOW(), NOW());

INSERT INTO users (itscno, customer_name, resident_reg_number_hash, ci, di, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('PAYEE_001', '김수취', 'HASH_PAYEE_RRN', 'CI_PAYEE_88CHARS', 'DI_PAYEE_64CHARS', '010-9876-5432', 'INDIVIDUAL', 'ACTIVE', NOW(), NOW());

-- 2. Account 데이터
INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('BANK_ESC_ACC_001', 'BANK_CORP_001', '020-12345-67890', 'CHECKING', 10000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1);

INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('PLATFORM_ACC_001', 'MERCHANT_001', '020-11111-11111', 'CHECKING', 1000000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'CUSTOMER', 1);

INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('PAYEE_ACC_001', 'PAYEE_001', '020-99999-99999', 'CHECKING', 50000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'CUSTOMER', 1);

-- 3. EscrowAccount 데이터
INSERT INTO escrow_accounts (
    escrow_account_id, account_id, trace_id, hold_amount,
    payer_bank_code, payer_account, payer_name,
    payee_bank_code, payee_account, payee_name,
    scheduled_release_date, expired_date, hold_status, release_type,
    platform_fee, payment_amount, hold_start_datetime,
    merchant_id, merchant_order_no
) VALUES (
             'ESC_2024120100001', 'BANK_ESC_ACC_001', 'TRACE_2024120100001', 10000.00,
             '020', '020-77777-77777', '이지급',
             '020', '020-99999-99999', '김수취',
             DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', 'MANUAL',
             0.03, 9700.00, NOW(),
             'MERCHANT_001', 'ORDER_20241201_001'
         );