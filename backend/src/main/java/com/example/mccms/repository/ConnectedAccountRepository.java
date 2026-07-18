package com.example.mccms.repository;

import com.example.mccms.model.ConnectedAccount;
import com.example.mccms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConnectedAccountRepository extends JpaRepository<ConnectedAccount, Long> {
    Optional<ConnectedAccount> findByUserAndPlatformName(User user, String platformName);
}
