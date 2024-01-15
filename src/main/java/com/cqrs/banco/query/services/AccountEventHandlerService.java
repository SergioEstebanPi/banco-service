package com.cqrs.banco.query.services;

import com.cqrs.banco.commonapi.enums.TransactionType;
import com.cqrs.banco.commonapi.events.AccountCreatedEvent;
import com.cqrs.banco.commonapi.events.AccountCreditEvent;
import com.cqrs.banco.commonapi.events.AccountDebitEvent;
import com.cqrs.banco.query.dto.AccountWatchEvent;
import com.cqrs.banco.query.entities.Account;
import com.cqrs.banco.query.entities.AccountTransaction;
import com.cqrs.banco.query.queries.GetAccountBalanceStream;
import com.cqrs.banco.query.repository.AccountRepository;
import com.cqrs.banco.query.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class AccountEventHandlerService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private QueryUpdateEmitter queryUpdateEmitter;

    @EventHandler
    public void on(AccountCreatedEvent accountCreatedEvent, EventMessage<AccountCreatedEvent> eventEventMessage){
        log.info("***********************");
        log.info("AccountCreatedEvent recibido");
        Account account = new Account();
        account.setId(accountCreatedEvent.getId());
        account.setCurrency(accountCreatedEvent.getCurrency());
        account.setBalance(accountCreatedEvent.getBalance());
        account.setStatus(accountCreatedEvent.getStatus());
        account.setCreatedAt(eventEventMessage.getTimestamp());
        accountRepository.save(account);
    }

    @EventHandler
    public void on(AccountCreditEvent accountCreditEvent, EventMessage<AccountCreditEvent> eventEventMessage){
        log.info("***********************");
        log.info("AccountCreditEvent recibido");
        Account account = accountRepository.findById(accountCreditEvent.getId()).get();
        AccountTransaction accountTransaction = AccountTransaction.builder()
                .account(account)
                .amount(accountCreditEvent.getAmount())
                .transactionType(TransactionType.CREDIT)
                .timestamp(eventEventMessage.getTimestamp())
                .build();
        transactionRepository.save(accountTransaction);
        account.setBalance(account.getBalance() + accountCreditEvent.getAmount());
        accountRepository.save(account);

        AccountWatchEvent accountWatchEvent = new AccountWatchEvent(
                accountTransaction.getTimestamp(),
                account.getId(),
                account.getBalance(),
                accountTransaction.getTransactionType(),
                accountTransaction.getAmount()
        );

        queryUpdateEmitter.emit(GetAccountBalanceStream.class, (query) ->
                (query.getAccountId().equals(account.getId())), accountWatchEvent);
    }

    @EventHandler
    public void on(AccountDebitEvent accountDebitEvent, EventMessage<AccountDebitEvent> eventEventMessage){
        log.info("***********************");
        log.info("AccountDebitEvent recibido");
        Account account = accountRepository.findById(accountDebitEvent.getId()).get();
        AccountTransaction accountTransaction = AccountTransaction.builder()
                .account(account)
                .amount(accountDebitEvent.getAmount())
                .transactionType(TransactionType.DEBIT)
                .timestamp(eventEventMessage.getTimestamp())
                .build();
        transactionRepository.save(accountTransaction);
        account.setBalance(account.getBalance() - accountDebitEvent.getAmount());
        accountRepository.save(account);

        AccountWatchEvent accountWatchEvent = new AccountWatchEvent(
                accountTransaction.getTimestamp(),
                account.getId(),
                account.getBalance(),
                accountTransaction.getTransactionType(),
                accountTransaction.getAmount()
        );

        queryUpdateEmitter.emit(GetAccountBalanceStream.class, (query) ->
                (query.getAccountId().equals(account.getId())), accountWatchEvent);
    }

}
