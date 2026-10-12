# **IntelliSure — Roles, Responsibilities & Complete Insurance Lifecycle**

**IntelliSure is a Small Business Insurance Lifecycle & Recovery Platform. Its purpose is to manage the insurance journey from customer onboarding and risk evaluation to policy issuance, claims, financial settlement, business recovery, and renewal.**

**The system must support both successful and unsuccessful outcomes: approved and declined applications, outstanding conditions, expired quotes, denied claims, failed vendor assignments, policy cancellation, and other exception paths—not just the happy path.**

**The following is the target business responsibility model for IntelliSure, aligned with the existing nine-microservice architecture and eight defined roles. It distinguishes business responsibilities from the functionality that still needs implementation or verification.**

# **1\. The eight roles**

## **1\. Policyholder / Small Business Owner**

**External customer · Owns the insurance relationship**

**Primary responsibility: Request insurance, manage policies, report losses, and coordinate business recovery.**

* **Register, authenticate, and maintain business/customer details.**  
* **Create, edit where allowed, submit, and track insurance quotes.**  
* **Provide business operations details, coverage requirements, supporting information, and requested effective dates.**  
* **Respond to information requests and outstanding underwriting conditions.**  
* **View appropriate risk and underwriting outcomes.**  
* **Accept or decline eligible quoted terms.**  
* **View binding, policy issuance, coverage, limits, deductibles, and policy dates.**  
* **Request supported policy servicing actions, such as endorsements, cancellation, reinstatement, and renewal.**  
* **File a First Notice of Loss (FNOL), provide incident details, and submit evidence.**  
* **Track claim status, decisions, settlements, and payments.**  
* **Choose a recovery path: `NETWORK_VENDOR`, `CUSTOMER_VENDOR`, or `CUSTOMER_MANAGED`.**  
* **Track recovery progress, view available documents, and manage notifications.**

**Must not: Make underwriting decisions, approve claims on behalf of the insurer, assign internal adjusters, manually bind or issue policies, or access another customer's records.**

## **2\. Underwriter**

**Insurance operations · Evaluates risk and determines insurability**

**Primary responsibility: Decide whether an applicant's risk can be insured and under what terms.**

* **Review submitted applications and supporting evidence.**  
* **Review risk assessments, risk scores, coverage requests, business exposure, and underwriting rules.**  
* **Evaluate eligibility, appetite, authority limits, and material risk concerns.**  
* **Request additional information, inspections, or risk-control actions.**  
* **Resolve referrals and determine appropriate underwriting outcomes.**  
* **Approve, approve with conditions, refer for further authority, or decline.**  
* **Specify offered limits, deductibles, exclusions, conditions, and subjectivities where supported.**  
* **Ensure that the final terms reflect the evaluated risk.**

**Must not: Accept policy terms on the customer's behalf, impersonate the Policyholder, or bypass required underwriting controls.**

## **3\. Risk Engineer**

**Risk prevention · Identifies and helps mitigate physical and operational risks**

**Primary responsibility: Reduce the likelihood and severity of future losses.**

* **Evaluate property conditions, fire protection, equipment, electrical risks, safety practices, and business continuity arrangements.**  
* **Conduct or review risk inspections where required.**  
* **Document observed hazards and supporting evidence.**  
* **Recommend practical loss-prevention and mitigation measures.**  
* **Record remediation requirements and follow-up findings.**  
* **Provide relevant findings to the underwriting process.**  
* **Support post-loss technical assessment when required by the business process.**

**Must not: Make final underwriting decisions unless separately authorized under an applicable role, or approve claim payments merely because an inspection was completed.**

## **4\. Claims Adjuster**

**Claims operations · Investigates individual losses**

**Primary responsibility: Establish what happened, determine the applicable coverage facts, and recommend or perform authorized claim actions.**

* **Review new FNOL submissions and validate incident information.**  
* **Investigate policy details and potentially applicable coverage.**  
* **Request evidence, photographs, invoices, estimates, and statements.**  
* **Coordinate inspections and assess the extent and cause of loss.**  
* **Record coverage investigation findings.**  
* **Estimate loss exposure and manage claim reserves within delegated authority.**  
* **Prepare claim decisions and settlement recommendations.**  
* **Process authorized settlement and payment steps.**  
* **Communicate status, required information, and decisions.**  
* **Identify potential subrogation or salvage opportunities.**  
* **Escalate complex, disputed, exceptional, or high-value claims to a Claims Manager.**

**Must not: Exceed delegated approval authority, alter policy terms, or treat every reported incident as automatically covered.**

## **5\. Claims Manager**

**Claims oversight · Controls decisions, exceptions, and escalations**

**Primary responsibility: Ensure claims are handled consistently, fairly, and within authority.**

* **Oversee claim assignments, workloads, and escalations.**  
* **Review complex coverage questions, disputed findings, and exceptional exposures.**  
* **Approve or review decisions and settlements when required by authority rules.** `[ADDED: Enforced $10,000 threshold requiring Claims Manager or Administrator authorization for settlements and reserves]`  
* **Review significant reserve changes and settlement recommendations.**  
* **Ensure claim handling follows applicable procedures.**  
* **Review denials or exceptions where required.**  
* **Monitor unresolved claims, delays, payment problems, and operational risks.**  
* **Coordinate claim-level recovery and subrogation oversight.**  
* **Review closure decisions when the claim meets required closure criteria.**

**Must not: Bypass mandatory evidence, approval, or authorization controls merely to accelerate closure.**

## **6\. Vendor Manager**

**Network operations · Manages approved service providers**

**Primary responsibility: Maintain a trustworthy network of contractors and coordinate network-based restoration.**

* **Review contractor onboarding requests.**  
* **Verify provider details and activate eligible vendors.**  
* **Maintain vendor capabilities, service categories, and availability information where supported.**  
* **Review vendor recommendations and suitability.**  
* **Coordinate explicit network-vendor assignments when requested.**  
* **Monitor assignment acceptance, dispatch, progress, rejection, cancellation, and completion.**  
* **Handle reassignment when a provider cannot complete the work.**  
* **Monitor operational issues and escalate unreliable or non-compliant vendors.**

**Must not: Force the customer to use a network vendor or create assignments automatically for customer-owned recovery paths.**

## **7\. Service Provider / Contractor**

**External service provider · Performs restoration or repair work**

`[CHANGED: System authority maps to ROLE_VENDOR in customer-party-service and vendor-partner-service; applicants register as VENDOR_APPLICANT and are promoted to VENDOR upon Vendor Manager verification approval]`

**Primary responsibility: Carry out authorized restoration or recovery work and report its actual progress.**

* **Submit a vendor onboarding request when not yet registered.**  
* **Provide required verification information.**  
* **View eligible assignments offered to the provider.**  
* **Accept or decline an assignment.**  
* **Coordinate site access and work arrangements.**  
* **Report actual dispatch, start, progress, delays, and completion.**  
* **Supply appropriate work evidence or documentation when supported.**  
* **Communicate blockers or additional requirements.**  
* **Cooperate with reassignment or cancellation procedures.**

**Must not: Claim work has been completed without evidence, access another vendor's assignments without authorization, or change insurance coverage or claim decisions.**

## **8\. System Administrator**

**Platform administration · Manages accounts, access, and system configuration**

**Primary responsibility: Maintain secure, reliable platform operation and appropriate user access.**

* **Create employee accounts and assign authorized roles.**  
* **Manage access revocation and account-status changes.**  
* **Administer platform-level configuration and operational settings.**  
* **Support identity, access-control, and system availability investigations.**  
* **Review relevant audit evidence for authorized administrative purposes.**  
* **Support incident investigation and operational recovery.**  
* **Ensure required access controls and configuration remain in place.**

**Employee roles cannot self-register; the administrator provisions their accounts. Contractor onboarding requires review and activation rather than automatic trust.**

**Must not: Make insurance underwriting or claims decisions merely because they have administrative access, or routinely expose business data without authorization.**

# **2\. Complete insurance lifecycle**

**A real insurance platform is not one straight sequence. It is a collection of connected lifecycles: customer onboarding, quoting, risk assessment, policy servicing, claims, restoration, and renewal.**

**1\. Customer onboarding & business profile**

**Registration · Identity · Business information**

**2\. Quote application**

**Product · Risk information · Requested coverages**

**3\. Risk assessment & underwriting**

**Evaluation · Additional evidence · Decision**

**Approved / Conditional**

**Quote offered, subjectivities may remain.**

**Referred / Declined**

**Further authority, remediation, or application stops.**

**4\. Quote response**

**Customer accepts · Declines · Quote expires**

**5\. Binding & policy issuance**

**Bound · Issued · In force**

**6\. In-force policy servicing**

**Endorsement · Cancellation · Reinstatement · Renewal**

**7\. Loss event & claim lifecycle**

**FNOL · Investigation · Coverage · Reserve · Decision · Settlement · Payment**

**8\. Business recovery & restoration**

**Network vendor · Customer's vendor · Customer-managed recovery**

**9\. Closure, audit & renewal**

**Close eligible work · Preserve evidence · Continue, renew, or end coverage**

**Supporting workflows—documents, notifications, audit, and escalation—operate across multiple stages. Claims and recovery can progress in parallel when business rules permit.**

# **3\. Lifecycle stage-by-stage responsibility mapping**

## **Stage 0 — Identity and business onboarding**

| Activity | Primary owner | Supporting roles |
| ----- | ----- | ----- |
| **Self-register as a Policyholder** | **Policyholder** | **Customer & Party Service** |
| **Authenticate and obtain a session** | **Policyholder** | **Customer & Party Service** |
| **Complete business profile** | **Policyholder** | **System Administrator only for authorized support** |
| **Create an employee account** | **System Administrator** | **Employee's respective operational manager** |
| **Request contractor onboarding** | **Service Provider / Contractor** | **Vendor Manager** |
| **Verify and activate a contractor** | **Vendor Manager / authorized administrator** | **Service Provider** |
| **Correct identity/access problems** | **System Administrator, within authorization** | **Customer & Party operations** |

**Exception branches: Duplicate registration; invalid credentials; inactive or blocked account; incomplete business profile; missing customer identity in the JWT; unverified contractor; rejected onboarding request.**

**The application must distinguish authentication from business-profile completion. A valid login alone does not mean that the customer's business record is ready for quote creation.**

## **Stage 1 — Quote application**

| Activity | Primary owner | Supporting roles |
| ----- | ----- | ----- |
| **Choose an insurance product** | **Policyholder** | **—** |
| **Enter insurance needs and business operations** | **Policyholder** | **Risk Engineer when inspections/findings are required** |
| **Specify requested limits and deductibles** | **Policyholder** | **Underwriter may later offer different terms** |
| **Validate the application** | **Quote & Policy Service** | **Policyholder corrects invalid or missing inputs** |
| **Save a draft quote** | **Quote & Policy Service** | **Policyholder** |
| **Submit a quote for assessment** | **Policyholder** | **Risk & Underwriting Service** |

**Possible branches:**

* **Draft saved successfully.**  
* **Validation fails: application remains unsubmitted until corrected.**  
* **Network or service failure: the interface reports failure and allows a safe retry.**  
* **Quote submission succeeds and assessment starts.**  
* **The quote cannot proceed because required profile or business information is missing.**  
* **The customer abandons or later withdraws an application where the system supports withdrawal.**  
* **Duplicate submissions are handled without accidentally creating multiple applications.**

**A draft quote is not an approved quote, and an approved risk is not an issued policy.**

## **Stage 2 — Risk assessment and underwriting**

| Activity | Primary owner | Supporting roles |
| ----- | ----- | ----- |
| **Start and score the risk assessment** | **Risk & Underwriting Service** | **Policyholder supplies accurate information** |
| **Interpret risk evidence** | **Underwriter** | **Risk Engineer** |
| **Inspect premises / assess controls** | **Risk Engineer** | **Policyholder** |
| **Apply underwriting appetite and authority** | **Underwriter** | **Risk & Underwriting Service** |
| **Record decision, terms, and conditions** | **Underwriter** | **Policyholder is informed of the customer-facing result** |

**The core underwriting outcomes are:**

| Decision | Meaning | Next business action |
| ----- | ----- | ----- |
| **`APPROVED`** | **Risk is acceptable under the evaluated terms** | **Quote may proceed to the quoted stage according to the configured flow** |
| **`APPROVED_WITH_CONDITIONS`** | **Risk is acceptable subject to specified conditions** | **Present conditions and track any required subjectivities** |
| **`REFERRED`** | **Risk needs additional authority or review** | **Keep the case pending and route to the appropriate underwriter/authority** |
| **`DECLINED`** | **Risk is not accepted** | **Notify the customer appropriately and prevent acceptance/binding of a declined quote** |

**Further branches include missing evidence, requested inspections, unresolved subjectivities, excessive exposure, out-of-appetite risk, and assessment errors.**

**Responsibility boundary: The Risk Engineer supplies technical findings. The Underwriter owns the underwriting decision. The Policyholder receives only information appropriate to their role; internal decision notes and restricted evidence are not automatically customer-visible.**

## **Stage 3 — Quoted terms and the customer's decision**

**The Policyholder must be able to review the actual offered terms rather than the terms originally requested.**

| Branch | Expected behavior |
| ----- | ----- |
| **Quote is ready and valid** | **Display offered price, offered coverage terms, limits, deductibles, conditions and expiry where available** |
| **Policyholder accepts** | **Accept only if the quote is in an eligible status and has not expired** |
| **Policyholder declines** | **Record the decline where supported; do not bind the policy** |
| **Quote expires** | **Reject acceptance of expired terms; require an authorized new or renewed quote process** |
| **Subjectivities are outstanding** | **Show outstanding customer actions where customer-visible data exists** |
| **Quote is referred** | **Keep acceptance unavailable until the required decision is reached** |
| **Quote is declined by underwriting** | **Display the appropriate outcome; block acceptance** |
| **Acceptance is attempted in an invalid state** | **Backend rejects the transition and the UI explains the failure** |

**The Underwriter sets risk terms. The Policyholder decides whether to accept the offered terms. Neither role should impersonate the other's action.**

## **Stage 4 — Binding and policy issuance**

**This stage creates the contractual policy record.**

| Activity | Primary owner | Supporting roles |
| ----- | ----- | ----- |
| **Accept quoted terms** | **Policyholder** | **Quote & Policy Service validates eligibility** |
| **Bind accepted quote** | **Quote & Policy business workflow** | **Relevant services validate terms and create the policy** |
| **Issue the policy** | **Quote & Policy business workflow** | **Authorized internal workflow, where required** |
| **Display the policy and documents** | **Policyholder views their records** | **Document & Audit Service** |

**Expected progression:**

**`QUOTED → ACCEPTED → BOUND → ISSUED / IN_FORCE`**

**The exact persisted policy states may include intermediate states such as `PENDING_ISSUANCE`.**

**Alternative branches:**

* **Binding is rejected because the quote has not been accepted.**  
* **Acceptance fails because terms expired or the quote is not in a valid state.**  
* **Issuance remains pending due to an operational problem.**  
* **The customer attempts to bind or issue manually: prohibited.**  
* **Policy creation or issuance fails: show the actual failure, do not fabricate an active policy.**  
* **Successful issuance: the customer can inspect the real policy, effective dates, coverage and associated documents.**

## **Stage 5 — In-force policy servicing**

**Insurance does not stop once the policy is issued.**

| Servicing event | Primary owner | Supporting roles | Result |
| ----- | ----- | ----- | ----- |
| **Change insured business details or coverage** | **Policyholder initiates** | **Authorized insurer operations** | **Request is evaluated and, if accepted, an endorsement is recorded** |
| **Add or amend coverage** | **Policyholder initiates** | **Underwriter / authorized operations** | **Terms may be adjusted under the applicable process** |
| **Cancel policy** | **Customer or authorized insurer actor, subject to applicable rules** | **Quote & Policy operations** | **Cancellation outcome and effective date recorded** |
| **Reinstate cancelled policy** | **Authorized policyholder request / insurer process** | **Relevant operations** | **Reinstatement succeeds or is rejected** |
| **Renew policy** | **Policyholder and underwriting workflow, according to system rules** | **Underwriter** | **New renewal evaluation and policy term** |
| **Non-renew policy** | **Authorized insurer workflow** | **Underwriter / operations** | **Policy ends at the applicable term boundary** |
| **Policy reaches its end date** | **System / policy lifecycle process** | **Policyholder is informed** | **Expiry or renewal processing** |
| **Policy is not yet in force** | **Policyholder can inspect its status** | **Quote & Policy Service** | **Coverage must not be represented as active** |

**Endorsement, cancellation, reinstatement, and renewal are distinct business events—not arbitrary status edits. A renewal can require its own risk evaluation and result in a new policy record.**

**Premium collection, billing failures, refunds, and payment plans should be treated as separate capabilities only if the relevant business processes and APIs exist. They should not be simulated by frontend placeholders.**

## **Stage 6 — Loss reporting and claim intake**

**A claim starts when an incident is reported, not when a payment is approved.**

| Activity | Primary owner | Supporting roles |
| ----- | ----- | ----- |
| **Report the incident / submit FNOL** | **Policyholder** | **Claims Service** |
| **Identify the relevant policy** | **Policyholder supplies/selects it** | **Claims Service validates identity and coverage context** |
| **Record event and loss details** | **Claims Service** | **Policyholder** |
| **Assign an adjuster** | **Claims workflow** | **Claims Service / appropriate operational service** |
| **Review new intake** | **Claims Adjuster** | **Claims Manager for exception cases** |

**The supported workflow includes statuses such as `FNOL_RECEIVED`, `OPEN`, `COVERAGE_REVIEW`, and `RESERVED`.**

**Exception branches:**

* **Missing or malformed incident details.**  
* **The selected policy cannot be found.**  
* **The claimant does not own the claim or is not authorized to access it.**  
* **The policy is not valid for the incident date.**  
* **Automatic adjuster assignment fails or no suitable adjuster is available.**  
* **FNOL is saved but remains unassigned for later operational handling.**  
* **Duplicate submissions or a retry after a timeout.**

**A reported claim does not automatically mean that the loss is covered.**

## **Stage 7 — Coverage investigation, assessment, and reserves**

| Activity | Primary owner | Supporting roles |
| ----- | ----- | ----- |
| **Investigate facts and evidence** | **Claims Adjuster** | **Policyholder, relevant experts** |
| **Determine applicable terms and exclusions** | **Claims Adjuster within delegated authority** | **Claims Manager for complex cases** |
| **Assess physical/financial loss** | **Claims Adjuster** | **Risk Engineer or specialist where appropriate** |
| **Establish and revise reserves** | **Claims Adjuster within authority** | **Claims Manager for higher authority** |
| **Decide coverage/claim outcome** | **Claims Adjuster or authorized decision-maker** | **Claims Manager where required** |

**Potential outcomes:**

* **Covered: the claim can progress through the authorized settlement process.**  
* **Partially covered: covered and uncovered elements are separated; payment reflects the supported covered amount.**  
* **Not covered: deny with the appropriate documented reason and customer communication.**  
* **Pending evidence: request more information and keep the claim unresolved.**  
* **Disputed or complex: escalate to Claims Manager or the required specialist.**  
* **Assessment or service failure: retain an accurate operational state and allow recovery of the workflow.**  
* **Reserve changes: update the actual reserve and audit trail rather than rewriting prior decisions without traceability.**

**Denial appeals, litigation, and formal dispute-resolution workflows are valid insurance-domain branches, but they should be implemented only through explicitly defined business rules and endpoints—not invented as if the current UI already supports them.**

## **Stage 8 — Claim decision, settlement, and payment**

**The claim decision, agreed settlement, and actual payment are different milestones.**

| Activity | Primary owner | Supporting roles |
| ----- | ----- | ----- |
| **Approve or deny within delegated authority** | **Claims Adjuster** | **Claims Manager where required** |
| **Review exceptional settlement or authority override** | **Claims Manager** | **Claims Adjuster** |
| **Record settlement terms** | **Claims workflow** | **Policyholder receives the appropriate information** |
| **Record actual payment outcome** | **Claims/payment workflow** | **Claims Adjuster / Claims Manager for exceptions** |
| **Close the claim when eligible** | **Authorized claims actor** | **Claims Manager if required** |

**Branches include:**

* **`APPROVED`: authorized claim approval.**  
* **`DENIED`: no approved indemnity under the recorded decision.**  
* **Settlement is pending agreement or additional evidence.**  
* **Settlement is approved but payment is not yet completed.**  
* **Payment succeeds: reflect the actual payment state.**  
* **Payment fails or is rejected: record the true failure and permit authorized retry or correction.**  
* **Partial payment: show the actual amount and remaining status.**  
* **Subrogation or salvage applies: track the applicable recovery activity.**  
* **Claim is eligible for closure versus still having unresolved payment, evidence, or recovery obligations.**

**The platform's claim statuses include `APPROVED`, `DENIED`, `SETTLED`, `PAID`, and `CLOSED`. The UI must not collapse these into one generic “Completed” status.**

## **Stage 9 — Recovery and business continuity**

**Recovery is related to a claim but is not the same as claim payment. A business may need to restore operations while coverage or payment is still being resolved.**

**The supported recovery paths are:**

| Recovery path | What happens | Vendor assignment rule |
| ----- | ----- | ----- |
| **`NETWORK_VENDOR`** | **Customer opts to use an insurer-network provider** | **Assignment only when explicitly requested with a vendor selected** |
| **`CUSTOMER_VENDOR`** | **Customer uses their own contractor** | **No IntelliSure network `VendorAssignment`** |
| **`CUSTOMER_MANAGED`** | **Customer manages restoration independently** | **No IntelliSure network `VendorAssignment`** |

**Responsibilities:**

* **The Policyholder selects the recovery approach and supplies progress information where required.**  
* **The Vendor Manager coordinates the network provider when the customer chooses that option.**  
* **The Service Provider / Contractor accepts, declines, or executes an eligible assignment.**  
* **The Claims Adjuster handles relevant claim evidence, loss assessment, and recovery-related claim matters.**  
* **The Claims Manager oversees escalated claim/recovery disputes.**  
* **The Recovery Service records the recovery lifecycle independently of the vendor's assignment lifecycle.**

**Recovery lifecycle progression can include:**

**`INITIATED → PLANNING → BUSINESS_RESTORED → COMPLETED`**

**Vendor assignments can have their own states, including `REQUESTED`, `ASSIGNED`, `DISPATCHED`, `OFFERED`, `ACCEPTED`, `IN_PROGRESS`, `COMPLETED`, `DECLINED`, `CANCELLED`, and `REASSIGNED`.**

**Branches that must be handled correctly:**

* **Customer selects `CUSTOMER_VENDOR` or `CUSTOMER_MANAGED`: recovery proceeds without a network assignment.**  
* **Customer selects `NETWORK_VENDOR` but no vendor is selected yet: do not assign one automatically.**  
* **Selected provider is inactive or unverified: reject or prevent assignment.**  
* **Provider declines: customer/authorized operations may need to choose another provider or path.**  
* **Assignment is cancelled or provider becomes unavailable: handle reassignment explicitly.**  
* **Provider work is delayed or incomplete: recovery stays open and progress remains truthful.**  
* **Recovery completes: record actual completion and preserve associated evidence.**  
* **Claim is denied or payment is delayed: do not automatically falsify or terminate the recovery status; apply the actual recovery business rules.**

## **Stage 10 — Documents, notifications, audit, and closure**

**These functions span the entire lifecycle rather than belonging only to one step.**

| Concern | Primary owner | Responsibilities |
| ----- | ----- | ----- |
| **Documents** | **Relevant business actor creates/submits; Document Service stores metadata** | **Attach evidence to the correct quote, policy, or claim; authorize access** |
| **Notifications** | **Workflow & Notification Service** | **Notify users of actual events, requests, decisions and operational changes** |
| **Audit trail** | **Document & Audit Service** | **Preserve relevant business events and changes, according to access and retention rules** |
| **Final claim closure** | **Authorized claims actor** | **Confirm the workflow has reached an eligible closure state** |
| **Policy term closure / renewal** | **Quote & Policy workflow** | **Preserve history and process the next term where applicable** |

**Notification branches include successful delivery, duplicate suppression, unread status, read status, unavailable notification records, and temporary service errors.**

**Document access must follow the ownership and role rules. Being able to see a claim does not automatically grant access to every internal audit event or another customer's documents.**

# **4\. Role-by-lifecycle responsibility matrix**

**Legend: P \= primary responsibility, S \= supporting responsibility, — \= normally not responsible.**

| Lifecycle stage | Policyholder | Underwriter | Risk Engineer | Claims Adjuster | Claims Manager | Vendor Manager | Contractor | System Admin |
| ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| **Identity & profile** | **P** | **—** | **—** | **—** | **—** | **—** | **S** | **S** |
| **Quote creation** | **P** | **S** | **S** | **—** | **—** | **—** | **—** | **—** |
| **Risk evidence & inspection** | **S** | **P** | **P** | **—** | **—** | **—** | **—** | **—** |
| **Underwriting decision** | **S** | **P** | **S** | **—** | **—** | **—** | **—** | **—** |
| **Quote acceptance** | **P** | **—** | **—** | **—** | **—** | **—** | **—** | **—** |
| **Binding & issuance** | **S** | **S** | **—** | **—** | **—** | **—** | **—** | **—** |
| **Policy servicing & renewal** | **P** | **S** | **S** | **—** | **—** | **—** | **—** | **—** |
| **FNOL & claim intake** | **P** | **—** | **S** | **P** | **S** | **—** | **—** | **—** |
| **Coverage investigation** | **S** | **—** | **S** | **P** | **S** | **—** | **—** | **—** |
| **Claim decision & settlement** | **S** | **—** | **S** | **P** | **P\*** | **—** | **—** | **—** |
| **Restoration/recovery** | **P** | **—** | **S** | **S** | **S** | **S** | **P\*** | **—** |
| **Vendor onboarding & assignment** | **S** | **—** | **—** | **S** | **S** | **P** | **P\*** | **S** |
| **Documents & notifications** | **S** | **S** | **S** | **S** | **S** | **S** | **S** | **S** |
| **Audit & access governance** | **—** | **S** | **—** | **S** | **S** | **S** | **—** | **P** |

**`P*` means authority is conditional: the Claims Manager is the primary owner for escalated or higher-authority claim decisions; the Service Provider / Contractor is the primary performer of their assigned restoration work. Vendor assignment responsibility depends on whether the customer has chosen a network vendor.**

# **5\. How the architecture should enforce these responsibilities**

**These roles should map to actual authorization, not just conditional buttons in Angular.**

| Layer | Responsibility |
| ----- | ----- |
| **Angular shell / MFE** | **Display role-appropriate actions and information; enforce frontend navigation and form usability** |
| **API Gateway** | **Validate JWT and provide coarse authorization at the application boundary** |
| **Business microservices** | **Validate the propagated JWT and enforce fine-grained role, ownership, and state-transition rules** |
| **Customer & Party Service** | **Authentication, identity, customer information, and account/role management** |
| **Quote & Policy Service** | **Quote lifecycle, commercial terms, acceptance, binding, policy issuance, and servicing** |
| **Risk & Underwriting Service** | **Risk assessment, underwriting rules, referrals, decisions, and subjectivities** |
| **Claims Service** | **FNOL, investigation, reserves, decisions, settlement, payment, and claim closure workflow** |
| **Vendor & Partner Service** | **Provider onboarding, verification, recommendations, assignments, and vendor lifecycle** |
| **Recovery & Business Continuity Service** | **Recovery case, recovery path, and progress lifecycle** |
| **Workflow & Notification Service** | **Workflow tasks, escalation, and notifications** |
| **Document & Audit Service** | **Document metadata/visibility and authorized audit-event access** |
| **Analytics & Intelligence Service** | **Aggregated insight from authorized and actual operational data** |

**Security rule: the frontend must not be the final enforcement point. Every protected action must still be authorized server-side. In particular, customer ownership must be verified by the backend rather than trusted solely because the frontend supplied a `customerId`.**

# **6\. The most important non-happy-path rules**

**These are the rules I would insist on before declaring the project complete:**

1. **An application can fail or stop. Invalid data, incomplete profiles, referrals, outstanding evidence, underwriting decline, quote expiry, and failed service calls must have real states.**  
2. **A policy is not automatically active. Acceptance, binding, and issuance must be separate, valid transitions.**  
3. **A claim is not automatically covered. Coverage investigation, missing evidence, denials, partial coverage, reserves, settlement, actual payment, and closure are separate concerns.**  
4. **Recovery is not mandatory network-vendor usage. The customer's selected path must be respected; customer-managed recovery must not create vendor assignments.**  
5. **An employee role does not grant unlimited authority. An underwriter cannot impersonate a customer, and a Claims Adjuster cannot bypass required Claims Manager approval.**  
6. **Failure is not success. A failed payment, failed vendor assignment, inaccessible document, or unavailable downstream service must never be represented as completed.**  
7. **All meaningful transitions need traceability. Decisions, approvals, cancellations, claim outcomes, and relevant changes must leave the appropriate audit evidence.**  
8. **No customer can cross ownership boundaries. A Policyholder may view only their own authorized quotes, policies, claims, recovery records, documents, and notifications.**  
9. **Notifications must reflect actual events. Sending a notification must not fabricate a decision or cause a state transition.**  
10. **The business lifecycle needs explicit terminal states. Declined, withdrawn, expired, denied, cancelled, completed, and closed must not all become one generic status.**

# **7\. What is a must-have versus a nice-to-have?**

## **Must-have for IntelliSure**

* **Correct roles and access boundaries.**  
* **Real onboarding, quote, underwriting, policy, claim, recovery, document, and notification integrations.**  
* **Valid business state transitions and rejection of invalid transitions.**  
* **Handling of incomplete, declined, expired, denied, delayed, and failed workflows.**  
* **Server-side authorization and ownership enforcement.**  
* **Real audit evidence and truthful status display.**  
* **No fake business data.**  
* **End-to-end verification across the gateway and business services.**

## **Nice-to-have after core workflows are reliable**

* **SLA timers and escalations based on configurable rules.**  
* **Advanced underwriting explanations and risk visualizations.**  
* **Sophisticated analytics and loss-trend dashboards.**  
* **Vendor performance scoring.**  
* **Predictive restoration duration.**  
* **Automated renewal reminders and richer renewal intelligence.**  
* **Enhanced search and bulk operational views.**  
* **Additional appeal/dispute workflows, billing and premium collection capabilities, where the business scope supports them.**

**The nice-to-haves should never replace correctness in core insurance workflows.**

**Final principle: IntelliSure should model the reality of an insurance company—not simply produce a successful quote and claim demo. Every role needs clear authority, every important lifecycle transition needs a valid owner, and every alternative outcome must leave the system in an honest, auditable state.**

**You're right. Risk Engineering is a real lifecycle activity, and my flow chart underrepresented it.**

**The distinction in Selection 2 is correct:**

> **Risk Engineer supplies technical findings → Underwriter owns the underwriting decision.**

**But the lifecycle should explicitly show where Risk Engineering enters, why it is triggered, what happens if the risk engineer finds problems, and how those findings return to underwriting.**

### **Corrected lifecycle**

                   **┌─────────────────────┐**  
                    **│ Customer Onboarding │**  
                    **│ Business Profile    │**  
                    **└──────────┬──────────┘**  
                               **│**  
                               **▼**  
                    **┌─────────────────────┐**  
                    **│   Quote Request     │**  
                    **│ Coverage \+ Risk Info │**  
                    **└──────────┬──────────┘**  
                               **│**  
                               **▼**  
                    **┌─────────────────────┐**  
                    **│ Automated / Initial │**  
                    **│   Risk Assessment   │**  
                    **└──────────┬──────────┘**  
                               **│**  
                    **┌──────────┴──────────┐**  
                    **│                     │**  
              **Risk acceptable       Risk requires**  
              **without inspection      deeper review**  
                    **│                     │**  
                    **│                     ▼**  
                    **│          ┌──────────────────────┐**  
                    **│          │   RISK ENGINEERING   │**  
                    **│          │                      │**  
                    **│          │ • Inspect premises   │**  
                    **│          │ • Assess hazards     │**  
                    **│          │ • Review controls    │**  
                    **│          │ • Identify exposures │**  
                    **│          │ • Recommend mitig.   │**  
                    **│          └──────────┬───────────┘**  
                    **│                     │**  
                    **│                     ▼**  
                    **│          ┌──────────────────────┐**  
                    **│          │ Technical Findings   │**  
                    **│          │ & Recommendations    │**  
                    **│          └──────────┬───────────┘**  
                    **│                     │**  
                    **└──────────┬──────────┘**  
                               **▼**  
                    **┌─────────────────────┐**  
                    **│     UNDERWRITING    │**  
                    **│                     │**  
                    **│ Underwriter reviews │**  
                    **│ risk \+ engineering  │**  
                    **│ findings \+ rules    │**  
                    **└──────────┬──────────┘**  
                               **│**  
                **┌──────────────┼─────────────────┐**  
                **│              │                 │**  
                **▼              ▼                 ▼**  
           **APPROVED       CONDITIONS          REFERRED**  
                **│              │                 │**  
                **│              ▼                 ▼**  
                **│       ┌──────────────┐   Additional**  
                **│       │ Subjectivity │   authority /**  
                **│       │ / remediation│   review**  
                **│       └──────┬───────┘**  
                **│              │**  
                **│              ▼**  
                **│       Risk Engineer /**  
                **│       Customer may**  
                **│       address findings**  
                **│              │**  
                **│              └───────┐**  
                **│                      │**  
                **└──────────────────────┘**  
                               **│**  
                               **▼**  
                         **┌───────────┐**  
                         **│  QUOTED   │**  
                         **└─────┬─────┘**  
                               **│**  
                     **┌─────────┴─────────┐**  
                     **│                   │**  
                  **ACCEPT              EXPIRE/**  
                     **│                DECLINE**  
                     **▼**  
                 **┌─────────┐**  
                 **│  BOUND  │**  
                 **└────┬────┘**  
                      **│**  
                      **▼**  
                 **┌─────────┐**  
                 **│ ISSUED  │**  
                 **│ IN FORCE│**  
                 **└────┬────┘**  
                      **│**  
                      **▼**  
              **┌───────────────┐**  
              **│ POLICY SERVICE│**  
              **└───────┬───────┘**  
                      **│**  
              **┌───────┴────────┐**  
              **│                │**  
           **No loss          LOSS EVENT**  
              **│                │**  
              **│                ▼**  
              **│          ┌────────────┐**  
              **│          │    FNOL    │**  
              **│          └─────┬──────┘**  
              **│                │**  
              **│                ▼**  
              **│          ┌────────────┐**  
              **│          │   CLAIMS   │**  
              **│          │ INVESTIG.  │**  
              **│          └─────┬──────┘**  
              **│                │**  
              **│        ┌───────┴────────┐**  
              **│        │                │**  
              **│      COVERED         DENIED**  
              **│        │**  
              **│        ▼**  
              **│   ASSESSMENT**  
              **│        │**  
              **│        ▼**  
              **│     RESERVE**  
              **│        │**  
              **│        ▼**  
              **│     DECISION**  
              **│        │**  
              **│        ▼**  
              **│    SETTLEMENT**  
              **│        │**  
              **│        ▼**  
              **│     PAYMENT**  
              **│**  
              **└─────────────────────┐**  
                                    **│**  
                                    **▼**  
                         **┌────────────────────┐**  
                         **│ BUSINESS RECOVERY  │**  
                         **└─────────┬──────────┘**  
                                   **│**  
                    **┌──────────────┼──────────────┐**  
                    **│              │              │**  
                    **▼              ▼              ▼**  
              **NETWORK VENDOR  CUSTOMER VENDOR  CUSTOMER**  
                                               **MANAGED**  
                    **│              │              │**  
                    **└──────────────┼──────────────┘**  
                                   **▼**  
                         **BUSINESS RESTORED**  
                                   **│**  
                                   **▼**  
                         **RECOVERY COMPLETED**  
                                   **│**  
                                   **▼**  
                    **┌────────────────────────┐**  
                    **│ RENEWAL / END OF TERM  │**  
                    **└────────────────────────┘**

### **And Risk Engineering isn't necessarily a one-time activity**

**This is the important part.**

**Risk Engineering can appear at multiple points:**

**Before underwriting decision**

**Quote**  
 **↓**  
**Risk Assessment**  
 **↓**  
**Risk Engineering**  
 **↓**  
**Technical Findings**  
 **↓**  
**Underwriter Decision**

**When underwriting imposes conditions**

**Underwriter**  
 **↓**  
**"Install fire protection"**  
 **↓**  
**Risk Engineer**  
 **↓**  
**Verify / assess remediation**  
 **↓**  
**Findings**  
 **↓**  
**Underwriter**  
 **↓**  
**Final decision**

**During policy servicing**

**A material change to the insured business/property can trigger another risk assessment or inspection.**

**Policy Change**  
 **↓**  
**Risk Review**  
 **↓**  
**Risk Engineering (if required)**  
 **↓**  
**Findings**  
 **↓**  
**Underwriter**  
 **↓**  
**Endorse / Modify / Decline request**

**After a loss**

**Depending on the nature of the claim, technical expertise can also contribute to understanding physical damage, hazards, or loss-prevention recommendations.**

**Loss**  
 **↓**  
**Claim Investigation**  
 **↓**  
**Technical assessment (if required)**  
 **↓**  
**Findings**  
 **↓**  
**Claims decision / recovery / risk improvement**

**The authority boundaries remain different:**

| Role | Owns |
| ----- | ----- |
| **Risk Engineer** | **Technical risk findings, inspections, hazards, recommendations, remediation verification** |
| **Underwriter** | **Insurability, underwriting terms, conditions, referral, approval/decline** |
| **Claims Adjuster** | **Claim investigation, coverage assessment, loss evaluation, settlement recommendation** |
| **Claims Manager** | **Higher-authority/complex claim decisions and oversight** |

**So yes: Risk Engineering should be visibly embedded in the insurance lifecycle, particularly between risk assessment and underwriting, with a feedback loop when conditions/remediation are required.**

**And for IntelliSure specifically, I would make this a first-class part of the role/lifecycle design, rather than treating Risk Engineer as a supporting footnote.**

