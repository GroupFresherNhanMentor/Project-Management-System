package fpt.qn.pms.common.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Repository<R> {

    Optional<R> findById(UUID id);

    List<R> findAll();

    R create(R record);

    R update(R record);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
