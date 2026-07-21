package fpt.qn.pms.comment.repository;

import java.util.List;
import java.util.UUID;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.tables.records.TaskCommentsRecord;

public interface CommentRepository extends Repository<TaskCommentsRecord> {

    List<TaskCommentsRecord> findByTaskId(UUID taskId);

    PaginationResult<TaskCommentsRecord> findByTaskId(UUID taskId, int page, int size);
}
