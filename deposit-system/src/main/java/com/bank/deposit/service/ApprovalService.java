package com.bank.deposit.service;

import com.bank.common.exception.CustomException;
import com.bank.common.exception.ErrorCode;
import com.bank.deposit.domain.EscrowAccount;
import com.bank.deposit.domain.enums.BankCode;
import com.bank.deposit.dto.ApprovalRequest;
import com.bank.deposit.dto.ApprovalResponse;
import com.bank.deposit.repository.EscrowAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalService {
    private final LedgerService ledgerService;
    private final EscrowAccountRepository escrowAccountRepository;
    private final ApprovalTokenService approvalTokenService;
    @Transactional
    public ApprovalResponse recodeLedger(ApprovalRequest request){
        //1.토큰 검증
        String confirmToken = request.getConfirmToken();
        if(!approvalTokenService.isValid(confirmToken)){
            throw new CustomException(ErrorCode.VALIDATION_TOKEN_EXPIRED);
        }
        //2. 에스크로 계좌 PK로 지급인 은행 코드 조회
        EscrowAccount escrowAccount = escrowAccountRepository.findById(request.getEscrowId())
                .orElseThrow(()->new CustomException(ErrorCode.INVALID_INPUT_VALUE));

        //3. 당/타행 분기
        String bankCode = escrowAccount.getPayerBankCode();
        //3.1 당행
        if(bankCode.equals(BankCode.OUR_BANK.getCode())){
            ledgerService.recodeSameBank(escrowAccount);
        }
        //3.2 타행
        else{
            ledgerService.recodeDifferentBank(escrowAccount);
        }

        ApprovalResponse approvalResponse = new ApprovalResponse();
        approvalResponse.setEscrowId(request.getEscrowId());
        return approvalResponse;
    }
}
