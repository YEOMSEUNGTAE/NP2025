package ch02_pit_01;

import javax.script.ScriptException;
import javax.swing.JOptionPane;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 플랜 응답 처리 핸들러.
 */
final class PlanResponseHandler {
    private static final Logger LOGGER = Logger.getLogger(PlanResponseHandler.class.getName());
    private final ChatbotPlannerClientGUI owner;
    private final PlanResultSection planResultSection;

    PlanResponseHandler(ChatbotPlannerClientGUI owner, PlanResultSection planResultSection) {
        this.owner = owner;
        this.planResultSection = planResultSection;
    }

    boolean handleResponse(String raw, PlannerViewContext viewContext) {
        try {
            Map<String, Object> json = parseJsonObject(raw);
            String type = String.valueOf(json.getOrDefault("type", ""));
            if ("plan_recommendations".equals(type)) {
                return handlePlanRecommendations(json, viewContext);
            } else if ("plan_final".equals(type)) {
                return handlePlanFinal(json);
            } else if ("error".equals(type)) {
                return handleError(json, raw, viewContext);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "플랜 추천 화면으로 전환하지 못했습니다.", ex);
        }
        return false;
    }

    private boolean handlePlanRecommendations(Map<String, Object> json, PlannerViewContext viewContext) {
        // session_id 업데이트는 ChatbotPlannerClientGUI에서 처리
        List<PlanEntry> entries = planResultSection.buildPlanEntries(json);
        planResultSection.renderPlanEntries(entries);
        String source = PlanResultSection.valueOrDefault(json.get("source"), "");
        if ("fallback".equalsIgnoreCase(source)) {
            planResultSection.setPlanHelperText("AI 생성에 실패하여 기본 일정을 표시합니다.");
        } else if (entries.isEmpty()) {
            planResultSection.setPlanHelperText("추천된 플랜이 없습니다.");
        } else {
            planResultSection.setPlanHelperText("추천 일정 " + entries.size() + "개를 확인해보세요.");
        }
        return true;
    }

    private boolean handlePlanFinal(Map<String, Object> json) {
        Object planObj = json.get("final_plan");
        if (!(planObj instanceof Map<?, ?>)) {
            planObj = json.get("plan");
        }
        if (planObj instanceof Map<?, ?> map) {
            planResultSection.updateFinalPlan(map, json);
            return true;
        }
        return false;
    }

    private boolean handleError(Map<String, Object> json, String raw, PlannerViewContext viewContext) {
        JOptionPane.showMessageDialog(
            owner,
            PlanResultSection.valueOrDefault(json.get("message"), "알 수 없는 오류가 발생했습니다."),
            "서버 오류",
            JOptionPane.ERROR_MESSAGE
        );
        viewContext.getResultArea().setText(JsonPrettyPrinter.format(raw));
        viewContext.ensureResultFrameVisible();
        viewContext.showJsonCard();
        return true;
    }

    private Map<String, Object> parseJsonObject(String raw) throws ScriptException {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            Object parsed = SimpleJsonParser.parse(raw);
            if (parsed instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> casted = (Map<String, Object>) map;
                return casted;
            }
        } catch (IllegalArgumentException ex) {
            throw new ScriptException("JSON Parse Error: " + ex.getMessage());
        }
        return Collections.emptyMap();
    }
}

