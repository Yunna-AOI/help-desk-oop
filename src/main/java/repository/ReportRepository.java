package repository;

import model.AgentStats;

import java.util.List;
import java.util.Map;

public interface ReportRepository {
    Map<String, Long> countByStatus();

    Map<String, Long> countByCategory();

    List<AgentStats> agentStats();

    Map<String, String> averageResolutionByCategory();
}
