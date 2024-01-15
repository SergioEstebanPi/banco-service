package com.cqrs.banco.query.repository;

import com.cqrs.banco.query.entities.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<AccountTransaction, Long> {

    AccountTransaction findTop1ByAccountIdOrderByTimestampDesc(String accountId);

}
