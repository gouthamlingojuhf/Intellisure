

**INTELLISURE**

**Microservice Business Definitions**

*Responsibilities, Ownership, Boundaries, Business Decisions and Cross-Service Workflow*

Research cut: September 18, 2026  
Technical implementation intentionally excluded

# **0\. How to Read This Document**

This document defines what each IntelliSure business microservice means in insurance-business terms. It is the business contract for the system: what each service is responsible for, what information it owns, which actors use it, what decisions happen there, and which responsibilities deliberately belong somewhere else.

The nine business services are fixed for the current capstone scope. Eureka Server and API Gateway are infrastructure services and are therefore not counted as business microservices.

## **The central rule**

A service owns a business truth, not merely a database table. Other services may request that truth, react to its events, or perform work because of it, but they must not silently create competing master records.

## **IntelliSure business channel**

| Stage | Primary business owner | Meaning |
| :---- | :---- | :---- |
| Customer relationship | Customer & Party | Who the party is and the business profile. |
| New business | Quote & Policy | What insurance is being requested and what contractual transaction is being created. |
| Risk decision support and underwriting | Risk & Underwriting | Whether the risk fits appetite, what hazards exist, what controls are required, and the underwriting decision/rationale. |
| Claims | Claims | What loss was reported, investigated, evaluated and settled under the policy. |
| External service providers | Vendor & Partner | Who can perform work for the insurer/customer and how their work is assigned and measured. |
| Post-loss recovery | Recovery & Business Continuity | How the business restores operations and continuity after a qualifying loss. |
| Process operations | Workflow & Notification | Who must do what, by when, in what sequence, with what SLA and notification. |
| Evidence and accountability | Document & Audit | Which documents/evidence exist and what happened, when, by whom, and under what business action. |
| Decision support | Analytics & Intelligence | Calculated insights, scores, priorities and recommendations; advisory unless an authorized business rule explicitly says otherwise. |

# **1\. The Nine Services at a Glance**

| \# | Service | One-line business responsibility | Primary business truth |
| :---- | :---- | :---- | :---- |
| 1 | Customer & Party Service | Manage identities, roles and the business/customer party profile. | Party and account identity |
| 2 | Quote & Policy Service | Manage the commercial insurance transaction from request through quote, bind, issue, servicing and renewal. | Quote and policy contract lifecycle |
| 3 | Risk & Underwriting Service | Evaluate risk, manage underwriting work, risk findings, recommendations and decision rationale. | Risk and underwriting assessment/decision |
| 4 | Claims Service | Manage FNOL through coverage evaluation, investigation, assessment, settlement, payment and claim closure. | Claim lifecycle and financial claim truth |
| 5 | Vendor & Partner Service | Manage service-provider eligibility, onboarding, assignment and performance. | Vendor/partner master and work relationship |
| 6 | Recovery & Business Continuity Service | Manage post-loss operational recovery, continuity plans and recovery support. | Recovery case, plan and milestones |
| 7 | Workflow & Notification Service | Orchestrate human/system tasks, assignment, SLA, escalation and notifications. | Process/task state |
| 8 | Document & Audit Service | Manage evidence/document metadata and immutable accountability history. | Evidence and audit trail |
| 9 | Analytics & Intelligence Service | Provide portfolio, risk, claims and renewal intelligence as decision support. | Analytical snapshots/model outputs |

# **2\. Non-Negotiable Business Boundaries**

* Customer & Party knows who the customer/user is; it does not decide whether the customer is insurable.  
* Quote & Policy knows the commercial insurance transaction and policy contract; it does not become the risk-engineering database.  
* Risk & Underwriting knows why the risk is acceptable, unacceptable or conditionally acceptable; it does not become the policy master.  
* Claims knows what happened and what is payable under the policy; it does not rewrite the policy contract.  
* Vendor & Partner knows which providers are eligible and what work they perform; it does not decide claim coverage.  
* Recovery & Business Continuity knows how the customer/business recovers operationally; it does not decide whether a loss is covered.  
* Workflow & Notification knows who is responsible for the next action and whether it is late/escalated; it does not become the source of domain truth.  
* Document & Audit knows the evidence and history; it does not become the owner of the business decision represented by that evidence.  
* Analytics & Intelligence explains patterns and recommends actions; it does not silently replace authorized underwriting, claims or management decisions.  
* Policy data remains authoritative in Quote & Policy; Claim data remains authoritative in Claims; recovery data remains authoritative in Recovery; and so on.

# **1\. Customer & Party Service**

**Business mission.** Create and maintain the identity of every person/organization interacting with IntelliSure and the canonical small-business party profile. It answers the question: “Who is this party, what business do they represent, and what role are they authorized to perform?”

| Primary business actors | Policyholder / small-business owner; Underwriter; Risk Engineer; Claims Adjuster; Claims Manager; Vendor Manager; Service Provider/Contractor; System Administrator |
| :---- | :---- |
| **Business source of truth** | User account, authentication identity, role/account lifecycle, and small-business customer/party profile. |

## **Core responsibilities**

* Policyholder self-registration and maintenance of the business profile.  
* Creation and maintenance of internal employee accounts by authorized administration; employee roles are not self-registered.  
* Contractor/vendor user onboarding identity handoff to the Vendor & Partner process.  
* Maintenance of party attributes used by downstream insurance processes: legal/business name, owner/contact, address, business type, and contact details.  
* Provide trusted party identity to quote, risk, claims and recovery processes without duplicating the customer master.  
* Maintain account status such as active/inactive and role assignment.  
* Preserve the distinction between a login identity (UserAccount) and the business/customer party (BusinessCustomer).

## **Business lifecycle handled by this service**

1. Party registration → profile completion → active customer account.  
2. Internal user creation → activation → role change/deactivation.  
3. Party profile update → downstream processes use the updated party information when appropriate.  
4. Account deactivation may stop future access while historical business records remain preserved elsewhere.

## **Business inputs**

* Identity/account details and role.  
* Business profile and contact details.  
* Customer ID referenced by quotes, policies, claims and recovery cases.  
* Account status changes and administrative actions.

## **Business outputs / outcomes**

* Authenticated identity.  
* Business profile.  
* Role and account status.  
* Customer/party lookup result for authorized internal business processes.

## **What this service must NOT own**

* Insurance eligibility or risk acceptance.  
* Premium calculation.  
* Policy terms.  
* Claim coverage decisions.  
* Vendor eligibility decisions.  
* Underwriter workload/assignment decisions.

## **Business decisions and authority**

* Who may create/maintain which type of account.  
* Whether a role requires administrator creation.  
* Whether an account is active enough to perform business actions.

## **Key business records / concepts**

* UserAccount.  
* BusinessCustomer.  
* Party/contact profile.  
* Role/account lifecycle.  
* Administrative change history (through Document & Audit).

## **Relationship with other services**

* Quote & Policy: Provides customer identity and profile context; does not own it.  
* Risk & Underwriting: Provides business/party facts needed for risk evaluation.  
* Claims: Resolves insured/customer identity for FNOL and claim handling.  
* Vendor & Partner: Provides identity for contractor/provider users when needed.  
* Workflow & Notification: Supplies eligible user identities for task ownership/notification.  
* Document & Audit: Receives auditable identity/account changes and decision actors.  
* Analytics & Intelligence: Supplies party/customer dimensions when analytics is authorized to use them.

## **Concrete IntelliSure example**

A restaurant owner registers, completes the restaurant business profile, and becomes an ACTIVE POLICYHOLDER. The same Customer ID is later attached to the quote, policy, claim and recovery case; no downstream service creates a second copy of the business as its own master.

## **Questions you should be able to answer**

* Why is UserAccount separate from BusinessCustomer?  
* Why cannot an employee self-register?  
* What information identifies the insured business?  
* Which service owns customer\_id?  
* What happens to policies and claims if a user account is deactivated?

## **Industry grounding**

* NAIC insurance glossary and producer/licensing material for party/insurance terminology; IntelliSure current customer/party entity design.

# **2\. Quote & Policy Service**

**Business mission.** Own the commercial insurance transaction itself: the customer request, submission/quote, offered terms, acceptance, binding, policy issuance, policy servicing, endorsements, cancellations/reinstatements where applicable, and renewal transaction. It answers: “What insurance contract or proposed contract exists, on what terms, for whom, and in what state?”

| Primary business actors | Policyholder; Underwriter; Claims Adjuster/Manager; Risk Engineer; System Administrator |
| :---- | :---- |
| **Business source of truth** | Quote and policy contract lifecycle, proposed terms, policy period, coverages, limits/deductibles/premium, endorsements and renewal transactions. |

## **Core responsibilities**

* Capture the policyholder’s insurance need, structured business/exposure information and requested coverages for a new-business transaction.  
* Distinguish a draft request/application/submission from a quoted set of insurer terms.  
* Maintain the quote lifecycle: draft, submitted, under review, quoted/terms offered, accepted/declined/expired and other controlled states needed by the workflow.  
* Apply the outcome of underwriting to the quote without becoming the owner of risk evidence.  
* Maintain customer acceptance and the path to binding.  
* Record the authoritative bound terms and create/issue the policy contract.  
* Maintain policy servicing transactions such as endorsements/MTA, cancellation, reinstatement and corrections according to the project’s scope.  
* Run renewal transactions using current exposure, loss history, risk findings and analytics as inputs rather than simply extending dates.  
* Provide authoritative policy coverage data to Claims.

## **Business lifecycle handled by this service**

5. Customer request → DRAFT.  
6. Completed submission → SUBMITTED.  
7. Triage/underwriting → UNDER\_REVIEW or equivalent.  
8. Terms offered → QUOTED.  
9. Customer acceptance → bind-ready.  
10. Coverage bound → BOUND.  
11. Policy issued → IN\_FORCE.  
12. Mid-term change → endorsement transaction.  
13. Term end → renewal evaluation / expiration.  
14. Cancellation/reinstatement → controlled policy transaction when applicable.

## **Business inputs**

* Customer need/narrative and structured business information.  
* Requested product/coverage and limits.  
* Underwriting/risk assessment outcome.  
* Customer acceptance.  
* Policy change request.  
* Renewal intelligence and current exposure/loss information.

## **Business outputs / outcomes**

* Quote number and current state.  
* Policy number and current policy state.  
* Authoritative coverages/limits/deductibles/premium.  
* Endorsement history.  
* Renewal transaction and decision status.  
* Coverage information consumed by Claims.

## **What this service must NOT own**

* Risk engineering findings as master data.  
* Claim state or settlement.  
* Vendor assignment or contractor profile.  
* Workflow task ownership/SLA.  
* Audit history.  
* Analytics model implementation details.

## **Business decisions and authority**

* Whether a quote is in a valid state for the next commercial transaction.  
* What contractual terms can be offered after underwriting.  
* Whether a customer acceptance can proceed to binding.  
* Whether a policy can be issued after bind and required conditions.  
* Whether a policy change is material enough to trigger underwriting/risk review.

## **Key business records / concepts**

* Quote.  
* Coverage terms.  
* Policy.  
* Policy coverage.  
* Endorsement / MTA.  
* Renewal transaction.  
* Policy version/history.

## **Relationship with other services**

* Customer & Party: Identifies the business and policyholder.  
* Risk & Underwriting: Supplies risk assessment and underwriting outcome; Quote & Policy remains contract/transaction authority.  
* Workflow & Notification: Starts and tracks submission, review, bind and servicing tasks.  
* Document & Audit: Stores/records supporting documents and transaction history.  
* Analytics & Intelligence: Provides decision support for risk/renewal/portfolio decisions.  
* Claims: Reads authoritative policy and coverage during FNOL and claim assessment.

## **Concrete IntelliSure example**

The owner describes a restaurant, asks for property, general liability and business-income protection, and submits the request. It is not yet a policy. An underwriter reviews the risk and offers terms. The owner accepts. The authorized binding step commits coverage, and Quote & Policy then issues the policy contract. Six months later the restaurant adds another location; Quote & Policy creates an endorsement transaction rather than overwriting the old policy record.

## **Questions you should be able to answer**

* Why is a submission different from a quote?  
* Why is a quote different from binding?  
* What exactly makes a policy “in force”?  
* Why should endorsements preserve history?  
* Why is renewal a transaction rather than a date update?  
* Which service is authoritative when Claims asks whether a policy has a specific coverage?

## **Industry grounding**

* ACORD P\&C standards distinguish new-business quote, new-business submission, policy change, renewal and reinstatement. Chubb current commercial materials distinguish new-business intake, quoting, confirm coverage/bind, policy administration, renewal and MTA. The Hartford states that actual pricing/coverage depends on information provided and underwriting/rating criteria.

# **3\. Risk & Underwriting Service**

**Business mission.** Convert raw business information into a structured view of risk and an authorized underwriting decision. It answers: “What risk are we taking, what hazards and exposures exist, does it fit appetite and authority, what conditions are needed, and why was the decision made?”

| Primary business actors | Underwriter; Risk Engineer; Claims Manager/Adjuster as information consumers; System Administrator for rules governance |
| :---- | :---- |
| **Business source of truth** | Risk assessment, risk findings, loss-control recommendations, underwriting rules/criteria and the underwriting decision rationale. |

## **Core responsibilities**

* Triage and classify the submission for underwriting attention, including completeness and referral indicators.  
* Maintain risk assessments linked to a quote or policy transaction.  
* Capture exposure information: business operations, locations, revenue/payroll where relevant, employees, assets, processes and other risk characteristics.  
* Identify hazards and controls: fire protection, housekeeping, equipment, safety practices, security, business continuity and other relevant controls.  
* Coordinate or record Risk Engineering assessments and recommendations when specialist inspection/input is warranted.  
* Incorporate loss history/loss runs and other relevant evidence into the underwriting picture.  
* Apply underwriting appetite, authority and rules; identify referrals and subjectivities.  
* Record the authorized underwriting outcome: accept/decline, terms, conditions, subjectivities, rationale and authority level.  
* Provide risk decision support to Quote & Policy without owning the policy contract itself.  
* Feed learnings to analytics for decision support, without becoming the analytics execution engine.

## **Business lifecycle handled by this service**

15. Submitted → triage.  
16. Complete → assigned/review queue.  
17. Risk assessment → findings/recommendations.  
18. Underwriting review → referred/needs information/terms/decline.  
19. Conditions/subjectivities → satisfied or outstanding.  
20. Authorized decision → approved/declined/conditional.  
21. Post-bind monitoring and renewal reassessment when required.

## **Business inputs**

* Submitted quote and business profile.  
* Exposure and operations details.  
* Documents, loss history and inspection evidence.  
* Risk-engineering findings.  
* Analytics indicators.  
* Policy change or renewal information.

## **Business outputs / outcomes**

* Risk assessment.  
* Risk findings and severity.  
* Loss-prevention recommendations.  
* Referral/subjectivity requirements.  
* Underwriting decision and rationale.  
* Risk/underwriting status that can be consumed by Quote & Policy and Workflow.

## **What this service must NOT own**

* Policy number generation/contract issuance.  
* Customer identity master.  
* Claim coverage determination.  
* Vendor eligibility/assignment.  
* Task notification delivery.  
* Final accounting settlement.

## **Business decisions and authority**

* Whether the risk fits appetite.  
* Whether human review is mandatory.  
* Whether specialist Risk Engineering input is required.  
* What risk-improvement actions or subjectivities are needed.  
* Whether the underwriter can approve within authority or must escalate.  
* Whether the risk should be declined, accepted, or offered subject to conditions.

## **Key business records / concepts**

* RiskAssessment.  
* RiskFinding.  
* RiskRecommendation.  
* Underwriting referral.  
* Subjectivity.  
* Underwriting decision/rationale.  
* Rule/version reference.

## **Relationship with other services**

* Quote & Policy: Provides the submission and receives the underwriting result to move the insurance transaction.  
* Customer & Party: Provides trusted business identity/profile.  
* Document & Audit: Stores evidence and records decisions/actors.  
* Workflow & Notification: Creates the underwriter/risk-engineer tasks, deadlines and escalations.  
* Analytics & Intelligence: Provides model/rule indicators and receives decision data for analysis.  
* Claims: Claims history becomes relevant underwriting evidence; claim state remains owned by Claims.

## **Concrete IntelliSure example**

A submitted restaurant risk is classified as requiring human underwriting because the requested coverage, revenue and operational characteristics fall outside the straight-through rules. The workflow assigns an underwriter. A Risk Engineer reviews fire protection and kitchen controls. The underwriter reviews the assessment, prior losses and documents, requests two subjectivities, then records a conditional approval within authority. Quote & Policy uses that result to produce the offered terms.

## **Questions you should be able to answer**

* What is underwriting actually deciding?  
* What is the difference between hazard, exposure and risk?  
* What is a referral?  
* What is a subjectivity?  
* Why is Risk Engineering not the same thing as underwriting?  
* What evidence should support an underwriter decision?  
* Why must model/rule versions be retained?

## **Industry grounding**

* NAIC describes underwriting as evaluating risk, deciding whether to accept it, classifying it and determining an appropriate rate. The Hartford describes Risk Engineering as exposure evaluations, consultations and recommendations for safer operations. Verisk current small-commercial underwriting materials emphasize richer business/exposure/property data and automated data enrichment.

# **4\. Claims Service**

**Business mission.** Manage an insured loss from the first report through investigation, coverage determination, evaluation, reserving, settlement/payment, recovery interests and closure. It answers: “What happened, is it covered, how much is payable, what evidence supports the decision, and what remains open?”

| Primary business actors | Policyholder; Claims Adjuster; Claims Manager; approved service providers; Recovery stakeholders |
| :---- | :---- |
| **Business source of truth** | Claim/FNOL state, investigation, coverage decision, assessment, financial evaluation, settlement and claim closure. |

## **Core responsibilities**

* Receive FNOL with when/where/what happened, affected property/operations, description, contacts and initial evidence.  
* Verify policy identity and relevant coverage with Quote & Policy; Claims does not create a competing policy master.  
* Triage claim priority/complexity and assign/reassign an adjuster under claims authority.  
* Investigate cause of loss, facts, liability where applicable and policy applicability.  
* Request and evaluate documents, statements, repair estimates, photographs, invoices and other evidence.  
* Maintain claim assessments and distinguish total loss from covered loss.  
* Maintain reserves and update financial estimates as new information arrives.  
* Calculate payout considering covered loss, deductible, limits, adjustments and policy terms.  
* Record settlement approvals and payments.  
* Identify and coordinate salvage/subrogation opportunities where applicable.  
* Create/coordinate Recovery cases when the loss has business-continuity consequences.  
* Close the claim only when required claim and recovery conditions are satisfied.

## **Business lifecycle handled by this service**

22. FNOL received → claim created.  
23. Triage → adjuster assigned.  
24. Investigation → evidence gathering.  
25. Coverage determination → covered/partially covered/not covered.  
26. Assessment → loss quantification.  
27. Reserve → revised as facts develop.  
28. Settlement → approved.  
29. Payment → financial completion.  
30. Subrogation/salvage → recovery activity if applicable.  
31. Closure → all required actions complete.

## **Business inputs**

* FNOL/loss report.  
* Policy and coverage data.  
* Evidence/documents.  
* Inspection/appraisal/repair information.  
* Business-impact information.  
* Vendor information.  
* Analytics prioritization/decision support.

## **Business outputs / outcomes**

* Claim number/status.  
* Coverage determination.  
* Claim assessment.  
* Reserve history/financial evaluation.  
* Payout calculation.  
* Settlement and payment outcome.  
* Claim timeline.  
* Recovery trigger.  
* Closure decision.

## **What this service must NOT own**

* Policy contract master.  
* Business recovery plan master.  
* Vendor eligibility master.  
* Underwriting risk decision.  
* Customer master.  
* Immutable audit history.

## **Business decisions and authority**

* Whether a loss is covered under the policy.  
* Claim priority and assignment within claims authority.  
* Required investigation scope.  
* Reserve estimates and updates.  
* Settlement/payout within claims authority.  
* Whether escalation or managerial approval is required.  
* Whether salvage/subrogation activity should be opened.  
* Whether closure conditions are satisfied.

## **Key business records / concepts**

* Claim.  
* ClaimAssessment.  
* PayoutCalculation.  
* ClaimSettlement.  
* BusinessImpact.  
* ClaimTimelineEvent.  
* Reserve history.  
* Recovery trigger.

## **Relationship with other services**

* Quote & Policy: Authoritative source for policy/coverage facts.  
* Customer & Party: Provides customer identity/profile.  
* Vendor & Partner: Supplies eligible repair/inspection/service providers.  
* Recovery & Business Continuity: Handles post-loss restoration and continuity.  
* Workflow & Notification: Manages claims tasks, SLAs, escalation and notifications.  
* Document & Audit: Stores claim evidence and immutable event history.  
* Analytics & Intelligence: Supports prioritization and intelligence; claims remains decision authority.

## **Concrete IntelliSure example**

A restaurant reports a kitchen fire at 10:15 AM. Claims creates FNOL, verifies the policy and coverage, assigns an adjuster, requests photos and repair estimates, and records the initial reserve. The adjuster determines the covered damage, the applicable deductible and policy limits, then approves a settlement within authority. A repair vendor is coordinated through Vendor & Partner. If another party caused the fire, subrogation activity can be opened. Because the restaurant lost operating capacity, Claims creates a Recovery case. The claim closes only after settlement/payment and required downstream obligations are complete.

## **Questions you should be able to answer**

* What is FNOL?  
* Why can a claim be reported before coverage is finally determined?  
* What is the difference between loss amount and covered loss?  
* Why does a reserve change?  
* What is the difference between settlement and payment?  
* What are salvage and subrogation?  
* When can a claim be closed?

## **Industry grounding**

* NAIC claims-handling materials describe investigation, cause-of-loss, liability, loss amount, claim conclusion, case reserves and payments, claim closure, salvage and subrogation. NAIC glossary defines subrogation. Current NAIC 2026 materials also describe claim-intake, exposure estimation, repair coordination, reserve recommendations and settlement workflows.

# **5\. Vendor & Partner Service**

**Business mission.** Manage the insurer’s ecosystem of external service providers and contractors who may inspect, repair, assess, restore, investigate or otherwise perform authorized work. It answers: “Who is qualified to perform this work, under what conditions, who was assigned, and how did they perform?”

| Primary business actors | Vendor Manager; Service Provider / Contractor; Claims Adjuster/Manager; Risk Engineer; Recovery Coordinator |
| :---- | :---- |
| **Business source of truth** | Vendor/provider profile, onboarding/verification, eligibility, assignment and performance. |

## **Core responsibilities**

* Receive and manage contractor/service-provider onboarding requests.  
* Verify required business credentials, service capabilities and eligibility criteria appropriate to the project scope.  
* Maintain provider categories, service areas, capabilities and status.  
* Maintain assignments for claims, inspections, repairs and recovery work.  
* Track assignment acceptance, work status and completion evidence.  
* Record provider performance, timeliness, quality and outcome information.  
* Suspend/deactivate provider eligibility when required by management rules.  
* Supply qualified-provider choices to Claims, Risk and Recovery.

## **Business lifecycle handled by this service**

32. Onboarding requested → verification → approved/rejected → active/inactive.  
33. Work requested → vendor search → assignment → accepted → in progress → completed → performance recorded.  
34. Provider issue → escalation/review → reassignment if necessary.

## **Business inputs**

* Contractor registration request.  
* Business credentials and service capabilities.  
* Location/service area.  
* Claim/recovery/inspection work requirements.  
* Performance feedback and completion evidence.

## **Business outputs / outcomes**

* Eligible vendor list.  
* Vendor assignment.  
* Assignment status.  
* Provider performance record.  
* Eligibility status.

## **What this service must NOT own**

* Claim coverage decision.  
* Policy issuance.  
* Customer identity master.  
* Recovery strategy/business-impact decision.  
* Final audit history.

## **Business decisions and authority**

* Whether a provider meets configured eligibility requirements.  
* Which eligible provider can receive a work assignment based on service/location/capability constraints.  
* Whether a vendor should be suspended/deactivated.

## **Key business records / concepts**

* Vendor.  
* VendorOnboardingRequest.  
* VendorAssignment.  
* VendorPerformance.  
* Eligibility/capability profile.

## **Relationship with other services**

* Claims: Requests eligible providers for inspections, repairs, appraisals or other claim work.  
* Recovery & Business Continuity: Requests providers for restoration and continuity work.  
* Risk & Underwriting: May request inspection/risk-engineering partners where applicable.  
* Workflow & Notification: Tracks provider task deadlines and notifications.  
* Customer & Party: Provides service-provider user identity when contractors interact with the platform.  
* Document & Audit: Stores supporting credentials/evidence and auditable actions.

## **Concrete IntelliSure example**

A restoration contractor requests onboarding. The Vendor Manager verifies the business and capability information and activates the provider. A restaurant fire claim requires emergency drying and repair. Claims requests eligible vendors; the selected vendor is assigned the work. The vendor completes the repair, submits completion evidence, and the assignment is recorded as completed. Vendor performance is updated for future selection decisions.

## **Questions you should be able to answer**

* Who is a vendor versus an insured?  
* Why can a contractor request onboarding but not activate itself?  
* What makes a vendor eligible?  
* Why should vendor assignment be separate from vendor master data?  
* How can vendor performance influence future operational choices without deciding claim coverage?

## **Industry grounding**

* NAIC claims-handling materials recognize outside parties such as independent adjusters, appraisers, investigators and counsel in claims operations. Current Chubb commercial materials reference digital servicing and partner/team workflows. IntelliSure’s current vendor entities already separate onboarding, assignment and performance.

# **6\. Recovery & Business Continuity Service**

**Business mission.** Help a business recover operationally after a qualifying insured loss, while keeping the coverage decision with Claims. It answers: “What has the loss disrupted, what must be restored first, what support is needed, and is the business getting back to normal?”

| Primary business actors | Policyholder; Claims Adjuster/Manager; Recovery coordinator; Service Provider/Contractor; Vendor Manager |
| :---- | :---- |
| **Business source of truth** | Recovery case, business-impact assessment, continuity/recovery plan and recovery support requests/milestones. |

## **Core responsibilities**

* Create a recovery case when claim circumstances create a material business interruption or restoration need.  
* Assess business impact: affected location, critical operations, lost capacity, dependencies, workforce/equipment impact and expected recovery milestones.  
* Develop a recovery/continuity plan around priorities, dependencies, target dates and responsibilities.  
* Create support requests for repairs, temporary facilities, equipment, specialist services or other approved support.  
* Coordinate eligible vendors without owning vendor eligibility.  
* Track restoration milestones, blockers and business reopening progress.  
* Escalate material recovery delays or dependencies to Claims/management.  
* Provide recovery status back to Claims and management for closure decisions.

## **Business lifecycle handled by this service**

35. Loss triggers recovery assessment.  
36. Business impact captured.  
37. Recovery plan created.  
38. Support requests opened.  
39. Vendor work underway.  
40. Milestones achieved.  
41. Business reopened/operational target reached.  
42. Recovery case closed.

## **Business inputs**

* Claim ID and loss context.  
* Business-impact facts.  
* Customer/business priorities.  
* Approved support needs.  
* Vendor assignment information.  
* Recovery milestones and evidence.

## **Business outputs / outcomes**

* Recovery case.  
* Business continuity plan.  
* Recovery support request.  
* Milestone status.  
* Recovery bottleneck/escalation.  
* Recovery completion outcome.

## **What this service must NOT own**

* Whether a claim is covered.  
* How much the insurer owes under coverage.  
* Vendor eligibility master.  
* Claim reserve/settlement master.  
* Policy contract.

## **Business decisions and authority**

* What recovery milestones are required.  
* Which business operations are critical and should be restored first.  
* What support request is needed.  
* When escalation is needed because recovery is delayed or blocked.

## **Key business records / concepts**

* RecoveryCase.  
* RecoveryPlan.  
* RecoverySupportRequest.  
* BusinessImpact.  
* Recovery milestone/status.

## **Relationship with other services**

* Claims: Creates/links the recovery case when claim impact requires continuity support; Claims remains coverage authority.  
* Vendor & Partner: Finds eligible providers and records assignments.  
* Workflow & Notification: Tracks recovery tasks, deadlines and escalations.  
* Document & Audit: Stores impact evidence and recovery accountability.  
* Analytics & Intelligence: Highlights recovery bottlenecks and operational patterns.  
* Customer & Party: Provides business context.

## **Concrete IntelliSure example**

A fire closes the restaurant for two weeks. Claims determines the covered claim and the applicable business-income coverage. Recovery then assesses which kitchen equipment, premises and supplier dependencies are blocking reopening, creates a recovery plan, assigns approved contractors for repairs, tracks milestones and escalates a delayed refrigeration repair. Recovery does not decide whether the lost income is covered; it manages restoration/continuity work after that business question is handled by Claims.

## **Questions you should be able to answer**

* What is business continuity versus insurance coverage?  
* Why should recovery be a separate business responsibility?  
* What is the difference between business impact and covered loss?  
* Who decides if a recovery support cost is covered?  
* When should the recovery case close?

## **Industry grounding**

* The Hartford BOP materials identify business-income protection for covered losses. Triple-I business-claim guidance discusses temporary repairs, documentation and business-income claim information. IntelliSure’s current design explicitly separates BusinessImpact and Recovery from claim coverage determination.

# **7\. Workflow & Notification Service**

**Business mission.** Act as the operational process-control layer that turns business events into assigned work, sequencing, deadlines, escalation and notifications. It answers: “Who is supposed to do what next, by when, and what happens when the work is not completed?”

| Primary business actors | All human roles; internal system processes; System Administrator / workflow administrator |
| :---- | :---- |
| **Business source of truth** | Workflow instance, human/system task state, assignment, SLA/elapsed-time tracking, escalation and notification delivery state. |

## **Core responsibilities**

* Start workflows when business events occur: submission, underwriting referral, quote acceptance, bind, policy change, FNOL, claim assessment, recovery creation, vendor onboarding, renewal, etc.  
* Create human tasks with a clear business action and accountable owner.  
* Assign/reassign tasks based on configured role, eligibility, workload, authority and escalation rules.  
* Track due dates, SLA clocks, waiting states and escalations.  
* Send in-app/system notifications for assignments, requests, reminders, approvals and escalations.  
* Prevent duplicate task creation for the same business event where idempotency/business correlation rules apply.  
* Maintain a traceable process history without becoming the domain master.  
* Expose operational workload and bottlenecks to Analytics.

## **Business lifecycle handled by this service**

43. Business event → workflow instance → tasks → assignment → work → completion/rejection/request-more-info → next task → escalation if overdue → workflow completion/cancellation.

## **Business inputs**

* Business event and correlation ID.  
* Required role/actor class.  
* Business status context.  
* SLA/priority rules.  
* Task assignment information.

## **Business outputs / outcomes**

* Workflow status.  
* Task list and ownership.  
* Due dates and escalation state.  
* Notification state.  
* Operational audit/event context.

## **What this service must NOT own**

* Quote status.  
* Policy status.  
* Underwriting decision.  
* Claim status.  
* Recovery status.  
* Vendor eligibility.  
* Document contents.

## **Business decisions and authority**

* Who receives a task.  
* When a task is due.  
* When a task escalates.  
* Which next task becomes eligible after completion.  
* Which notification should be generated.

## **Key business records / concepts**

* Workflow.  
* WorkflowTask.  
* Notification.  
* SLA/escalation state.  
* Assignment history.

## **Relationship with other services**

* Customer & Party: Provides user/role identity information.  
* Risk & Underwriting: Consumes underwriting task orchestration.  
* Quote & Policy: Triggers new-business, bind, servicing and renewal workflows.  
* Claims: Triggers claim handling and approval workflows.  
* Vendor & Partner: Triggers onboarding/assignment workflows.  
* Recovery: Tracks continuity/recovery workflows.  
* Document & Audit: Stores auditable process events.  
* Analytics & Intelligence: Consumes workflow performance and workload patterns.

## **Concrete IntelliSure example**

A quote is submitted. Workflow starts a new-business underwriting process, creates a risk-review task, determines an eligible underwriter, sets a due date, and sends the assignment notification. If the underwriter requests more information, Workflow pauses the underwriting task and creates the customer/document request. If overdue, it escalates. Workflow does not change the quote’s business truth from SUBMITTED to APPROVED; the domain service responsible for that truth does so.

## **Questions you should be able to answer**

* Why is Workflow not allowed to own quote/claim/policy status?  
* How is an underwriter assigned?  
* What is an SLA versus a business status?  
* Why do escalation and notification belong here?  
* What prevents duplicate tasks?

## **Industry grounding**

* Chubb current commercial operating materials describe separate workflow areas for new-business intake, quoting, bind confirmation, policy administration, renewals and MTA. Current IntelliSure service-to-service design already separates domain state from workflow task state.

# **8\. Document & Audit Service**

**Business mission.** Provide the evidence and accountability layer for insurance operations. It answers two different questions: “What evidence/document exists?” and “What happened, when, by whom, and under what business action?”

| Primary business actors | All business services; all authorized human roles; System Administrator; policyholder for permitted documents |
| :---- | :---- |
| **Business source of truth** | Document metadata/object-storage references and immutable audit events. |

## **Core responsibilities**

* Register document metadata and link it to the relevant business object/case without owning that object’s business lifecycle.  
* Support document categories such as application/submission, loss evidence, inspection report, estimate, proof-of-loss information, policy document, endorsement, vendor credential and recovery evidence.  
* Maintain document version and status metadata where applicable.  
* Preserve immutable audit events for material business actions and state transitions.  
* Retain actor identity, timestamp, business object, action, reason/correlation context and outcome needed to reconstruct decisions.  
* Keep sensitive credentials/secrets out of audit payloads; audit records should be business evidence, not security tokens.  
* Support retrieval of evidence for underwriting, claims, customer service, disputes and operational review.

## **Business lifecycle handled by this service**

44. Document submitted → metadata registered → reviewed/accepted/rejected/superseded where applicable.  
45. Business action → audit event appended → retained for traceability.  
46. Correction → new version/event rather than destructive history replacement.

## **Business inputs**

* Uploaded evidence.  
* Document category/type.  
* Business object correlation.  
* Actor/action/reason.  
* State transition event.

## **Business outputs / outcomes**

* Document metadata and storage reference.  
* Version history.  
* Audit event.  
* Evidence availability/status.

## **What this service must NOT own**

* Policy decisions.  
* Claim coverage decisions.  
* Risk assessments.  
* Vendor eligibility.  
* Workflow ownership.

## **Business decisions and authority**

* Whether a document satisfies an evidence requirement when the owning domain service asks for that determination.  
* Whether an audit event is recorded for a material business action.  
* Which document version is current/superseded.

## **Key business records / concepts**

* Document.  
* AuditEvent.  
* Document version/status.  
* Evidence link/correlation.

## **Relationship with other services**

* Quote & Policy: Stores quote/policy documents and audit events.  
* Risk & Underwriting: Stores risk findings, inspection evidence and underwriting decision support documents.  
* Claims: Stores FNOL evidence, assessment reports, settlement documents and proof-of-loss information.  
* Vendor & Partner: Stores provider credentials and onboarding evidence.  
* Recovery: Stores impact/recovery evidence.  
* Workflow: Stores process history/event references.  
* Customer & Party: Stores/records profile or administrative supporting evidence when needed.  
* Analytics: May reference evidence lineage/model version metadata; it does not own original evidence.

## **Concrete IntelliSure example**

An underwriter requests a fire-protection inspection report. The document is uploaded and linked to the risk assessment. The underwriter then approves with a condition. The audit trail records the approval actor, time, decision and rationale reference. Later, when a claim is disputed, authorized staff can reconstruct which policy version, inspection report and decision event were present at the time.

## **Questions you should be able to answer**

* Why are documents and audit separate concepts?  
* Why should audit be append-only?  
* Why should a state transition have an actor and reason?  
* Why should a corrected document create a new version?  
* What evidence can support an underwriting or claims decision?

## **Industry grounding**

* NAIC claims-handling materials emphasize claims documentation, investigations and information flows; ACORD standards emphasize lifecycle data exchange. The current IntelliSure design already separates Document metadata from AuditEvent and treats audit as append-only.

# **9\. Analytics & Intelligence Service**

**Business mission.** Turn accumulated operational and insurance data into explainable decision support for underwriting, claims, recovery, renewals and management. It answers: “What does the data suggest, what should a human review, and what patterns or signals are visible?”

| Primary business actors | Underwriter; Risk Engineer; Claims Adjuster/Manager; System Administrator/management |
| :---- | :---- |
| **Business source of truth** | Analytical snapshots and decision-support outputs, including risk scores, claim prioritization, recovery signals and renewal intelligence. |

## **Core responsibilities**

* Create risk/underwriting indicators from authorized historical and current data.  
* Prioritize claims using configured indicators such as severity/urgency/complexity, while leaving claim authority with Claims.  
* Provide renewal intelligence based on current exposure, loss history, risk findings and prior policy performance.  
* Highlight recovery bottlenecks, workload patterns and operational trends.  
* Persist model/rule versions with each important analytical result.  
* Provide explanations/feature summaries suitable for human review rather than unexplained final decisions.  
* Maintain clear separation between analytical output and the authorized business decision.  
* Support portfolio-level reporting and management views.

## **Business lifecycle handled by this service**

47. Data available → feature preparation → score/intelligence generation → snapshot retained with version → human/service consumes it → decision outcome recorded by the responsible domain service → result can feed future analysis.

## **Business inputs**

* Risk/claim/policy/workflow historical data.  
* Current case attributes.  
* Risk rules/model versions.  
* Operational performance data.

## **Business outputs / outcomes**

* Risk score snapshot.  
* Claim prioritization.  
* Renewal intelligence.  
* Portfolio/operational insight.  
* Model/rule/version metadata.

## **What this service must NOT own**

* Final underwriting decision.  
* Final claim coverage decision.  
* Final settlement authority.  
* Policy issuance authority.  
* Vendor eligibility decision.

## **Business decisions and authority**

* Whether a case deserves additional attention.  
* Which indicators are strongest or unusual.  
* What renewal patterns or exposure changes merit review.  
* Which recovery/workflow bottlenecks should be investigated.

## **Key business records / concepts**

* ClaimIntelligenceSnapshot.  
* RiskScoreSnapshot.  
* RenewalIntelligenceSnapshot.  
* Model version.  
* Rule version.  
* Explanation/indicator metadata.

## **Relationship with other services**

* Risk & Underwriting: Consumes risk indicators; underwriter remains responsible for authorized underwriting decision.  
* Claims: Consumes claim priority/intelligence; adjuster/manager remains responsible for claim decisions.  
* Quote & Policy: Consumes renewal intelligence; underwriting/business authority remains responsible for renewal decision.  
* Recovery: Consumes bottleneck/forecast indicators.  
* Workflow: Consumes workload/SLA trends.  
* Document & Audit: Decision-support output version/traceability can be retained for accountability.

## **Concrete IntelliSure example**

Analytics flags a restaurant renewal because revenue, locations and prior loss experience changed materially. It creates a renewal intelligence snapshot with model/rule version and explanatory indicators. The underwriter reviews it alongside current submission information and risk evidence and makes the renewal decision. Analytics records what it suggested; it does not claim to have made the underwriting decision.

## **Questions you should be able to answer**

* Why is Analytics advisory?  
* Why must model/rule versions be stored?  
* Why should a score never be the only explanation for a consequential decision?  
* What data can help prioritize a claim without deciding coverage?  
* How can analytics support renewal without automatically renewing?

## **Industry grounding**

* The Hartford and Chubb current materials describe data-enriched/streamlined small-commercial underwriting and lifecycle analytics. NAIC 2026 materials discuss AI use in underwriting and claims as decision-support/assistance; IntelliSure should preserve explicit human authority for consequential decisions.

# **11\. How the Services Work Together Across the Insurance Lifecycle**

The services form one business process, but each remains accountable for a different truth. The sequence below is the minimum mental model you should be able to explain in a mentor/interviewer discussion.

| \# | Business stage | Main services | Business meaning |
| :---- | :---- | :---- | :---- |
| 1 | Customer onboarding | Customer & Party | Policyholder establishes identity and business profile. |
| 2 | Quote request | Quote & Policy | Creates draft insurance request containing business context, insurance need/description and requested coverages. |
| 3 | Submission | Quote & Policy \+ Workflow | Customer submits a completed request; workflow starts triage/underwriting work. |
| 4 | Triage/assignment | Workflow \+ Risk & Underwriting | Completeness, appetite/referral signals and appropriate underwriter/risk-engineer task assignment are determined. |
| 5 | Risk assessment | Risk & Underwriting \+ Document | Risk evidence, findings, recommendations and subjectivities are gathered. |
| 6 | Underwriting decision | Risk & Underwriting | Authorized underwriter decides accept/decline/conditional terms and records rationale. |
| 7 | Quote/terms | Quote & Policy | Contractual terms are presented to the customer and remain separate from binding. |
| 8 | Acceptance/bind | Quote & Policy \+ Workflow | Customer accepts; required subjectivities/conditions are confirmed; authorized binding occurs. |
| 9 | Policy issue | Quote & Policy \+ Document | Policy contract and declarations/other policy documents are issued. |
| 10 | Policy service | Quote & Policy | Endorsements/MTA, cancellations, reinstatements, corrections and other servicing transactions are managed. |
| 11 | Claim/FNOL | Claims | Loss is reported and the claim is created. |
| 12 | Investigation/coverage | Claims \+ Quote & Policy \+ Document | Policy coverage, evidence, cause/loss facts and applicable limits/deductible are evaluated. |
| 13 | Assessment/reserve | Claims \+ Vendor \+ Analytics | Damage and financial exposure are assessed; reserves evolve with facts. |
| 14 | Settlement/payment | Claims | Authorized settlement is approved and paid. |
| 15 | Recovery | Recovery \+ Vendor \+ Workflow | Business impact and restoration plan are tracked; providers perform approved recovery work. |
| 16 | Subrogation/salvage | Claims | Recovery from responsible third parties or salvage is pursued where applicable. |
| 17 | Closure | Claims \+ Recovery \+ Document | Required claim/recovery conditions are satisfied and the record is closed with audit history. |
| 18 | Renewal | Quote & Policy \+ Risk \+ Analytics \+ Workflow | Current exposure, losses, risk controls and intelligence feed a new renewal transaction. |

# **12\. Underwriter Assignment: Exact Business Responsibility**

Because this was a major question raised during the project, IntelliSure should treat assignment as an explicit business operation rather than an accidental side effect of quote creation.

| Step | What happens | Primary responsibility |
| :---- | :---- | :---- |
| 1\. Submission arrives | Quote becomes ready for underwriting work. | Quote & Policy |
| 2\. Triage | Check completeness, product appetite/referral indicators and whether specialist review is required. | Risk & Underwriting, supported by Workflow |
| 3\. Find eligible staff | Identify active underwriters with the required authority/role and applicable business specialization. | Workflow using Customer & Party identity data and configured assignment rules |
| 4\. Balance work | Consider configured workload, SLA and case complexity. | Workflow |
| 5\. Assign task | Create an accountable underwriting task with due date and correlation to the quote. | Workflow |
| 6\. Notify | Notify the assigned underwriter. | Workflow & Notification |
| 7\. Accept/reassign/escalate | Underwriter accepts; manager can reassign or escalate. | Workflow \+ Claims/Risk management authority as applicable |
| 8\. Decision | Underwriter performs the actual underwriting decision. | Risk & Underwriting |
| 9\. Contract outcome | Quote/policy state moves based on the authorized underwriting result. | Quote & Policy |

Important boundary: Workflow chooses and tracks the work owner; Risk & Underwriting owns the underwriting judgment; Quote & Policy owns the resulting insurance transaction/contract state.

# **13\. Industry Alignment: Capabilities Deliberately Not Separate Microservices**

A real commercial insurer has additional business functions that are important to understand even when IntelliSure does not create a separate service for each one.

| Capability | Why it matters in industry | IntelliSure treatment |
| :---- | :---- | :---- |
| Billing / collections | Premium invoices, payments, refunds, non-payment cancellation, payment plans and reconciliation are material policy-lifecycle processes. | Not a separate service in the current 9-service MVP. Treat payment status as an explicit future business capability; do not pretend policy issuance includes billing. |
| Distribution / agency / broker | Many commercial placements are intermediated and require producer/agency, commission and submission workflows. | Out of scope for the direct digital MVP. Keep producer/broker terminology in the business glossary. |
| Product / rating administration | Insurers maintain coverage forms, rates, rules, territories, class codes and rating factors. | Rule/rating concepts are represented in Risk & Underwriting and Quote & Policy; separate product/rating service is out of scope. |
| Reinsurance | Insurers may cede risk and manage treaties/facultative placements. | Out of scope for the small-business capstone. |
| Regulatory/compliance | Licensing, filings, mandated notices, jurisdictional rules and record retention matter. | Document & Audit \+ domain rules cover basic traceability; regulatory automation is out of scope. |
| Actuarial/reserving | Portfolio-level reserving and actuarial analysis is broader than individual-claim reserve management. | Analytics can provide management intelligence; full actuarial platform is out of scope. |

# **14\. Current IntelliSure Business Objects Mapped to Services**

| Service | Current core objects | Business meaning |
| :---- | :---- | :---- |
| Customer & Party | UserAccount, BusinessCustomer | Identity \+ customer/party master |
| Quote & Policy | Quote, Policy, Endorsement, RenewalTransaction | Insurance transaction \+ policy contract/service lifecycle |
| Risk & Underwriting | RiskAssessment, RiskFinding, RiskRecommendation, RiskRule | Risk evidence, findings, prevention and underwriting logic |
| Claims | Claim, ClaimAssessment, PayoutCalculation, ClaimSettlement, BusinessImpact, ClaimTimelineEvent | Claim lifecycle \+ financial and operational impact |
| Vendor & Partner | Vendor, VendorOnboardingRequest, VendorAssignment, VendorPerformance | Provider master \+ work relationship |
| Recovery & Business Continuity | RecoveryCase, RecoveryPlan, RecoverySupportRequest | Operational restoration/continuity |
| Workflow & Notification | Workflow, WorkflowTask, Notification | Process/task ownership and SLA |
| Document & Audit | Document, AuditEvent | Evidence metadata \+ immutable accountability |
| Analytics & Intelligence | ClaimIntelligenceSnapshot, RiskScoreSnapshot, RenewalIntelligenceSnapshot | Decision-support snapshots |

# **15\. State Ownership Matrix**

| Business state | Authoritative service | Reason |
| :---- | :---- | :---- |
| Customer account status | Customer & Party | Account identity is a party-management truth. |
| Quote status | Quote & Policy | Quote is the insurance transaction truth. |
| Underwriting/risk assessment status | Risk & Underwriting | Risk evaluation is owned by underwriting/risk domain. |
| Policy status | Quote & Policy | Policy is the contract truth. |
| Endorsement status | Quote & Policy | Policy change transaction is part of policy servicing. |
| Claim status | Claims | Claim handling is a claims-domain truth. |
| Recovery status | Recovery & Business Continuity | Operational recovery is a separate domain truth. |
| Vendor status | Vendor & Partner | Provider eligibility/master state belongs to partner domain. |
| Task/workflow status | Workflow & Notification | Operational task state is not domain state. |
| Document status/metadata | Document & Audit | Evidence lifecycle belongs to evidence domain. |
| Audit event | Document & Audit | Audit history is immutable accountability data. |
| Analytical snapshot | Analytics & Intelligence | Model/score output belongs to analytics, while the business decision remains with the authorized domain service. |

# **16\. Business Terms You Must Not Mix Up**

| Term | Business meaning |
| :---- | :---- |
| Submission | A package/request presented for underwriting consideration. |
| Quote | Insurer-proposed price/terms for the risk; not necessarily binding coverage. |
| Bind | Authorized commitment to coverage under agreed terms. |
| Policy issuance | Creation/delivery of the formal policy contract/documents. |
| Underwriting | Evaluation and acceptance/pricing/terms decision for risk. |
| Risk Engineering | Specialist hazard/control/loss-prevention assessment that supports underwriting and prevention. |
| Subjectivity | A requirement that must be satisfied before a transaction can proceed, often before binding/issuance. |
| Referral | A case sent for human/higher-authority review because rules, appetite or authority require it. |
| FNOL | First Notice of Loss, the initial claim report. |
| Coverage determination | Claims-side decision about whether/how the loss is covered under the policy. |
| Reserve | Current estimate of expected claim cost; changes as information develops. |
| Settlement | Authorized agreement/decision on the claim amount or resolution. |
| Payment | Actual disbursement associated with the settlement/claim. |
| Salvage | Value recovered from damaged property/items where applicable. |
| Subrogation | Insurer’s recovery rights against a responsible third party when applicable. |
| Endorsement/MTA | Contractual mid-term policy change transaction. |
| Renewal | New policy-term transaction based on current risk/exposure/loss information. |
| Business Impact | Operational effect of a loss on the insured business; not itself a coverage decision. |
| Workflow task | Operational work assigned to a person/system; not the domain decision itself. |
| Decision support | Analytics/rule output used by an authorized human/domain decision-maker. |

# **17\. Source Basis and Current Industry Signals**

The business boundaries in this document were checked against the supplied IntelliSure artifacts and current public material available as of September 18, 2026\. Exact legal/regulatory processes vary by product, jurisdiction and insurer; this is an industry-aligned business model for the capstone, not a claim that every carrier implements identical steps.

* ACORD Property & Casualty Data Standards — business cases include New Business Quote, New Business Submission, Policy Change, Renewal and Reinstatement.  
* The Hartford — current small-business/BOP material describes customized quoting and states that actual costs, premium and coverage depend on information provided and underwriting/rating criteria.  
* The Hartford — current Risk Engineering material describes exposure evaluations, consultations and recommendations for improving business safety/security.  
* The Hartford — current Premium Audit material shows the importance of actual exposure reconciliation (payroll, sales, job duties, locations, subcontractors) in commercial policies.  
* Chubb — current commercial operating material separates new-business intake, quoting, bind confirmation, policy administration, renewal, mid-term activity and document issuance.  
* NAIC — current glossary and claims materials cover insurance terminology including underwriting, claim, reserve, salvage and subrogation; claims-handling materials identify investigation, cause of loss, liability, loss amount, reserves, payments and closure as claims activities.  
* Verisk — small-commercial underwriting material emphasizes business classification, exposure and property/risk data to support faster underwriting decisions.  
* NAIC 2026 technology materials show active industry use of AI/automation in commercial underwriting and claims workflows; IntelliSure therefore treats analytics/AI as decision support with explicit human/domain authority.

# **18\. Key External References**

| Source | URL |
| :---- | :---- |
| ACORD P\&C Data Standards | https://www-dev.acord.org/standards-architecture/acord-data-standards/Property\_Casualty\_Data\_Standards |
| NAIC Insurance Glossary | https://content.naic.org/es/node/11821 |
| NAIC Accelerated Underwriting | https://content.naic.org/insurance-topics/accelerated-underwriting |
| NAIC Claims / actuarial process definitions | https://content.naic.org/sites/default/files/inline-files/index\_posting\_pc\_actuarial\_definiton\_letter.pdf |
| The Hartford Business Owners Policy | https://www.thehartford.com/business-owners-policy |
| The Hartford Small Business Insurance | https://www.thehartford.com/business-insurance |
| The Hartford Risk Engineering example | https://www.thehartford.com/business-insurance/midsize-architect-engineer |
| The Hartford Premium Audit | https://www.thehartford.com/business-insurance/premium-audit |
| Chubb EMEA Commercial Insurance workflow | https://www.chubb.com/uk-en/campaign/emea-commercial-insurance.html |
| Chubb Small Business Insurance | https://www.chubb.com/us-en/agents-brokers/campaigns/why-chubb-small-business-insurance.html |
| Verisk Small Commercial Underwriting | https://integration.verisk.com/498325/siteassets/media/downloads/underwriting/small-commercial/the-secret-to-shrinking-small-commercial-underwriting-time-to-seconds.pdf |
| Triple-I Filing a Business Insurance Claim | https://www.iii.org/article/filing-a-business-insurance-claim |

# **19\. Final Business Contract for IntelliSure**

* Customer & Party owns who the party is.  
* Quote & Policy owns what insurance transaction/contract exists.  
* Risk & Underwriting owns the risk evaluation and underwriting rationale/decision.  
* Claims owns what happened, whether the loss is covered, and what claim amount is payable under policy terms.  
* Vendor & Partner owns who can perform external service work and how that work relationship is managed.  
* Recovery & Business Continuity owns operational restoration/continuity after a qualifying loss.  
* Workflow & Notification owns who must act, by when, and how process work escalates/notifies.  
* Document & Audit owns evidence metadata and immutable business accountability history.  
* Analytics & Intelligence owns analytical outputs and decision support, not the final consequential business decision.  
* No service may create a shadow master for another service’s business truth.  
* Every important business state change should have a valid reason, actor/system origin and traceable evidence/event.  
* This business definition comes before technical implementation. Technical APIs, entities and database tables should be adjusted to reflect these boundaries, not the other way around.