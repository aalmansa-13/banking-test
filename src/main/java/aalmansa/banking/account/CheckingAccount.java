package aalmansa.banking.account;

import aalmansa.banking.customer.Person;

import java.math.BigDecimal;

public class CheckingAccount extends BasicAccount {
    private final BigDecimal overDraftLimit;

    public CheckingAccount(BigDecimal balance, Person owner, BigDecimal overdraftLimit) {
        super(balance.add(overdraftLimit.negate()), owner);
        this.overDraftLimit = overdraftLimit;
    }

    @Override
    public BigDecimal getBalance() {
        return super.getBalance().add(overDraftLimit);
    }

    public void transfer(CheckingAccount destinationAccount, BigDecimal amount) {
        this.withdrawal(amount);
        try {
            destinationAccount.deposit(amount);
        } catch (Exception e) {
            this.deposit(amount);
        }
    }
}
