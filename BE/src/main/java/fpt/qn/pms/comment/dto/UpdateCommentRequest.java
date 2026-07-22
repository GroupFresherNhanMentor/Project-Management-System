package fpt.qn.pms.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request body to update an existing comment")
public class UpdateCommentRequest {

    @NotBlank(message = "Content is required")
    @Schema(description = "Updated comment text content", example = "Updated analysis after further investigation", requiredMode = Schema.RequiredMode.REQUIRED)
    String content;
}
