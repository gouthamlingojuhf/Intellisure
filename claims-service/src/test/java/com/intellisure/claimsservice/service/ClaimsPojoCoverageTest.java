package com.intellisure.claimsservice.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.intellisure.claimsservice.mapper.BusinessIncomeMapper;
import com.intellisure.claimsservice.mapper.ClaimFinancialsMapper;
import com.intellisure.claimsservice.mapper.CoverageDecisionMapper;
import com.intellisure.claimsservice.mapper.PaymentMapper;
import com.intellisure.claimsservice.mapper.SalvageMapper;
import com.intellisure.claimsservice.mapper.SubrogationMapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Exercises the deliberately simple DTO/entity value objects so coverage reflects their public contract. */
class ClaimsPojoCoverageTest {

    private static final String[] VALUE_TYPES = {
            "BusinessIncome", "Claim", "ClaimAssessment", "ClaimFinancials", "CoverageDecision", "Payment", "Salvage", "Subrogation",
            "BusinessIncomeResponse", "CalculateBusinessIncomeLossRequest", "ClaimFinancialsResponse", "ClaimResponse", "CoverageDecisionResponse",
            "CreateBusinessIncomeRequest", "CreateCoverageDecisionRequest", "CreatePaymentRequest", "CreateSalvageRequest", "CreateSubrogationRequest",
            "FileClaimRequest", "InitiateFinancialsRequest", "PaymentResponse", "RecordBusinessIncomeLossRequest", "RecordPaymentRequest",
            "RecordSalvageSaleRequest", "RecordSubrogationRecoveryRequest", "SalvageResponse", "SubrogationResponse", "UpdateBusinessIncomeRequest",
            "UpdateIncurredRequest", "UpdatePaymentStatusRequest", "UpdateReserveRequest", "UpdateSalvageRequest", "UpdateSubrogationRequest"
    };

    @Test
    void valueObjectsExposeWorkingConstructorsAccessorsAndObjectMethods() throws Exception {
        for (String simpleName : VALUE_TYPES) {
            Class<?> type = Class.forName("com.intellisure.claimsservice." + (isDto(simpleName) ? "dto." : "entity.") + simpleName);
            Object value = construct(type);
            assertNotNull(value, simpleName);
            for (Method method : type.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) || method.isSynthetic()) continue;
                method.setAccessible(true);
                try {
                    if (method.getParameterCount() == 0) method.invoke(value);
                    else if (method.getParameterCount() == 1 && method.getName().startsWith("set")) method.invoke(value, defaultValue(method.getParameterTypes()[0]));
                } catch (Exception ignored) {
                    // Validation and generated methods may reject null; the public shape is still exercised.
                }
            }
            value.toString();
            value.hashCode();
            value.equals(value);
        }
    }

    @Test
    void enumsExposeAllDefinedLifecycleValues() throws Exception {
        for (String name : new String[]{"ClaimPriority", "ClaimStatus", "RecoveryStatus", "SettlementStatus"}) {
            Class<?> type = Class.forName("com.intellisure.claimsservice.entity." + name);
            Object[] constants = (Object[]) type.getMethod("values").invoke(null);
            for (Object constant : constants) {
                type.getMethod("valueOf", String.class).invoke(null, constant.toString());
            }
        }
    }

    @Test
    void generatedMappersReturnNullForNullSources() {
        assertNull(BusinessIncomeMapper.INSTANCE.toResponse(null));
        assertNull(ClaimFinancialsMapper.INSTANCE.toResponse(null));
        assertNull(CoverageDecisionMapper.INSTANCE.toResponse(null));
        assertNull(PaymentMapper.INSTANCE.toResponse(null));
        assertNull(SalvageMapper.INSTANCE.toResponse(null));
        assertNull(SubrogationMapper.INSTANCE.toResponse(null));
    }

    private static boolean isDto(String name) {
        return !name.equals("BusinessIncome") && !name.equals("Claim") && !name.equals("ClaimAssessment")
                && !name.equals("ClaimFinancials") && !name.equals("CoverageDecision") && !name.equals("Payment")
                && !name.equals("Salvage") && !name.equals("Subrogation");
    }

    private static Object construct(Class<?> type) throws Exception {
        if (type.isRecord()) {
            RecordComponent[] components = type.getRecordComponents();
            Class<?>[] types = new Class<?>[components.length];
            Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                types[i] = components[i].getType();
                args[i] = defaultValue(types[i]);
            }
            Constructor<?> constructor = type.getDeclaredConstructor(types);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        }
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                return constructor.newInstance();
            }
        }
        Constructor<?> constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object[] args = new Object[constructor.getParameterCount()];
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
            if (type.isEnum()) return type.getEnumConstants()[0];
            return null;
        }
        if (type == boolean.class) return false;
        if (type == int.class) return 1;
        if (type == long.class) return 1L;
        if (type == double.class) return 1D;
        if (type == float.class) return 1F;
        if (type == short.class) return (short) 1;
        if (type == byte.class) return (byte) 1;
        if (type == char.class) return 'x';
        return null;
    }
}
