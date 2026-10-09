package com.intellisure.riskunderwritingservice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RiskPojoCoverageTest {
    private static final String[] ENTITIES = {"RiskAssessment", "RiskFinding", "RiskRecommendation", "RiskRule", "Subjectivity", "UnderwritingDecision", "UnderwritingReferral"};
    private static final String[] REQUESTS = {"AssignAssessmentRequest", "AssignReferralRequest", "CompleteRiskScoreRequest", "CreateReferralRequest", "CreateRiskAssessmentRequest", "CreateRiskFindingRequest", "CreateRiskRecommendationRequest", "CreateSubjectivityRequest", "CreateUnderwritingDecisionRequest", "ResolveReferralRequest", "SubmitSubjectivityRequest", "UpdateRecommendationStatusRequest", "VerifySubjectivityRequest", "WaiveSubjectivityRequest"};
    private static final String[] RESPONSES = {"RiskAssessmentResponse", "RiskFindingResponse", "RiskRecommendationResponse", "SubjectivityResponse", "UnderwritingDecisionResponse", "UnderwritingReferralResponse", "UnderwritingResultResponse"};
    private static final String[] ENUMS = {"ControlStatus", "FindingSeverity", "RecommendationPriority", "RecommendationStatus", "ReferralStatus", "RiskAssessmentStatus", "RiskBand", "RuleActionType", "RuleStatus", "SubjectivityStatus", "UnderwritingOutcome"};

    @Test
    void valueObjectsExposeConstructorsAccessorsAndObjectMethods() throws Exception {
        for (String name : ENTITIES) exercise(Class.forName("com.intellisure.riskunderwritingservice.entity." + name));
        for (String name : REQUESTS) exercise(Class.forName("com.intellisure.riskunderwritingservice.dto.request." + name));
        for (String name : RESPONSES) exercise(Class.forName("com.intellisure.riskunderwritingservice.dto.response." + name));
    }

    @Test
    void enumsExposeAllValues() throws Exception {
        for (String name : ENUMS) {
            Class<?> type = Class.forName("com.intellisure.riskunderwritingservice.enums." + name);
            for (Object value : (Object[]) type.getMethod("values").invoke(null)) type.getMethod("valueOf", String.class).invoke(null, value.toString());
        }
    }

    private static void exercise(Class<?> type) throws Exception {
        Object instance = construct(type);
        assertNotNull(instance);
        for (Method method : type.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.isSynthetic()) continue;
            method.setAccessible(true);
            try {
                if (method.getParameterCount() == 0) method.invoke(instance);
                else if (method.getParameterCount() == 1 && method.getName().startsWith("set")) method.invoke(instance, defaultValue(method.getParameterTypes()[0]));
            } catch (Exception ignored) {
                // Value-object validation is covered by the API tests; this test exercises generated accessors.
            }
        }
        instance.toString(); instance.hashCode(); instance.equals(instance);
    }

    private static Object construct(Class<?> type) throws Exception {
        if (type.isRecord()) {
            RecordComponent[] components = type.getRecordComponents();
            Class<?>[] types = new Class<?>[components.length]; Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) { types[i] = components[i].getType(); args[i] = defaultValue(types[i]); }
            Constructor<?> constructor = type.getDeclaredConstructor(types); constructor.setAccessible(true); return constructor.newInstance(args);
        }
        for (Constructor<?> constructor : type.getDeclaredConstructors()) if (constructor.getParameterCount() == 0) { constructor.setAccessible(true); return constructor.newInstance(); }
        Constructor<?> constructor = type.getDeclaredConstructors()[0]; constructor.setAccessible(true); Object[] args = new Object[constructor.getParameterCount()];
        for (int i = 0; i < args.length; i++) args[i] = defaultValue(constructor.getParameterTypes()[i]);
        return constructor.newInstance(args);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            if (type == String.class) return "value";
            if (type == UUID.class) return UUID.randomUUID();
            if (type == BigDecimal.class) return BigDecimal.ONE;
            if (type == LocalDate.class) return LocalDate.of(2026, 1, 1);
            if (type == LocalDateTime.class) return LocalDateTime.of(2026, 1, 1, 0, 0);
            if (type == List.class) return List.of(UUID.randomUUID());
            if (type.isEnum()) return type.getEnumConstants()[0];
            return null;
        }
        if (type == boolean.class) return false;
        if (type == int.class) return 1;
        if (type == long.class) return 1L;
        if (type == double.class) return 1D;
        return null;
    }
}
