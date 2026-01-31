package it.medcare.profiling.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.medcare.profiling.entity.MdtRole;
import it.medcare.profiling.enums.RoleType;

@Repository
public interface MdtRoleRepo extends JpaRepository<MdtRole, Long> {

    Optional<MdtRole> findByRoleType(RoleType roleType);
}

