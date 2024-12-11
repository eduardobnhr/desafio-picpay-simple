package br.com.picpay.picpay_desafio_backend.wallet;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;

public record Wallet(
    @Id long id,
    String fullName,
    Long cpf,
    String email,
    String password,
    int type,
    BigDecimal balance
) {

    public Wallet debit(BigDecimal value) { 
        return new Wallet(id, fullName, cpf, email, password, type, balance.subtract(value));
    }
    
}
