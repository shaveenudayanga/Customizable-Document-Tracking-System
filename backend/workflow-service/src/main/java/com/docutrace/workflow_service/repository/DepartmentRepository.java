package com.docutrace.workflow_service.repository;

import com.docutrace.workflow_service.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByKey(String key);
    boolean existsByKey(String key);
}
