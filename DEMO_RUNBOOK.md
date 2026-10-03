# IntelliSure Demo Runbook

This guide shows the sequence to demonstrate the working insurance lifecycle in a single run.

Important: the current implementation does not have a dedicated admin login + admin user creation endpoint. The only public user-creation endpoint in the code is:

- `POST /api/auth/register`

That endpoint always creates a user with role `POLICYHOLDER` and status `ACTIVE`, because the service currently hard-codes the role during registration in `UserAccountService.register(...)`.

If you want to demo other roles such as `UNDERWRITER`, `CLAIMS_ADJUSTER`, `VENDOR_MANAGER`, or `SYSTEM_ADMINISTRATOR`, you must either:

1. create those rows directly in the MySQL `user_account` table, or
2. add a small temporary admin-only endpoint in `customer-party-service`.

For a demo, the safest approach is:
- create policyholder accounts via `/api/auth/register`
- log in as those users
- perform the customer-to-quote flow
- use a pre-created admin/employee account for internal actions if needed

## Assumptions

- Eureka is running at `http://localhost:8761`
- API Gateway is running at `http://localhost:8080`
- Customer Party Service is running at `http://localhost:8081`
- Services are started in the order shown by the workspace startup scripts
- MySQL is available and the required databases exist

---

## Phase 0: Create demo user accounts

### A. Create policyholder customer accounts

There is no admin login endpoint in the current code. Use the public registration endpoint.

| Step | Endpoint | Method | Body | Expected Outcome | Actor |
|---|---|---|---|---|---|
| 1 | `http://localhost:8080/api/auth/register` | POST | ```json
{
  "email": "customer1@demo.com",
  "password": "Demo@123",
  "displayName": "Alice Customer"
}
``` | A new `POLICYHOLDER` account is created and stored in `user_account`. Response returns user ID and account details. | Demo customer |
| 2 | `http://localhost:8080/api/auth/register` | POST | ```json
{
  "email": "customer2@demo.com",
  "password": "Demo@456",
  "displayName": "Bob Customer"
}
``` | Second policyholder account created. | Demo customer |

### B. Login to get JWT token

| Step | Endpoint | Method | Body | Expected Outcome | Actor |
|---|---|---|---|---|---|
| 3 | `http://localhost:8080/api/auth/login` | POST | ```json
{
  "email": "customer1@demo.com",
  "password": "Demo@123"
}
``` | Response includes JWT token: `token`, `tokenType`, `expiresIn`. Save it as `CUSTOMER1_TOKEN`. | Demo customer |
| 4 | `http://localhost:8080/api/auth/login` | POST | ```json
{
  "email": "customer2@demo.com",
  "password": "Demo@456"
}
``` | Response includes JWT token: `CUSTOMER2_TOKEN`. | Demo customer |

### C. Optional: create internal role accounts

The code currently does not expose an admin endpoint for role assignment. For demo purposes, create employee/admin users directly in the database if needed.

Example SQL:

```sql
INSERT INTO user_account (
  user_id,
  email,
  password_hash,
  role,
  account_status,
  display_name,
  created_at,
  updated_at
) VALUES (
  UUID_TO_BIN(UUID()),
  'underwriter@demo.com',
  '$2a$10$HASH_FROM_BCRYPT',
  'UNDERWRITER',
  'ACTIVE',
  'Demo Underwriter',
  NOW(),
  NOW()
);
```

Use a real bcrypt hash for the password. This is the recommended workaround if you need to show internal roles in the demo.

---

## Phase 1: Customer logs in and creates quote

| Step | Endpoint | Method | Body | Expected Outcome | Actor |
|---|---|---|---|---|---|
| 5 | `http://localhost:8080/api/auth/me` | GET | Header: `Authorization: Bearer <CUSTOMER1_TOKEN>` | Returns the logged-in user profile. | Customer |
| 6 | `http://localhost:8080/api/quotes` | POST | Header: `Authorization: Bearer <CUSTOMER1_TOKEN>`  ```json
{
  "customerId": "<customer-1-user-id>",
  "businessName": "Demo Retail Group",
  "businessType": "RETAIL",
  "annualRevenue": 2500000.00,
  "employeeCount": 18,
  "requestedCoverageAmount": 500000.00
}
``` | A new quote is created in the quote-policy service with status `DRAFT` (or equivalent created state). | Customer |
| 7 | `http://localhost:8080/api/quotes?customerId=<customer-1-user-id>` | GET | Header: `Authorization: Bearer <CUSTOMER1_TOKEN>` | Returns all quotes for the customer. | Customer |
| 8 | `http://localhost:8080/api/quotes/<quote-id>/submit` | POST | Header: `Authorization: Bearer <CUSTOMER1_TOKEN>` | Quote is moved to `SUBMITTED` state. | Customer |

Expected business flow: customer registers -> logs in -> creates quote -> submits quote.

---

## Phase 2: Underwriter / risk assessment

| Step | Endpoint | Method | Body | Expected Outcome | Actor |
|---|---|---|---|---|---|
| 9 | `http://localhost:8080/api/risk-assessments` | POST | Header: `Authorization: Bearer <UNDERWRITER_TOKEN>`  ```json
{
  "quoteId": "<quote-id>",
  "policyId": null,
  "customerId": "<customer-1-user-id>",
  "assessmentType": "COMMERCIAL",
  "location": "Boston, MA",
  "businessOperations": "Retail sales and logistics",
  "riskScore": 42.5,
  "summary": "Moderate risk profile with stable revenue and moderate operational exposure."
}
``` | Risk assessment is created for that quote/customer. | Underwriter |

The code allows both `/api/risks` and `/api/risk-assessments` as mapping roots. Use whichever route is easier for your demo.

---

## Phase 3: Quote approval / rejection

| Step | Endpoint | Method | Body | Expected Outcome | Actor |
|---|---|---|---|---|---|
| 10 | `http://localhost:8080/api/quotes/<quote-id>/approve` | POST | Header: `Authorization: Bearer <UNDERWRITER_TOKEN>` | Quote status changes to `ACCEPTED` or equivalent approved status. | Underwriter |
| 11 | `http://localhost:8080/api/quotes/<quote-id>/reject` | POST | Header: `Authorization: Bearer <UNDERWRITER_TOKEN>` | Quote status changes to `REJECTED` if you want to demonstrate rejection path. | Underwriter |

---

## Phase 4: Policy issue and claims

| Step | Endpoint | Method | Body | Expected Outcome | Actor |
|---|---|---|---|---|---|
| 12 | `http://localhost:8080/api/policies/issue/<quote-id>` | POST | Header: `Authorization: Bearer <UNDERWRITER_TOKEN>` | A policy is generated from the approved quote. | Underwriter |
| 13 | `http://localhost:8080/api/claims` | POST | Header: `Authorization: Bearer <CUSTOMER1_TOKEN>`  ```json
{
  "policyId": "<policy-id>",
  "incidentDate": "2026-09-15",
  "description": "Warehouse equipment damaged during storm",
  "estimatedLoss": 18000.00
}
``` | Claim is filed against the policy. | Customer |
| 14 | `http://localhost:8080/api/claims?customerId=<customer-1-user-id>` | GET | Header: `Authorization: Bearer <CUSTOMER1_TOKEN>` | Lists the customer's claims. | Customer |
| 15 | `http://localhost:8080/api/claims/<claim-id>/status?value=IN_REVIEW` | PATCH | Header: `Authorization: Bearer <CLAIMS_ADJUSTER_TOKEN>` | Claim status changes to `IN_REVIEW`. | Claims adjuster |

---

## Phase 5: Vendor assignment and service completion

| Step | Endpoint | Method | Body | Expected Outcome | Actor |
|---|---|---|---|---|---|
| 16 | `http://localhost:8080/api/vendors` | POST | Header: `Authorization: Bearer <SYSTEM_ADMIN_TOKEN>`  ```json
{
  "vendorName": "RapidRestore Services",
  "vendorType": "REPAIR",
  "contactEmail": "ops@rapidrestore.demo",
  "contactPhone": "+1-555-0101",
  "serviceRegions": "Northeast,Midwest"
}
``` | Vendor is registered. | Vendor manager / admin |
| 17 | `http://localhost:8080/api/vendors/<vendor-id>/verify` | POST | Header: `Authorization: Bearer <SYSTEM_ADMIN_TOKEN>` | Vendor becomes active/verified. | Vendor manager |
| 18 | `http://localhost:8080/api/vendor-assignments` | POST | Header: `Authorization: Bearer <SYSTEM_ADMIN_TOKEN>`  ```json
{
  "vendorId": "<vendor-id>",
  "claimId": "<claim-id>",
  "serviceRequested": "REPAIR"
}
``` | Assignment is created for a claim/vendor. | Vendor manager |
| 19 | `http://localhost:8080/api/vendor-assignments/<assignment-id>/accept` | POST | Header: `Authorization: Bearer <VENDOR_MANAGER_TOKEN>` | Assignment is accepted by the vendor. | Vendor |
| 20 | `http://localhost:8080/api/vendor-assignments/<assignment-id>/complete` | POST | Header: `Authorization: Bearer <VENDOR_MANAGER_TOKEN>` | Assignment is marked as completed. | Vendor |

---

## Phase 6: Notifications and audit trail

| Step | Endpoint | Method | Body | Expected Outcome | Actor |
|---|---|---|---|---|---|
| 21 | `http://localhost:8080/api/notifications` | POST | Header: `Authorization: Bearer <CUSTOMER1_TOKEN>`  ```json
{
  "recipientId": "<customer-1-user-id>",
  "message": "Your claim has been assigned to a vendor.",
  "channel": "EMAIL",
  "templateKey": "CLAIM_ASSIGNMENT"
}
``` | Notification is sent and stored. | Workflow system / claims team |
| 22 | `http://localhost:8080/api/notifications?recipientId=<customer-1-user-id>` | GET | Header: `Authorization: Bearer <CUSTOMER1_TOKEN>` | Lists notifications for that user. | Customer |
| 23 | `http://localhost:8080/api/notifications/<notification-id>/read` | PATCH | Header: `Authorization: Bearer <CUSTOMER1_TOKEN>` | Notification marked as read. | Customer |
| 24 | `http://localhost:8080/api/audit-events` | POST | Header: `Authorization: Bearer <CUSTOMER1_TOKEN>`  ```json
{
  "entityType": "QUOTE",
  "entityId": "<quote-id>",
  "eventType": "QUOTE_SUBMITTED",
  "actorId": "<customer-1-user-id>",
  "details": "Quote submitted by customer 1"
}
``` | Audit event is stored. | System / customer action |

---

## Recommended demo sequence (short version)

This is the cleanest end-to-end flow for a 5–10 minute live demo:

1. Register 2 customer users with `/api/auth/register`
2. Login as `customer1@demo.com` and capture `CUSTOMER1_TOKEN`
3. Create a quote with `/api/quotes`
4. Submit the quote with `/api/quotes/<quote-id>/submit`
5. Create a risk assessment with `/api/risk-assessments`
6. Approve the quote with `/api/quotes/<quote-id>/approve`
7. Issue a policy with `/api/policies/issue/<quote-id>`
8. File a claim with `/api/claims`
9. Register a vendor with `/api/vendors`
10. Assign the vendor with `/api/vendor-assignments`
11. Accept and complete the assignment
12. Send a notification and show the audit trail

This sequence covers the live lifecycle and avoids trying to show all unrelated services at once.

---

## Demo notes for the presenter

- Use the Gateway URL `http://localhost:8080` for all calls unless you are testing a service directly.
- Copy the JWT from the login response and send it as:
  - `Authorization: Bearer <token>`
- For policyholder demos, use accounts created through `/api/auth/register`.
- For internal staff roles, the codebase currently does not expose a dedicated admin creation endpoint; seed the DB directly if you need those roles for a presentation.
- The cleanest honest demo is:
  - customer registration
  - customer login
  - quote creation
  - quote submission
  - underwriter risk assessment
  - quote approval
  - policy issue
  - claim filing
  - vendor assignment
  - notification/audit event

---

## Summary

The important part is this:

- no real admin user-account creation endpoint exists in the current code
- the public registration endpoint is the only actual user creation endpoint
- that registration flow creates `POLICYHOLDER` users by default
- for demoing internal roles, you need either DB seeding or a small temporary admin endpoint

This is the best way to make the demo reliable and truthful.
