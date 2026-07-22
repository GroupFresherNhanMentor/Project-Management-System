package fpt.qn.pms.comment.dto;

import fpt.qn.pms.common.dto.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Pagination parameters for listing comments of a task")
public class CommentSearchRequest extends PageRequest {
}
