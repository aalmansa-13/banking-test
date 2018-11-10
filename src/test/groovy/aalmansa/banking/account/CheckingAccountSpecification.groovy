package aalmansa.banking.account

import aalmansa.banking.account.exception.AccountOverdraftException
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CheckingAccountSpecification extends Specification {
    CheckingAccount testObj
    CheckingAccount destinationAccount
    CheckingAccount destinationAccountMock = Mock()

    def setup() {
        testObj = new CheckingAccount(new BigDecimal(100d), null, new BigDecimal(-50))
        destinationAccount = new CheckingAccount(new BigDecimal(200d), null, new BigDecimal(-100))
    }

    def "should allow withdrawals within the overdraft limit"() {
        when:
        testObj.withdrawal(110)

        then:
        testObj.getBalance() == -10d
    }

    def "should not allow withdrawals beyond the overdraft limit"() {
        when:
        testObj.withdrawal(160)

        then:
        thrown(AccountOverdraftException)
    }

    def "should transfer money between two checking accounts"() {
        when:
        testObj.transfer(destinationAccount, 100)

        then:
        testObj.getBalance() == 0
        destinationAccount.getBalance() == 300d
    }

    def "should not allow transfer when account is overdraft"() {
        when:
        testObj.transfer(destinationAccount, 200)

        then:
        thrown(AccountOverdraftException)
        destinationAccount.getBalance() == 200d
    }

    def "should deposit back the amount when transfer fails in the destination account"() {
        given:
        destinationAccountMock.deposit(50) >> { throw new RuntimeException() }

        when:
        testObj.transfer(destinationAccountMock, 50)

        then:
        testObj.getBalance() == 100d
    }

    def "should allow transfers, deposits and withdrawals in parallel"() {

        def account1 = new CheckingAccount(new BigDecimal(1000.25d), null, new BigDecimal(-50))
        def account2 = new CheckingAccount(new BigDecimal(2500.56d), null, new BigDecimal(-100))


        when:
        ExecutorService executorService1 = Executors.newFixedThreadPool(10)
        ExecutorService executorService2 = Executors.newFixedThreadPool(10)
        ExecutorService executorService3 = Executors.newFixedThreadPool(10)
        ExecutorService executorService4 = Executors.newFixedThreadPool(10)

        (1..10000).each {
            executorService1.submit({
                account1.transfer(account2, new BigDecimal(0.01))
            })

            executorService2.submit({
                account2.transfer(account1, new BigDecimal(0.02))
            })

            executorService3.submit({
                account2.withdrawal(new BigDecimal(0.03))
            })

            executorService4.submit({
                account1.deposit(new BigDecimal(0.005))
            })

        }

        executorService1.shutdown()
        executorService2.shutdown()
        executorService3.shutdown()
        executorService4.shutdown()
        executorService1.awaitTermination(5, TimeUnit.SECONDS)
        executorService2.awaitTermination(5, TimeUnit.SECONDS)
        executorService3.awaitTermination(5, TimeUnit.SECONDS)
        executorService4.awaitTermination(5, TimeUnit.SECONDS)

        then:
        account1.getBalance() == 1150.25d
        account2.getBalance() == 2100.56d
    }

}
