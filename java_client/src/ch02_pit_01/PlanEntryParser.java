package ch02_pit_01;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 플랜 엔트리 파서.
 */
final class PlanEntryParser {

    static List<PlanEntry> buildPlanEntries(Map<String, Object> json) {
        if (json == null) {
            return List.of();
        }
        Object plansNode = json.get("plans");
        if (plansNode == null) {
            plansNode = json.get("options");
        }
        List<PlanEntry> entries = new ArrayList<>();
        collectPlanEntries(plansNode, "", 0, entries);
        return entries;
    }

    private static void collectPlanEntries(Object node, String currentDayLabel, int currentDayNumber, List<PlanEntry> entries) {
        if (node == null) {
            return;
        }
        if (node instanceof Map<?, ?> map) {
            if (looksLikePlan(map)) {
                entries.add(new PlanEntry(map, currentDayLabel, currentDayNumber));
                return;
            }

            String nextDayLabel = deriveDayLabel(map, currentDayLabel);
            int nextDayNumber = normalizeDayNumber(map.get("day"), nextDayLabel);

            Object daysNode = map.get("days");
            if (daysNode != null) {
                collectPlanEntries(daysNode, nextDayLabel, nextDayNumber, entries);
            }

            Object plansNode = map.get("plans");
            if (plansNode != null && plansNode != node) {
                collectPlanEntries(plansNode, nextDayLabel, nextDayNumber, entries);
            }

            Object optionsNode = map.get("options");
            if (optionsNode != null) {
                collectPlanEntries(optionsNode, nextDayLabel, nextDayNumber, entries);
            }

            Object variantsNode = map.get("variants");
            if (variantsNode != null) {
                collectPlanEntries(variantsNode, nextDayLabel, nextDayNumber, entries);
            }
            return;
        }

        if (node instanceof List<?> list) {
            for (Object item : list) {
                collectPlanEntries(item, currentDayLabel, currentDayNumber, entries);
            }
        }
    }

    private static String deriveDayLabel(Map<?, ?> map, String fallback) {
        String label = valueOrDefault(map.get("title"), fallback);
        if ((label == null || label.isBlank()) && map.get("day") != null) {
            label = "Day " + map.get("day");
        }
        return label == null ? "" : label;
    }

    private static boolean looksLikePlan(Map<?, ?> map) {
        boolean hasSchedule = map.containsKey("schedule") || map.containsKey("activities");
        boolean hasTitle = map.containsKey("title") || map.containsKey("name");
        boolean hasId = map.containsKey("id") || map.containsKey("plan_id");
        return hasSchedule && (hasTitle || hasId);
    }

    private static int normalizeDayNumber(Object dayValue, String dayLabel) {
        if (dayValue instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        String candidate = null;
        if (dayValue != null) {
            candidate = dayValue.toString();
        } else if (dayLabel != null) {
            candidate = dayLabel;
        }
        if (candidate != null) {
            String digits = candidate.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                try {
                    return Integer.parseInt(digits);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }

    private static String valueOrDefault(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }
}

