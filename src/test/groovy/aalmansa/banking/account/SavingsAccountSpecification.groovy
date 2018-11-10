package aalmansa.banking.account

import aalmansa.banking.account.exception.AccountOverdraftException
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SavingsAccountSpecification extends Specification {

    SavingsAccount  testObj

    def setup() {
        testObj = new SavingsAccount(new BigDecimal(100d), null, 0.1)
    }

    def "should return balance"()  {
        when:
        testObj.deposit(100d)

        then:
        testObj.getBalance() == 200d

        when:
        testObj.withdrawal(50d)

        then:
        testObj.getBalance() == 150d

        when:
        testObj.payInterest()

        then:
        testObj.getBalance() == 165d
    }

    def "should throw AccountOverdraftException when not enough balance"() {
        when:
        testObj.withdrawal(1000d)

        then:
        thrown(AccountOverdraftException)
    }

    def "should allow withdrawals and deposits in parallel"() {
        given:
        ExecutorService executorService1 = Executors.newFixedThreadPool(10)
        ExecutorService executorService2 = Executors.newFixedThreadPool(10)

        (1..10000).each {
            executorService1.submit({
                testObj.withdrawal(new BigDecimal(0.0002))
            })
            executorService2.submit({
                testObj.deposit(new BigDecimal(0.0001))
            })
        }

        executorService1.shutdown()
        executorService2.shutdown()
        executorService1.awaitTermination(5, TimeUnit.SECONDS)
        executorService2.awaitTermination(5, TimeUnit.SECONDS)

        expect:
        testObj.getBalance() == 99d
    }
}
