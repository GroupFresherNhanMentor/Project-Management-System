package fpt.qn.pms.activity.event;

import java.util.UUID;
import fpt.qn.pms.jooq.enums.ActivityAction;

public record TaskActivityEvent(
    UUID taskId,
    UUID userId,
    ActivityAction action,
    String oldValue,
    String newValue
) {}
