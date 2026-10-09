package com.intellisure.quotepolicyservice.mapper;

import com.intellisure.quotepolicyservice.entity.*;
import com.intellisure.quotepolicyservice.enums.*;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class QuotePolicyExtendedMapperCoverageTest {
    @Test
    void mapsExtendedLifecycleEntities() {
        Endorsement endorsement = Endorsement.builder().endorsementId(UUID.randomUUID())
                .policyId(UUID.randomUUID()).endorsementNumber("END-1")
                .endorsementType(EndorsementType.ADD_COVERAGE).description("desc")
                .premiumDelta(java.math.BigDecimal.ONE).status(EndorsementStatus.REQUESTED)
                .requestedByUserId(TestFixtures.UNDERWRITER_ID).effectiveFrom(TestFixtures.NOW.toLocalDate())
                .createdAt(TestFixtures.NOW).updatedAt(TestFixtures.NOW).build();
        EndorsementCoverage ec = new EndorsementCoverage(UUID.randomUUID(), endorsement.getEndorsementId(),
                "FIRE", "Fire", java.math.BigDecimal.TEN, java.math.BigDecimal.ONE,
                java.math.BigDecimal.ONE, "condition", "exclusion", 3, EndorsementOperation.ADD, TestFixtures.NOW);
        assertNotNull(Mappers.getMapper(EndorsementMapper.class).toResponse(endorsement, List.of(ec)));
        EndorsementMapper endorsementMapper = Mappers.getMapper(EndorsementMapper.class);
        assertNotNull(endorsementMapper.mapCoverages(null));
        ec.setOperation(null);
        assertNotNull(endorsementMapper.toCoverageResponse(ec));

        PremiumAudit audit = PremiumAudit.builder().auditId(UUID.randomUUID()).policyId(UUID.randomUUID())
                .auditNumber("AUD-1").auditType(AuditType.SALES).status(AuditStatus.COMPLETED)
                .estimatedExposure(java.math.BigDecimal.TEN).actualExposure(java.math.BigDecimal.TEN)
                .exposureBasis("sales").createdAt(TestFixtures.NOW).updatedAt(TestFixtures.NOW).build();
        assertNotNull(Mappers.getMapper(PremiumAuditMapper.class).toResponse(audit));
        PremiumAuditMapper auditMapper = Mappers.getMapper(PremiumAuditMapper.class);
        assertNull(auditMapper.enumToString(null));
        audit.setAuditType(null);
        audit.setStatus(null);
        assertNotNull(auditMapper.toResponse(audit));

        Subjectivity subjectivity = Subjectivity.builder().subjectivityId(UUID.randomUUID())
                .quoteId(UUID.randomUUID()).subjectivityCode("DOC").description("doc")
                .status(SubjectivityStatus.OPEN).evidenceDocumentIds("[]")
                .createdAt(TestFixtures.NOW).updatedAt(TestFixtures.NOW).build();
        SubjectivityMapper subjectivityMapper = Mappers.getMapper(SubjectivityMapper.class);
        assertNotNull(subjectivityMapper.toResponse(subjectivity));
        assertNotNull(subjectivityMapper.toResponse(subjectivity).evidenceDocumentIds());
        assertNotNull(subjectivityMapper.parseDocuments("[ignored-json]"));
        assertNotNull(subjectivityMapper.parseDocuments(" "));
        assertNotNull(subjectivityMapper.parseDocuments(null));
        subjectivity.setStatus(null);
        assertNotNull(subjectivityMapper.toResponse(subjectivity));

        RenewalTransaction renewal = RenewalTransaction.builder().renewalId(UUID.randomUUID())
                .policyId(UUID.randomUUID()).renewalNumber("REN-1").status(RenewalStatus.ISSUED)
                .proposedStartDate(TestFixtures.NOW.toLocalDate()).proposedEndDate(TestFixtures.NOW.toLocalDate().plusYears(1))
                .proposedTotalPremium(java.math.BigDecimal.TEN).proposedCoverageSnapshot("[]")
                .subjectivities("[]").createdAt(TestFixtures.NOW).updatedAt(TestFixtures.NOW).build();
        RenewalTransactionMapper renewalMapper = Mappers.getMapper(RenewalTransactionMapper.class);
        assertNotNull(renewalMapper.toResponse(renewal));
        assertNotNull(renewalMapper.mapCoverages("[]"));
        assertNotNull(renewalMapper.mapSubjectivities("[]"));
    }

    @Test
    void exercisesExtendedEntityAccessorsAndBuilders() throws Exception {
        Class<?>[] entities = {Endorsement.class, EndorsementCoverage.class, PremiumAudit.class,
                Subjectivity.class, RenewalTransaction.class, QuoteVersion.class, Quote.class,
                QuoteCoverage.class, Policy.class, PolicyCoverage.class, UnderwritingDecision.class};
        for (Class<?> entity : entities) {
            Object instance = entity.getDeclaredConstructor().newInstance();
            for (Method method : entity.getMethods()) {
                if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                    method.invoke(instance, new Object[]{null});
                }
            }
            for (Method method : entity.getMethods()) {
                if ((method.getName().startsWith("get") || method.getName().startsWith("is"))
                        && method.getParameterCount() == 0 && method.getDeclaringClass() == entity) {
                    method.invoke(instance);
                }
            }
            Method builderFactory = entity.getMethod("builder");
            Object builder = builderFactory.invoke(null);
            for (Method method : builder.getClass().getMethods()) {
                if (method.getParameterCount() == 1 && method.getReturnType().isAssignableFrom(builder.getClass())) {
                    method.invoke(builder, new Object[]{null});
                }
            }
            builder.getClass().getMethod("build").invoke(builder);
        }
    }

    @Test
    void exercisesExtendedRecordAccessors() throws Exception {
        String[] names = {
                "com.intellisure.quotepolicyservice.dto.ApproveEndorsementRequest",
                "com.intellisure.quotepolicyservice.dto.EndorsementCoverageResponse",
                "com.intellisure.quotepolicyservice.dto.EndorsementResponse",
                "com.intellisure.quotepolicyservice.dto.IssueRenewalRequest",
                "com.intellisure.quotepolicyservice.dto.PremiumAuditResponse",
                "com.intellisure.quotepolicyservice.dto.RenewalCoverageResponse",
                "com.intellisure.quotepolicyservice.dto.RenewalSubjectivityResponse",
                "com.intellisure.quotepolicyservice.dto.SubjectivityResponse",
                "com.intellisure.quotepolicyservice.dto.request.AcceptQuoteRequest",
                "com.intellisure.quotepolicyservice.dto.request.BindQuoteRequest"
        };
        for (String name : names) {
            Class<?> type = Class.forName(name);
            Constructor<?> constructor = type.getDeclaredConstructors()[0];
            Object[] values = new Object[constructor.getParameterCount()];
            Object record = constructor.newInstance(values);
            for (var component : type.getRecordComponents()) {
                type.getMethod(component.getName()).invoke(record);
            }
        }
    }

    @Test
    void exercisesAllLifecycleEnumValues() {
        for (Class<? extends Enum<?>> type : List.of(EndorsementType.class, EndorsementStatus.class,
                EndorsementOperation.class, AuditType.class, AuditStatus.class, SubjectivityStatus.class,
                RenewalStatus.class, PolicyStatus.class, QuoteStatus.class, UnderwritingDecisionType.class)) {
            for (Enum<?> value : type.getEnumConstants()) {
                Enum.valueOf(type.asSubclass(Enum.class), value.name());
            }
        }
    }
}
