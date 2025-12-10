package com.bank.deposit.domain;

import com.bank.deposit.domain.enums.UserStatus;
import com.bank.deposit.domain.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "itscno", length = 20, nullable = false)
    private String itscno;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "resident_reg_number_hash", length = 200)
    private String residentRegNumberHash;

    @Column(name = "ci", length = 88)
    private String ci;

    @Column(name = "di", length = 64)
    private String di;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private UserType userType = UserType.INDIVIDUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}