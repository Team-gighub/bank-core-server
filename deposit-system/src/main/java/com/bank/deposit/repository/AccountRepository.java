package com.bank.deposit.repository;

import com.bank.deposit.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// JpaRepository를 상속받아 기본 CRUD 메서드를 제공받습니다.
@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    // 필요한 경우 추가적인 조회 메서드를 정의할 수 있습니다.
}
