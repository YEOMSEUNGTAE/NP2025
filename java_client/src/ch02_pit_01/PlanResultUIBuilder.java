package ch02_pit_01;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 플랜 결과 UI 빌더.
 */
final class PlanResultUIBuilder {

    static JPanel buildPlanCardContainer(List<PlanEntry> entries, PlanResultSection.Listener listener) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        int index = 1;
        for (PlanEntry entry : entries) {
            container.add(createPlanCard(index++, entry, listener));
            container.add(Box.createVerticalStrut(8));
        }
        return container;
    }

    private static JPanel createPlanCard(int index, PlanEntry entry, PlanResultSection.Listener listener) {
        Map<?, ?> option = entry.data();
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(223, 227, 235)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        String title = valueOrDefault(option.get("title"), "플랜 " + index);
        JLabel titleLabel = new JLabel(index + ". " + title);
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 13));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleLabel, BorderLayout.WEST);
        String dayLabelText = entry.dayLabel();
        if (dayLabelText != null && !dayLabelText.isBlank()) {
            JLabel dayLabel = new JLabel(dayLabelText);
            dayLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 11));
            dayLabel.setForeground(new Color(120, 130, 149));
            header.add(dayLabel, BorderLayout.EAST);
        }
        card.add(header, BorderLayout.NORTH);

        card.add(createScheduleFlow(option.get("schedule")), BorderLayout.CENTER);

        String planId = valueOrDefault(option.get("id"), "");
        JButton selectButton = new JButton("AI 세부 플랜 생성");
        selectButton.setEnabled(!planId.isBlank());
        selectButton.addActionListener(e -> {
            if (listener != null) {
                listener.onPlanSelectionRequested(planId, title);
            }
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(selectButton);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private static JComponent createScheduleFlow(Object scheduleObj) {
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        flow.setOpaque(false);
        List<Map<?, ?>> entries = extractScheduleEntries(scheduleObj);
        if (entries.isEmpty()) {
            flow.add(new JLabel("일정 정보가 없습니다."));
            return flow;
        }
        for (Map<?, ?> entry : entries) {
            String time = valueOrDefault(entry.get("time"), "");
            String activity = valueOrDefault(entry.get("activity"), "");
            flow.add(createScheduleChip(time, activity));
        }
        return flow;
    }

    private static JComponent createScheduleChip(String time, String activity) {
        JPanel chip = new JPanel();
        chip.setOpaque(false);
        chip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(208, 217, 232)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));

        JLabel timeLabel = new JLabel(time.isBlank() ? "시간 미정" : time);
        timeLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 11));
        timeLabel.setForeground(new Color(76, 86, 106));
        chip.add(timeLabel);

        JLabel activityLabel = new JLabel(activity.isBlank() ? "활동 정보 없음" : activity);
        activityLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        chip.add(activityLabel);

        return chip;
    }

    private static List<Map<?, ?>> extractScheduleEntries(Object scheduleObj) {
        List<Map<?, ?>> result = new ArrayList<>();
        if (scheduleObj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    result.add(map);
                }
            }
        } else if (scheduleObj instanceof Map<?, ?> map) {
            result.add(map);
        }
        return result;
    }

    static JComponent buildFinalPlanComponent(Map<?, ?> plan) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        Object daysObj = plan.get("days");
        if (daysObj instanceof List<?> list) {
            int index = 1;
            for (Object day : list) {
                if (day instanceof Map<?, ?> map) {
                    container.add(createFinalDayCard(index++, map));
                    container.add(Box.createVerticalStrut(6));
                }
            }
        } else {
            container.add(createChatTextArea("세부 일정 정보를 찾지 못했습니다."));
        }
        return container;
    }

    private static Component createFinalDayCard(int index, Map<?, ?> dayMap) {
        JPanel card = new JPanel(new BorderLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(223, 227, 235)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        Object titleObj = dayMap.get("title");
        if (titleObj == null) {
            titleObj = dayMap.get("day");
        }
        String title = valueOrDefault(titleObj, "Day " + index);
        JLabel header = new JLabel(title);
        header.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        card.add(header, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setText(buildScheduleText(dayMap.get("schedule")));
        area.setOpaque(false);
        card.add(area, BorderLayout.CENTER);
        return card;
    }

    private static String buildScheduleText(Object scheduleObj) {
        if (scheduleObj instanceof List<?> list) {
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    Object time = map.get("time");
                    Object activity = map.get("activity");
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(valueOrDefault(time, "")).append(" - ").append(valueOrDefault(activity, ""));
                } else {
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(String.valueOf(item));
                }
            }
            return sb.toString();
        }
        if (scheduleObj instanceof Map<?, ?> map) {
            return map.toString();
        }
        return scheduleObj == null ? "" : scheduleObj.toString();
    }

    private static JTextArea createChatTextArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Malgun Gothic", Font.PLAIN, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder());
        return area;
    }

    private static String valueOrDefault(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }
}

