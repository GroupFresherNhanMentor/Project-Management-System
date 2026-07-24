package fpt.qn.pms.activity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import fpt.qn.pms.activity.dto.TaskActivityDto;
import fpt.qn.pms.jooq.tables.records.TaskActivitiesRecord;

import org.jooq.JSONB;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "createdTime", source = "createdAt")
    TaskActivityDto toDto(TaskActivitiesRecord record);

    default String mapJsonbToString(JSONB jsonb) {
        return jsonb != null ? jsonb.data() : null;
    }
}
