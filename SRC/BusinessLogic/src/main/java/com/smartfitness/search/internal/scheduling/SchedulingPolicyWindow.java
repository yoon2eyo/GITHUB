package com.smartfitness.search.internal.scheduling;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Defines the daily window when low-priority match jobs may run.
 */
public class SchedulingPolicyWindow {
    private final int startHourInclusive;
    private final int endHourExclusive;
    private final ZoneId zoneId;

    public SchedulingPolicyWindow(int startHourInclusive, int endHourExclusive, ZoneId zoneId) {
        this.startHourInclusive = startHourInclusive;
        this.endHourExclusive = endHourExclusive;
        this.zoneId = zoneId;
    }

    public boolean isWithinWindow(Instant instant) {
        ZonedDateTime dateTime = instant.atZone(zoneId);
        int hour = dateTime.getHour();
        if (startHourInclusive <= endHourExclusive) {
            return hour >= startHourInclusive && hour < endHourExclusive;
        }
        return hour >= startHourInclusive || hour < endHourExclusive;
    }
}
