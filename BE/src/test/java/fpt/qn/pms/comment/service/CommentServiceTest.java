package fpt.qn.pms.comment.service;

import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.TASKS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.comment.dto.CommentDto;
import fpt.qn.pms.comment.dto.CreateCommentRequest;
import fpt.qn.pms.comment.dto.UpdateCommentRequest;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskStatus;
import fpt.qn.pms.jooq.enums.TaskType;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;

class CommentServiceTest extends BaseIntegrationTest {

    @Autowired
    CommentService commentService;

    @Autowired
    DSLContext dsl;

    UUID taskId;

    @BeforeEach
    void setUp() {
        // Create a user
        UsersRecord user = new UsersRecord();
        user.setEmployeeId("COMMENT_EMP");
        user.setUsername("comment_tester");
        user.setFullName("Comment Tester");
        user.setEmail("comment_tester@test.com");
        user.setPassword("$2a$10$dummyhash");
        user.setRole(SysRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        UsersRecord savedUser = dsl.insertInto(USERS).set(user).returning().fetchOne();

        // Create a project
        ProjectsRecord project = new ProjectsRecord();
        project.setProjectCode("CMT");
        project.setProjectName("Comment Test Project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(LocalDate.of(2026, 7, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        project.setCreatedBy(savedUser.getId());
        ProjectsRecord savedProject = dsl.insertInto(PROJECTS).set(project).returning().fetchOne();

        // Create a task
        TasksRecord task = new TasksRecord();
        task.setTaskKey("CMT-1");
        task.setProjectId(savedProject.getId());
        task.setSummary("Comment Test Task");
        task.setTaskType(TaskType.TASK);
        task.setPriority(TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.TODO);
        task.setReporterId(savedUser.getId());
        task.setCreatedBy(savedUser.getId());
        TasksRecord savedTask = dsl.insertInto(TASKS).set(task).returning().fetchOne();

        taskId = savedTask.getId();
    }

    // ── 1. Create Comment ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createComment - Should create and return mapped CommentDto")
    void createComment_shouldReturnMappedDto() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("This is a test comment");

        CommentDto dto = commentService.createComment(taskId, req);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getTaskId()).isEqualTo(taskId);
        assertThat(dto.getContent()).isEqualTo("This is a test comment");
        assertThat(dto.getCreatedBy()).isNotNull();
        assertThat(dto.getCreatedByName()).isNotNull();
        assertThat(dto.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("createComment - Should store content correctly")
    void createComment_shouldStoreContentCorrectly() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("  Content with spaces  ");

        CommentDto dto = commentService.createComment(taskId, req);

        assertThat(dto.getContent()).isEqualTo("  Content with spaces  ");
    }

    // ── 2. Get Comments By Task Id ──────────────────────────────────────────────

    @Test
    @DisplayName("getCommentsByTaskId - Should return comments in creation order")
    void getCommentsByTaskId_shouldReturnCommentsInOrder() {
        createComment("First comment");
        createComment("Second comment");
        createComment("Third comment");

        List<CommentDto> comments = commentService.getCommentsByTaskId(taskId);

        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getContent()).isEqualTo("First comment");
        assertThat(comments.get(1).getContent()).isEqualTo("Second comment");
        assertThat(comments.get(2).getContent()).isEqualTo("Third comment");
    }

    @Test
    @DisplayName("getCommentsByTaskId - Should scope to task")
    void getCommentsByTaskId_shouldScopeToTask() {
        UUID secondTaskId = createAdditionalTask();
        createComment(taskId, "Comment for task 1");
        createComment(secondTaskId, "Comment for task 2");

        List<CommentDto> comments = commentService.getCommentsByTaskId(taskId);

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("Comment for task 1");
    }

    @Test
    @DisplayName("getCommentsByTaskId - Should return empty list when no comments")
    void getCommentsByTaskId_shouldReturnEmpty_whenNoComments() {
        List<CommentDto> comments = commentService.getCommentsByTaskId(taskId);

        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("getCommentsByTaskId - Should return empty list for non-existent task")
    void getCommentsByTaskId_shouldReturnEmpty_whenTaskNotExists() {
        List<CommentDto> comments = commentService.getCommentsByTaskId(UUID.randomUUID());

        assertThat(comments).isEmpty();
    }

    // ── 3. Update Comment ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateComment - Should update content")
    void updateComment_shouldUpdateContent() {
        CommentDto created = createComment("Original content");

        UpdateCommentRequest req = new UpdateCommentRequest();
        req.setContent("Updated content");

        CommentDto updated = commentService.updateComment(created.getId(), req);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getContent()).isEqualTo("Updated content");
        assertThat(updated.getCreatedBy()).isEqualTo(created.getCreatedBy());
    }

    @Test
    @DisplayName("updateComment - Should throw when comment not found")
    void updateComment_shouldThrow_whenNotExists() {
        UpdateCommentRequest req = new UpdateCommentRequest();
        req.setContent("Anything");

        assertThatThrownBy(() -> commentService.updateComment(UUID.randomUUID(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Comment not found");
    }

    // ── 4. Delete Comment ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteComment - Should delete existing comment")
    void deleteComment_shouldRemoveComment() {
        CommentDto created = createComment("To be deleted");

        commentService.deleteComment(created.getId());

        List<CommentDto> comments = commentService.getCommentsByTaskId(taskId);
        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("deleteComment - Should delete only the specified comment")
    void deleteComment_shouldDeleteOnlySpecifiedComment() {
        CommentDto comment1 = createComment("Keep this");
        createComment("Keep this too");

        commentService.deleteComment(comment1.getId());

        List<CommentDto> comments = commentService.getCommentsByTaskId(taskId);
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("Keep this too");
    }

    @Test
    @DisplayName("deleteComment - Should throw when comment does not exist")
    void deleteComment_shouldThrow_whenNotExists() {
        assertThatThrownBy(() -> commentService.deleteComment(UUID.randomUUID()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Comment not found");
    }

    // ── Helper methods ──────────────────────────────────────────────────────────

    private CommentDto createComment(String content) {
        return createComment(taskId, content);
    }

    private CommentDto createComment(UUID commentTaskId, String content) {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent(content);
        return commentService.createComment(commentTaskId, req);
    }

    private UUID createAdditionalTask() {
        UUID projectId = dsl.select(PROJECTS.ID).from(PROJECTS).limit(1).fetchOne(PROJECTS.ID);
        UUID userId = dsl.select(USERS.ID).from(USERS).limit(1).fetchOne(USERS.ID);

        TasksRecord task = new TasksRecord();
        task.setTaskKey("CMT-2");
        task.setProjectId(projectId);
        task.setSummary("Second Comment Test Task");
        task.setTaskType(TaskType.TASK);
        task.setPriority(TaskPriority.LOW);
        task.setStatus(TaskStatus.TODO);
        task.setReporterId(userId);
        task.setCreatedBy(userId);
        return dsl.insertInto(TASKS).set(task).returning().fetchOne().getId();
    }
}
