package model;

public class AgentStats {
    private String name;
    private long active;
    private long resolved;
    private long closed;

    public AgentStats() {
    }

    public AgentStats(String name, long active, long resolved, long closed) {
        this.name = name;
        this.active = active;
        this.resolved = resolved;
        this.closed = closed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getActive() {
        return active;
    }

    public void setActive(long active) {
        this.active = active;
    }

    public long getResolved() {
        return resolved;
    }

    public void setResolved(long resolved) {
        this.resolved = resolved;
    }

    public long getClosed() {
        return closed;
    }

    public void setClosed(long closed) {
        this.closed = closed;
    }

    @Override
    public String toString() {
        return String.format("%s: активных=%d, решённых=%d, закрытых=%d",
                name, active, resolved, closed);
    }
}
