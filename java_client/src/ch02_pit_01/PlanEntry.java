package ch02_pit_01;

import java.util.Map;

/**
 * 플랜 엔트리 데이터 모델.
 */
final class PlanEntry {
    private final Map<?, ?> data;
    private final String dayLabel;
    private final int dayNumber;

    PlanEntry(Map<?, ?> data, String dayLabel, int dayNumber) {
        this.data = data;
        this.dayLabel = dayLabel == null ? "" : dayLabel;
        this.dayNumber = dayNumber;
    }

    Map<?, ?> data() {
        return data;
    }

    String dayLabel() {
        return dayLabel;
    }

    int dayNumber() {
        return dayNumber;
    }
}

