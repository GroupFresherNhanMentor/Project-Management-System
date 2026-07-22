package fpt.qn.pms.task.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.tables.records.TaskStatusesRecord;

public interface TaskStatusRepository extends Repository<TaskStatusesRecord> {

    List<TaskStatusesRecord> findAllByProjectId(UUID projectId, Boolean isInitial, Boolean isActive);

    Optional<TaskStatusesRecord> findInitialByProjectId(UUID projectId);

    boolean existsByProjectIdAndName(UUID projectId, String name);

    boolean existsByProjectIdAndNameAndIdNot(UUID projectId, String name, UUID excludeId);

    void softDeleteById(UUID id);
}
