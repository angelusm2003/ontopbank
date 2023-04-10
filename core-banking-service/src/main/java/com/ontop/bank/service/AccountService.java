package com.ontop.bank.service;

import com.ontop.bank.exception.EntityNotFoundException;
import com.ontop.bank.model.dto.BankAccount;
import com.ontop.bank.model.dto.UtilityAccount;
import com.ontop.bank.model.entity.BankAccountEntity;
import com.ontop.bank.model.entity.UtilityAccountEntity;
import com.ontop.bank.model.mapper.BankAccountMapper;
import com.ontop.bank.model.mapper.UtilityAccountMapper;
import com.ontop.bank.repository.BankAccountRepository;
import com.ontop.bank.repository.UtilityAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private BankAccountMapper bankAccountMapper = new BankAccountMapper();
    private UtilityAccountMapper utilityAccountMapper = new UtilityAccountMapper();

    private final BankAccountRepository bankAccountRepository;
    private final UtilityAccountRepository utilityAccountRepository;

    public BankAccount readBankAccount(String accountNumber) {
        BankAccountEntity entity = bankAccountRepository.findByNumber(accountNumber).orElseThrow(EntityNotFoundException::new);
        return bankAccountMapper.convertToDto(entity);
    }

    public UtilityAccount readUtilityAccount(String provider) {
        UtilityAccountEntity utilityAccountEntity = utilityAccountRepository.findByProviderName(provider).orElseThrow(EntityNotFoundException::new);
        return utilityAccountMapper.convertToDto(utilityAccountEntity);
    }

    public UtilityAccount readUtilityAccount(Long id){
        return utilityAccountMapper.convertToDto(utilityAccountRepository.findById(id).orElseThrow(EntityNotFoundException::new));
    }

}
