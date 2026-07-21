package fpt.qn.pms.comment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Task comment response data")
public class CommentDto {

    @Schema(description = "Comment ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    UUID id;

    @Schema(description = "Task ID", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    UUID taskId;

    @Schema(description = "Comment content", example = "This task is blocked by dependency X")
    String content;

    @Schema(description = "User ID of the comment author", example = "c3d4e5f6-a7b8-9012-cdef-123456789012")
    UUID createdBy;

    @Schema(description = "Full name of the comment author", example = "John Doe")
    String createdByName;

    @Schema(description = "Creation timestamp", example = "2026-07-20T10:30:00+07:00")
    OffsetDateTime createdAt;
}
