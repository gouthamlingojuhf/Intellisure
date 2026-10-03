**INTELLISURE**

**Insurance Lifecycle Business Blueprint**

Industry-aligned business logic, terminology, workflows, decisions, data requirements, and design changes

Research cut: September 18, 2026  |  Technical implementation intentionally excluded

&nbsp;

| Area | Locked scope for IntelliSure |
| :---- | :---- |
| Business focus | Small-business commercial Property & Casualty insurance lifecycle |
| Core lifecycle | Prospect/need \-\> application/submission \-\> intake \-\> underwriting \-\> risk engineering \-\> quote \-\> acceptance \-\> bind \-\> issue \-\> policy service \-\> loss prevention \-\> FNOL/claim \-\> investigation \-\> assessment \-\> settlement \-\> recovery/business continuity \-\> closure \-\> renewal |
| Primary users | Policyholder, Underwriter, Risk Engineer, Claims Adjuster, Claims Manager, Vendor Manager, Service Provider/Contractor, System Administrator |
| Core principle | Human business authority remains explicit. Analytics/AI is decision support, not the final underwriting or claim settlement authority. |

&nbsp;

*Project source review: IntelliSure Entities, DTO, API Endpoint, Service-to-Service Communication, User Journey Mapping, and DB Design workbooks/CSVs supplied in this conversation. External references are listed throughout.*

# **1\. Executive Summary**

IntelliSure should behave like a digital small-commercial insurance operating platform, not a CRUD application. The central business object is a risk that moves through a controlled lifecycle: a business describes itself and its insurance need; the insurer evaluates whether the risk fits its appetite, what controls are needed, what coverages/limits/deductibles are appropriate, what premium/terms should be offered, and whether/when coverage is bound and issued. Once in force, the policy is serviced through endorsements, cancellations and renewals. If a loss occurs, claims determines whether and how the loss is covered, estimates and reserves the loss, investigates, settles, and—where applicable—coordinates repair and business recovery.

The most important correction to our existing design is that Quote \-\> Approved \-\> Issue is too compressed. Industry workflows distinguish a submission/application, underwriting review, quote/terms, customer acceptance, binding, and policy issuance. Some digital small-commercial products can move through these steps automatically, but the business states are still conceptually distinct. ACORD's P\&C standards explicitly separate New Business Quote, New Business Submission, Policy Change, Renewal and Reinstatement, while current commercial operating models describe new-business intake, quoting, confirm coverage/bind, policy administration and renewals as distinct work areas.

# **2\. The Insurance Business in One Sentence**

Insurance is a contract under which the insurer accepts specified risk in return for premium, subject to defined coverage, limits, deductibles/retentions, conditions and exclusions; underwriting decides what risk and terms the insurer will accept, while claims determines whether a reported loss falls within the contract and what amount is payable under its terms.

*NAIC defines a policy as a written contract and underwriting as the process of examining risk, deciding whether to accept it, classifying accepted risk, and determining an appropriate rate.*

# **3\. Terminology You Must Be Comfortable With**

Core commercial-insurance vocabulary:

Industry-state nuance: exact binding, disclosure, cancellation, claim handling, producer licensing, filing, and documentation rules vary by product, insurer, jurisdiction and policy wording. This blueprint defines a realistic insurance operating model for the project; it is not legal or regulatory advice and should not be presented as one mandatory process used by every carrier.

Industry-channel note: commercial insurance is not one universal workflow. A direct digital small-business journey can be highly automated (quote → accept → bind → issue), while broker/agent or larger commercial placements can contain richer submission, referral, negotiation, underwriting authority and documentation steps. IntelliSure uses a direct digital policyholder channel for the MVP, but its terminology and internal workflow intentionally preserve these industry concepts.

Applicant / proposed insured — the party asking an insurer for coverage. Once coverage is issued, the applicant may become the policyholder/named insured, depending on the contract.

Policyholder / named insured — the person or organization named in the policy and entitled to the policy rights described by the contract. In IntelliSure, the small-business owner/business is the primary customer actor.

Insured — a person or organization protected by the policy. “Named insured” is the specifically named insured; other insured parties can be added by policy terms or endorsements.

Producer / agent / broker — distribution-side professionals who sell, solicit, negotiate, or place insurance. Our capstone is designed as a direct digital small-business channel, so a producer is not a required actor in the MVP, but the terminology matters for industry conversations.

Submission — the package of information presented for underwriting consideration. It is broader than a price quote and can contain the application, business facts, loss history, documents, requested coverages, and narrative.

Application — information supplied by the applicant to request insurance. Depending on product/channel, this can be a structured form, supplemental questionnaires, signed application, or digital submission data.

Quote — an insurer's proposed price and terms for a requested risk, subject to stated assumptions, conditions, underwriting rules and policy wording. A quote is not the same thing as binding coverage.

Indication — an early, non-final indication of possible pricing/terms before the insurer has completed all underwriting requirements. An indication should not be described as active insurance.

Subjectivity — a requirement that must be satisfied before coverage can be bound or issued, such as a document, inspection, loss-control action, signed form, or clarification.

Referral — a case sent to a human underwriter or a higher authority because it falls outside automated rules, appetite, authority, or required data thresholds.

Underwriting — the process of evaluating risk, deciding whether the insurer will accept it, determining terms/conditions, and helping determine price.

Underwriter — the decision-making professional responsible for applying underwriting guidelines, risk appetite, authority, and judgment to the submission.

Risk engineering / loss control — specialist evaluation and risk-improvement work focused on identifying hazards, controls, prevention measures and business continuity needs. It supports underwriting and prevention; it does not replace the underwriter's contractual decision.

Rating — applying an insurer's rating methodology to exposures and risk characteristics to determine premium. “Premium \= fixed percentage of coverage limit” is only a demo simplification, not industry-standard logic.

Quote acceptance — the customer agrees to proceed with the quoted terms. Acceptance alone does not necessarily mean the insurer has bound coverage.

Bind / binding — the insurer or an authorized party commits coverage according to the agreed terms. Binding authority determines who is authorized to bind and under what conditions.

Binder — evidence/documentation of bound coverage issued before the full policy contract is delivered, where the applicable process uses binders.

Policy issuance — generating/delivering the formal policy contract and policy documents after binding and required conditions are satisfied.

Declarations / information page — the summary page(s) identifying the insured, policy number, policy period, key coverages, limits, deductibles, premium and related policy information.

Policy period — the dates during which the policy is in force, subject to its terms, cancellation, expiration and other conditions.

Endorsement / rider — a contractual change that adds, removes, excludes, or modifies coverage or policy terms. It can be requested at purchase, during the term, or at renewal.

MTA / mid-term adjustment — a change to a policy during the policy term, often implemented through an endorsement and potentially changing premium.

Renewal — a new policy term offered after the current term, generally requiring updated information and review rather than blindly extending the old risk.

Exposure — the measurable basis on which risk or premium may be evaluated, such as payroll, sales, employees, area, vehicles, property values, occupancy or units.

Hazard — a condition that increases the chance or severity of loss, such as combustible materials, poor housekeeping, unsafe operations, or deficient controls.

Loss history / loss runs — records of prior claims/losses used to understand frequency, severity, causes and trends.

FNOL (First Notice of Loss) — the initial report that a loss event or potential claim has occurred.

Claim — a demand/request for payment or benefits under an insurance contract arising from a covered event or alleged covered event.

Coverage determination — the claims-side decision about whether and to what extent the reported loss is covered under the policy.

Adjuster / claims adjuster — the professional who investigates, evaluates and handles the claim in accordance with the policy, applicable law and claims procedures.

Proof of loss — information/documentation supporting the claimed amount and circumstances of loss, where required by the policy/process.

Reserve — the amount set aside/estimated by the insurer for expected claim costs. Reserves can be revised as facts develop; incurred amounts and accounting treatment are distinct from the initial estimate.

IBNR (incurred but not reported) — losses that have occurred but have not yet been reported to the insurer; important in insurer reserving but usually not a policyholder-facing transaction.

LAE (loss adjustment expense) — expenses associated with investigating, adjusting and resolving claims, tracked separately in insurer accounting.

Indemnity — payment/benefit intended to compensate for a covered loss subject to policy terms, limits, deductibles and exclusions.

Deductible — the portion of covered loss the insured generally retains before the insurer pays, subject to the policy's wording.

Limit of insurance — the maximum amount the insurer will pay for a specified coverage, subject to policy terms and aggregates.

Aggregate limit — a maximum total payable for multiple claims or losses during the specified policy period for a coverage, where applicable.

Occurrence — an event or series of events that triggers coverage under an occurrence-based coverage form, subject to its terms.

Claims-made — a coverage trigger commonly based on when a claim is made/reported, with policy-specific requirements. Use only for products where applicable; not every commercial policy is claims-made.

Exclusion — policy wording that removes or limits coverage for specified causes, property, situations or circumstances.

Condition — a contractual requirement that must be met by the insured or insurer, such as notice, cooperation, loss-control requirements, or other stated duties.

Business income / business interruption — coverage intended to address qualifying loss of income and/or continuing expenses after a covered disruption, according to the specific policy wording.

Extra expense — qualifying additional expenses incurred to continue or restore operations after a covered loss, according to policy terms.

Waiting period / time deductible — a period that must elapse before certain business-income or similar benefits become payable, when the policy contains one.

Restoration period — the period over which qualifying business-income or extra-expense loss may be covered, subject to policy terms and limits.

Reservation of rights — a claims communication in which the insurer states that it may investigate/handle a claim while reserving the right to later rely on specified coverage defenses under the policy/law. It is not itself a final denial.

Denial — a decision that the insurer will not pay a claim or portion of a claim because of coverage, causation, policy condition, documentation, fraud, limits, exclusions, or another applicable reason.

Partial denial / partial acceptance — only some claimed items or amounts are payable; other parts are not.

Settlement — resolution of the claim amount/obligation under the applicable process. Settlement and payment are related but not identical.

Payment transaction — the actual disbursement of approved claim funds, potentially in one or multiple payments.

Salvage — recovery/resale or value obtained from damaged property where applicable.

Subrogation — the insurer's pursuit of recovery from a responsible third party after paying a covered loss, to the extent permitted.

Certificate of Insurance (COI) — evidence summarizing certain insurance information for a requesting third party. A certificate generally does not itself change the policy's coverage.

Additional insured — a party granted insured status under a policy or endorsement; the exact scope depends on the wording.

Loss payee / mortgagee — parties whose financial interest in insured property is recognized under the policy/endorsement, subject to the applicable wording.

Premium audit — post-policy-term reconciliation in products where premium depends on estimated exposures; actual exposures can produce additional premium, return premium, or other adjustment.

Appeal / reconsideration — a request to review a decision, where the insurer/process or applicable law provides such a path.

Audit trail — chronological evidence of material actions, decisions, status changes, approvals, document events and communications.

| Term | Plain-English meaning |
| :---- | :---- |
| Applicant / Proposed Insured | The person or business seeking insurance before a policy is issued. |
| Insured | The party whose risk is covered by the insurance contract. |
| Policyholder | The party that owns/holds the policy and is typically responsible for premium and policy obligations. |
| Producer / Agent / Broker | Distribution-side insurance professional. In the U.S., a producer may be an agent or broker and must be licensed under state rules. A producer solicits, sells or negotiates insurance. |
| Submission | The package of risk information presented to an insurer for underwriting consideration. In commercial insurance this is usually richer than a single quote request. |
| Application | The information and representations supplied to seek insurance. The application may be collected directly, by an agent/broker, or through a digital workflow. |
| Underwriting | Risk selection, classification and pricing/terms determination. |
| Risk appetite | The categories and characteristics of risk a carrier is willing to write, usually with boundaries and authority rules. Current 2026 market commentary emphasizes appetite aligned to risk quality, data and risk controls. |
| Referral | A case that cannot be handled within automated or delegated rules/authority and must be reviewed by an underwriter or higher authority. |
| Risk engineer / loss control | A specialist who evaluates physical/operational hazards and recommends measures to reduce loss frequency/severity. |
| Quote | An offer/proposal of insurance terms and price based on the risk information available at that point. Premium and coverage remain subject to policy terms and underwriting criteria. |
| Subjectivity / subjectivities | Information, conditions or requirements that must be satisfied before a quote can be bound or a policy finalized. |
| Bind / binding | The point at which coverage is contractually bound according to the insurer's authority/process. NAIC licensing material treats binding coverage as a distinct insurance activity. |
| Binder | Evidence/temporary documentation of bound insurance pending policy issuance in workflows that use a binder. |
| Issue / policy issuance | Creation/delivery of the formal policy contract and associated documents after the required bind/issue conditions are satisfied. Industry systems commonly separate binding and issue. |
| Declaration / information page | A summary page with key policy facts such as policy number, effective/expiration dates, insured, coverages, limits, premium and endorsements. |
| Endorsement / rider | An amendment to an existing policy that adds, removes, excludes or changes coverage/terms and can change premium. |
| MTA / mid-term adjustment | Operational term for a mid-term policy change/transaction; often implemented through an endorsement. |
| FNOL (First Notice of Loss) | The initial report of an event/loss to the insurer. It starts the claims workflow. |
| Claim | A request/demand for payment for a loss that may fall within policy coverage. |
| Adjuster | Claims professional who investigates facts, evaluates damage/liability and helps determine the amount owed under policy terms. |
| Reserve | An insurer's estimate/provision for amounts expected to be paid on a claim. |
| Loss expense / LAE | Loss adjustment expenses associated with investigating and settling claims, such as adjuster, legal or investigation costs. |
| Salvage | Value recoverable from property remaining after a loss. |
| Subrogation | Insurer's right, after paying a covered loss, to pursue recovery from a responsible third party where applicable. |
| Indemnity | Payment/benefit intended to make the insured whole for covered loss, subject to policy terms and limits. |
| Deductible | Amount borne by the insured before insurer payment applies for a covered loss under the relevant coverage. |
| Limit / aggregate | Maximum amount payable under a coverage, often with per-occurrence and/or aggregate dimensions. |
| Waiting period / time deductible | A time interval before certain time-element/business-income benefits begin, where the policy uses such a mechanism. |
| Business income / business interruption | Coverage that may replace certain lost income and continuing expenses during restoration after a covered loss, subject to policy terms. |
| Reinstatement | Restoring a policy or coverage after it has lapsed/cancelled, subject to policy and insurer rules; ACORD treats reinstatement as a distinct P\&C business case. |
| Renewal | A new policy term/transaction continuing coverage after the current term, with updated risk, loss and pricing review. |
| Loss run / loss history | History of prior claims/losses used as underwriting information. |
| Exposure | A measurable characteristic that contributes to potential loss, such as payroll, sales, employees, vehicles, square footage, building value, units, locations, etc. |
| Hazard | A condition that increases the likelihood or potential severity of loss. |
| Risk | The uncertainty of loss to which the insurer is exposed. |

&nbsp;

# **4\. The Full Commercial Small-Business Insurance Lifecycle**

| \# | Stage | What happens |
| :---- | :---- | :---- |
| 0 | Need / prospect / channel | Business recognizes a risk need; may approach insurer directly or through a producer/broker. |
| 1 | Customer & business profile | Capture who the business is, what it does, where it operates, ownership and key exposure facts. |
| 2 | Coverage need / application | Business explains what it wants protected, desired effective date, limits, deductibles and insurance need. |
| 3 | Submission / intake | Insurer receives a usable submission; checks completeness, eligibility and routing. |
| 4 | Triage / appetite screening | Determine whether the risk fits product/state/class/appetite and whether automated processing is possible. |
| 5 | Underwriting review | Evaluate operations, exposures, loss history, controls, financial/operational information and required documentation. |
| 6 | Risk engineering / loss control | Where appropriate, inspect/evaluate hazards and document findings/recommendations. |
| 7 | Pricing & terms | Determine coverage structure, limits, deductibles, premium and any conditions/subjectivities. |
| 8 | Quote / terms offered | Present proposed terms with validity/expiry and requirements. |
| 9 | Customer decision / acceptance | Business accepts, requests changes, or declines/lets quote expire. |
| 10 | Bind | Insurer/authorized producer binds coverage after required conditions are satisfied. |
| 11 | Issue | Formal policy, declarations and endorsements/documents are produced and delivered. |
| 12 | In-force policy administration | Premium, certificates, questions, updates, audits, policy changes and compliance servicing occur during the term. |
| 13 | Loss prevention / risk improvement | Risk recommendations may be tracked; material changes can trigger reassessment. |
| 14 | Loss / incident | Covered or potentially covered event occurs; customer takes reasonable steps to protect property and reports the loss. |
| 15 | FNOL / claim intake | Claim is opened; policy and claimant are validated; priority assigned. |
| 16 | Investigation / coverage determination | Adjuster investigates facts, coverage, cause, liability and damages. |
| 17 | Assessment / reserve / settlement | Loss estimate evolves; reserve is established/updated; covered amount and payment are determined under policy terms. |
| 18 | Recovery / repairs / continuity | Repairs, vendors, temporary resources and business recovery are coordinated when appropriate. |
| 19 | Closure / recovery / subrogation | Payment/settlement completed; outstanding documents/actions resolved; salvage/subrogation pursued where applicable. |
| 20 | Renewal / non-renewal / termination | Upcoming term is re-underwritten using updated exposure, claims and risk information; outcome may be renewal, change, referral or non-renewal. |

&nbsp;

This stage model reconciles the project's current journey with industry process evidence. ACORD explicitly lists new-business quote, new-business submission, policy change, renewal and reinstatement as separate P\&C business cases; Chubb's current commercial operating model distinguishes new-business intake, quoting, bind, policy administration, renewal classification, renewal quoting/negotiation and bind.

# **5\. Stage-by-Stage Business Logic**

## **5.1 Customer & Business Onboarding**

A policyholder account is an identity; the insured business is a separate business/party profile. They are related but not the same concept.

The profile should contain the facts required to identify and classify the business: legal/business name, owner/contact, industry/business type, operating location(s), and communication details.

The business profile is reused across quotes, policies, claims and renewals. Material profile changes can trigger risk reassessment.

For IntelliSure's direct digital channel, the policyholder can self-register. Internal roles are provisioned by the administrator; that remains a project rule, not an industry-wide universal rule.

## **5.2 The Insurance Need / Quote Request**

The policyholder should not merely enter a coverage amount. The request needs a business narrative: what the business does, what assets/operations are exposed, why insurance is being sought, what risks the customer is particularly concerned about, and the desired effective date.

The customer should choose or be guided toward a product/line such as a Business Owner's Policy (BOP), general liability, commercial property, business income, professional liability, cyber, workers' compensation, commercial auto, etc., depending on eligibility. The Hartford notes that BOP combines business property and liability and can be customized with additional options such as business income and specialized coverages.

For a realistic application, the quote should carry both structured data and narrative/context. Structured data supports rating/validation; narrative helps underwriting understand operations and exceptions.

## **5.3 Submission Completeness & Triage**

A quote request becomes an underwriting submission only when the required information is sufficiently complete for the product.

Triage checks mandatory information, duplicate submissions, appetite fit, jurisdiction/product eligibility, obvious out-of-appetite characteristics and whether a referral is needed.

Modern commercial underwriting increasingly uses data prefill and external risk data. Verisk describes small-commercial workflows that enrich a submission with business classification, employees, financials, operational hazards, management/credit information, property characteristics and location data before rating and bind.

Triage is not the final underwriting decision. A case can be eligible for the product but still require human review.

## **5.4 Underwriter Assignment**

Assignment should be explicit and auditable. A submitted quote creates a work item/task, and the workflow layer records who owns that task.

Selection can be based on workload/capacity, line-of-business specialty, geography, authority level, complexity, referral type and availability.

Do not store underwriter identity as if it were a permanent master fact of the quote unless the business wants that as a domain attribute. For IntelliSure, WorkflowTask.assigneeUserId is the cleaner source for current operational ownership; the Quote can optionally keep assignedUnderwriterId only as a convenient read-model field if needed.

A referral should explain why the normal path cannot proceed: out-of-appetite characteristic, amount above authority, unusual exposure, missing information, adverse loss experience, unusual coverage request, regulatory/compliance sensitivity or other configured rule.

## **5.5 Risk Engineering / Loss Control**

Risk engineering is not the same as underwriting. Underwriters decide whether and on what terms the insurer should write the risk; risk engineers focus on hazard identification, controls and loss-prevention recommendations.

Examples include fire protection, electrical/utility risk, housekeeping, security, business continuity, machinery safeguards, ergonomics, catastrophe exposure and other hazards relevant to the business.

The Hartford currently describes its risk-engineering resources as tailored by business type to help businesses reduce risks, improve safety and prevent disruption.

A recommendation may be advisory, required before bind, required after bind, or tracked as a risk-improvement action. IntelliSure should distinguish these business meanings rather than treating every recommendation as the same.

## **5.6 Underwriting Decision**

The underwriter assembles the risk story: business operations, exposures, claims history, risk controls, documents, external data, risk-engineering findings, product appetite and pricing.

The underwriter can: accept as submitted; accept with modified terms; request more information; refer/escalate; or decline.

A risk score can support this decision, but the project should preserve the human decision and rationale. This is consistent with the current project design and with modern underwriting commentary that places automated triage/decision support alongside human ownership of judgment-heavy risks.

The final business record should preserve what was decided, why, by whom, under what authority/rule set, and what documents/evidence were considered.

## **5.7 Rating, Premium, Quote and Subjectivities**

Pricing is not simply 'coverage amount x 2%'. A production insurance premium is based on product-specific rating/underwriting criteria and the insured's characteristics. The Hartford explicitly says premium depends on underwriting and rating criteria and can consider industry, location, employees, building size, equipment/tools, claims history and payroll.

A quote should contain proposed terms: product/coverage, limits, deductibles/retentions, premium, effective dates, exclusions/conditions and quote validity/expiration.

Subjectivities should be explicit. Example: submit a signed application; provide loss runs; complete risk-improvement work; provide a fire-protection certificate; verify revenue; provide a lease; confirm no material changes.

A quote can be modified before acceptance. Each version/transaction should remain auditable so the business can explain which terms were actually offered.

## **5.8 Customer Acceptance vs Bind vs Issue**

These are different business moments.

Acceptance means the customer agrees to the offered terms.

Bind means authorized coverage is contractually bound after required conditions are satisfied. A binder may be used as interim evidence in some workflows.

Issue means the formal policy contract and documents are generated/delivered.

Some small-commercial digital products may combine these steps operationally, but IntelliSure should keep the concepts separate because they matter for status, audit, effective coverage, authority and document generation. Current Vertafore and Chubb workflows explicitly show bind/issue as separate steps or capabilities.

## **5.9 In-Force Policy Administration**

Once active, the policy is no longer a quote. It is the insurer's authoritative contract.

The policy record needs its own number, effective/expiration dates, coverages, limits, deductibles, premium and version/history.

The insured may request changes: add/remove location or property, change limits, add coverage, remove coverage, change named parties, update operations, etc. These are processed as policy transactions and may create endorsements.

NAIC describes an endorsement as an amendment that adds, deletes, excludes or changes coverage and may change premium; it can be issued at purchase, mid-term or renewal.

Material changes can trigger underwriting/risk review.

## **5.10 Audits / Exposure Reconciliation**

Some commercial policies are written using estimated exposures and later audited against actual exposure. Examples can include payroll, sales or other rating bases depending on product.

The Hartford's premium-audit materials explain that completed-term audit information can differ from the assumptions used for premium and may lead to additional premium or a return premium; changes can also result in an endorsement.

Therefore an enterprise-quality commercial lifecycle needs an exposure-update/audit concept even if IntelliSure implements only a simplified version.

## **5.11 Loss Prevention / Continuous Risk**

Risk engineering does not end at initial underwriting. New hazards, business expansion, new locations, replaced equipment or changes in operations can materially change exposure.

The business should be able to report changes; the insurer can request a new assessment or issue an endorsement/re-underwriting transaction.

Annual review is a normal business discipline for commercial insurance. Triple-I advises businesses to review and adjust coverage annually as the business changes.

## **5.12 Loss Event and FNOL**

A loss event is a real-world incident; the claim is the insurance transaction opened in response to that event.

At FNOL capture what happened, when, where, the affected policy, loss type, narrative, initial estimate, contact information, immediate actions taken and evidence.

Loss date should not be future-dated. The customer should be prompted to prevent further damage where safe and appropriate, preserve damaged evidence, take photographs/video and keep records.

Triple-I's current business-claim guidance emphasizes inventory, proof of loss, adjuster cooperation, documentation, reasonable temporary repairs and business records for business-income claims.

## **5.13 Coverage Verification and Claim Investigation**

Claims must verify that the policy was in force for the loss date and that the reported event falls within the relevant insuring agreement/coverage, subject to exclusions, conditions, deductibles, sublimits, waiting periods and other policy terms.

This is why Claims should read authoritative policy data from Quote & Policy Service rather than maintaining a second policy master. The current IntelliSure design already follows this rule.

The adjuster investigates cause, circumstances, ownership/interest, damage, liability, supporting evidence and potential recovery from third parties.

Claims investigation may involve specialists such as engineers, architects, contractors, accountants, lawyers or other experts depending on loss type.

## **5.14 Reserve, Assessment, Payout and Settlement**

The initial estimated loss is not the final payable amount.

A claim assessment determines the loss facts/damage and may split total assessed loss from covered loss.

A reserve is the insurer's evolving estimate of what it expects to pay. It is not automatically the settlement amount.

Payout calculation applies coverage rules, deductibles, limits, sublimits and approved adjustments. The settlement is the authorized outcome.

IntelliSure correctly separates ClaimAssessment, PayoutCalculation and ClaimSettlement; this separation should be preserved.

## **5.15 Fraud / Anomaly / Catastrophe Intelligence**

Analytics can flag suspicious patterns, severity, catastrophe correlation, duplicate claims or unusual signals.

The signal should support investigation/prioritization. It should not be represented as a final fraud finding or coverage denial by the model itself.

The current IntelliSure design already states that claim intelligence is decision support and human review remains required.

## **5.16 Vendor / Repair Network**

A repair/vendor is used when the claim/recovery case needs external work, such as inspection, repair, equipment replacement, temporary workspace or other services.

The vendor must be eligible/verified/active before assignment. Matching can consider service type, capability, location, availability, capacity and performance.

A contractor accepting or declining the job is an operational event; decline should return the job to a controlled reassignment path.

## **5.17 Recovery and Business Continuity**

Claims settlement and business recovery are related but not identical. A claim asks what loss is covered/payable; recovery asks how the business gets functioning again.

Recovery may require temporary equipment, temporary workspace, repairs, alternate suppliers, milestone tracking and coordination with vendors.

Business-impact data should capture affected operations, downtime, financial exposure and continuity needs. It is an operational estimate, not a promise of insurance benefits. IntelliSure already models this distinction correctly.

## **5.18 Claim Closure, Salvage and Subrogation**

Closure should occur only when required claim actions, settlement/payment and mandatory documentation are complete, and any required recovery/continuity conditions are satisfied or formally waived.

Where damaged property has recoverable value, salvage may be realized. Where a third party is responsible, subrogation may allow the insurer to pursue recovery after paying the insured.

This recovery activity may continue after the insured has received payment; claim closure and accounting/recovery processes must therefore be carefully separated.

## **5.19 Renewal**

Renewal is not simply copying the old policy dates forward.

The insurer evaluates current exposure, claims, changes in operations, risk-engineering findings, premium adequacy, appetite and available coverage.

Current commercial systems show renewal classification, renewal quoting, negotiation, and confirm coverage/bind as distinct steps. Chubb also documents an express renewal path for lower-complexity accounts based on conditions such as claim history and material change.

Analytics can produce renewal intelligence, but the final renewal action belongs to authorized underwriting/business authority in IntelliSure.

# **6\. The Most Important State Machines**

## **6.1 Quote / New Business**

| State | Meaning |
| :---- | :---- |
| DRAFT | Customer is preparing or has saved a quote request; editable. |
| SUBMITTED | Customer has completed the request and submitted it for insurer processing. |
| TRIAGE | Submission is being checked for completeness/eligibility/routing. |
| IN\_REVIEW | Underwriter workflow is active. |
| NEEDS\_INFORMATION | Additional information or documents requested from customer/producer. |
| RISK\_ASSESSMENT | Risk engineer work is required/active. |
| QUOTED | Insurer has issued proposed terms and premium. |
| ACCEPTED | Customer has accepted quoted terms. |
| BOUND | Authorized coverage is bound. |
| ISSUED | Formal policy documents issued. |
| DECLINED | Insurer will not offer coverage on the requested risk/terms. |
| WITHDRAWN | Applicant withdrew the request. |
| EXPIRED | Quote validity ended without acceptance/bind. |

&nbsp;

## **6.2 Policy**

| State | Meaning |
| :---- | :---- |
| PENDING\_ISSUANCE | Approved/bound transaction exists but formal issue is not yet complete. |
| BOUND | Coverage is bound; formal policy documents may still be pending. |
| IN\_FORCE | Policy coverage is active for its term. |
| CANCEL\_PENDING | Cancellation transaction has been initiated but not yet effective. |
| CANCELLED | Policy terminated according to applicable rules. |
| EXPIRED | Policy term ended without a replacement/renewal becoming effective. |
| REINSTATED | Previously terminated/lapsed policy has been restored under applicable rules. |

&nbsp;

Use one authoritative policy lifecycle state. Do not overload a generic status field to mean both workflow progress and legal/contract status.

## **6.3 Claim**

| State | Meaning |
| :---- | :---- |
| FNOL\_RECEIVED | Initial notice received; claim record opened. |
| UNDER\_REVIEW | Assignment/initial investigation underway. |
| AWAITING\_INFORMATION | Customer/vendor/third-party evidence requested. |
| INVESTIGATING | Detailed fact, coverage, cause, liability and damage work underway. |
| ASSESSING | Loss/damage assessment being developed. |
| PENDING\_DECISION | Enough evidence exists for a coverage/settlement decision. |
| APPROVED\_FOR\_SETTLEMENT | Authorized settlement amount determined. |
| REJECTED | Claim denied in whole; reason recorded and auditable. |
| SETTLED | Settlement approved and processed. |
| RECOVERY\_OPEN | Post-loss recovery/subrogation/salvage or related actions remain. |
| CLOSED | Required claim actions completed. |

&nbsp;

# **7\. What the Policyholder Actually Does**

| Policyholder action | Business purpose |
| :---- | :---- |
| Register and establish business profile | Who the business is. |
| Describe insurance need | Why coverage is needed and what is being protected. |
| Provide application/submission information | Business operations, exposure and requested terms. |
| Upload supporting documents | Loss runs, licenses, property information, financial documents, certificates, etc. as required. |
| Submit the application/quote request | Signals readiness for insurer review. |
| Respond to underwriter questions | Resolve missing information or subjectivities. |
| Review quote | Understand coverage, limits, deductibles, premium, conditions and validity. |
| Accept / decline / request changes | Customer decision. |
| Bind/complete purchase step | Coverage becomes bound once required conditions and authority are satisfied. |
| Maintain policy | Pay premium, report material changes, obtain certificates/documents, request endorsements. |
| Use loss-prevention support | Complete recommendations where relevant. |
| Report FNOL quickly | Tell insurer what happened. |
| Provide claim evidence | Proof of loss, invoices, photos, business records, estimates and other evidence. |
| Cooperate with adjuster/vendor | Inspection, questions, repair coordination and documentation. |
| Confirm recovery status | Where continuity support is part of the process. |
| Renew / update information | Confirm current exposures and desired coverage for next term. |

&nbsp;

# **8\. What the Underwriter Actually Does**

| Underwriter activity | Meaning |
| :---- | :---- |
| Accept/decline/referral | Applies appetite, authority, product and underwriting judgement. |
| Review submission quality | Check whether the risk story is sufficiently known. |
| Classify the risk | Determine appropriate business/class/risk category. |
| Evaluate exposure | Sales/revenue, payroll, employees, location, property, operations, vehicles, etc. as relevant. |
| Review loss history | Past losses can change risk selection and price. |
| Review controls | Safety, security, fire protection, continuity, management and other relevant controls. |
| Use data intelligently | External/third-party data can prefill and validate facts. |
| Coordinate risk engineering | Request inspection or specialized hazard assessment when needed. |
| Set terms | Coverage structure, limits, deductibles, exclusions/conditions and premium. |
| Issue decision and rationale | Create a traceable record of why the case was accepted, modified, referred or declined. |
| Manage authority | Escalate cases above delegated limits or outside authority. |

&nbsp;

# **9\. What the Risk Engineer Actually Does**

| Risk-engineering activity | Meaning |
| :---- | :---- |
| Review physical/operational hazards | Understand how the business can suffer or cause loss. |
| Document findings | Each finding should have evidence and severity. |
| Create recommendations | Specify what should be improved. |
| Set priority/target date | Distinguish critical from advisory improvements. |
| Track mitigation | Open \-\> in progress \-\> completed \-\> verified/waived. |
| Verify completion | Confirm evidence or inspection result where required. |
| Update risk assessment | Feed the changed risk view into underwriting/renewal. |

&nbsp;

# **10\. What the Claims Team Actually Does**

| Claims activity | Meaning |
| :---- | :---- |
| Intake and validate FNOL | Confirm policy/customer context and capture the loss story. |
| Assign adjuster | Use claim manager authority, workload, specialty and geography as appropriate. |
| Investigate | Cause, circumstances, coverage, liability, damages and documentation. |
| Request information | Build an evidence file without exposing internal notes to the customer. |
| Assess loss | Separate total physical/financial loss from covered loss. |
| Establish/update reserves | Maintain a realistic expected payment estimate. |
| Calculate settlement | Apply policy terms, deductible, limits, sublimits and authorized adjustments. |
| Approve or reject | Authorized claim manager/authority makes the final claim decision. |
| Coordinate vendor/repair | When necessary. |
| Coordinate recovery | Business continuity/recovery case where needed. |
| Close | Only when mandatory work is complete. |

&nbsp;

# **11\. Industry Patterns We Should Explicitly Preserve**

| Pattern | Why it matters to IntelliSure |
| :---- | :---- |
| New-business submission is richer than a quote | Commercial underwriting often starts from a submission/application package, not just a premium request. ACORD separately models new business submission and new business quote. |
| Human referrals are normal | Not every risk fits automated rules. Current commercial platforms explicitly route referral accounts to experienced underwriters when an exception requires review. |
| Bind and issue are conceptually distinct | Operational systems can bind first and issue documents afterward; some systems later mark a policy in-force after mail-out/issuance. |
| Policy changes are transactions | Endorsements preserve what changed instead of mutating history. |
| Renewals use fresh information | Current commercial renewal workflows classify and re-quote based on claims and material changes, rather than blindly copying the prior term. |
| Claims are evidence-driven | Proof of loss, photos, records, estimates, adjuster inspection and business records can all matter. |
| Business recovery is broader than claim payment | A covered business loss may involve operational downtime, temporary location, equipment rental and other extra expenses. |
| Data quality is an underwriting capability | Commercial insurers use extensive exposure/loss data and data quality controls; Verisk reports 38.9B+ underwriting records in its 2025 annual report. |

&nbsp;

# **12\. Gap Analysis: Current IntelliSure vs Business Reality**

| Gap | Recommended business change | Priority |
| :---- | :---- | :---- |
| Quote request lacks insurance narrative | Add insurance need/description and preferably requested coverage rationale / primary concerns. The current project DTO has structured fields but no narrative field. | HIGH |
| Quote lifecycle is compressed | Introduce TRIAGE, IN\_REVIEW, NEEDS\_INFORMATION, RISK\_ASSESSMENT, QUOTED, ACCEPTED, BOUND, ISSUED, plus DECLINED/WITHDRAWN/EXPIRED. | HIGH |
| No explicit quote version/offer history | Preserve each material quote version/terms offered to explain what the customer accepted. | HIGH |
| Underwriter assignment is not a domain concept yet | Use WorkflowTask assignee for current work ownership; optionally add assignment history if reporting requires it. | HIGH |
| RiskAssessment is missing richer hazard structure at top level | Existing RiskFinding helps. Keep granular findings and evidence rather than only a single list. | MEDIUM |
| Policy lacks explicit bind concept | Add policy state distinction BOUND vs IN\_FORCE/ISSUED. Do not conflate issuance with legal effective coverage. | HIGH |
| Policy model is too single-row/simple for real servicing | Existing version field is good; strengthen with effective-dated policy transaction history via Endorsement and preserve prior versions. | HIGH |
| Coverage structure is too flattened | coverageTypes \+ one coverageLimit \+ one deductible cannot model multiple coverage lines with different limits/deductibles/exclusions. Existing quote\_coverage/policy\_coverage in the earlier working schema should be restored/used, or modeled explicitly. | HIGH |
| Exclusions/waiting periods missing from Policy entity | CoverageResponse has exclusions/waitingPeriodDays/businessIncomeCoverage, but Policy entity lacks these as authoritative structured data. | MEDIUM |
| Premium audit/exposure reconciliation absent | Add an audit/exposure transaction concept if we want stronger commercial realism; at minimum keep an exposure change event. | MEDIUM |
| Claims reserve missing | Current Claim has estimatedLossAmount and approvedAmount but no explicit reserve field/history. | HIGH |
| Claim payment transaction missing | Settlement is not the same as payment. For a full claims lifecycle, add payment transaction/status or at least a payment record. | HIGH |
| Subrogation/salvage not represented | Needed for realistic post-settlement recovery logic. | MEDIUM |
| Claim coverage decision is implicit | Add explicit coverageDecision \+ rationale (or a claimCoverageDecision entity) so covered/partial/denied is distinguishable from overall claim status. | HIGH |
| Business income logic should be explicit | BusinessImpact is good, but claim-side policy coverage should include limits/waiting period/period of restoration concepts where supported by the selected product. | MEDIUM |
| Vendor SLA/compliance is light | Current vendor onboarding/assignment/performance is strong for MVP; add compliance expiry/insurance certificate/license concepts only if time permits. | LOW/MEDIUM |
| Renewal transaction is too compact | Add renewal workflow stages: initiated, information requested, under review, quoted, accepted, bound/issued, declined/non-renewed, expired. | MEDIUM |
| Quote creation currently uses a demo 2% premium | Keep only as a temporary demo placeholder; industry logic should compute or accept premium from a rating/underwriting result, not a fixed percentage. | HIGH |

&nbsp;

# **13\. Required Database/Entity Modifications Before More Coding**

| Entity/Table area | Modification |
| :---- | :---- |
| Quote | Add insuranceDescription / insuranceNeed narrative; add submissionNote; add quote validity/versioning fields as needed; separate offered quote stage from underwriting review. |
| Quote | Prefer product-specific coverage child records so each coverage can have its own limit, deductible, premium component, exclusion/condition metadata. |
| Quote | Add operational ownership only if needed for query speed; current assignment should live in workflow task/assignment history rather than duplicating authority logic. |
| Policy | Add/clarify bind/issue/in-force statuses; keep policy version and effective dates. |
| Policy | Use policy coverage child records rather than a single limit/deductible if multiple coverages are supported. |
| Endorsement | Add requestedBy/approvedBy, decision/reason, effectiveFrom/effectiveTo or transaction timestamps, and premiumDelta where needed. |
| RenewalTransaction | Add renewal stage/status history, proposed terms, decision, and non-renewal/decline reason where applicable. |
| RiskAssessment | Add referral/decision linkage and richer assessment status/history; preserve submitted versions. |
| Claim | Add reserveAmount / totalIncurred or a dedicated ClaimFinancials concept; add coverageDecision and coverageDecisionReason. |
| Claim | Add payment record or settlement-payment lifecycle; distinguish approved settlement from payment issued/cleared. |
| Claim | Add salvage/subrogation case data if these are in scope for the MVP. |
| BusinessImpact | Keep it explicitly operational; never let it itself imply coverage entitlement. |
| RecoveryCase | Add recovery objectives/milestones, dependencies and completion evidence where needed. |
| Document | Extend document types for application, loss run, policy, endorsement, FNOL evidence, proof of loss, estimate, settlement, recovery evidence. |
| AuditEvent | Capture business decision context: state before/after, actor, reason, rule/model version where relevant. |

&nbsp;

# **14\. Suggested Final Business Data Model (Conceptual)**

| Concept | Business responsibility | Why separate |
| :---- | :---- | :---- |
| Customer / Party | Identity \+ business profile | Source of truth for who the insured is. |
| Submission / Quote | Requested insurance \+ proposed terms \+ narrative | The commercial intake and offer lifecycle. |
| Risk Assessment | Hazards \+ findings \+ recommendations \+ assessment decision context | Source of underwriting/risk assessment facts. |
| Policy | Bound/issued insurance contract | Authoritative contract. |
| Endorsement | Policy change transaction | Immutable change history. |
| Renewal Transaction | Next-term evaluation and decision | Tracks renewal separately from current policy. |
| Claim | Loss case / FNOL | Source of claim state. |
| Claim Assessment | Detailed loss/damage evaluation | Evidence-based assessment. |
| Claim Financials | Reserve, incurred, settlement, payment | Financial progression separate from workflow state. |
| Vendor Assignment | Repair/service job | Operational work assigned to third party. |
| Recovery Case | Business continuity/recovery | Post-loss operational restoration. |
| Workflow / Task | Work ownership and process state | Who must do what next. |
| Document | Evidence/contract artifact metadata | What evidence exists and where it is stored. |
| Audit Event | Immutable decision/history record | Who changed what, when, and why. |
| Analytics Snapshot | Model/rule output | Decision support only. |

&nbsp;

# **15\. End-to-End Example You Should Be Able to Explain**

1. 1\. A small restaurant owner creates an account and business profile.  
2. 2\. The owner says: 'We operate a 40-seat restaurant. We want protection for building contents/equipment, general liability and loss of income after a covered property loss. We are concerned about fire, water damage and refrigeration failure.'  
3. 3\. IntelliSure creates a draft submission with structured business facts plus this narrative. The customer attaches property details and other requested documents.  
4. 4\. The customer submits. The quote becomes SUBMITTED; the workflow creates intake/underwriting work.  
5. 5\. Triage confirms product eligibility and completeness. If something is missing, the case becomes NEEDS\_INFORMATION rather than being silently routed.  
6. 6\. A qualified underwriter receives the underwriting task. A risk engineer may be assigned if the exposure warrants inspection or specialist input.  
7. 7\. Risk assessment records operations, location, fire protection, equipment, hazards, findings and recommendations.  
8. 8\. Underwriter reviews risk, claims/loss history, documents and risk-engineering findings; analytics may supply decision-support indicators.  
9. 9\. Underwriter decides to offer terms with defined coverages, limits, deductible and premium. The quote becomes QUOTED and has an expiry date.  
10. 10\. Customer reviews the quote and accepts. The transaction enters the bind path. Any subjectivities must be satisfied before binding.  
11. 11\. Coverage is BOUND. Formal policy documents are issued and the policy becomes IN\_FORCE for the effective period.  
12. 12\. Six months later the restaurant adds a second location. The owner requests a policy change. IntelliSure creates an endorsement transaction; underwriting/risk review occurs if the change affects risk.  
13. 13\. A fire causes damage. The owner reports FNOL with loss date, location, description, estimated loss and initial evidence.  
14. 14\. Claims validates the policy, assigns an adjuster, requests evidence and investigates.  
15. 15\. The adjuster assesses total damage, covered damage and applicable deductible/limits; the claim reserve evolves as facts become clearer.  
16. 16\. A repair vendor is assigned. Recovery tracks restoration of the restaurant and business continuity.  
17. 17\. Claims approves settlement. Payment is processed. Salvage/subrogation work is opened if applicable.  
18. 18\. Recovery tracks reopening. Claim closes only after required claim/recovery conditions are satisfied.  
19. 19\. Near policy expiry, renewal begins. Current revenue, operations, new location, loss experience and risk controls are reviewed. Analytics provides renewal intelligence; the underwriter makes the renewal decision.

# **16\. Questions a Mentor/Interviewer Can Ask**

| Question | Answer you should be able to give |
| :---- | :---- |
| Why does a policyholder submit a quote request? | To request proposed insurance terms. The request should explain the insurance need and provide risk information; the insurer then underwrites and prices the risk. |
| Is a quote a policy? | No. A quote is a proposed set of terms/price; binding/issuance create the insurance contract/coverage according to the applicable process. |
| Why do we need both customer acceptance and bind? | Acceptance is the customer's agreement to offered terms; bind is the insurer/authorized party establishing coverage under required conditions. Some digital flows operationally combine them, but the business concepts differ. |
| Does the underwriter receive the quote automatically? | The system should create a work item and assignment; the underwriter receives a task when the submission is eligible for underwriting review. |
| Who decides whether the customer gets insurance? | The insurer's underwriting authority decides within its rules/authority. IntelliSure should preserve the underwriter decision rather than let AI make the final decision. |
| Why do we need a risk engineer if we already have an underwriter? | The underwriter evaluates acceptability/terms; risk engineering provides specialized hazard/control assessment and loss-prevention recommendations. |
| Is a risk score the same as an underwriting decision? | No. It is an analytical signal. The human underwriting decision and rationale remain authoritative in this project. |
| Is a claim the same as FNOL? | FNOL is the first notice/reporting event; the claim is the ongoing case created and handled from that report. |
| Does estimated loss equal payout? | No. Estimated loss is an early estimate. Investigation, coverage, deductible, limits, sublimits and other policy terms determine the payable amount. |
| Why do we need reserve if we have approvedAmount? | Reserve is the evolving expected liability; approved settlement is an authorized outcome. They serve different business purposes. |
| Why can a claim continue after settlement? | Subrogation/salvage or other recovery and administrative work can remain after the insured payment. |
| What is an endorsement? | An amendment to an existing policy that adds, removes, excludes or changes terms/coverage. |
| Why is renewal not just extending expiry date? | Because the risk can change. Current exposures, claims and controls can change, so the insurer may change price, terms, conditions, or decline/non-renew. |
| Why are documents a separate service? | Because applications, evidence, policies and audit records are shared artifacts that should have controlled storage, metadata, access and retention. |
| What is the single source of truth for policy coverage in claims? | Quote & Policy Service. Claims should not maintain a second authoritative policy master. |

&nbsp;

# **17\. Project-Specific Role Matrix (Business Meaning)**

| Role | Business responsibility |
| :---- | :---- |
| Policyholder / Small Business Owner | Provides business/risk information; requests insurance; submits; responds to questions; accepts terms; services policy; reports losses; supplies claim evidence; participates in recovery and renewal. |
| Underwriter | Owns underwriting judgement, quote/terms, referrals, approval/decline, renewal decision and actions requiring underwriting authority. |
| Risk Engineer | Owns hazard/risk assessment, findings, recommendations, mitigation verification and follow-up assessments. |
| Claims Adjuster | Owns assigned claim investigation, evidence review, assessment, reserve development and recommendation/processing within authority. |
| Claims Manager | Owns claims assignment/reassignment, operational escalation, approval/denial authority as configured, recovery monitoring and claim closure governance. |
| Vendor Manager | Owns vendor onboarding, verification, eligibility, matching/assignment governance, performance and SLA escalation. |
| Service Provider / Contractor | Performs assigned repair/recovery service, accepts/declines, updates progress, supplies completion evidence. |
| System Administrator | Creates internal users, roles, system-level workflow parameters, audit review and platform administration. |

&nbsp;

# **18\. Current Source Model: What We Already Got Right**

* Separation of Customer & Party, Quote & Policy, Risk & Underwriting, Claims, Vendor & Partner, Recovery & Business Continuity, Workflow & Notification, Document & Audit, and Analytics & Intelligence is business-coherent.  
* The current BusinessCustomer separates identity from the business/insured profile, which is the right conceptual split.  
* RiskAssessment, RiskFinding and RiskRecommendation are appropriately separated.  
* ClaimAssessment, PayoutCalculation and ClaimSettlement are correctly distinct.  
* BusinessImpact is explicitly separated from coverage determination, which is correct.  
* VendorOnboardingRequest, VendorAssignment and VendorPerformance give a credible partner lifecycle.  
* Workflow \+ WorkflowTask \+ Notification is a sensible separation between domain state and operational task state.  
* Document metadata plus audit event are appropriately separated.  
* Analytics snapshots carry model version and are marked as decision support, which is the correct business framing.  
* Current service-to-service design correctly avoids cross-service database joins and treats policy data as authoritative in Quote & Policy Service.

# **19\. Source Notes and Evidence Base**

• ACORD — Property & Casualty Data Standards; current P\&C standards and business cases including new-business quote, submission, policy change and renewal.

• NAIC — Insurance Topics / Glossary; terminology for claim, underwriter, underwriting, policy, reserves, salvage, subrogation, and related concepts.

• NAIC — State Licensing Handbook; distinction among indication, binding, binder, and policy issuance, with jurisdictional caveats.

• NAIC — Consumer guidance on endorsements/riders and declarations/information pages.

• Triple-I (Insurance Information Institute) — Business insurance shopping and business-claim guidance, including proof of loss, adjuster interaction, temporary repairs, repair bids and business-income documentation.

• The Hartford — Business Owners Policy, Risk Engineering, and Premium Audit materials; commercial coverage examples, underwriting/rating factors, risk-prevention services, and post-term exposure reconciliation.

• Chubb — Current commercial insurance operating/workflow materials covering new-business intake, quoting, coverage/bind confirmation, renewal, mid-term adjustments, document issuance and account servicing; express products also distinguish reserve/quote/bind/issue and underwriting referral.

• Verisk — Small-commercial underwriting materials and current data-quality/underwriting literature covering enrichment, industry classification, exposure information, property characteristics and automated risk data.

• Aon — 2026 insurance-market commentary discussing data-rich submissions, underwriting rigor and risk-control information in current commercial underwriting.

• Vertafore — Commercial insurance workflow documentation showing quote selection, bind/issue and policy/binder status transitions.

• ACORD — 2025 Global/Reinsurance Lifecycle Data Standards update emphasizing end-to-end digital support from placing and binding through claims and settlement.

| Source | What we used it for |
| :---- | :---- |
| ACORD P\&C Data Standards | Current listed P\&C materials include new business quote, new business submission, policy change, renewal, reinstatement, coverage codes and renewal processing workflow; ACORD P\&C XML v2.13.0 is listed as January 2025\. |
| NAIC Glossary | Common definitions for policy, claim, underwriting, underwriter, loss reserve, salvage, subrogation and related terms. |
| The Hartford \- BOP | BOP combines property and liability; coverage can be customized; premium/coverage are subject to underwriting and policy terms. Updated May 20, 2026\. |
| The Hartford \- premium audit | Explains audit vs estimated premium and endorsement consequences. |
| Triple-I \- shopping for business insurance | Risk assessment, business property/operations, professional guidance and annual coverage review. |
| Triple-I \- filing a business claim | Proof of loss, inventory, adjuster inspection, temporary repairs and business records. |
| Chubb \- commercial operating model | 2026 commercial workflow view includes new-business intake, quoting, bind, policy admin, renewal classification, renewal quoting/negotiation and mid-term activity. |
| Verisk \- small commercial underwriting | Illustrates business data enrichment, risk classification, exposures, management/risk information, property characteristics, rating, quote, bind and issue. |
| Aon \- 2026 market context | Current market commentary stresses risk appetite, quality of risk controls and data-rich underwriting submissions. |
| Current IntelliSure project files | Project-specific source of current entities, DTOs, endpoints, communication patterns, journey mapping and 31-table DB design. These are the baseline to revise, not external industry standards. |

&nbsp;

# **20\. Final Locked Business Blueprint**

For the remainder of the project, treat the following as the business contract of IntelliSure unless we deliberately revise it:

* IntelliSure is a small-business commercial P\&C lifecycle platform.  
* A quote request starts with the customer's insurance need plus structured business/exposure information.  
* Submission and quote are not the same business state.  
* Triage and underwriting are explicit stages.  
* Underwriter assignment is a work-management decision recorded by workflow/task ownership.  
* Risk engineering is a specialist assessment/loss-prevention function, not a duplicate underwriter.  
* Quote, customer acceptance, bind and policy issuance are distinct business moments even if one digital action may orchestrate several.  
* Policy is the authoritative contract after issuance/binding.  
* Endorsements preserve policy change history.  
* Claims begins at FNOL and progresses through investigation, coverage decision, assessment, reserve/financial evaluation, settlement, recovery and closure.  
* Recovery/business continuity is operational recovery support and is not itself a coverage decision.  
* Analytics/AI provides decision support with model/rule versioning; human authority remains explicit for consequential decisions.  
* Renewal is a fresh transaction informed by current risk, claims and exposure rather than a simple date extension.  
* Audit history is immutable and decision rationale is retained.  
* Every business state transition must have a valid next-state rule and an identifiable actor/system event.

# **21\. External Sources and Direct Links**

**ACORD Property & Casualty Data Standards:** https://www-dev.acord.org/standards-architecture/acord-data-standards/Property\_Casualty\_Data\_Standards

**ACORD GRLC Generation 2.0 announcement (2025):** https://www-dev.acord.org/standards-architecture/ruschlikon/ruschlikon-news/2025/04/10/acord-launches-grlc-generation-2.0-data-standards-to-support-digitalization-throughout-global-%28re%29insurance-industry

**NAIC Insurance Glossary:** https://content.naic.org/es/node/11821

**NAIC Producer Licensing:** https://content.naic.org/insurance-topics/producer-licensing

**NAIC State Licensing Handbook:** https://content.naic.org/sites/default/files/inline-files/prod\_serv\_marketreg\_stl\_hb.pdf

**NAIC Endorsement / Rider consumer guidance:** https://content.naic.org/article/consumer-insight-what-insurance-endorsement-or-rider

**NAIC Declarations / shopping-tool material:** https://content.naic.org/sites/default/files/committee\_related\_documents/committees\_c\_trans\_read\_wg\_related\_shopping\_tool\_singles.pdf

**Triple-I — Shopping for Business Insurance:** https://www.iii.org/article/shopping-for-business-insurance

**Triple-I — Filing a Business Insurance Claim:** https://www.iii.org/article/filing-a-business-insurance-claim

**Triple-I — Filing a Business Insurance Claim After a Disaster:** https://www.iii.org/article/filing-a-business-insurance-claim-after-a-disaster

**The Hartford — Business Owners Policy:** https://www.thehartford.com/business-owners-policy

**The Hartford — Premium Audit:** https://www.thehartford.com/business-insurance/premium-audit

**The Hartford — You Speak, We Act / risk services:** https://www.thehartford.com/business-insurance/you-speak-we-act

**Chubb — EMEA commercial insurance operating materials:** https://www.chubb.com/uk-en/campaign/emea-commercial-insurance.html

**Chubb — Express Umbrella:** https://www.chubb.com/au-en/business/products/umbrella-and-excess-liability.html

**Chubb — Express Renewal Guide:** https://www.chubb.com/content/dam/chubb-sites/chubb-com/au-en/business/casualty/documents/casualty-lmm-express-renewal-guide.pdf

**Verisk — Small Commercial Underwriting:** https://integration.verisk.com/498325/siteassets/media/downloads/underwriting/small-commercial/the-secret-to-shrinking-small-commercial-underwriting-time-to-seconds.pdf

**Verisk — 2025 Annual Report:** https://www.verisk.com/496732/siteassets/media/corporate-social-responsibility/downloads/verisk-2025-annual-report.pdf

**Aon — Global Insurance Market Insights, Q1 2026:** https://www.aon.com/en/insights/reports/global-insurance-market-insights/q1-2026-overview

**Aon — Insurance Market Report 2026 Spring Update:** https://assets.aon.com/-/media/files/aon/reports/2026/aon-insurance-market-report-2026-spring-update-en.pdf

**Vertafore — AIM quote/bind/issue workflow:** https://help.vertafore.com/AIM/content/aim\_netrate\_guide.htm