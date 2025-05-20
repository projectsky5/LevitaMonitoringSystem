package com.levita.levita_monitoring.repository;

import com.levita.levita_monitoring.enums.Role;
import com.levita.levita_monitoring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);

    @Query("""
        SELECT 
            u 
        FROM User u 
        JOIN FETCH UserKpi uk 
            ON u.id = uk.user.id 
        WHERE u.role = :role
    """)
    List<User> findAllAdminsWithKpi(@Param("role") Role role);
}
