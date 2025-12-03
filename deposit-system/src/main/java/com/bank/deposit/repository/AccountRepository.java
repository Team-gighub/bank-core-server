package com.bank.deposit.repository;

import com.bank.deposit.domain.Account;
import com.bank.deposit.domain.enums.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// JpaRepository를 상속받아 기본 CRUD 메서드를 제공받습니다.
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    /**
     * 계좌번호로 계좌 조회
     * 수취인 계좌를 찾을 때 사용
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * 계좌번호로 비관적 락을 걸고 조회
     * 수취인 계좌 조회 시 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockByAccountNumber(String accountNumber);


    /**
     * 계좌 ID로 비관적 락을 걸고 조회
     * 잔액 변경이 필요한 경우 동시성 제어를 위해 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockByAccountId(String accountId);

    /**
     * User의 itscno와 계좌 타입으로 비관적 락을 걸고 조회
     * 플랫폼 계좌 조회 시 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockByUserItscnoAndAccountType(String itscno, AccountType accountType);

}
