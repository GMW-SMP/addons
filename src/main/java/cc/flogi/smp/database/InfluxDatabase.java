package cc.flogi.smp.database;

import cc.flogi.smp.database.influx.InfluxRetentionPolicy;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;

public class InfluxDatabase {

    private final InfluxDB DB;
    private String retentionPolicy;

    public InfluxDatabase(String host, String username, String password) {
        DB = InfluxDBFactory.connect(host, username, password);

        if (DB.ping().getVersion().equalsIgnoreCase("unknown"))
            return;

        DB.enableBatch(5, 1, TimeUnit.SECONDS);
        System.out.println("Connected to Influx!");
    }

    public InfluxDatabase withDatabase(String name, InfluxRetentionPolicy policy) {
        if (DB.databaseExists(name))
            return this;

        retentionPolicy = policy.getName();

        DB.createDatabase(name);
        DB.createRetentionPolicy(
                policy.getName(),
                name,
                policy.getDuration(),
                policy.getReplicationPolicy(),
                policy.isDefault()
        );
        return this;
    }

    public void addBatch(BatchPoints points) {
        DB.write(points);
    }

    public void addPoint(Point point) {
        DB.write("smp", retentionPolicy, point);
    }
}
