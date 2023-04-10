package com.ontop.bank.service;

import com.ontop.bank.exception.EntityNotFoundException;
import com.ontop.bank.exception.GlobalErrorCode;
import com.ontop.bank.exception.InsufficientFundsException;
import com.ontop.bank.model.TransactionType;
import com.ontop.bank.model.dto.BankAccount;
import com.ontop.bank.model.dto.UtilityAccount;
import com.ontop.bank.model.dto.request.FundTransferRequest;
import com.ontop.bank.model.dto.request.UtilityPaymentRequest;
import com.ontop.bank.model.dto.response.FundTransferResponse;
import com.ontop.bank.model.dto.response.UtilityPaymentResponse;
import com.ontop.bank.model.entity.BankAccountEntity;
import com.ontop.bank.model.entity.TransactionEntity;
import com.ontop.bank.repository.BankAccountRepository;
import com.ontop.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final AccountService accountService;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    public FundTransferResponse fundTransfer(FundTransferRequest fundTransferRequest) {

        BankAccount fromBankAccount = accountService.readBankAccount(fundTransferRequest.getFromAccount());
        BankAccount toBankAccount = accountService.readBankAccount(fundTransferRequest.getToAccount());

        //validating account balances
        validateBalance(fromBankAccount, fundTransferRequest.getAmount());

        String transactionId = internalFundTransfer(fromBankAccount, toBankAccount, fundTransferRequest.getAmount());
        return FundTransferResponse.builder().message("Transaction successfully completed").transactionId(transactionId).build();

    }

    public UtilityPaymentResponse utilPayment(UtilityPaymentRequest utilityPaymentRequest) {

        String transactionId = UUID.randomUUID().toString();

        BankAccount fromBankAccount = accountService.readBankAccount(utilityPaymentRequest.getAccount());

        //validating account balances
        validateBalance(fromBankAccount, utilityPaymentRequest.getAmount());

        UtilityAccount utilityAccount = accountService.readUtilityAccount(utilityPaymentRequest.getProviderId());

        BankAccountEntity fromAccount = bankAccountRepository.findByNumber(fromBankAccount.getNumber()).get();

        //we can call third party API to process UTIL payment from payment provider from here.

        //evaluate if is a withdraw or deposit
        BigDecimal amount = utilityPaymentRequest.getAmount();
        int type = amount.signum();

        if (type==1) { //is Deposit

            fromAccount.setActualBalance(fromAccount.getActualBalance().add(utilityPaymentRequest.getAmount()));
            fromAccount.setAvailableBalance(fromAccount.getActualBalance().add(utilityPaymentRequest.getAmount()));

            transactionRepository.save(TransactionEntity.builder().transactionType(TransactionType.DEPOSIT)
                    .account(fromAccount)
                    .transactionId(transactionId)
                    .referenceNumber(utilityPaymentRequest.getReferenceNumber())
                    .amount(utilityPaymentRequest.getAmount()).build());
        }

        if (type==-1) { //is Withdraw

            fromAccount.setActualBalance(fromAccount.getActualBalance().subtract(utilityPaymentRequest.getAmount().negate()));
            fromAccount.setAvailableBalance(fromAccount.getActualBalance().subtract(utilityPaymentRequest.getAmount().negate()));

            transactionRepository.save(TransactionEntity.builder().transactionType(TransactionType.WITHDRAW)
                    .account(fromAccount)
                    .transactionId(transactionId)
                    .referenceNumber(utilityPaymentRequest.getReferenceNumber())
                    .amount(utilityPaymentRequest.getAmount()).build());
        }


        return UtilityPaymentResponse.builder().message("Utility payment successfully completed")
                .transactionId(transactionId).build();

    }

    private void validateBalance(BankAccount bankAccount, BigDecimal amount) {
        if (bankAccount.getActualBalance().compareTo(BigDecimal.ZERO) < 0 || bankAccount.getActualBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in the account " + bankAccount.getNumber(), GlobalErrorCode.INSUFFICIENT_FUNDS);
        }
    }

    public String internalFundTransfer(BankAccount fromBankAccount, BankAccount toBankAccount, BigDecimal amount) {

        String transactionId = UUID.randomUUID().toString();

        BankAccountEntity fromBankAccountEntity = bankAccountRepository.findByNumber(fromBankAccount.getNumber()).orElseThrow(EntityNotFoundException::new);
        BankAccountEntity toBankAccountEntity = bankAccountRepository.findByNumber(toBankAccount.getNumber()).orElseThrow(EntityNotFoundException::new);

        fromBankAccountEntity.setActualBalance(fromBankAccountEntity.getActualBalance().subtract(amount));
        fromBankAccountEntity.setAvailableBalance(fromBankAccountEntity.getActualBalance().subtract(amount));
        bankAccountRepository.save(fromBankAccountEntity);

        transactionRepository.save(TransactionEntity.builder().transactionType(TransactionType.FUND_TRANSFER)
                .referenceNumber(toBankAccountEntity.getNumber())
                .transactionId(transactionId)
                .account(fromBankAccountEntity).amount(amount.negate()).build());

        //apply fee
        BigDecimal fee = new BigDecimal("0.10");
        amount =amount.subtract(amount.multiply(fee));

        toBankAccountEntity.setActualBalance(toBankAccountEntity.getActualBalance().add(amount));
        toBankAccountEntity.setAvailableBalance(toBankAccountEntity.getActualBalance().add(amount));
        bankAccountRepository.save(toBankAccountEntity);

        transactionRepository.save(TransactionEntity.builder().transactionType(TransactionType.FUND_TRANSFER)
                .referenceNumber(toBankAccountEntity.getNumber())
                .transactionId(transactionId)
                .account(toBankAccountEntity).amount(amount).build());

        return transactionId;

    }

}
