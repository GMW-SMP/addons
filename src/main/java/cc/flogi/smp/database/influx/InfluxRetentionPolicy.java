package cc.flogi.smp.database.influx;

import lombok.Builder;
import lombok.Data;

/**
 * I don't know why they don't have something like this in the client.
 */
@Builder
@Data
public class InfluxRetentionPolicy {

    private String name;
    private String database;
    private String duration;
    private int replicationPolicy;
    private boolean isDefault;
}
