package fpt.qn.project_management_system.user.repository;

import java.util.Optional;

import fpt.qn.project_management_system.common.dto.PaginationResult;
import fpt.qn.project_management_system.common.repository.Repository;
import fpt.qn.project_management_system.jooq.enums.SysRole;
import fpt.qn.project_management_system.jooq.enums.UserStatus;
import fpt.qn.project_management_system.jooq.tables.records.UsersRecord;

public interface UserRepository extends Repository<UsersRecord> {

    Optional<UsersRecord> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    PaginationResult<UsersRecord> findAll(String keyword, SysRole role, UserStatus status, int page, int size);

}
