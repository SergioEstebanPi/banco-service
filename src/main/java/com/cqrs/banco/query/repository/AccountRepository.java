package com.cqrs.banco.query.repository;

import com.cqrs.banco.query.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}
