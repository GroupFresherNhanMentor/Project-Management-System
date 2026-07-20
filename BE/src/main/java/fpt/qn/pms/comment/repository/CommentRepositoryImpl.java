package fpt.qn.pms.comment.repository;

import static fpt.qn.pms.jooq.Tables.TASK_COMMENTS;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.TaskCommentsRecord;

@Repository
public class CommentRepositoryImpl extends BaseRepository<TaskCommentsRecord> implements CommentRepository {

    public CommentRepositoryImpl(DSLContext dsl) {
        super(dsl, TASK_COMMENTS);
    }
}
