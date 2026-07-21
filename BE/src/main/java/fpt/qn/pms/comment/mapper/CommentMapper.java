package fpt.qn.pms.comment.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import fpt.qn.pms.comment.dto.CommentDto;
import fpt.qn.pms.comment.dto.CreateCommentRequest;
import fpt.qn.pms.comment.dto.UpdateCommentRequest;
import fpt.qn.pms.jooq.tables.records.TaskCommentsRecord;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "createdByName", ignore = true)
    CommentDto toDto(TaskCommentsRecord record);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "taskId", ignore = true)
    TaskCommentsRecord toRecord(CreateCommentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taskId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateRecord(@MappingTarget TaskCommentsRecord record, UpdateCommentRequest request);
}
