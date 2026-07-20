package fpt.qn.pms.user.repository;

import java.util.Optional;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;

public interface UserRepository extends Repository<UsersRecord> {

    Optional<UsersRecord> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    PaginationResult<UsersRecord> findAll(String keyword, SysRole role, UserStatus status, int page, int size);

}
