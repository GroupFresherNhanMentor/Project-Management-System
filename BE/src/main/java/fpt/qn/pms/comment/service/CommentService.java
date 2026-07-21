package fpt.qn.pms.comment.service;

import java.util.UUID;

import fpt.qn.pms.comment.dto.CommentDto;
import fpt.qn.pms.comment.dto.CommentSearchRequest;
import fpt.qn.pms.comment.dto.CreateCommentRequest;
import fpt.qn.pms.comment.dto.UpdateCommentRequest;
import fpt.qn.pms.common.dto.PageResponse;

public interface CommentService {

    CommentDto createComment(UUID taskId, CreateCommentRequest request);

    PageResponse<CommentDto> getCommentsByTaskId(UUID taskId, CommentSearchRequest request);

    CommentDto updateComment(UUID id, UpdateCommentRequest request);

    void deleteComment(UUID id);
}
