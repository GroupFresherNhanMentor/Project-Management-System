package fpt.qn.pms.comment.service;

import static fpt.qn.pms.jooq.Tables.TASK_ACTIVITIES;
import static fpt.qn.pms.jooq.Tables.USERS;

import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.comment.dto.CommentDto;
import fpt.qn.pms.comment.dto.CreateCommentRequest;
import fpt.qn.pms.comment.dto.UpdateCommentRequest;
import fpt.qn.pms.comment.exception.CommentNotFoundException;
import fpt.qn.pms.comment.mapper.CommentMapper;
import fpt.qn.pms.comment.repository.CommentRepository;
import fpt.qn.pms.common.exception.InternalServerErrorException;
import fpt.qn.pms.jooq.enums.ActivityAction;
import fpt.qn.pms.jooq.tables.records.TaskActivitiesRecord;
import fpt.qn.pms.jooq.tables.records.TaskCommentsRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.user.exception.UserNotFoundException;
import fpt.qn.pms.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;
    UserRepository userRepository;
    CommentMapper commentMapper;
    DSLContext dsl;

    private UsersRecord getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            var mockUser = dsl.selectFrom(USERS).limit(1).fetchOne();
            if (mockUser == null) {
                throw new InternalServerErrorException("No users found in database to mock authentication");
            }
            return mockUser;
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException());
    }

    @Override
    @Transactional
    public CommentDto createComment(UUID taskId, CreateCommentRequest request) {
        var currentUser = getCurrentUser();

        TaskCommentsRecord record = commentMapper.toRecord(request);
        record.setTaskId(taskId);
        record.setCreatedBy(currentUser.getId());

        TaskCommentsRecord saved = commentRepository.create(record);

        // Record activity
        TaskActivitiesRecord activity = dsl.newRecord(TASK_ACTIVITIES);
        activity.setTaskId(saved.getTaskId());
        activity.setUserId(currentUser.getId());
        activity.setAction(ActivityAction.COMMENT_ADDED);
        activity.setNewValue("Comment added");
        activity.store();

        return toDtoWithUserName(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByTaskId(UUID taskId) {
        return commentRepository.findByTaskId(taskId).stream()
                .map(this::toDtoWithUserName)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto updateComment(UUID id, UpdateCommentRequest request) {
        TaskCommentsRecord record = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException());

        commentMapper.updateRecord(record, request);
        // content is required (NotBlank), so no null check needed — jOOQ dirty-tracking handles it

        TaskCommentsRecord saved = commentRepository.update(record);
        return toDtoWithUserName(saved);
    }

    @Override
    @Transactional
    public void deleteComment(UUID id) {
        commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException());
        commentRepository.deleteById(id);
    }

    private CommentDto toDtoWithUserName(TaskCommentsRecord record) {
        CommentDto dto = commentMapper.toDto(record);
        if (record.getCreatedBy() != null) {
            String name = dsl.select(USERS.FULL_NAME)
                    .from(USERS)
                    .where(USERS.ID.eq(record.getCreatedBy()))
                    .fetchOne(USERS.FULL_NAME);
            dto = CommentDto.builder()
                    .id(dto.getId())
                    .taskId(dto.getTaskId())
                    .content(dto.getContent())
                    .createdBy(dto.getCreatedBy())
                    .createdByName(name)
                    .createdAt(dto.getCreatedAt())
                    .build();
        }
        return dto;
    }
}
