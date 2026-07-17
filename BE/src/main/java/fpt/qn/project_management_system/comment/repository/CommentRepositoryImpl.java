package fpt.qn.project_management_system.comment.repository;

import static fpt.qn.project_management_system.jooq.Tables.TASK_COMMENTS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.project_management_system.common.repository.BaseRepository;
import fpt.qn.project_management_system.jooq.tables.records.TaskCommentsRecord;

@Repository
public class CommentRepositoryImpl extends BaseRepository<TaskCommentsRecord> implements CommentRepository {

    public CommentRepositoryImpl(DSLContext dsl) {
        super(dsl, TASK_COMMENTS);
    }
}
