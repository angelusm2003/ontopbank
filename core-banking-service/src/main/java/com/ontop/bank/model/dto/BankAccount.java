package com.ontop.bank.model.dto;

import com.ontop.bank.model.AccountStatus;
import com.ontop.bank.model.AccountType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankAccount {

    private Long id;
    private String number;
    private AccountType type;
    private AccountStatus status;
    private BigDecimal availableBalance;
    private BigDecimal actualBalance;
    private User user;

}
