import { Page, Route } from '@playwright/test';
import {
  TEST_USERS,
  TestUser,
  MOCK_AVAILABLE_EMPLOYEES,
  MOCK_CUSTOMER_PROFILE,
  MOCK_QUOTES,
  MOCK_POLICIES,
  MOCK_CLAIMS,
  MOCK_DOCUMENTS,
  MOCK_NOTIFICATIONS,
  MOCK_VENDOR_ASSIGNMENTS,
  MOCK_ANALYTICS_DASHBOARD,
} from './mock-data';

/**
 * Attaches backend API route mocks to the Playwright Page context.
 * Implements authoritative backend filtering rules:
 * - Admin never receives DRAFT quotes
 * - Underwriters only receive quotes assigned to them (and non-drafts)
 * - /api/documents returns bound documents without 403
 * - /api/users/available returns live active database users
 */
export async function setupApiMocks(page: Page, currentUser: TestUser): Promise<void> {
  // 1. Auth & Current User
  await page.route('**/api/users/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        userId: currentUser.userId,
        email: currentUser.email,
        role: currentUser.role,
        displayName: currentUser.displayName,
        accountStatus: 'ACTIVE',
      }),
    });
  });

  await page.route('**/api/auth/login', async (route) => {
    const postData = route.request().postDataJSON() || {};
    const matchedUser =
      Object.values(TEST_USERS).find((u) => u.email === postData.email) || currentUser;
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        token: matchedUser.token,
        role: matchedUser.role,
        userId: matchedUser.userId,
        email: matchedUser.email,
        displayName: matchedUser.displayName,
        customerId: matchedUser.customerId || null,
      }),
    });
  });

  await page.route('**/api/auth/register', async (route) => {
    const postData = route.request().postDataJSON() || {};
    await route.fulfill({
      status: 201,
      contentType: 'application/json',
      body: JSON.stringify({
        userId: '88888888-8888-8888-8888-888888888888',
        email: postData.email || 'new.user@intellisure.test',
        role: postData.registrationType === 'VENDOR_APPLICANT' ? 'VENDOR_APPLICANT' : 'POLICYHOLDER',
        displayName: postData.displayName || 'New Registrant',
        accountStatus: 'ACTIVE',
      }),
    });
  });

  // 2. Dynamic Available Employees API (DB as SST)
  await page.route('**/api/users/available*', async (route) => {
    const url = new URL(route.request().url());
    const roleParam = url.searchParams.get('role');
    let filtered = MOCK_AVAILABLE_EMPLOYEES;
    if (roleParam) {
      filtered = filtered.filter((e) => e.role.toUpperCase() === roleParam.toUpperCase());
    }
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(filtered),
    });
  });

  await page.route('**/api/users/role/*/available*', async (route) => {
    const url = new URL(route.request().url());
    const match = url.pathname.match(/\/role\/([^/]+)\/available/);
    const role = match ? match[1] : '';
    const filtered = MOCK_AVAILABLE_EMPLOYEES.filter(
      (e) => e.role.toUpperCase() === role.toUpperCase()
    );
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(filtered),
    });
  });

  // 3. Customer Profile (Commercial Entity Onboarding)
  await page.route('**/api/customers/profile*', async (route) => {
    if (currentUser.role !== 'POLICYHOLDER') {
      // Employees should never reach this or have a customer profile
      await route.fulfill({
        status: 404,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'No customer profile for employee account' }),
      });
      return;
    }
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(MOCK_CUSTOMER_PROFILE),
      });
    } else {
      const updateData = route.request().postDataJSON() || {};
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          ...MOCK_CUSTOMER_PROFILE,
          ...updateData,
          updatedAt: new Date().toISOString(),
        }),
      });
    }
  });

  await page.route('**/api/customers/**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(MOCK_CUSTOMER_PROFILE),
    });
  });

  // 4. Quotes Service (Authoritative Backend Filtering)
  await page.route('**/api/quotes', async (route) => {
    if (route.request().method() === 'GET') {
      let result = [...MOCK_QUOTES];
      if (currentUser.role === 'ADMIN' || currentUser.role === 'SYSTEM_ADMINISTRATOR') {
        // ADMIN: Never see DRAFT quotes
        result = result.filter((q) => q.status !== 'DRAFT');
      } else if (currentUser.role === 'UNDERWRITER') {
        // UNDERWRITER: Only see non-draft quotes assigned to them
        result = result.filter(
          (q) => q.status !== 'DRAFT' && q.assignedUnderwriterId === currentUser.userId
        );
      } else if (currentUser.role === 'POLICYHOLDER') {
        // POLICYHOLDER: Only see own quotes (including drafts)
        result = result.filter((q) => q.customerId === currentUser.customerId);
      }
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(result),
      });
    } else if (route.request().method() === 'POST') {
      const newQuote = route.request().postDataJSON() || {};
      const created = {
        quoteId: `q0000009-${Date.now().toString().slice(-12)}`,
        quoteNumber: `QTE-2026-${Math.floor(1000 + Math.random() * 9000)}`,
        customerId: currentUser.customerId || 'c0000001-0000-0000-0000-000000000001',
        businessName: newQuote.businessName || 'Acme Logistics & Warehousing LLC',
        policyType: newQuote.policyType || 'COMMERCIAL_PROPERTY',
        coverageAmount: newQuote.coverageAmount || 1000000,
        annualPremium: 9500.0,
        status: 'SUBMITTED',
        assignedUnderwriterId: '22222222-2222-2222-2222-222222222222',
        riskScore: 35,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify(created),
      });
    } else {
      await route.continue();
    }
  });

  await page.route('**/api/quotes/admin*', async (route) => {
    // Admin quotes endpoint filters out DRAFTs
    const nonDraftQuotes = MOCK_QUOTES.filter((q) => q.status !== 'DRAFT');
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(nonDraftQuotes),
    });
  });

  await page.route(/.*\/api\/quotes\/[0-9a-f-]{36}$/, async (route) => {
    const url = route.request().url();
    const id = url.split('/').pop();
    const quote = MOCK_QUOTES.find((q) => q.quoteId === id) || MOCK_QUOTES[1];
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(quote),
      });
    } else if (route.request().method() === 'PUT' || route.request().method() === 'PATCH') {
      const updates = route.request().postDataJSON() || {};
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ ...quote, ...updates, updatedAt: new Date().toISOString() }),
      });
    }
  });

  // Re-assign underwriter endpoint
  await page.route(/.*\/api\/quotes\/[0-9a-f-]{36}\/assign.*/, async (route) => {
    const updates = route.request().postDataJSON() || {};
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        ...MOCK_QUOTES[1],
        assignedUnderwriterId: updates.underwriterId || '22222222-2222-2222-2222-333333333333',
        status: 'UNDER_REVIEW',
        updatedAt: new Date().toISOString(),
      }),
    });
  });

  // 5. Policies
  await page.route('**/api/policies*', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(MOCK_POLICIES),
    });
  });

  // 6. Claims
  await page.route('**/api/claims*', async (route) => {
    if (route.request().method() === 'GET') {
      let claims = [...MOCK_CLAIMS];
      if (currentUser.role === 'CLAIMS_ADJUSTER') {
        claims = claims.filter((c) => c.assignedAdjusterId === currentUser.userId);
      } else if (currentUser.role === 'POLICYHOLDER') {
        claims = claims.filter((c) => c.customerId === currentUser.customerId);
      }
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(claims),
      });
    } else if (route.request().method() === 'POST') {
      const payload = route.request().postDataJSON() || {};
      const newClaim = {
        claimId: `cl000009-${Date.now().toString().slice(-12)}`,
        claimNumber: `CLM-2026-${Math.floor(1000 + Math.random() * 9000)}`,
        policyId: payload.policyId || MOCK_POLICIES[0].policyId,
        customerId: currentUser.customerId || 'c0000001-0000-0000-0000-000000000001',
        incidentDate: payload.incidentDate || new Date().toISOString(),
        incidentDescription: payload.incidentDescription || 'Filed claim incident description',
        estimatedDamage: payload.estimatedDamage || 12000.0,
        approvedPayout: null,
        status: 'SUBMITTED',
        assignedAdjusterId: '44444444-4444-4444-4444-444444444444',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify(newClaim),
      });
    }
  });

  // 7. Documents (Zero 403 Errors for all logged in users)
  await page.route('**/api/documents*', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(MOCK_DOCUMENTS),
      });
    } else if (route.request().method() === 'POST') {
      const doc = {
        documentId: `d0000009-${Date.now().toString().slice(-12)}`,
        entityId: 'p0000001-0000-0000-0000-000000000001',
        entityType: 'POLICY',
        fileName: 'Uploaded_Supporting_Document.pdf',
        contentType: 'application/pdf',
        fileSizeBytes: 1048576,
        uploadedBy: currentUser.userId,
        uploadedAt: new Date().toISOString(),
        classification: 'POLICYHOLDER_UPLOAD',
        downloadUrl: '/api/documents/uploaded/download',
      };
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify(doc),
      });
    }
  });

  // 8. Analytics
  await page.route('**/api/analytics/**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(MOCK_ANALYTICS_DASHBOARD),
    });
  });

  // 9. Vendor Assignments
  await page.route('**/api/vendor/**', async (route) => {
    const url = route.request().url();
    if (url.includes('/assignments/')) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(MOCK_VENDOR_ASSIGNMENTS[0]),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(MOCK_VENDOR_ASSIGNMENTS),
      });
    }
  });

  // 10. Notifications
  await page.route('**/api/notifications*', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(MOCK_NOTIFICATIONS),
      });
    } else {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true }),
      });
    }
  });
}
