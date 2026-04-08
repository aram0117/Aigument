package com.example.aigument.domain.user.repoistory;

import com.example.aigument.domain.user.entity.UserStats;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<UserStats> findByUserId(Long userId);
}
