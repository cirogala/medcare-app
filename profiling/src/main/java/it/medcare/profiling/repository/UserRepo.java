package it.medcare.profiling.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.medcare.profiling.entity.User;

public interface UserRepo extends JpaRepository<User, Long> {
	
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
    
    Optional <User> findByUsername (String username);
    Optional<User> findByEmail(String email);
    
    List<User> findAllByIsMed(Boolean isMed);
    
    List<User> findAllByIsMedAndIsInternal(Boolean isMed, Boolean isInternal);

    List<User> findAllByIsInternal(Boolean isInternal);
	
}
