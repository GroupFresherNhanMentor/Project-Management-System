package fpt.qn.pms.comment.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.comment.dto.CommentDto;
import fpt.qn.pms.comment.dto.CommentSearchRequest;
import fpt.qn.pms.comment.dto.CreateCommentRequest;
import fpt.qn.pms.comment.dto.UpdateCommentRequest;
import fpt.qn.pms.comment.service.CommentService;
import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Comments", description = "Endpoints for managing task comments (FR-CMT)")
public class CommentController {

    CommentService commentService;

    @PostMapping("/tasks/{taskId}/comments")
    @PreAuthorize("@projectSecurityEvaluator.hasAccessToTask(#taskId) or hasRole('ADMIN')")
    @Operation(summary = "Add a comment to a task (FR-CMT-01)")
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentDto created = commentService.createComment(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Comment added successfully"));
    }

    @GetMapping("/tasks/{taskId}/comments")
    @PreAuthorize("@projectSecurityEvaluator.hasAccessToTask(#taskId) or hasRole('ADMIN')")
    @Operation(summary = "Get paginated comments for a task (FR-CMT-02)")
    public ResponseEntity<ApiResponse<PageResponse<CommentDto>>> getCommentsByTaskId(
            @PathVariable UUID taskId,
            @Valid CommentSearchRequest request) {
        PageResponse<CommentDto> comments = commentService.getCommentsByTaskId(taskId, request);
        return ResponseEntity.ok(ApiResponse.success(comments, null));
    }

    @PutMapping("/comments/{id}")
    @Operation(summary = "Update a comment")
    public ResponseEntity<ApiResponse<CommentDto>> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommentRequest request) {
        CommentDto updated = commentService.updateComment(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Comment updated successfully"));
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Comment deleted successfully"));
    }
}
