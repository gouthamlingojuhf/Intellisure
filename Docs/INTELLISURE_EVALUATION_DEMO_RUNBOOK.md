# IntelliSure Enterprise Demo Runbook & Insurance Lifecycle Guide
**The Official Step-by-Step Evaluation Presentation Manual**

---

## 1. Executive Summary & Evaluator Cheat Sheet

### 1.1 What is IntelliSure?
**IntelliSure** is an enterprise-grade commercial insurance platform purpose-built for small and medium-sized businesses. It automates and governs the entire commercial insurance lifecycle:
1. **Customer Onboarding & Quoting:** Self-service commercial package quote submission.
2. **Risk Engineering & Loss Prevention:** Physical site safety inspections, risk scoring (0–100), and hazard mitigation recommendations.
3. **Underwriting Governance:** Exposure analysis, appetite checks, tailored deductible/limit rating, and binding quotes.
4. **Policy Contract Servicing:** Instant policy issuance, digital endorsements, and audit documentation.
5. **Claims Operations:** First Notice of Loss (FNOL) filing, automated adjuster assignment, reserve accounting, and coverage determination.
6. **Financial Governance & Settlement:** Multi-tier authorization, payment disbursement, and settlement releases.
7. **Business Recovery & Network Contractors:** Policyholder-driven recovery choices, network vendor dispatch, and repair tracking.
8. **Omnichannel Collaboration:** Real-time contextual chat, unread badge notification, and stacked audit events.

---

### 1.2 System Architecture Overview
The platform runs on a **Micro-Frontend (MFE)** and **Microservices** architecture:
* **Frontend:** Angular 17 with Module Federation (`shell-app`, `claims-mfe`, `vendor-mfe`, `intelligence-mfe`, `auth-mfe`), running on `http://localhost:4200`.
* **API Gateway:** Spring Cloud Gateway on `http://localhost:8080` (handles JWT propagation, routing, and rate limiting).
* **Service Discovery:** Netflix Eureka on `http://localhost:8761`.
* **9 Backend Microservices:**
  * `customer-party-service` (`:8081`): User accounts, roles, and business customer profiles.
  * `quote-policy-service` (`:8082`): Submissions, rating, terms, and policy contracts.
  * `risk-underwriting-service` (`:8083`): Engineering inspections, risk scoring, and rule evaluation.
  * `claims-service` (`:8084`): FNOL, adjusters, reserves, coverage decisions, and disbursements.
  * `vendor-partner-service` (`:8085`): Network vendor profiles, onboarding, and assignments.
  * `recovery-service` (`:8086`): Policyholder recovery choices, salvage, and subrogation.
  * `workflow-notification-service` (`:8087`): Notification stacking and real-time chat.
  * `document-audit-service` (`:8088`): Storage, SHA-256 integrity, and document registry.
  * `analytics-intelligence-service` (`:8089`): Operational metrics and loss ratios.
* **Database:** MySQL 8.4 with dedicated relational databases per microservice.

---

### 1.3 Master Credentials Directory
All pre-seeded employee and vendor accounts share the standard password: **`Password@123`**.

| Role Name | User Email | Password | Primary Platform Responsibility |
| :--- | :--- | :--- | :--- |
| **Policyholder** | *(Register fresh in Step 1)* | *(Chosen during demo)* | Requests quotes, binds contracts, files claims, selects recovery path. |
| **Underwriter 1** | `underwriter1@intellisure.com` | `Password@123` | Evaluates applications, reviews risk reports, and issues bound quote offers. |
| **Underwriter 2** | `underwriter2@intellisure.com` | `Password@123` | Peer underwriter for reassignments and secondary reviews. |
| **Risk Engineer 1** | `riskengineer1@intellisure.com` | `Password@123` | Inspects hazards, calculates risk score, and issues safety recommendations. |
| **Risk Engineer 2** | `riskengineer2@intellisure.com` | `Password@123` | Alternate risk engineer for inspection load-balancing. |
| **Claims Adjuster 1** | `claimsadjuster1@intellisure.com` | `Password@123` | Investigates losses, checks linked policies, sets reserves, and decides coverage. |
| **Claims Adjuster 2** | `claimsadjuster2@intellisure.com` | `Password@123` | Peer adjuster for multi-claim balancing. |
| **Claims Manager 1** | `claimsmanager1@intellisure.com` | `Password@123` | High-value governance, payout authorization, and financial disbursement. |
| **Vendor Manager 1** | `vendormanager1@intellisure.com` | `Password@123` | Accredits contractors and oversees service provider networks. |
| **Network Vendor 1** | `vendor1@intellisure.com` | `Password@123` | Emergency restoration contractor; accepts jobs and logs repair milestones. |
| **System Administrator** | `admin1@intellisure.com` | `Password@123` | Workspace oversight, workload reassignment, and platform audit. |

---

### 1.4 Pre-Demo Presenter Setup Checklist
1. **Clean Slate Guarantee:** The system is freshly reset with zero stale transactional clutter.
2. **Browser Recommendation (Pro Tip):**
   * **Window 1 (Standard):** Keep open for the **Policyholder** (`balaji@acmefabrication.com`).
   * **Window 2 (Incognito / Private):** Use for logging in as **Employees** (`riskengineer1`, `underwriter1`, `claimsadjuster1`, `claimsmanager1`, `admin1`).
   * *This avoids having to constantly clear session tokens while demonstrating role handoffs.*
3. **Locale & Currency Standards:**
   * **Currency:** Formatted universally in Indian Rupees (**`₹`**).
   * **Timezone:** Timestamps render in Indian Standard Time (**IST / +05:30**).

---

## 2. The Golden Path: One Complete Insurance Lifecycle

### Storyline: The Journey of *Acme Fabrication Works*
To deliver an engaging presentation, follow this coherent commercial insurance story:
> **Balaji**, owner of **Acme Fabrication Works** (a precision metal parts manufacturer in Peenya Industrial Area), applies for a commercial insurance policy. His facility undergoes a risk engineering inspection, receives underwriting approval, and binds the policy. Later, an accidental hydraulic oil pipe burst causes machinery fire damage. Balaji files a claim, an adjuster investigates and approves coverage, a claims manager disburses the settlement payment in rupees, and an accredited emergency restoration vendor repairs the facility to full operation.

---

### Phase 1: Customer Onboarding & Business Profile

#### Step 1: Policyholder Registration & Sign-In
* **Actor:** Balaji (Small Business Owner).
* **Navigation:** `http://localhost:4200/login` &rarr; Click **"Create Account"** or click direct register.
* **Action Steps:**
  1. Fill in the registration form:
     * **Full Name:** `Balaji Lingoju`
     * **Email Address:** `balaji@acmefabrication.com`
     * **Password:** `Password@123`
     * **Confirm Password:** `Password@123`
  2. Click **"Register as Policyholder"**.
  3. Sign in immediately with `balaji@acmefabrication.com` and `Password@123`.
* **Plain English Explanation:** Commercial insurance requires strong identity authentication before commercial underwriting begins.
* **Visual Checkpoint:** The landing screen redirects to the Customer Workspace with the welcome banner and empty records prompt.

---

#### Step 2: Establish Business Customer Profile
* **Actor:** Policyholder.
* **Navigation:** Click **"🏢 Business Profile"** in the sidebar (or navigate to `http://localhost:4200/profile`).
* **Action Steps:**
  1. Fill in the corporate profile fields:
     * **Legal Business Name:** `Acme Precision Fabrication Works Pvt Ltd`
     * **Trade / DBA Name:** `Acme Fabrication`
     * **Registration Number (CIN / GST):** `U29100KA2026PTC098765`
     * **Industry Sector:** `MANUFACTURING`
     * **Primary Business Address:** `Plot 42, 4th Phase, Peenya Industrial Area, Bengaluru, KA 560058`
     * **Primary Phone:** `+91 98450 12345`
     * **Annual Turnover / Revenue:** `₹45,00,000`
     * **Employee Headcount:** `28`
  2. Click **"Save Business Profile"**.
* **Plain English Explanation:** Insurers require verified corporate legal identities and industry codes to classify risk appetites and evaluate business liability.
* **Visual Checkpoint:** A green success notification confirms *"Business profile saved successfully"*. The customer ID is generated in the background.

---

### Phase 2: Commercial Quote Application

#### Step 3: Configure and Submit Quote Application
* **Actor:** Policyholder.
* **Navigation:** Click **"📋 Quotes"** in sidebar &rarr; Click **"Request New Quote"** (`http://localhost:4200/quotes/new`).
* **Action Steps:**
  1. **Product Line:** Select **"Business Owner's Policy (BOP)"**.
  2. **Requested Coverages:** Select all three core packages:
     * `☑ Building & Real Property Protection`
     * `☑ Business Personal Property & Machinery (BPP)`
     * `☑ Business Interruption Loss (BI)`
  3. **Effective Date:** Select a date starting next week (e.g. `2026-11-01`).
  4. **Property Valuation:**
     * **Building Replacement Value:** `₹2,50,00,000`
     * **Machinery & Contents Value:** `₹85,00,000`
  5. Click **"Save Draft Quote"**. The screen updates to show Quote Number (e.g. `QT-2026-XXXX`).
  6. Click **"Submit Application for Review"**.
* **Plain English Explanation:** The small business owner selects the perils they want protected against and submits the proposal to the underwriting team.
* **Visual Checkpoint:**
  * Quote status badge transitions to **`SUBMITTED`**.
  * A banner announces: *"Quote submitted successfully. Assigned for risk evaluation."*
  * Take note of the **Quote Number** (e.g. `QT-2026-XXXX`) or Quote ID for the next steps.

---

### Phase 3: Risk Engineering Inspection & Loss Prevention

#### Step 4: Risk Engineer Conducts Site Hazard Assessment
* **Actor:** Risk Engineer (`riskengineer1@intellisure.com` / `Password@123`).
* **Navigation:** Sign in using an Incognito tab &rarr; Click **"📋 Quotes"** in sidebar &rarr; Select the submitted quote (`QT-2026-XXXX`).
* **Action Steps:**
  1. Click **"Risk Engineering Assessment"** tab or section.
  2. The system indicates the risk assessment is in draft/assigned status.
  3. Enter the site safety evaluation:
     * **Construction Class:** `FIRE_RESISTIVE`
     * **Fire Protection System:** `AUTOMATIC_SPRINKLERS`
     * **Housekeeping & Maintenance Rating:** `GOOD`
     * **Electrical Switchgear Status:** `VERIFIED_CIRCUIT_BREAKERS`
     * **Calculated Risk Score:** Enter `78` (Scale 0–100, where higher represents superior safety controls).
     * **Risk Band:** Select Band **`B`** (Moderate Low Risk).
  4. **Add Risk Recommendations:**
     * Recommendation: *"Install localized class-B dry chemical extinguishers near hydraulic oil pump stations."*
     * Priority: `HIGH`
     * Target Completion Date: `2026-11-15`
  5. Click **"Complete Risk Assessment & Recommendations"**.
* **Plain English Explanation:** Before insuring high-value industrial machinery, the insurer sends an engineer to inspect fire hazards, maintenance protocols, and safety cut-offs.
* **Visual Checkpoint:**
  * Assessment Status changes to **`COMPLETED`** / **`EVALUATED`**.
  * The risk band **`B (Score: 78/100)`** is recorded.
  * A notification event `RISK_ASSESSMENT_COMPLETED` is logged into the audit ledger.

---

### Phase 4: Underwriting Rating & Terms Offering

#### Step 5: Underwriter Evaluates Application & Offers Terms
* **Actor:** Underwriter (`underwriter1@intellisure.com` / `Password@123`).
* **Navigation:** Sign in as `underwriter1@intellisure.com` &rarr; Go to **"📋 Quotes"** &rarr; Open `QT-2026-XXXX`.
* **Action Steps:**
  1. Review the Underwriting Workbench:
     * Review the **Risk Engineer's Score (78/100, Band B)** and the fire suppression recommendation.
     * Note that the business fits within commercial underwriting appetite.
  2. Click **"Prepare Quote Terms & Pricing"** (or **"Offer Terms"**).
  3. Specify the final terms:
     * **Building Coverage Limit:** `₹2,50,00,000` (Deductible: `₹50,000`, Premium: `₹24,500`)
     * **Business Personal Property Limit:** `₹85,00,000` (Deductible: `₹25,000`, Premium: `₹14,200`)
     * **Business Interruption Limit:** `₹30,00,000` (Waiting Period: `48 Hours`, Premium: `₹6,800`)
     * **Total Annual Written Premium:** Auto-calculated at **`₹45,500`**.
  4. Click **"Issue Quote Offer to Customer"**.
* **Plain English Explanation:** The underwriter determines that the risk is acceptable with appropriate deductibles, calculates the annual premium, and issues a formal legal offer to the client.
* **Visual Checkpoint:**
  * Quote status badge transitions from `SUBMITTED` &rarr; **`OFFERED`**.
  * Total Premium prominently displays in INR: **`₹45,500`**.
  * The underwriter assignment shows `Underwriter One`.

---

#### Step 6 (Optional Demo Highlight): Admin Workload Governance
* **Actor:** System Administrator (`admin1@intellisure.com` / `Password@123`).
* **Navigation:** Go to **"📋 Quotes"** &rarr; Notice the **Assigned Underwriter** column on the quotes list.
* **Action Steps:**
  1. Click **"Reassign"** on the quote row.
  2. Select `Underwriter Two (underwriter2@intellisure.com)` or back to `Underwriter One`.
  3. Click **"Confirm Reassignment"**.
* **Plain English Explanation:** Demonstrates administrative oversight and load-balancing across regional underwriting desks.
* **Visual Checkpoint:** The table updates dynamically without page reloads, showing the new underwriter name.

---

### Phase 5: Policy Acceptance, Binding & Official Documents

#### Step 7: Policyholder Reviews & Accepts Quote Terms
* **Actor:** Balaji (Policyholder in Window 1).
* **Navigation:** Refresh or open **"📋 Quotes"** &rarr; Click on `QT-2026-XXXX`.
* **Action Steps:**
  1. Point out the real-time notification badge on the sidebar: **`🔔 Notifications (1)`** with the message *"Underwriter has issued formal terms for quote QT-2026-XXXX"*.
  2. Review the coverage schedule: limits, deductibles, and annual premium of `₹45,500`.
  3. Click the green **"Accept & Bind Policy"** button.
  4. Confirm the prompt: *"Confirm commercial policy binding effective from 2026-11-01"*.
* **Plain English Explanation:** The small business owner agrees to the proposed coverages and premium, executing the binding insurance contract.
* **Visual Checkpoint:**
  * The quote status updates to **`BOUND`**.
  * The screen presents a link to the generated Policy Contract: **`POL-2026-XXXX`**.
  * Click **"View Bound Policy Contract &rarr;"** (or navigate to `http://localhost:4200/policy`).

---

#### Step 8: Verify Active Policy & Document Repository in `/docs`
* **Actor:** Policyholder.
* **Navigation:** Click **"📁 Documents"** in the sidebar (`http://localhost:4200/docs`).
* **Action Steps:**
  1. Show the evaluation team the **Enterprise Insurance Document Register**:
     * **Stat Counters:** Displays total documents, policies, quotes, claims, and risk reports.
     * **Category Filter Tabs:** Click **"Policies"**, **"Quotes"**, and **"Risk Reports"**.
  2. Highlight the generated documents:
     * `Policy Contract Declaration — POL-2026-XXXX.pdf` (Category: `POLICIES`, Status: `ACTIVE`)
     * `Commercial Underwriting Proposal — QT-2026-XXXX.pdf` (Category: `QUOTES`, Status: `BOUND`)
     * `Risk Engineering Assessment & Recommendations — QT-2026-XXXX.pdf` (Category: `RISK`, Status: `EVALUATED`)
  3. Click **"Print PDF"** on the Policy Declaration row.
  4. A pop-up generates the official IntelliSure declaration document with policy limits, audit ID, and corporate signature header.
* **Plain English Explanation:** Regulations require policyholders to have immediate access to policy declarations, coverage endorsements, and inspection reports.
* **Visual Checkpoint:** Every operational stage automatically synthesized legal records without manual uploading.

---

### Phase 6: Loss Incident & First Notice of Loss (FNOL)

#### Step 9: Policyholder Reports Loss Incident
* **Actor:** Balaji (Policyholder).
* **Navigation:** Click **"📄 Claims"** in sidebar &rarr; Click **"File First Notice of Loss (FNOL)"** (or click **"Report a Claim"** directly from the policy screen).
* **Action Steps:**
  1. **Select Policy:** Choose the active contract **`POL-2026-XXXX`**.
  2. **Incident Details:**
     * **Incident Date & Time:** Select yesterday's date at `14:30 IST`.
     * **Loss Location:** `Peenya Industrial Area, Fabrication Bay 2`.
     * **Loss Type:** Select **`FIRE`** (or `PROPERTY_DAMAGE`).
     * **Estimated Gross Loss:** Enter `₹3,50,000`.
     * **Incident Summary:**
       > *"A high-pressure hydraulic pipe on the stamping press ruptured, spraying oil onto the heating element and causing a localized flash fire. Two computer control panels and inventory pallets were destroyed before factory extinguishers suppressed the flames."*
  3. Click **"Submit Claim (FNOL)"**.
* **Plain English Explanation:** The policyholder alerts the insurance company immediately after an incident occurs so emergency assistance and loss assessments can begin.
* **Visual Checkpoint:**
  * System allocates an official Claim ID (e.g. `CLM-2026-XXXX`).
  * Claim status displays as **`OPEN`** / **`FNOL_RECEIVED`**.
  * The system automatically assigns a Claims Adjuster to the file.

---

### Phase 7: Claims Investigation & Coverage Decision

#### Step 10: Adjuster Investigates Loss & Establishes Initial Reserve
* **Actor:** Claims Adjuster (`claimsadjuster1@intellisure.com` / `Password@123`).
* **Navigation:** Sign in as `claimsadjuster1@intellisure.com` in Incognito &rarr; Click **"📄 Claims"** &rarr; Select `CLM-2026-XXXX`.
* **Action Steps:**
  1. Point out the **Linked Policy Contract Card** at the top of the claim view:
     * Displays Policy Number `POL-2026-XXXX` (clickable), Product Line `BOP`, Annual Premium `₹45,500`, and Term Period.
  2. Note the workbench explanation banners:
     * *Reserve:* Explains why the insurer must set aside funds for statutory solvency.
     * *Assessment:* Explains damage inspection protocols.
     * *Coverage Decision:* Explains the legal distinction between approval and denial.
  3. **Set Financial Reserve:**
     * Under Financial Governance, enter Initial Reserve: **`₹3,00,000`**.
     * Reason: *"Initial physical damage assessment on 2 damaged CNC controller modules."*
     * Click **"Set Financial Reserve"**.
* **Plain English Explanation:** An insurance company is legally mandated to set aside money (reserves) in a dedicated bank fund the moment an incident is verified.
* **Visual Checkpoint:** The financial summary card updates: Incurred Reserve shows **`₹3,00,000`**.

---

#### Step 11: Adjuster Records Formal Coverage Decision
* **Actor:** Claims Adjuster (`claimsadjuster1@intellisure.com`).
* **Navigation:** On the same claim detail page (`CLM-2026-XXXX`).
* **Action Steps:**
  1. Scroll to the **Coverage Decision** panel.
  2. Select Decision: **`APPROVED`**.
  3. Select Coverage Line: `BPP - Business Personal Property & Machinery`.
  4. Calculation Breakdown:
     * **Gross Evaluated Loss:** `₹3,20,000`
     * **Applied Deductible:** `₹25,000`
     * **Net Approved Payout:** `₹2,95,000`
  5. Enter Adjuster Rational:
     * *"Investigation confirms fire originated from sudden mechanical failure of hydraulic pipe. No exclusions apply. Damage verified via maintenance logs. Settlement approved."*
  6. Click **"Record Coverage Decision"**.
* **Plain English Explanation:** The adjuster reviews the policy terms, confirms that fire damage to machinery is covered, subtracts the policyholder's deductible, and officially approves the claim.
* **Visual Checkpoint:**
  * Claim status updates to **`APPROVED`**.
  * A green banner confirms *"Coverage decision APPROVED recorded successfully"*.

---

### Phase 8: Financial Governance & Settlement Disbursement

#### Step 12: Claims Manager Authorizes & Disburses Settlement Payment
* **Actor:** Claims Manager (`claimsmanager1@intellisure.com` / `Password@123`).
* **Navigation:** Sign in as `claimsmanager1@intellisure.com` &rarr; Go to **"📄 Claims"** &rarr; Open `CLM-2026-XXXX`.
* **Action Steps:**
  1. Point out the **Claims Manager Governance & Settlement Release** action panel.
  2. Enter payment disbursement details:
     * **Settlement Amount:** `₹2,95,000`
     * **Payment Reference / UTR:** `NEFT-SBI-2026-981240`
     * **Disbursement Channel:** `BANK_TRANSFER_NEFT`
  3. Click **"Authorize & Release Payment"**.
* **Plain English Explanation:** Senior claims managers operate as a secondary financial control tier, ensuring adjusters did not exceed authorization limits, releasing funds directly to the customer's account.
* **Visual Checkpoint:**
  * The payout amount updates to **`₹2,95,000`**.
  * Claim status transitions from `APPROVED` &rarr; **`SETTLED`**.
  * The button **"Close Claim File"** is now enabled (previously locked to prevent closing unliquidated claims).

---

### Phase 9: Business Recovery & Network Contractors

#### Step 13: Policyholder Selects Recovery Path & Chooses Vendor
* **Actor:** Balaji (Policyholder in Window 1).
* **Navigation:** Click **"💰 Recovery"** in the sidebar (`http://localhost:4200/recovery`).
* **Action Steps:**
  1. Notice the active recovery workbench for claim `CLM-2026-XXXX`.
  2. Select Recovery Strategy: Choose **`NETWORK_VENDOR` (Accredited IntelliSure Partner Network)**.
     *(Explain to evaluators: The policyholder has full autonomy to choose Network Vendor, Customer's Own Contractor, or Customer-Managed Recovery).*
  3. View the list of accredited vendors:
     * **Apex Property Restoration** (Structural drying, soot abatement, commercial rebuild).
     * **AquaGuard Mitigation Services** (Water extraction, dehumidification).
     * **VoltPro Electrical Solutions** (Industrial switchgear, panel certification).
  4. Select **Apex Property Restoration**.
  5. Enter Work Scope: *"Industrial cleanup of hydraulic oil residue and decontamination of electrical bay."*
  6. Estimated Cost: `₹85,000`.
  7. Click **"Dispatch Network Vendor Request"**.
* **Plain English Explanation:** Commercial insurance goes beyond paying money—it helps business owners reopen quickly by connecting them to trusted repair contractors.
* **Visual Checkpoint:**
  * Recovery strategy is saved as `NETWORK_VENDOR`.
  * Vendor assignment status shows **`ASSIGNED`** / **`DISPATCHED`**.

---

#### Step 14: Network Vendor Accepts Job & Updates Progress
* **Actor:** Network Contractor (`vendor1@intellisure.com` / `Password@123`).
* **Navigation:** Sign in as `vendor1@intellisure.com` in Incognito &rarr; Open **"Vendor Workspace"**.
* **Action Steps:**
  1. Locate the incoming dispatch request for Acme Fabrication Works (`CLM-2026-XXXX`).
  2. Click **"Accept Assignment"**. Status changes to **`ACCEPTED`**.
  3. Enter mobilization update:
     * Target Arrival Date: Tomorrow morning at `09:00 IST`.
     * Note: *"Decontamination crew mobilized with high-efficiency degreasers."*
  4. Click **"Begin Work / In Progress"**.
* **Plain English Explanation:** The contractor accepts the job directly on the platform, providing transparent scheduling updates to both the insurer and the policyholder.
* **Visual Checkpoint:** Assignment status updates to **`IN_PROGRESS`**.

---

### Phase 10: Omnichannel Collaboration, Notifications & File Closure

#### Step 15: Cross-Role Chat with Real-Time Unread Indicator
* **Actor:** Balaji (Policyholder) and Underwriter/Adjuster.
* **Navigation:** Click **"💬 Chat"** in sidebar (`http://localhost:4200/chat`).
* **Action Steps:**
  1. Point out the chat interface:
     * Clean conversation list with human-readable titles (e.g. *Balaji (Policyholder)* and *Underwriter One*).
     * No raw UUIDs in headers or conversation lists.
  2. Type a message from Policyholder:
     * *"Hello, the restoration crew has arrived on site and commenced cleaning Bay 2."*
     * Click **"Send"**.
  3. Switch to the Adjuster or Underwriter window:
     * Point out the **pulsing red dot / badge indicator** on the sidebar: **`💬 Chat (1)`** (polled automatically every 10 seconds).
     * Open chat and reply: *"Acknowledged. Please upload the completion certificate once work wraps up."*
* **Plain English Explanation:** Commercial claims involve constant communication between the factory owner, adjuster, and contractors. Built-in contextual messaging keeps all evidence in one place.
* **Visual Checkpoint:** Messages render instantly with IST timestamps and verified sender role badges.

---

#### Step 16: Review Stacked Notification Audit Log
* **Actor:** Any user.
* **Navigation:** Click **"🔔 Notifications"** in sidebar (`http://localhost:4200/notifications`).
* **Action Steps:**
  1. Showcase the chronological audit trail:
     * `QUOTE_SUBMITTED`: *"Commercial quote proposal submitted."*
     * `RISK_ASSESSMENT_ASSIGNED` & `COMPLETED`: *"Site engineering evaluation concluded."*
     * `QUOTE_OFFERED`: *"Underwriting terms offered with ₹45,500 premium."*
     * `POLICY_ISSUED`: *"Policy contract POL-2026-XXXX bound and active."*
     * `CLAIM_FILED`: *"FNOL received for hydraulic fire incident."*
     * `CLAIM_APPROVED`: *"Adjuster approved coverage decision."*
     * `CLAIM_SETTLED`: *"Settlement payment of ₹2,95,000 disbursed via NEFT."*
  2. Click **"Mark All as Read"**.
* **Plain English Explanation:** Every critical action is captured as a permanent, timestamped event, ensuring end-to-end auditability.
* **Visual Checkpoint:** The notifications stack neatly in descending order without disappearing or hiding older events.

---

#### Step 17: Official Claim File Closure
* **Actor:** Claims Adjuster or Claims Manager (`claimsmanager1@intellisure.com`).
* **Navigation:** Open claim `CLM-2026-XXXX`.
* **Action Steps:**
  1. Scroll down to the final governance section.
  2. Click the green button **"Close Claim File"**.
  3. Confirmation prompt: *"All payouts disbursed and recovery dispatched. Confirm file closure?"* Click **"Confirm Closure"**.
* **Plain English Explanation:** Once repairs are completed and all funds are disbursed, the claim file is formally audited and closed.
* **Visual Checkpoint:**
  * Claim status changes to **`CLOSED`**.
  * A green badge confirms *"Claim successfully settled and closed"*.
  * The lifecycle of this commercial policy is now complete!

---

## 3. Alternative & Exception Paths (Bonus Demo Showcase)

To demonstrate that IntelliSure handles real-world business complexity beyond the "happy path", present these three alternative paths if requested by the evaluation committee:

### Path A: Underwriting Referral or Decline
* **Scenario:** An applicant operates in a prohibited risk class (e.g. explosives manufacturing or uninsurable hazard).
* **How to demo:**
  1. As Policyholder, submit a quote.
  2. As Underwriter, open the quote and click **"Decline Quote"**.
  3. Enter reason: *"Property exceeds maximum combustible fire load tolerance for standard BOP package."*
  4. Visual check: Quote status changes to red badge **`DECLINED`**, and customer receives a decline notification.

### Path B: Claim Investigation Rejection
* **Scenario:** Incident occurred prior to policy inception or falls under explicit contract exclusions (e.g. intentional sabotage or unendorsed flood).
* **How to demo:**
  1. As Claims Adjuster, on Step 11, select Decision: **`REJECTED`**.
  2. Select Reason: *"Incident occurred prior to policy effective inception date."*
  3. Visual check: Claim status becomes red **`REJECTED`**. Financial disbursement buttons are locked.

### Path C: Customer-Managed Recovery
* **Scenario:** The policyholder prefers to use their own internal maintenance staff rather than hiring a network vendor.
* **How to demo:**
  1. On Step 13, Policyholder chooses **`CUSTOMER_MANAGED`**.
  2. The system bypasses contractor dispatch, logs self-restoration milestones, and allows the policyholder to directly mark repair completion.

---

## 4. Key Insurance Concepts & Terminology Guide
Use this section if non-technical evaluators ask questions regarding insurance terms:

| Insurance Concept | Plain-English Definition | Real-World Purpose in IntelliSure |
| :--- | :--- | :--- |
| **FNOL (First Notice of Loss)** | The initial report made by a policyholder alerting the insurer that damage or theft has occurred. | Creates the claim record, triggers automated adjuster assignment, and alerts restoration teams. |
| **Reserve** | An allocated fund set aside immediately by the insurance company to guarantee it can pay the final claim cost. | Government regulations require insurers to lock away reserves so they remain financially solvent. |
| **Assessment** | The formal investigation and inspection conducted by a surveyor or adjuster to calculate gross damages. | Verifies the cause of damage, checks building maintenance records, and inspects damaged machinery. |
| **Coverage Decision** | The legal determination declaring whether the damaged items and cause of loss are covered under the policy contract. | Determines whether the claim is `APPROVED` or `REJECTED` after subtracting the policyholder's deductible. |
| **Deductible** | The initial out-of-pocket amount the policyholder must pay before the insurance company pays the rest. | Applied during coverage calculation (e.g., ₹25,000 subtracted from ₹3,20,000 loss). |
| **Claims Manager Governance** | A dual-control financial approval tier where a senior manager audits high-value reserves and releases payments. | Prevents fraud and unauthorized payouts exceeding individual adjuster authority limits. |
| **Subrogation** | The legal right of an insurer to pursue third-party wrongdoers who caused the loss to recover payout funds. | Managed under the recovery microservice when third-party vehicles or outside contractors cause damage. |
| **Subjectivities / Conditions** | Safety requirements that an applicant must agree to complete before full coverage is bound. | Recommended by Risk Engineers (e.g., installing dry-chemical fire extinguishers). |

---

## 5. Troubleshooting & Presenter Tips

1. **"I clicked button X and nothing happened":**
   * Check your browser console.
   * If a form field was missed (e.g. missing deductible or settlement reference), the input border will turn red.
2. **"Sidebar badges didn't update immediately":**
   * Background polling occurs automatically every 10 seconds.
   * You can also click the manual **"Refresh"** button at the top right of any page header.
3. **"Where do I find the PDF statements?":**
   * Navigate to `http://localhost:4200/docs`. Every policy, quote proposal, and FNOL report is synthesized as an official downloadable statement. Click **"Print PDF"** to preview and save.
4. **"Can I switch roles in the same browser window?":**
   * Yes, by logging out and signing in with another email from the Credentials Table.
   * However, using a **Standard Window for Policyholder** and an **Incognito Window for Employees** provides the smoothest live presentation flow.
