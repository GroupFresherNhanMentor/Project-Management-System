package fpt.qn.pms.sprint.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.jooq.tables.records.SprintsRecord;
import fpt.qn.pms.sprint.repository.SprintRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SprintAutoCloser {

    SprintRepository sprintRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void closeExpiredSprints() {
        LocalDate today = LocalDate.now();
        List<SprintsRecord> expiredSprints = sprintRepository.findActiveSprintsPastEndDate(today);

        if (expiredSprints.isEmpty()) {
            log.debug("No expired sprints to close today.");
            return;
        }

        for (SprintsRecord sprint : expiredSprints) {
            sprint.setStatus(SprintStatus.CLOSED);
            sprintRepository.update(sprint);
            log.info("Auto-closed sprint '{}' (id={}) — end date {} was before today {}",
                    sprint.getSprintName(), sprint.getId(), sprint.getEndDate(), today);
        }

        log.info("Auto-closed {} expired sprint(s).", expiredSprints.size());
    }
}
