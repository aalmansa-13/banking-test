package aalmansa.banking.account;

import aalmansa.banking.account.exception.AccountOverdraftException;
import aalmansa.banking.customer.Person;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BasicAccount implements Account {
    private Person owner;
    private final AtomicReference<BigDecimal> balance;

    public BasicAccount(BigDecimal balance, Person owner) {
        this.balance = new AtomicReference<>(balance);
        this.owner = owner;
    }

    public BigDecimal getBalance() {
        return balance.get();
    }

    @Override
    public void withdrawal(BigDecimal amount) {
        BigDecimal currentBalance;
        do {
            currentBalance = balance.get();
            if (currentBalance.compareTo(amount) < 0) {
                throw new AccountOverdraftException();
            }
        } while (!balance.compareAndSet(currentBalance, currentBalance.add(amount.negate())));
    }

    @Override
    public void deposit(BigDecimal amount) {
        BigDecimal currentBalance;
        do {
            currentBalance = balance.get();
        } while (!balance.compareAndSet(currentBalance, currentBalance.add(amount)));
    }
}
