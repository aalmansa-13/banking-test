package aalmansa.banking.account;

import java.math.BigDecimal;

public interface Account {
    void withdrawal(BigDecimal amount);
    void deposit(BigDecimal amount);
}

