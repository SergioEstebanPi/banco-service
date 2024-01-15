package com.cqrs.banco.commands.aggregates;

import com.cqrs.banco.commonapi.commands.CreateAccountCommand;
import com.cqrs.banco.commonapi.commands.CreditAccountCommand;
import com.cqrs.banco.commonapi.commands.DebitAccountCommand;
import com.cqrs.banco.commonapi.enums.AccountStatus;
import com.cqrs.banco.commonapi.events.AccountCreatedEvent;
import com.cqrs.banco.commonapi.events.AccountCreditEvent;
import com.cqrs.banco.commonapi.events.AccountDebitEvent;
import com.cqrs.banco.commonapi.exceptions.NegativeInitialBalanceException;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
@Slf4j
public class AccountAggregate {

    @AggregateIdentifier
    private String accountId;
    private String currency;
    private double balance;
    private AccountStatus status;

    public AccountAggregate(){} // requisito axon

    @CommandHandler
    public AccountAggregate(CreateAccountCommand createAccountCommand){
        log.info("CreateAccountCommand recibido");
        if(createAccountCommand.getInitialBalance() < 0){
            throw new NegativeInitialBalanceException("Error, no se puede tener un saldo negativo");
        }
        AggregateLifecycle.apply(new AccountCreatedEvent(
                createAccountCommand.getId(),
                createAccountCommand.getCurrency(),
                createAccountCommand.getInitialBalance(),
                AccountStatus.CREATED
        ));
    }

    @EventSourcingHandler
    public void on(AccountCreatedEvent accountCreatedEvent){
        log.info("Evento AccountCreatedEvent");
        this.accountId = accountCreatedEvent.getId();
        this.currency = accountCreatedEvent.getCurrency();
        this.balance = accountCreatedEvent.getBalance();
        this.status = accountCreatedEvent.getStatus();
    }

    @CommandHandler
    public void handle(CreditAccountCommand creditAccountCommand){
        log.info("CreditAccountCommand recibido");
        if(creditAccountCommand.getAmount() < 0) {
            throw new NegativeInitialBalanceException("Error, no se puede tener un saldo negativo");
        }
        AggregateLifecycle.apply(new AccountCreditEvent(
                creditAccountCommand.getId(),
                creditAccountCommand.getCurrency(),
                creditAccountCommand.getAmount()
        ));
    }

    @EventSourcingHandler
    public void on(AccountCreditEvent accountCreditEvent){
        log.info("Evento AccountCreditEvent");
        this.balance += accountCreditEvent.getAmount();
    }

    @CommandHandler
    public void handle(DebitAccountCommand debitAccountCommand){
        log.info("DebitAccountCommand recibido");
        if(debitAccountCommand.getAmount() < 0) {
            throw new NegativeInitialBalanceException("Error, no se puede tener un saldo negativo");
        }
        if(debitAccountCommand.getAmount() > this.balance) {
            throw new RuntimeException("Error, saldo insuficiente");
        }
        AggregateLifecycle.apply(new AccountDebitEvent(
                debitAccountCommand.getId(),
                debitAccountCommand.getCurrency(),
                debitAccountCommand.getAmount()
        ));
    }

    @EventSourcingHandler
    public void on(DebitAccountCommand debitAccountCommand){
        log.info("Evento DebitAccountCommand");
        this.balance -= debitAccountCommand.getAmount();
    }
}
