package fpt.qn.project_management_system.user.repository;

import static fpt.qn.project_management_system.jooq.Tables.USERS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.project_management_system.common.dto.PaginationResult;
import fpt.qn.project_management_system.common.repository.BaseRepository;
import fpt.qn.project_management_system.jooq.enums.SysRole;
import fpt.qn.project_management_system.jooq.enums.UserStatus;
import fpt.qn.project_management_system.jooq.tables.records.UsersRecord;

@Repository
public class UserRepositoryImpl extends BaseRepository<UsersRecord> implements UserRepository {

    public UserRepositoryImpl(DSLContext dsl) {
        super(dsl, USERS);
    }

    @Override
    public Optional<UsersRecord> findById(UUID id) {
        return dsl.selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOptional();
    }

    @Override
    public Optional<UsersRecord> findByUsername(String username) {
        return dsl.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOptional();
    }

    @Override
    public boolean existsByUsername(String username) {
        return dsl.fetchExists(USERS, USERS.USERNAME.eq(username));
    }

    @Override
    public boolean existsByEmail(String email) {
        return dsl.fetchExists(USERS, USERS.EMAIL.eq(email));
    }

    @Override
    public boolean existsByEmployeeId(String employeeId) {
        return dsl.fetchExists(USERS, USERS.EMPLOYEE_ID.eq(employeeId));
    }

    @Override
    public PaginationResult<UsersRecord> findAll(String keyword, SysRole role, UserStatus status, int page, int size) {
        Condition condition = buildCondition(keyword, role, status);

        long total = dsl.fetchCount(USERS, condition);

        List<UsersRecord> items = dsl.selectFrom(USERS)
                .where(condition)
                .orderBy(USERS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch();

        return new PaginationResult<>(total, items);
    }

    @Override
    public UsersRecord create(UsersRecord record) {
        return dsl.insertInto(USERS)
                .set(record)
                .returning()
                .fetchOne();
    }

    @Override
    public UsersRecord update(UsersRecord record) {
        record.store();
        return record;
    }

    private Condition buildCondition(String keyword, SysRole role, UserStatus status) {
        Condition condition = DSL.noCondition();

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            condition = condition.and(
                    USERS.USERNAME.likeIgnoreCase(pattern)
                            .or(USERS.FULL_NAME.likeIgnoreCase(pattern))
                            .or(USERS.EMAIL.likeIgnoreCase(pattern))
            );
        }
        if (role != null) condition = condition.and(USERS.ROLE.eq(role));
        if (status != null) condition = condition.and(USERS.STATUS.eq(status));

        return condition;
    }
}
