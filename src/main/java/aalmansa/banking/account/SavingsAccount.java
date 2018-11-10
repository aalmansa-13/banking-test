package aalmansa.banking.account;

import aalmansa.banking.customer.Person;

import java.math.BigDecimal;

public class SavingsAccount extends BasicAccount {

    private final double interestRate;

    public SavingsAccount(BigDecimal balance, Person owner, double interestRate) {
        super(balance, owner);
        this.interestRate = interestRate;
    }

    public void payInterest() {
        deposit(getBalance().multiply(new BigDecimal(interestRate)));
    }
}
