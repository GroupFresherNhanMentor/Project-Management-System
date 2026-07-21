package fpt.qn.pms.activity.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.activity.service.ActivityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityEventListener {

    ActivityService activityService;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void handleTaskActivityEvent(TaskActivityEvent event) {
        activityService.logActivity(event);
    }
}
