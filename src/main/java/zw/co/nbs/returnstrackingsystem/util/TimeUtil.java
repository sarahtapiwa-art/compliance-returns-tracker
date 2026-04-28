package zw.co.nbs.returnstrackingsystem.util;

import java.time.*;

public final class TimeUtil {
    private TimeUtil() {}

    public static OffsetDateTime harareLocalToUtc(OffsetDateTime anyUtc, String hhmm) {
        ZoneId zh = ZoneId.of("Africa/Harare");
        LocalTime lt = LocalTime.parse(hhmm);          // "10:00"
        LocalDate d = anyUtc.atZoneSameInstant(zh).toLocalDate();
        ZonedDateTime zdt = ZonedDateTime.of(d, lt, zh);
        return zdt.toOffsetDateTime();
    }
    public static OffsetDateTime convertToHarareOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.of("Africa/Harare"))
                .toOffsetDateTime();
    }

}
