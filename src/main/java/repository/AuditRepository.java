package repository;

import java.util.List;

public interface AuditRepository {
    void log(Long userId, String action, String entityType, Long entityId, String details);

    List<String> findRecent(int limit);
}
