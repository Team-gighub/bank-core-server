package com.bank.deposit.repository;

import com.bank.deposit.domain.EscrowAccount;
import com.bank.deposit.domain.enums.HoldStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// JpaRepository를 상속받아 기본 CRUD 메서드를 제공받습니다.
@Repository
public interface EscrowAccountRepository extends JpaRepository<EscrowAccount, String> {

    /**
     * 에스크로 계좌 ID로 비관적 락을 걸고 조회
     * 동시성 제어를 위해 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EscrowAccount> findWithLockByEscrowAccountId(String escrowAccountId);

}
