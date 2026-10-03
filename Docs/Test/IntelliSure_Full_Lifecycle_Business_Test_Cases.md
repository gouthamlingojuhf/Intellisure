**INTELLISURE | BUSINESS QA BLUEPRINT** 

#### **INTELLISURE** 

# **Full Insurance Lifecycle Business Test Cases** 

3 end-to-end business flows | Technical implementation intentionally excluded 

Business scope: Small-business commercial Property & Casualty insurance 

##### **PURPOSE** 

Validate that IntelliSure behaves as an insurance lifecycle platform rather than a collection of CRUD screens. Each test case verifies a business state transition, authority boundary, evidence requirement, or operational outcome. 

## **1. Test Strategy** 

- These tests use the business vocabulary and lifecycle defined in the IntelliSure Insurance Lifecycle Business Blueprint and Microservice Business Definitions. 

- The three flows are intentionally different: a clean new-business cycle, a mid-term-loss/recovery cycle, and an exception/referral/non-renewal cycle. 

- AI/Analytics is excluded from pass/fail dependency. Where analytics could exist, the human business authority remains the decision-maker. 

- A test passes only when the stated business outcome occurs and the responsible role remains the authorized owner of the decision. 

- Exact regulatory timing, cancellation rules, payment rails and product wording vary by jurisdiction and product; these are IntelliSure target business behaviors, not universal legal rules. 

## **2. Lifecycle Model Under Test** 

|**Stage**|**Business meaning**|**Primary owner**|
|---|---|---|
|0 Need /prospect|Business recognizes an insurance need.|Policyholder|
|1 Customer / businessprofile|Who is thepartyand what business does it represent?|Customer & Party|
|i<br>2 Coverage need / application|Desired protection, effective date, limits, deductibles and insurance<br>need are captured.|Policyholder / Quote & Policy|
|3 Submission / intake|Completed information is submitted to the insurer.|Quote & Policy/ Workflow|
|4 Triage / appetite|Completeness,eligibility,routingand appetite are checked.|Workflow / Risk & Underwriting|
|5 Underwriting/ risk|Risk,hazards,controls,loss historyand required conditions are|Risk & Underwriting|



IntelliSure | Business Lifecycle Test Cases | 1 

###### **INTELLISURE | BUSINESS QA BLUEPRINT** 

|**Stage**|**Business meaning**|**Primary owner**|
|---|---|---|
||evaluated.<br>i||
|6 Risk engineering|Specialist inspection/findings/recommendations where warranted.|Risk Engineer|
|7 Pricing / terms|Coverage structure, limits, deductibles, premium and conditions are<br>determined.|Underwriter / Quote & Policy|
|8Quote / terms offered|Insurerprovidesproposed terms with validity/requirements.|Quote & Policy|
|9 Customer acceptance|Customer accepts,requests changes,declines or lets terms expire.|Policyholder|
|10 Bind|Authorized coverage is bound after required conditions.|Authorized underwriting/businessprocess|
|11 Issue / in-force|Formal policy documents are issued and coverage is active for its<br>effectiveperiod.|Quote & Policy / Document & Audit|
|12-13 Service / loss prevention|Endorsements, exposure changes, audits and risk-improvement<br>activities occur duringterm.|Quote & Policy / Risk & Underwriting|
|14-19 Loss to closure|Loss → FNOL → investigation → coverage → reserve/assessment →<br>settlement/payment → recovery→ closure.|Claims / Recovery / Vendor|
|20 Renewal / non-renewal|Current exposure, claims and risk controls are reassessed for the next<br>term.|Risk & Underwriting / Quote & Policy|



## **3. Standard Test Data Profiles** 

|**Profile**|**Business facts**|**Coverage need**|**Expectedpath**|
|---|---|---|---|
|A - Low-complexity restaurant<br>i|40-seat restaurant; stable operations; one location;<br>complete information;no material loss history.<br>i   i|Property/equipment, general liability, business<br>income.|Triage → Underwriting → Quote → Accept → Bind →<br>Issue → Renewal|
|B - Restaurant with covered fire loss|Profile A plus fire loss during policy term;<br>property/equipment damage; temporary closure.|Existing policy with applicable property/business-<br>income coverage.|FNOL → Investigation → Assessment → Reserve →<br>Vendor/Recovery → Settlement/Payment → Closure<br>→ Renewal|
|C - Higher-complexity contractor|Construction/contracting operations; material<br>exposure; incomplete initial submission; specialist<br>risk review required.|Property/liability and requested operational<br>coverage.|Needs info → Referral → Risk Engineering →<br>Conditional terms / decline depending on decision →<br>Non-renewal branch if risk later becomes<br>unacceptable|



IntelliSure | Business Lifecycle Test Cases | 2 

**INTELLISURE | BUSINESS QA BLUEPRINT** 

## **FLOW 1 — Clean New-Business → In-Force → Renewal** 

##### **BUSINESS OBJECTIVE** 

Demonstrate the complete happy-path lifecycle from a policyholder request through underwriting, <u>quote, acceptance, bind, issue, servicing and renewal.</u> 

|**Item**|**Definition**|
|---|---|
|Entrycriteria|Activepolicyholder + complete businessprofile + nopriorpolicyrequired + complete submission data.|
|Exit criteria|An in-forcepolicyhas completed the term transaction and a renewal decision/path exists for the next term.|
|Primary roles|Policyholder; Underwriter; Risk Engineer; Claims Adjuster/Manager; Vendor Manager; Service<br>Provider/Contractor;System Administrator as applicable|
|Services traversed|Customer & Party; Quote & Policy; Risk & Underwriting; Claims; Vendor & Partner; Recovery & Business<br>Continuity;Workflow & Notification;Document & Audit;Analytics optional and non-authoritative|



### **Business Flow** 

|**Step**|**Business event / action**|**Expected state / outcome**|**Primary owner**|
|---|---|---|---|
|1|Register + business profile|ACTIVE policyholder with a canonical<br>customer/businessprofile.|Customer & Party|
|2|Start draft insurance request|DRAFT submission contains insurance need,<br>business facts and requested coverages.|Policyholder<br>l|
|3|Complete + submit|Submission is SUBMITTED and becomes eligible for<br>intake/triage.|Policyholder / Workflow<br>l|
|4|Triage|Completeness/appetite checks pass; underwriting<br>work is created.|Workflow / Risk & Underwriting|
|5|Underwriter assignment|Eligible underwriter receives a task based on<br>configured work rules;assignment is recorded.|Workflow|
|6|Risk assessment<br>f|Risk facts, hazards, controls, loss history and<br>evidence are evaluated.|Risk & Underwriting|
|7|Offer terms|QUOTE contains insurer terms, limits, deductibles,<br>premium,validityand anysubjectivities.<br>f|Underwriter / Quote & Policy|
|8|Customer accepts|Customer accepts the offered terms while quote is<br>valid.|Policyholder|
|9|Bind + issue|Coverage is BOUND, then formal policy is<br>issued/IN_FORCE for the effectiveperiod.|Authorized insurer process / Quote & Policy|
|10|In-force service|Policy remains authoritative; ordinary servicing can<br>occur without rewritinghistory.|Quote & Policy|
|11|Renewal|Current exposures, loss experience and changes<br>are evaluated for next term.|Underwriter / Quote & Policy|



### **Test Cases** 

|**TC ID**|**Lifecycle stage**|**Role**|**Business test / action**|**Expected business outcome**|**Priority**|
|---|---|---|---|---|---|
|F1-T01|Registration|Policyholder|Create a new policyholder<br>account and complete the small-<br>business profile.|Account becomes ACTIVE and<br>the business has a canonical<br>customer/party identity used<br>downstream.|High|



IntelliSure | Business Lifecycle Test Cases | 3 

###### **INTELLISURE | BUSINESS QA BLUEPRINT** 

|**TC ID**|**Lifecycle stage**|**Role**|**Business test / action**|**Expected business outcome**|**Priority**|
|---|---|---|---|---|---|
|F1-T02|Authorization|Customer & Party / Admin|Attempt employee-role self-<br>registration instead of<br>administrator creation.|Employee role creation is<br>rejected; internal roles are<br>created/activated through the<br>authorized administrativepath.|High|
|F1-T03|Draft request|Policyholder|Create a quote request with<br>business facts, desired coverage<br>and insurance-need narrative.|A DRAFT request exists and<br>remains editable.|High|
|F1-T04|Draft validation|Policyholder|Submit with missing required<br>business/exposure information.|Submission is not accepted as<br>complete; missing information is<br>identified rather than silently<br>routed for underwriting.|High|
|F1-T05|Submission|Policyholder|Submit a complete request.|Quote/submission moves to<br>SUBMITTED; workflow starts<br>intakeprocessing.|Critical|
|F1-T06|Triage|Workflow / Underwriting|Run completeness, eligibility and<br>appetite screening on the clean<br>restaurantprofile.|Submission clears triage and is<br>eligible for underwriting review.|Critical|
|F1-T07|Assignment|Workflow|i<br>Create underwriting work for the<br>submitted case.|Exactly one current accountable<br>underwriting task is created for<br>the appropriate underwriter;<br>assignment historyis traceable.|Critical|
|F1-T08|Risk assessment|Underwriter / Risk Engineer|Review operations, location, fire<br>protection, equipment, controls,<br>loss history and supporting<br>documents.|Risk assessment contains<br>findings and recommendations;<br>risk evidence is separate from the<br>policycontract.|Critical|
|F1-T09|Subjectivity|Underwriter|Identify a required condition<br>before binding, e.g., install/verify<br>a control.|A subjectivity is recorded with an<br>accountable task and is not<br>silentlytreated as satisfied.|High|
|F1-T10|Pricing / terms|Underwriter|Set acceptable coverage<br>structure, limits, deductibles,<br>premium and quote validity.|A QUOTED offer contains explicit<br>proposed terms and expiry; fixed-<br>demo premium logic is not<br>treated as authoritative business<br>pricing.|Critical|
|F1-T11|Quote presentation|Policyholder|Open the quoted offer before<br>expiry.|Customer can see the proposed<br>terms and any outstanding<br>requirements.|High|
|F1-T12|Customer rejection|Policyholder|Decline the quoted terms.|Quote records a controlled<br>decline/withdrawal outcome; it<br>cannotproceed to bind.|High|
|F1-T13|Quote expiry|Policyholder|Allow quote validity to end<br>without acceptance.|Quote becomes EXPIRED and<br>cannot be bound under the<br>expired offer.|High|
|F1-T14|Acceptance|Policyholder|Accept a valid quote with all<br>required subjectivities satisfied.|f<br>Customer acceptance is<br>recorded and the transaction<br>becomes bind-ready.|Critical|
|F1-T15|Bind control|Authorized insurer process|Attempt to bind while a<br>mandatory subjectivity is<br>outstanding.|Bind is blocked until the required<br>condition is satisfied or formally<br>dispositioned according to<br>configured authority.|Critical|



IntelliSure | Business Lifecycle Test Cases | 4 

###### **INTELLISURE | BUSINESS QA BLUEPRINT** 

|**TC ID**|**Lifecycle stage**|**Role**|**Business test / action**|**Expected business outcome**|**Priority**|
|---|---|---|---|---|---|
|F1-T16|Bind|Authorized insurer process|Bind the accepted, condition-<br>satisfied transaction.|Policy reaches BOUND with the<br>contractual terms captured; bind<br>is distinct from issuance.|Critical|
|F1-T17|Issue|Quote & Policy / Document|Issue formal policy documents<br>for the bound transaction.|Policy becomes the authoritative<br>in-force contract with policy<br>number, effective dates,<br>coverages, limits, deductibles<br>and premium; issue evidence is<br>retained.|Critical|
|F1-T18|Policy servicing|Policyholder / Quote & Policy|Request a non-material policy<br>update during the term.|A controlled servicing<br>transaction/endorsement is<br>created; original policy history<br>remainspreserved.|High|
|F1-T19|Material change referral|Policyholder / Underwriter|Request a material exposure<br>change, such as adding a second<br>location.|Change is treated as a policy<br>transaction and can trigger<br>underwriting/risk review rather<br>than silently changing the existing<br>contract.|Critical|
|F1-T20|Renewal|Underwriter / Policyholder|Start renewal using current<br>exposure and any term<br>claims/changes.|Renewal is a distinct transaction<br>with current information;<br>outcome can be renewed,<br>changed, referred or non-<br>renewed.|Critical|



## **FLOW 2 — In-Force Policy → Loss → Claim → Recovery → Settlement → Renewal** 

##### **BUSINESS OBJECTIVE** 

Demonstrate the full post-loss lifecycle, including FNOL, coverage, investigation, reserve, vendor work, business recovery, settlement/payment, recovery and renewal. 

|**Item**|**Definition**|
|---|---|
|Entrycriteria|An activepolicyexists with a loss occurringduringthepolicy period;the loss haspotential covered impact.|
|Exit criteria|Claim is properly closed after required claim actions; recovery work is complete or governed; renewal<br>evaluates the updated risk.|
|Primary roles|Policyholder; Underwriter; Risk Engineer; Claims Adjuster/Manager; Vendor Manager; Service<br>Provider/Contractor;System Administrator as applicable|
|Services traversed|Customer & Party; Quote & Policy; Risk & Underwriting; Claims; Vendor & Partner; Recovery & Business<br>Continuity;Workflow & Notification;Document & Audit;Analytics optional and non-authoritative|



### **Business Flow** 

|**Step**|**Business event / action**|**Expected state / outcome**|**Primary owner**|
|---|---|---|---|
|1|Policy already in force|A valid policy exists with relevant<br>property/business-income coverage.<br>f|Quote & Policy|
|2|Loss occurs|Real-world loss event affects insured|Policyholder|
||||IntelliSure | Business Lifecycle Test Cases | 5|



###### **INTELLISURE | BUSINESS QA BLUEPRINT** 

|**Step**|**Business event / action**|**Expected state / outcome**|**Primary owner**|
|---|---|---|---|
|||operations/property.||
|3|FNOL<br>i|Loss date, location, narrative, estimate, immediate<br>actions and evidence are captured.|Claims|
|4|Coverage verification|Policy is checked for in-force status and applicable<br>coverage/terms.|Claims|
|5|Investigation|Cause, damage, liability, evidence and recovery<br>opportunities are investigated.|Claims Adjuster|
|6|Reserve + assessment|Total loss, covered loss and expected payment are<br>distinguished;reserve evolves.|Claims|
|7|Repair/vendor|Eligible service provider is matched and assigned<br>where required.|Vendor & Partner / Workflow|
|8|Business recovery|Continuity actions, repair milestones and reopening<br>plan are tracked.|Recovery & Business Continuity|
|9|Settlement + payment|Authorized settlement is determined; payment is<br>separatelytracked.|Claims Manager / Claims|
|10|Salvage/subrogation|Potential recovery from damaged property or<br>responsible thirdpartyispursued where applicable.|Claims|
|11|Closure + renewal|Claim closes when required work/evidence is<br>complete; renewal later uses updated<br>loss/exposure history.|Claims / Quote & Policy / Underwriting|



### **Test Cases** 

|**TC ID**|**Lifecycle stage**|**Role**|**Business test / action**|**Expected business outcome**|**Priority**|
|---|---|---|---|---|---|
|F2-T01|Loss event|Policyholder|Report a fire affecting insured<br>property during the active policy<br>period.|Loss becomes a candidate claim<br>event; customer receives<br>immediate next-step guidance to<br>protect property and preserve<br>evidence.|Critical|
|F2-T02|FNOL|Claims Adjuster|Open FNOL with loss date,<br>location, policy, narrative, initial<br>estimate and evidence.|A claim case is created with<br>traceable FNOL information;<br>FNOL is distinguishable from the<br>ongoingclaim lifecycle.|Critical|
|F2-T03|FNOL validation<br>i|Claims|Attempt to report a future-dated<br>loss.|FNOL is rejected or corrected<br>because loss date must be<br>plausible and not future-dated.|High|
|F2-T04|Policy verification|Claims|Validate the policy was in force<br>on the loss date and inspect<br>relevant coverage.|Claims uses authoritative<br>policy/coverage information from<br>Quote & Policy rather than<br>maintaining a competing policy<br>master.|Critical|
|F2-T05|Evidence request|Claims|Request photos, inventory, proof<br>of loss, repair bids and relevant<br>business records.|Claim enters an evidence-driven<br>handling path and outstanding<br>documents are visible as work.<br>i|High|
|F2-T06|Investigation|Claims Adjuster|Investigate cause,<br>circumstances,<br>ownership/interest, damage and<br>potential third-party|Investigation findings are<br>recorded; potential recovery<br>paths are identified.|Critical|



IntelliSure | Business Lifecycle Test Cases | 6 

###### **INTELLISURE | BUSINESS QA BLUEPRINT** 

|**TC ID**|**Lifecycle stage**|**Role**|**Business test / action**<br>responsibility|**Expected business outcome**|**Priority**|
|---|---|---|---|---|---|
|F2-T07|Coverage decision|Claims Adjuster / Manager|.<br>Determine whether the loss is<br>covered, partially covered or<br>denied underpolicyterms.|An explicit coverage decision and<br>rationale exist, separate from<br>generic claim status.|Critical|
|F2-T08|Exclusions / deductible|Claims Adjuster|Apply applicable exclusion,<br>deductible, limit or sublimit<br>during claim evaluation.|Covered loss and payable<br>amount reflect policy terms; total<br>physical loss is not automatically<br>equal topayout.|Critical|
|F2-T09|Reserve|Claims Adjuster|Set an initial expected payment<br>amount and update it as facts<br>evolve.|Reserve evolves separately from<br>settlement/approved amount and<br>remains auditable.|Critical|
|F2-T10|Assessment|Claims Adjuster|Record detailed<br>damage/financial assessment.|Claim assessment distinguishes<br>total assessed loss from covered<br>loss and supports settlement<br>calculation.|Critical|
|F2-T11|Vendor eligibility|Vendor Manager|Attempt assignment to an<br>unverified/inactive contractor.|Assignment is blocked or<br>rerouted; only eligible providers<br>can receive operational work.|Critical|
|F2-T12|Vendor assignment|Vendor Manager|Assign an eligible repair<br>contractor to restore damaged<br>equipment/property.|A vendor assignment exists with<br>service scope, responsible<br>provider and operational status.|High|
|F2-T13|Vendor decline|Service Provider|Contractor declines the assigned<br>job.|Job returns to controlled<br>reassignment/escalation rather<br>than becominglost.|High|
|F2-T14|Recovery case|Workflow / Recovery|Create a business continuity<br>case for temporary location,<br>equipment and reopening.|Recovery case contains<br>operational objectives and<br>milestones; it does not imply<br>coverage entitlement byitself.|Critical|
|F2-T15|Recovery progress|Service Provider / Recovery|Update repair/reopening<br>milestones with completion<br>evidence.|Recovery progress is visible and<br>evidence-backed; overdue<br>milestones can trigger workflow<br>escalation.|High|
|F2-T16|Settlement|Claims Manager|Approve a settlement after<br>investigation and assessment.|Claim records an authorized<br>settlement outcome distinct from<br>the reserve and payment<br>transaction.|Critical|
|F2-T17|Payment|Claims|Record payment issued against<br>the approved settlement.|Payment status is separately<br>represented; approved<br>settlement is not incorrectly<br>treated aspayment completion.|Critical|
|F2-T18|Salvage / subrogation|Claims|Identify damaged equipment with<br>recoverable value and a third<br>party potentially responsible for<br>the loss.|Salvage/subrogation work is<br>opened and tracked separately<br>from the insured payment.|High|
|F2-T19|Closure gate|Claims Manager|Attempt to close claim while<br>mandatory<br>evidence/payment/recovery<br>actions remain incomplete.|Closure is blocked or flagged<br>until required actions are<br>complete or formally waived.|Critical|
|F2-T20|Renewal after claim|Underwriter|Begin renewal after a significant|Renewal uses current loss|Critical|



IntelliSure | Business Lifecycle Test Cases | 7 

###### **INTELLISURE | BUSINESS QA BLUEPRINT** 

|**TC ID**|**Lifecycle stage**|**Role**|**Business test / action**|**Expected business outcome**|**Priority**|
|---|---|---|---|---|---|
||||claim and updated business|history, operations and risk||
||||exposure.|controls; the prior policy is not<br>simplycopied forward.||



## **FLOW 3 — Exception / Referral → Conditional Acceptance → Material Change → Non-Renewal** 

##### **BUSINESS OBJECTIVE** 

Demonstrate realistic insurance exceptions: missing information, referral, specialist risk engineering, subjectivities, material policy change, reassessment and non-renewal. 

|**Item**|**Definition**|
|---|---|
|Entrycriteria|Complex or incomplete submission requiringhuman intervention andpotentiallyspecialist risk engineering.|
|Exit criteria|The transaction is either correctly issued with conditions or correctly ends through a controlled non-<br>renewal/declinepath with evidence.|
|Primary roles|Policyholder; Underwriter; Risk Engineer; Claims Adjuster/Manager; Vendor Manager; Service<br>Provider/Contractor;System Administrator as applicable|
|Services traversed|Customer & Party; Quote & Policy; Risk & Underwriting; Claims; Vendor & Partner; Recovery & Business<br>Continuity;Workflow & Notification;Document & Audit;Analytics optional and non-authoritative|



### **Business Flow** 

|**Step**|**Business event / action**|**Expected state / outcome**|**Primary owner**|
|---|---|---|---|
|1|Complex/incomplete submission|Submission has material missing information or<br>complexity.|Policyholder / Workflow|
|2|Needs information|Customer/document tasks are created with a clear<br>owner and due date.|Workflow|
|3|Referral|Risk requires specialist/manual underwriting<br>authority.|Risk & Underwriting|
|4|Risk engineering|Site/operations hazards and controls are assessed.|Risk Engineer|
|5|Conditional outcome|Underwriter may issue conditions/subjectivities or<br>decline.|Underwriter|
|6|Policyin force if accepted|Conditions are satisfied andpolicyis bound/issued.|Quote & Policy|
|7|Material change / continuous risk|Exposure worsens or operations change materially.|Policyholder / Risk & Underwriting|
|8|Renewal reassessment|Current exposure and claims are reviewed.|Underwriter|
|9|Non-renewal / decline|Authorized business decision ends continuation for<br>next term where rules support it.|Underwriter / Quote & Policy|
|10|Evidence + audit|Decision reasons, actor, timing and supporting<br>evidence arepreserved.|Document & Audit|



### **Test Cases** 

|**TC ID**<br>**Lifecycle stage**|**Role**|**Business test / action**|**Expected business outcome**|**Priority**|
|---|---|---|---|---|
|F3-T01<br>Submission|Policyholder|Submit a complex contractor|Case is accepted onlyas|Critical|



IntelliSure | Business Lifecycle Test Cases | 8 

###### **INTELLISURE | BUSINESS QA BLUEPRINT** 

|**TC ID**|**Lifecycle stage**|**Role**|**Business test / action**<br>i|**Expected business outcome**|**Priority**|
|---|---|---|---|---|---|
||||profile with incomplete exposure<br>information.|SUBMITTED/needs-processing;<br>missing information prevents<br>silentprogression to aquote.||
|F3-T02|Needs information|Workflow|Create a customer information<br>request for missing<br>operations/exposure details.|Workflow creates an accountable<br>task with due date and<br>notification; the task is not<br>confused with the underwriting<br>decision itself.|Critical|
|F3-T03|Customer response|Policyholder|Provide requested information<br>and supporting documents<br>before due date.|Submission returns to an eligible<br>review path; prior request<br>remains traceable.|High|
|F3-T04|Overdue escalation|Workflow|Allow an information task to pass<br>its SLA without response.|Workflow escalates the task<br>according to configured<br>escalation rules.|High|
|F3-T05|Underwriting referral|Underwriter|Identify a risk that exceeds<br>automated/standard authority or<br>requires specialist attention.|Referral is explicitly recorded with<br>reason/authority context; human<br>underwriting authority remains<br>accountable.|Critical|
|F3-T06|Risk engineering|Risk Engineer|Perform a specialist<br>hazard/control assessment for<br>the contractor operation.|Risk findings and<br>recommendations are recorded<br>with evidence and remain<br>separate from the contractual<br>policyrecord.|Critical|
|F3-T07|Mitigation requirement|Risk Engineer / Underwriter|Require corrective action before<br>binding, e.g., a specified<br>safety/control improvement.|Subjectivity/condition is linked to<br>the underwriting outcome and<br>remains outstanding until<br>verified.|Critical|
|F3-T08|Conditional quote|Underwriter|Offer terms subject to required<br>corrective actions.|i<br>Quote is QUOTED with explicit<br>conditions, expiry and terms;<br>conditions are visible to the<br>customer.|Critical|
|F3-T09|Condition not satisfied|Workflow / Underwriter|Attempt to proceed to bind<br>without verified corrective action.|Bind path is blocked or referred<br>to authorized exception handling.|Critical|
|F3-T10|Condition satisfied|Risk Engineer / Underwriter|Verify corrective action and<br>record completion evidence.|Subjectivity becomes<br>satisfied/closed and the<br>transaction is eligible for next<br>step.|Critical|
|F3-T11|Bind + issue|Authorized process|Bind and issue the accepted<br>conditional transaction.|Coverage is BOUND and formal<br>policy becomes IN_FORCE after<br>required issuance/effective<br>conditions.|Critical|
|F3-T12|Material exposure change|Policyholder|Add a materially different<br>operation/location during the<br>term.|Policy change becomes an<br>endorsement/service transaction<br>and triggers underwriting/risk<br>review when material.|Critical|
|F3-T13|Risk deterioration|Risk Engineer|Find a significant unresolved<br>hazard during follow-up.|New risk<br>finding/recommendation is<br>recorded; underwriting receives<br>the condition/reassessment|High|



IntelliSure | Business Lifecycle Test Cases | 9 

###### **INTELLISURE | BUSINESS QA BLUEPRINT** 

|**TC ID**|**Lifecycle stage**|**Role**|**Business test / action**|**Expected business outcome**|**Priority**|
|---|---|---|---|---|---|
|||||signal.||
|F3-T14|Evidence ownership|Document & Audit|Record the underwriting decision,<br>reason, actor and supporting<br>documents.|Evidence/audit records preserve<br>who changed what, when and<br>why; they do not replace the<br>domain decision itself.|High|
|F3-T15|Renewal review|Underwriter|Review current operations,<br>material changes, claims and<br>unresolved risk findings for<br>renewal.|Renewal starts as a fresh<br>evaluation rather than date<br>extension.|Critical|
|F3-T16|Renewal quote change|Underwriter|Offer materially different next-<br>term terms after reassessment.<br>f|Renewal transaction contains<br>proposed terms and a distinct<br>customer decisionpath.|High|
|F3-T17|Customer declines renewal<br>terms|Policyholder|Decline the renewal offer.|Renewal transaction records<br>customer decision; current term<br>remains governed by its existing<br>lifecycle.|High|
|F3-T18|Non-renewal decision|Underwriter|Make an authorized non-renewal<br>decision for the next term.|Non-renewal outcome and<br>business reason are recorded;<br>the system does not mislabel it<br>as a mid-term cancellation.|Critical|
|F3-T19|Access after end of term|Policyholder|Attempt to use the expired/non-<br>renewed policy as if it were<br>active.|Business flow recognizes that the<br>prior policy is no longer<br>IN_FORCE; no new coverage is<br>implied.|Critical|
|F3-T20|Audit completeness|System Administrator / Auditor|Review the end-to-end exception<br>case history.|Key state changes, actors,<br>reasons and supporting evidence<br>are traceable across submission,<br>underwriting, risk engineering and<br>renewal outcomes.|High|



## **4. Cross-Flow Business Invariants** 

|**ID**|**Business invariant**|**Pass condition**|
|---|---|---|
|BI-01|A Quote is not a Policy.|A submitted/quoted transaction must not be represented as an in-<br>force insurance contract.|
|BI-02|Submission is richer than a price request.|Insurance need/narrative and structured business/exposure<br>information are captured before underwriting.|
|BI-03|Acceptance is not the same as Bind.|Customer agreement and insurer/authorized binding remain distinct<br>business moments even if orchestrated together.|
|BI-04|Bind is not the same as Issue.|Coverage can be bound before formalpolicydocuments are issued.|
|BI-05|Policy is authoritative for coverage.|Claims uses Quote & Policy as the source of truth for policy terms and<br>coverage.|
|BI-06|Workflow owns work, not domain truth.|Workflow can assign a task and escalate it but does not decide<br>underwriting,claim coverage orpolicystatus.|
|BI-07|Underwriter owns underwriting authority.|Risk scores, analytics and risk findings support the decision but do not<br>silentlyreplace authorized human business authority.|



IntelliSure | Business Lifecycle Test Cases | 10 

**INTELLISURE | BUSINESS QA BLUEPRINT** 

|**ID**|**Business invariant**|**Pass condition**|
|---|---|---|
|BI-08|Risk Engineer is not the Underwriter.|Risk Engineering assesses hazards/controls and recommends actions;<br>underwritingdecides acceptability/terms.<br>f      i|
|BI-09|Estimated loss is not payout.|Initial estimates can differ from assessed covered loss and final<br>settlement/payment.|
|BI-10|Reserve is not settlement.|Reserve is the evolving expected payment liability; settlement is an<br>authorized outcome.|
|BI-11|Settlement is notpayment.|An approved settlement must have a separatepayment lifecycle/state.|
|BI-12|Recovery is not coverage determination.|Recovery tracks operational restoration and continuity; Claims<br>determines claim coverage/payment.|
|BI-13|Vendor eligibility precedes assignment.|Onlyverified/eligibleproviders should receive repair/service work.|
|BI-14|Endorsements preserve history.|Material policy changes are recorded as transactions rather than<br>silentlyoverwritinghistorical terms.|
|BI-15|Renewal is fresh evaluation.|Current exposure, claims, material changes and risk controls are used<br>for the next-term decision.|
|BI-16|Closure has gates.|A claim should not close while required evidence, settlement/payment<br>or governed recovery actions remain unresolved without formal<br>disposition.|
|BI-17|Documents are evidence.|Document & Audit preserves evidence and history but does not<br>become the owner of the business decision.|
|BI-18|Customer identity is centralized.|Customer/party identity is not duplicated as a competing master in<br>downstream services.|



## **5. Recommended Execution Order for Your Demo** 

|**Order**|**Flow**|**What to demonstrate**|**Must-pass business checkpoints**|
|---|---|---|---|
|1|Flow 1|New business from registration to renewal setup.|SUBMITTED → triage → underwriter assigned → risk<br>assessment → QUOTED → ACCEPTED → BOUND →<br>IN_FORCE|
|2|Flow 2|Real loss and claim handling.|FNOL → coverage decision → investigation →<br>reserve/assessment → vendor → recovery →<br>settlement →payment → closure|
|3|Flow 3|Exception path.|NEEDS_INFORMATION → referral → risk engineering<br>→ subjectivity → conditional terms → bind/issue →<br>material change → renewal/non-renewal|



##### **WHAT THIS SUITE IS TESTING** 

Not just whether endpoints return 200/201. It verifies that IntelliSure preserves the meaning of insurance work: who owns the decision, what the current business state means, what evidence is required, what can move to the next stage, and what must be blocked. 

IntelliSure | Business Lifecycle Test Cases | 11 

###### **INTELLISURE | BUSINESS QA BLUEPRINT** 

## **6. Source Basis** 

- IntelliSure Insurance Lifecycle Business Blueprint — September 2026 (project source). 

- IntelliSure Microservice Business Definitions — September 2026 (project source). 

- The underlying project entities, DTOs, endpoints, service-to-service communication and user-journey artifacts were treated as the current implementation baseline where relevant. 

- The terminology and lifecycle model intentionally follow the business vocabulary established in the project blueprint, including submission, triage, underwriting review, risk engineering, subjectivities, quote, acceptance, bind, issue, endorsements, FNOL, reserve, settlement, payment, recovery, salvage/subrogation and renewal. 

IntelliSure | Business Lifecycle Test Cases | 12 

