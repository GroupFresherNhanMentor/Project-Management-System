package fpt.qn.pms.comment.service;

import java.util.List;
import java.util.UUID;

import fpt.qn.pms.comment.dto.CommentDto;
import fpt.qn.pms.comment.dto.CreateCommentRequest;
import fpt.qn.pms.comment.dto.UpdateCommentRequest;

public interface CommentService {

    CommentDto createComment(UUID taskId, CreateCommentRequest request);

    List<CommentDto> getCommentsByTaskId(UUID taskId);

    CommentDto updateComment(UUID id, UpdateCommentRequest request);

    void deleteComment(UUID id);
}
