-- ============================================
-- 타행 이체 테스트용 데이터
-- ============================================

-- 1. User 데이터
-- 1-1. 우리은행 에스크로 관리법인
INSERT INTO users (itscno, customer_name, customer_type, status, registered_at, updated_at)
VALUES ('BANK_CORP_001', '우리은행 에스크로 관리법인', 'CORPORATE', 'ACTIVE', NOW(), NOW());

-- 1-2. 플랫폼(고객사) - 워켓
INSERT INTO users (itscno, customer_name, resident_reg_number_hash, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('MERCHANT_001', '주식회사 워켓', 'HASH_MERCHANT_RRN', '02-1234-5678', 'INDIVIDUAL', 'ACTIVE', NOW(), NOW());

-- 1-3. 지급인
INSERT INTO users (itscno, customer_name, resident_reg_number_hash, ci, di, phone_number, customer_type, status, registered_at, updated_at)
VALUES ('PAYER_001', '이지급', 'HASH_PAYER_RRN', 'CI_PAYER_88CHARS', 'DI_PAYER_64CHARS', '010-1111-2222', 'INDIVIDUAL', 'ACTIVE', NOW(), NOW());

-- 1-4. 수취인 (타행 계좌 보유자)


-- 2. Account 데이터
-- 2-1. 우리은행 법인의 에스크로용 모계좌
INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('BANK_ESC_ACC_001', 'BANK_CORP_001', '020-12345-67890', 'CHECKING', 10000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'BANK_SYSTEM', 1);

-- 2-2. 플랫폼 계좌 (수수료 입금용)
INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('PLATFORM_ACC_001', 'MERCHANT_001', '020-11111-11111', 'CHECKING', 1000000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'CUSTOMER', 1);

-- 2-3. 지급인 계좌 (당행)
INSERT INTO accounts (account_id, itscno, account_number, account_type, balance, status, open_date, created_at, updated_at, created_by, version)
VALUES ('PAYER_ACC_001', 'PAYER_001', '020-77777-77777', 'CHECKING', 500000.00, 'ACTIVE', NOW(), NOW(), NOW(), 'CUSTOMER', 1);

-- 2-4. 수취인 계좌는 타행이므로 우리 DB에 없음 (외부 은행 계좌)


-- 3. EscrowAccount 데이터 (타행 수취인)
INSERT INTO escrow_accounts (
    escrow_account_id, account_id, trace_id, hold_amount,
    payer_bank_code, payer_account, payer_name,
    payee_bank_code, payee_account, payee_name,
    scheduled_release_date, expired_date, hold_status, release_type,
    platform_fee, payment_amount, hold_start_datetime,
    merchant_id, merchant_order_no
) VALUES (
             'ESC_EXTERNAL_001',           -- 에스크로 계좌 ID
             'BANK_ESC_ACC_001',           -- 우리은행 에스크로 계좌
             'TRACE_EXT_2024120100001',   -- 추적 ID
             10000.00,                     -- 보유 금액
             '020',                        -- 지급인 은행 코드 (우리은행)
             '020-77777-77777',            -- 지급인 계좌번호
             '이지급',                     -- 지급인 이름
             '004',                        -- 수취인 은행 코드 (국민은행 - 타행)
             '004-88888-88888',            -- 수취인 계좌번호 (타행)
             '박타행',                     -- 수취인 이름
             DATE_ADD(NOW(), INTERVAL 7 DAY),   -- 예정 지급일
             DATE_ADD(NOW(), INTERVAL 30 DAY),  -- 만료일
             'ACTIVE',                     -- 보유 상태
             'MANUAL',                     -- 해지 유형
             0.03,                         -- 플랫폼 수수료 비율 (3%)
             9700.00,                      -- 수취인 실수령액 (10000 - 300)
             NOW(),                        -- 보유 시작 시간
             'MERCHANT_001',               -- 고객사 ID
             'ORDER_EXTERNAL_20241201_001' -- 주문 번호
         );