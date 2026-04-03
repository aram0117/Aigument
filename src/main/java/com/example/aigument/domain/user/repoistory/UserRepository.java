package com.example.aigument.domain.user.repoistory;

import com.example.aigument.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByNickName(String nickName);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderId(String provider);
}
