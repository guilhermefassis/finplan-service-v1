package io.finplan.common.utils;

import java.time.YearMonth;

public class DateReferenceUtils {

    public static YearMonth fromReference(Integer ref) {
        if (ref == null || ref < 100000 || ref > 999912) {
            throw new IllegalArgumentException("Invalid Format: expected YYYYMM");
        }
        int year = ref / 100;
        int month = ref % 100;
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid Month: " + month);
        }
        return YearMonth.of(year, month);
    }

    public static Integer toReference(YearMonth ym) {
        return (ym.getYear() * 100) + ym.getMonthValue();
    }
}
