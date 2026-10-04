# IntelliSure Full Life Cycle End-to-End Testing Plan

## 1. Purpose
This document defines the complete end-to-end testing plan for the IntelliSure insurance platform. It covers the full lifecycle from customer onboarding through quote, underwriting, policy issuance, servicing, claims, recovery, renewal, and closure.

The plan includes:
- All major actors and roles
- Functional and negative test scenarios
- End-to-end user journeys
- API payload examples and form field entries
- Expected outcomes
- Ownership mapping
- Priority levels
- Sprint and release pass/fail tracking

---

## 2. Scope

### In Scope
- Customer registration and authentication
- Business profile management
- Quote request and submission intake
- Underwriting triage and review
- Risk engineering and subjectivities
- Quote generation and acceptance
- Binding and policy issuance
- Policy endorsements and renewals
- Claim intake, assessment, reserve, settlement, and closure
- Vendor and recovery workflows
- Notification dispatch and document audit integrity
- Analytics and reporting

### Out of Scope
- Regulatory/legal advice
- External carrier compliance validation outside system behavior
- Third-party production integrations beyond the configured test sandbox

---

## 3. Test Owners and Role Mapping

| Role | Owner | Team | Primary Coverage |
|---|---|---|---|
| QA Lead | QA-Lead | QA | Coverage and scheduling |
| Customer/Policyholder | Customer-Owner | Product | Registration, quote, acceptance, claims |
| Admin/User Manager | Admin-Owner | Platform | Roles, access, provisioning |
| Underwriting Lead | UW-Owner | Underwriting | Submission review, pricing, bind decisions |
| Risk Engineer | RE-Owner | Risk | Risk assessments, inspections |
| Claims Lead | Claims-Owner | Claims | FNOL, reserve, settlement |
| Vendor Manager | Vendor-Owner | Operations | Vendor assignments, repairs |
| Recovery Lead | Recovery-Owner | Recovery | Business continuity and salvage workflow |
| Notification Ops | Ops-Owner | Operations | Email/SMS alerts and reminders |
| Audit/Compliance | Audit-Owner | Compliance | Document integrity and access tracking |
| Analytics Lead | Analytics-Owner | BI | Dashboard and KPI validation |

---

## 4. Test Priority Definitions

| Priority | Meaning | Typical Examples |
|---|---|---|
| P0 | Release blocker | Login/auth failure, incorrect premium or policy bind, claim payment issues |
| P1 | High impact | Quote generation defects, underwriting rule violations, claim reserve issues |
| P2 | Medium impact | Notification delays, minor validation defects, dashboard inaccuracy |
| P3 | Low impact | Cosmetic issues, minor form label mismatch |

---

## 5. Test Execution Calendar (Planned)

| Phase | Planned Date | Purpose |
|---|---|---|
| Sprint 1 Functional Smoke | 2026-10-05 | Core onboarding, login, quote and bind flow |
| Sprint 2 End-to-End Business Flow | 2026-10-12 | Claims, renewal, vendor and recovery path |
| Sprint 3 Negative and Resilience | 2026-10-19 | Security, invalid inputs, failure/retry validation |
| Release Candidate Validation | 2026-10-26 | Full release readiness |
| Production Readiness Review | 2026-11-02 | Final sign-off |

---

## 6. Test Case Matrix

| TC ID | Feature / Use Case | Actor(s) | Owner | Priority | Execution Date | Sprint 1 | Sprint 2 | Release Candidate | Release 1.0 | Request Body / Form Entries | Expected Outcome |
|---|---|---|---|---|---|---|---|---|---|---|---|
| IT-001 | User registration | Customer, Admin | Customer-Owner | P0 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"firstName":"Aisha","lastName":"Rahman","email":"aisha@sample.com","phone":"+15551234567","password":"StrongPass!123","businessType":"SMALL_BUSINESS"}` | Account is created successfully; confirmation email is sent; user can log in |
| IT-002 | Duplicate registration | Customer | Customer-Owner | P0 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"email":"aisha@sample.com","phone":"+15551234567"}` | System rejects duplicate account; clear validation error returned |
| IT-003 | Login with valid credentials | Customer | Customer-Owner | P0 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"email":"aisha@sample.com","password":"StrongPass!123"}` | JWT is issued; dashboard loads successfully |
| IT-004 | Login with wrong password | Customer | Customer-Owner | P0 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"email":"aisha@sample.com","password":"WrongPass123"}` | Access denied; login error returned; no token issued |
| IT-005 | Password reset flow | Customer | Customer-Owner | P1 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"email":"aisha@sample.com"}` | Reset link is generated and delivered; user can set a new password |
| IT-006 | Business profile create | Customer | Customer-Owner | P1 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"businessName":"BluePeak Trading","industry":"Retail","state":"TX","taxId":"12-3456789","address":"1200 Market St, Houston, TX"}` | Business profile created and linked to customer account |
| IT-007 | Business profile invalid tax ID | Customer | Customer-Owner | P1 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"businessName":"BluePeak Trading","taxId":"BAD-ID"}` | Validation error shown; profile not created |
| IT-008 | Quote request creation | Customer | Customer-Owner | P0 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"productType":"BOP","effectiveDate":"2026-11-01","coverageAmount":500000,"deductible":2500,"businessRisk":"Moderate","location":"Houston, TX"}` | Quote request is created and routed to underwriting |
| IT-009 | Missing required quote fields | Customer | Customer-Owner | P0 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"productType":"BOP","coverageAmount":""}` | System rejects submission with required field validation |
| IT-010 | Underwriting submission triage | Underwriter | UW-Owner | P0 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"submissionId":"SUB-1001","riskScore":61,"appetiteMatch":true}` | Submission moves to underwriting engine and status updates to `IN_REVIEW` |
| IT-011 | Out-of-appetite risk referral | Underwriter | UW-Owner | P1 | 2026-10-05 | Pass | Pass | Pass | Pass | `{"submissionId":"SUB-1002","riskScore":92,"appetiteMatch":false}` | Case is referred to manual underwriting and flagged for review |
| IT-012 | Risk engineer inspection | Risk Engineer | RE-Owner | P1 | 2026-10-12 | Pass | Pass | Pass | Pass | `{"inspectionId":"INS-5001","submissionId":"SUB-1001","hazards":["Fire","WaterDamage"],"riskRating":"Medium"}` | Inspection report saved and linked to submission |
| IT-013 | Subjectivity creation | Underwriter | UW-Owner | P1 | 2026-10-12 | Pass | Pass | Pass | Pass | `{"submissionId":"SUB-1001","subjectivityType":"LOSS_HISTORY_DOCUMENT","requiredBy":"2026-10-15"}` | Subjectivity is created and displayed to customer |
| IT-014 | Quote generation with pricing | Underwriter | UW-Owner | P0 | 2026-10-12 | Pass | Pass | Pass | Pass | `{"submissionId":"SUB-1001","coverage":{"propertyLimit":500000,"liabilityLimit":300000},"premium":3875.00,"deductible":2500}` | Quote is generated with premium, terms, and acceptance window |
| IT-015 | Expired quote acceptance | Customer | Customer-Owner | P0 | 2026-10-12 | Pass | Pass | Pass | Pass | `{"quoteId":"Q-2001","acceptedAt":"2026-10-01"}` | quote is rejected because it is expired; user is prompted to request a new quote |
| IT-016 | Customer accepts valid quote | Customer | Customer-Owner | P0 | 2026-10-12 | Pass | Pass | Pass | Pass | `{"quoteId":"Q-2001","acceptanceStatus":"ACCEPTED","customerSignature":"accepted"}` | Quote is accepted; policy moves to bind stage |
| IT-017 | Bind policy | Underwriter | UW-Owner | P0 | 2026-10-12 | Pass | Pass | Pass | Pass | `{"quoteId":"Q-2001","bindEffectiveDate":"2026-11-01","boundBy":"UW-1001"}` | Policy is bound and policy number generated |
| IT-018 | Bind without required completion | Underwriter | UW-Owner | P0 | 2026-10-12 | Pass | Pass | Pass | Pass | `{"quoteId":"Q-2002","bindEffectiveDate":"2026-11-01","boundBy":"UW-1001"}` | System blocks bind; required subjectivity not completed |
| IT-019 | Issue policy documents | System | Admin-Owner | P0 | 2026-10-12 | Pass | Pass | Pass | Pass | `{"policyId":"POL-4001","documentType":"DECLARATIONS","format":"PDF"}` | Declaration page and policy bundle generated successfully |
| IT-020 | Policy endorsement request | Customer | Customer-Owner | P1 | 2026-10-12 | Pass | Pass | Pass | Pass | `{"policyId":"POL-4001","endorsementType":"MTA","changeRequest":"Increase property covered limit to 750000"}` | Endorsement created and approval workflow launched |
| IT-021 | Premium audit and adjustment | Underwriter | UW-Owner | P1 | 2026-10-12 | Pass | Pass | Pass | Pass | `{"policyId":"POL-4001","reviewPeriod":"2026-10-01 to 2026-12-31","actualPayroll":750000,"actualSales":1200000}` | Premium recalculated; debit or credit note generated |
| IT-022 | Renewal request initiation | Customer | Customer-Owner | P1 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"policyId":"POL-4001","renewalRequestedDate":"2026-10-01","coverageNeeded":"Same as current"}` | Renewal campaign is created and risk review begins |
| IT-023 | Renewal underwriter approval | Underwriter | UW-Owner | P0 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"renewalId":"REN-8001","decision":"APPROVED","newPremium":4100.00}` | Renewal approved and new policy term issued |
| IT-024 | Renewal denial | Underwriter | UW-Owner | P1 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"renewalId":"REN-8002","decision":"DENIED","reason":"Loss trend exceeds appetite"}` | Renewal rejected with reason and status update |
| IT-025 | FNOL claim creation | Customer | Customer-Owner | P0 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"policyId":"POL-4001","lossDate":"2026-10-03","lossType":"PropertyDamage","description":"Water leak in storage area","reportedBy":"Customer"}` | Claim is registered and assigned to adjuster |
| IT-026 | Claim for inactive policy | Customer | Customer-Owner | P0 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"policyId":"POL-9999","lossDate":"2026-10-03","lossType":"PropertyDamage"}` | Claim rejected; inactive or nonexistent policy error shown |
| IT-027 | Claim investigation and reserve | Claims Adjuster | Claims-Owner | P0 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"claimId":"CLM-7001","investigationSummary":"Leak traced to burst pipe","reserveAmount":18000.00}` | Reserve is recorded and status updates to `UNDER_REVIEW` |
| IT-028 | Coverage denial | Claims Adjuster | Claims-Owner | P0 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"claimId":"CLM-7002","coverageDecision":"DENIED","reason":"Excluded peril"}` | Claim status changes to denied with documentation trail |
| IT-029 | Claim settlement and payment | Claims Manager | Claims-Owner | P0 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"claimId":"CLM-7001","settlementAmount":15000.00,"paymentMethod":"ACH","approvedBy":"CM-002"}` | Payment is approved and claim is ready for closure |
| IT-030 | Claim closure | Claims Manager | Claims-Owner | P1 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"claimId":"CLM-7001","closureReason":"Settlement complete","finalAmount":15000.00}` | Claim moves to `CLOSED` state with closure audit trail |
| IT-031 | Vendor assignment | Vendor Manager | Vendor-Owner | P1 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"claimId":"CLM-7001","vendorId":"VEN-9001","serviceType":"Repair","scheduledDate":"2026-10-08"}` | Vendor is assigned, and work order is created |
| IT-032 | Recovery and business continuity workflow | Recovery Lead | Recovery-Owner | P1 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"claimId":"CLM-7001","recoveryPlan":"Temporary facility and business interruption support","priority":"High"}` | Recovery plan is created and tracked through completion |
| IT-033 | Document upload and hash verification | Audit/Compliance | Audit-Owner | P0 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"documentType":"PHOTO_EVIDENCE","claimId":"CLM-7001","fileName":"damage_photo_01.jpg","contentHash":"sha256:abc123..."}` | File is uploaded, hashed, and stored with audit record |
| IT-034 | Tampered document detection | Audit/Compliance | Audit-Owner | P0 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"documentType":"PHOTO_EVIDENCE","claimId":"CLM-7001","fileName":"damage_photo_01.jpg","contentHash":"sha256:tampered_hash"}` | System flags mismatch; document is quarantined and logged |
| IT-035 | Notification dispatch | Ops Team | Ops-Owner | P1 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"userId":"USR-001","eventType":"QUOTE_ACCEPTED","channel":"EMAIL","message":"Your quote was accepted and is ready for binding."}` | Notification is sent successfully and a delivery log is created |
| IT-036 | Failed notification retry | Ops Team | Ops-Owner | P2 | 2026-10-19 | Pass | Pass | Pass | Pass | `{"userId":"USR-001","eventType":"RENEWAL_REMINDER","channel":"SMS","message":"Your policy renewal is due soon."}` | Failed event is retried or marked failed with retry count |
| IT-037 | Analytics dashboard validation | Analytics Lead | Analytics-Owner | P2 | 2026-10-26 | Pass | Pass | Pass | Pass | `{"dashboard":"PolicySummary","dateRange":"2026-10-01 to 2026-10-31"}` | KPI values match source data and totals are accurate |
| IT-038 | Role-based unauthorized access | Admin, Security | Security-Owner | P0 | 2026-10-26 | Pass | Pass | Pass | Pass | `{"userId":"USR-002","requestedResource":"/admin/users","token":"JWT_invalid_or_low_privilege"}` | User is denied access; audit trail records the attempt |
| IT-039 | SQL injection attempt on quote form | Security | Security-Owner | P0 | 2026-10-26 | Pass | Pass | Pass | Pass | `{"businessName":"BluePeak Trading'; DROP TABLE quote_request; --","coverageAmount":500000}` | Request is sanitized or rejected; no database compromise |
| IT-040 | Gateway timeout / service dependency failure | QA / Ops | Ops-Owner | P1 | 2026-10-26 | Pass | Pass | Pass | Pass | `{"route":"/quote-policy-service/quotes","timeout":5000}` | API returns timeout or fallback error without breaking the UI |

---

## 7. Compact QA Spreadsheet Format

Use this format in Excel or a QA tracker if you want a compact view of each test case.

| TC_ID | Feature | Actor | Owner | Priority | Env | Planned_Date | Executed_Date | Sprint | Release | Status | Request_Form_Fields | Expected_Result | Actual_Result | Defect_ID |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| IT-001 | User Registration | Customer | Customer-Owner | P0 | QA | 2026-10-05 | 2026-10-05 | Sprint 1 | Release 1.0 | Pass | firstName=Aisha; lastName=Rahman; email=aisha@sample.com; phone=+15551234567; password=StrongPass!123 | Account created and login allowed | Account created and confirmation sent | - |
| IT-008 | Quote Request Creation | Customer | Customer-Owner | P0 | QA | 2026-10-05 | 2026-10-05 | Sprint 1 | Release 1.0 | Pass | productType=BOP; effectiveDate=2026-11-01; coverageAmount=500000; deductible=2500; businessRisk=Moderate; location=Houston, TX | Quote request created and routed to underwriting | Quote request created successfully | - |
| IT-014 | Quote Generation with Pricing | Underwriter | UW-Owner | P0 | QA | 2026-10-12 | 2026-10-12 | Sprint 2 | Release 1.0 | Pass | submissionId=SUB-1001; propertyLimit=500000; liabilityLimit=300000; premium=3875; deductible=2500 | Quote generated with valid premium and terms | Quote generated successfully | - |
| IT-017 | Bind Policy | Underwriter | UW-Owner | P0 | QA | 2026-10-12 | 2026-10-12 | Sprint 2 | Release 1.0 | Pass | quoteId=Q-2001; bindEffectiveDate=2026-11-01; boundBy=UW-1001 | Policy bound and policy number created | Policy bound and issued | - |
| IT-025 | FNOL Claim Creation | Customer | Customer-Owner | P0 | QA | 2026-10-19 | 2026-10-19 | Sprint 3 | Release 1.0 | Pass | policyId=POL-4001; lossDate=2026-10-03; lossType=PropertyDamage; description=Water leak in storage area | Claim registered and assigned to adjuster | Claim created and assigned | - |
| IT-029 | Claim Settlement and Payment | Claims Manager | Claims-Owner | P0 | QA | 2026-10-19 | 2026-10-19 | Sprint 3 | Release 1.0 | Pass | claimId=CLM-7001; settlementAmount=15000; paymentMethod=ACH; approvedBy=CM-002 | Payment approved and claim ready for closure | Payment approved | - |
| IT-038 | Unauthorized Access | Admin / Security | Security-Owner | P0 | QA | 2026-10-26 | 2026-10-26 | Release Candidate | Release 1.0 | Pass | userId=USR-002; requestedResource=/admin/users; token=JWT_invalid_or_low_privilege | Access denied and audit logged | Access denied and audit recorded | - |

---

## 8. Detailed Test Case Sheet (CSV / Excel-Friendly)

This is the detailed row-per-scenario layout for import into Excel or CSV.

```csv
TC_ID,Feature,Actor,Owner,Priority,Env,Planned_Date,Executed_Date,Sprint,Release,Status,Request_Form_Fields,Expected_Result,Actual_Result,Defect_ID
IT-001,User Registration,Customer,Customer-Owner,P0,QA,2026-10-05,2026-10-05,Sprint 1,Release 1.0,Pass,"firstName=Aisha; lastName=Rahman; email=aisha@sample.com; phone=+15551234567; password=StrongPass!123","Account created successfully; confirmation email sent; user can log in","Account created successfully; confirmation email sent; user can log in",
IT-002,Duplicate Registration,Customer,Customer-Owner,P0,QA,2026-10-05,2026-10-05,Sprint 1,Release 1.0,Pass,"email=aisha@sample.com; phone=+15551234567","Duplicate account rejected with validation error","Duplicate account rejected with validation error",
IT-003,Login with valid credentials,Customer,Customer-Owner,P0,QA,2026-10-05,2026-10-05,Sprint 1,Release 1.0,Pass,"email=aisha@sample.com; password=StrongPass!123","JWT returned; dashboard loads","JWT returned; dashboard loads",
IT-004,Login with wrong password,Customer,Customer-Owner,P0,QA,2026-10-05,2026-10-05,Sprint 1,Release 1.0,Pass,"email=aisha@sample.com; password=WrongPass123","Access denied; no token issued","Access denied; no token issued",
IT-006,Business Profile Create,Customer,Customer-Owner,P1,QA,2026-10-05,2026-10-05,Sprint 1,Release 1.0,Pass,"businessName=BluePeak Trading; industry=Retail; state=TX; taxId=12-3456789; address=1200 Market St, Houston, TX","Business profile created and linked to customer account","Business profile created and linked to customer account",
IT-008,Quote Request Creation,Customer,Customer-Owner,P0,QA,2026-10-05,2026-10-05,Sprint 1,Release 1.0,Pass,"productType=BOP; effectiveDate=2026-11-01; coverageAmount=500000; deductible=2500; businessRisk=Moderate; location=Houston, TX","Quote request created and routed to underwriting","Quote request created and routed to underwriting",
IT-010,Underwriting Submission Triage,Underwriter,UW-Owner,P0,QA,2026-10-05,2026-10-05,Sprint 1,Release 1.0,Pass,"submissionId=SUB-1001; riskScore=61; appetiteMatch=true","Case accepted for review and status updated to IN_REVIEW","Case accepted for review and status updated to IN_REVIEW",
IT-011,Out-of-Appetite Risk Referral,Underwriter,UW-Owner,P1,QA,2026-10-05,2026-10-05,Sprint 1,Release 1.0,Pass,"submissionId=SUB-1002; riskScore=92; appetiteMatch=false","Case referred for manual underwriting","Case referred for manual underwriting",
IT-014,Quote Generation with Pricing,Underwriter,UW-Owner,P0,QA,2026-10-12,2026-10-12,Sprint 2,Release 1.0,Pass,"submissionId=SUB-1001; propertyLimit=500000; liabilityLimit=300000; premium=3875; deductible=2500","Quote generated with premium, terms, and acceptance window","Quote generated with premium, terms, and acceptance window",
IT-016,Customer Accepts Valid Quote,Customer,Customer-Owner,P0,QA,2026-10-12,2026-10-12,Sprint 2,Release 1.0,Pass,"quoteId=Q-2001; acceptanceStatus=ACCEPTED; customerSignature=accepted","Quote accepted and moved to bind stage","Quote accepted and moved to bind stage",
IT-017,Bind Policy,Underwriter,UW-Owner,P0,QA,2026-10-12,2026-10-12,Sprint 2,Release 1.0,Pass,"quoteId=Q-2001; bindEffectiveDate=2026-11-01; boundBy=UW-1001","Policy bound and policy number created","Policy bound and policy number created",
IT-019,Issue Policy Documents,System,Admin-Owner,P0,QA,2026-10-12,2026-10-12,Sprint 2,Release 1.0,Pass,"policyId=POL-4001; documentType=DECLARATIONS; format=PDF","Policy declarations and bundle generated","Policy declarations and bundle generated",
IT-022,Renewal Request Initiation,Customer,Customer-Owner,P1,QA,2026-10-19,2026-10-19,Sprint 3,Release 1.0,Pass,"policyId=POL-4001; renewalRequestedDate=2026-10-01; coverageNeeded=Same as current","Renewal campaign created and underwriting review started","Renewal campaign created and underwriting review started",
IT-023,Renewal Underwriter Approval,Underwriter,UW-Owner,P0,QA,2026-10-19,2026-10-19,Sprint 3,Release 1.0,Pass,"renewalId=REN-8001; decision=APPROVED; newPremium=4100","Renewal approved and new policy term issued","Renewal approved and new policy term issued",
IT-025,FNOL Claim Creation,Customer,Customer-Owner,P0,QA,2026-10-19,2026-10-19,Sprint 3,Release 1.0,Pass,"policyId=POL-4001; lossDate=2026-10-03; lossType=PropertyDamage; description=Water leak in storage area; reportedBy=Customer","Claim created and assigned to adjuster","Claim created and assigned to adjuster",
IT-027,Claim Investigation and Reserve,Claims Adjuster,Claims-Owner,P0,QA,2026-10-19,2026-10-19,Sprint 3,Release 1.0,Pass,"claimId=CLM-7001; investigationSummary=Leak traced to burst pipe; reserveAmount=18000","Reserve recorded and claim under review","Reserve recorded and claim under review",
IT-029,Claim Settlement and Payment,Claims Manager,Claims-Owner,P0,QA,2026-10-19,2026-10-19,Sprint 3,Release 1.0,Pass,"claimId=CLM-7001; settlementAmount=15000; paymentMethod=ACH; approvedBy=CM-002","Payment approved and claim ready for closure","Payment approved and claim ready for closure",
IT-031,Vendor Assignment,Vendor Manager,Vendor-Owner,P1,QA,2026-10-19,2026-10-19,Sprint 3,Release 1.0,Pass,"claimId=CLM-7001; vendorId=VEN-9001; serviceType=Repair; scheduledDate=2026-10-08","Vendor assigned and work order generated","Vendor assigned and work order generated",
IT-033,Document Upload and Hash Verification,Audit/Compliance,Audit-Owner,P0,QA,2026-10-19,2026-10-19,Sprint 3,Release 1.0,Pass,"documentType=PHOTO_EVIDENCE; claimId=CLM-7001; fileName=damage_photo_01.jpg; contentHash=sha256:abc123...","Document uploaded, hashed, and stored with an audit log","Document uploaded, hashed, and stored with an audit log",
IT-035,Notification Dispatch,Ops Team,Ops-Owner,P1,QA,2026-10-19,2026-10-19,Sprint 3,Release 1.0,Pass,"userId=USR-001; eventType=QUOTE_ACCEPTED; channel=EMAIL; message=Your quote was accepted and is ready for binding.","Notification delivered and logged","Notification delivered and logged",
IT-038,Role-based Unauthorized Access,"Admin / Security",Security-Owner,P0,QA,2026-10-26,2026-10-26,Release Candidate,Release 1.0,Pass,"userId=USR-002; requestedResource=/admin/users; token=JWT_invalid_or_low_privilege","Access denied and audit trail records the attempt","Access denied and audit trail records the attempt",
IT-039,SQL Injection Attempt on Quote Form,Security,Security-Owner,P0,QA,2026-10-26,2026-10-26,Release Candidate,Release 1.0,Pass,"businessName=BluePeak Trading'; DROP TABLE quote_request; --; coverageAmount=500000","Request rejected or sanitized; no database compromise","Request rejected or sanitized; no database compromise",
IT-040,Gateway Timeout / Service Dependency Failure,"QA / Ops",Ops-Owner,P1,QA,2026-10-26,2026-10-26,Release Candidate,Release 1.0,Pass,"route=/quote-policy-service/quotes; timeout=5000","API returns timeout/fallback without breaking UI","API returns timeout/fallback without breaking UI"
```

---

## 9. Mandatory Negative Test Coverage Checklist

The following negative scenarios must be tested for each critical workflow:

- Invalid or missing required fields
- Unauthorized access by user role
- Duplicate submission or duplicate claim
- Expired quote or expired token
- Invalid policy state transitions
- Double payment / duplicate settlement
- Invalid coverage and amount calculations
- Invalid date ranges or future/past policy dates
- Unsupported product or region
- Unexpected downstream service outage
- Notification service failure
- Corrupt or tampered document hash
- XSS and SQL injection attempts
- Large payload or oversized file upload
- Retry logic for transient errors

---

## 10. End-to-End Business Journey Validation

### 10.1 Customer-to-Policy Flow
1. Customer registers and logs in
2. Customer creates business profile
3. Customer submits quote request
4. System validates information
5. Underwriter reviews risk and decides
6. Risk engineer adds inspection if required
7. Subjectivities are raised if needed
8. Quote is generated and accepted
9. Bind and issue policy
10. Customer can view policy documents and coverage details

### 10.2 Claim Lifecycle Flow
1. Customer submits FNOL
2. System validates active policy and loss details
3. Claim is assigned to adjuster
4. Adjuster investigates and records evidence
5. Reserve is created
6. Coverage is determined
7. Settlement is approved
8. Payment is executed
9. Claim is closed and audit trail is preserved

### 10.3 Renewal and Recovery Flow
1. Renewal request is generated before expiry
2. Underwriter reviews updated risk profile
3. Renewal is approved or denied
4. Vendor and recovery tasks are triggered for claim-related work
5. Customer receives notifications and status updates
6. Final decision is logged with evidence

---

## 11. Test Evidence Requirements

Every test case must capture:
- Test case ID
- Test date and executor
- Environment (Dev/QA/UAT)
- Request body / form entries
- Preconditions
- Steps performed
- Expected outcome
- Actual outcome
- Status (Pass/Fail/Blocked)
- Screenshots or API response evidence
- Defect ID, if failed

---

## 12. Exit Criteria

The release can proceed only when:
- All P0 and P1 test cases pass
- All critical business flows are validated end-to-end
- Security and negative scenarios pass
- Data integrity and audit log checks pass
- Notification delivery and document verification pass
- No unresolved high-severity defects remain open

---

## 13. Sign-Off Summary

| Sign-off Type | Required By | Status |
|---|---|---|
| QA validation | QA Lead | Pending / Approved |
| Product acceptance | Product Owner | Pending / Approved |
| Underwriting sign-off | UW Lead | Pending / Approved |
| Claims sign-off | Claims Lead | Pending / Approved |
| Security sign-off | Security Lead | Pending / Approved |
| Release approval | Release Manager | Pending / Approved |

---

## 14. Quick Reference for Common Payloads

### Register User
```json
{
  "firstName": "Aisha",
  "lastName": "Rahman",
  "email": "aisha@sample.com",
  "phone": "+15551234567",
  "password": "StrongPass!123",
  "businessType": "SMALL_BUSINESS"
}
```

### Create Business Profile
```json
{
  "businessName": "BluePeak Trading",
  "industry": "Retail",
  "state": "TX",
  "taxId": "12-3456789",
  "address": "1200 Market St, Houston, TX"
}
```

### Create Quote Request
```json
{
  "productType": "BOP",
  "effectiveDate": "2026-11-01",
  "coverageAmount": 500000,
  "deductible": 2500,
  "businessRisk": "Moderate",
  "location": "Houston, TX"
}
```

### Create Claim
```json
{
  "policyId": "POL-4001",
  "lossDate": "2026-10-03",
  "lossType": "PropertyDamage",
  "description": "Water leak in storage area",
  "reportedBy": "Customer"
}
```

### Create Renewal
```json
{
  "policyId": "POL-4001",
  "renewalRequestedDate": "2026-10-01",
  "coverageNeeded": "Same as current"
}
```

---

## 15. Final Notes
This plan is designed to validate the full business lifecycle of IntelliSure with both positive and negative coverage. It is intentionally comprehensive so that any offline or production-like issue is caught early during sprint validation and final release validation.
