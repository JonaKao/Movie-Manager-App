package de.uhd.ifi.se.moviemanager.util;

import static java.util.Arrays.stream;

import java.util.Objects;

public enum Month {
    JANUARY(31), FEBRUARY(28), MARCH(31), APRIL(30), MAY(31), JUNE(30), JULY(
            31), AUGUST(31), SEPTEMBER(30), OCTOBER(31), NOVEMBER(30), DECEMBER(
            31);

    private static final String[] MONTHS_AS_STRINGS = stream(values())
            .map(Objects::toString).toArray(String[]::new);

    private final int maxDays;

    Month(int maxDays) {
        this.maxDays = maxDays;
    }

    /**
     * Returns corresponding month to a given number, where 1 is JANUARY and 12
     * is DECEMBER
     *
     * @param number
     * @return
     */
    public static Month of(int number) {
        return values()[number - 1];
    }

    public static String[] asStrings() {
        return MONTHS_AS_STRINGS;
    }

    public int getMaxDaysWithoutLeap() {
        return maxDays;
    }

    @Override
    public String toString() {
        return name();
    }

}
