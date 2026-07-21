package fpt.qn.pms.comment.repository;

import static fpt.qn.pms.jooq.Tables.TASK_COMMENTS;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.TaskCommentsRecord;

@Repository
public class CommentRepositoryImpl extends BaseRepository<TaskCommentsRecord> implements CommentRepository {

    public CommentRepositoryImpl(DSLContext dsl) {
        super(dsl, TASK_COMMENTS);
    }

    @Override
    public List<TaskCommentsRecord> findByTaskId(UUID taskId) {
        return dsl.selectFrom(TASK_COMMENTS)
                .where(TASK_COMMENTS.TASK_ID.eq(taskId))
                .orderBy(TASK_COMMENTS.CREATED_AT.asc())
                .fetch();
    }

    @Override
    public PaginationResult<TaskCommentsRecord> findByTaskId(UUID taskId, int page, int size) {
        long total = dsl.fetchCount(TASK_COMMENTS, TASK_COMMENTS.TASK_ID.eq(taskId));

        List<TaskCommentsRecord> items = dsl.selectFrom(TASK_COMMENTS)
                .where(TASK_COMMENTS.TASK_ID.eq(taskId))
                .orderBy(TASK_COMMENTS.CREATED_AT.asc())
                .limit(size)
                .offset((long) page * size)
                .fetch();

        return new PaginationResult<>(total, items);
    }
}
