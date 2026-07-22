package fpt.qn.pms.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request body to create a new comment on a task")
public class CreateCommentRequest {

    @NotBlank(message = "Content is required")
    @Schema(description = "Comment text content", example = "This task needs more investigation", requiredMode = Schema.RequiredMode.REQUIRED)
    String content;
}
