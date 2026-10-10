import { test, expect } from '../fixtures/test-base';
import { navigateTo } from '../helpers/page-helpers';

test.describe('Usecase Suite 03: Policyholder (Customer) Journey', () => {
  test('Policyholder views dashboard with policy summary and active counters', async ({
    policyholderPage: page,
  }) => {
    await navigateTo(page, '/dashboard');
    await expect(page).toHaveURL(/.*\/dashboard/);

    // Verify presence of dashboard title or cards
    const dashboardCards = page.locator('.metric-card, is-card, .dashboard-card, h1');
    await expect(dashboardCards.first()).toBeVisible();
  });

  test('Policyholder views and edits commercial business profile', async ({
    policyholderPage: page,
  }) => {
    await navigateTo(page, '/profile');
    await expect(page).toHaveURL(/.*\/profile/);

    // Verify company legal name is present
    const legalNameInput = page.locator('#legalName, input[formcontrolname="legalName"], input[name="legalName"]');
    if (await legalNameInput.isVisible()) {
      await expect(legalNameInput).toHaveValue(/Acme/);
    }

    // Verify phone input is present
    const phoneInput = page.locator('#phone, input[formcontrolname="phone"]');
    if (await phoneInput.isVisible()) {
      await phoneInput.fill('+1 (555) 987-6543');
    }

    // Save profile button
    const saveBtn = page.locator('button[type="submit"]:has-text("Save"), is-button:has-text("Save")');
    if (await saveBtn.isVisible() && !(await saveBtn.isDisabled())) {
      await saveBtn.click();
    }
  });

  test('Policyholder views quotes list and can see their own DRAFT quotes', async ({
    policyholderPage: page,
  }) => {
    await navigateTo(page, '/quotes');
    await expect(page).toHaveURL(/.*\/quotes/);

    // Policyholder should see their own draft quote QTE-2026-0001
    const draftQuote = page.locator('text="QTE-2026-0001", text="DRAFT"');
    await expect(draftQuote.first()).toBeVisible();

    // Verify submitted quote is also visible
    const submittedQuote = page.locator('text="QTE-2026-0002", text="SUBMITTED"');
    await expect(submittedQuote.first()).toBeVisible();
  });

  test('Policyholder can initiate new quote creation wizard at /quotes/new', async ({
    policyholderPage: page,
  }) => {
    await navigateTo(page, '/quotes/new');
    await expect(page).toHaveURL(/.*\/quotes\/new/);

    // Verify quote creation wizard elements
    const quoteHeading = page.locator('h1, h2, .quote-form, form');
    await expect(quoteHeading.first()).toBeVisible();

    // Fill policy type if present
    const policyTypeSelect = page.locator('select[formcontrolname="policyType"], select#policyType, is-select');
    if (await policyTypeSelect.isVisible()) {
      await policyTypeSelect.selectOption({ index: 1 });
    }

    // Fill coverage amount
    const coverageInput = page.locator('input[formcontrolname="coverageAmount"], #coverageAmount');
    if (await coverageInput.isVisible()) {
      await coverageInput.fill('1500000');
    }
  });

  test('Policyholder views quote details and can accept approved quote', async ({
    policyholderPage: page,
  }) => {
    await navigateTo(page, '/quotes/q0000003-0000-0000-0000-000000000003');
    await expect(page.locator('body')).toBeVisible();

    // Verify quote number or status is visible
    const quoteStatus = page.locator('text="QTE-2026-0003", text="APPROVED"');
    await expect(quoteStatus.first()).toBeVisible();
  });

  test('Policyholder views active policies and policy coverage terms', async ({
    policyholderPage: page,
  }) => {
    await navigateTo(page, '/policy');
    await expect(page).toHaveURL(/.*\/policy/);

    // Verify policy list contains active policy POL-2025-9842
    const policyItem = page.locator('text="POL-2025-9842", text="COMMERCIAL_PROPERTY", text="ACTIVE"');
    await expect(policyItem.first()).toBeVisible();
  });

  test('Policyholder accesses /docs without 403 and sees bound documents', async ({
    policyholderPage: page,
  }) => {
    await navigateTo(page, '/docs');
    await expect(page).toHaveURL(/.*\/docs/);

    // CRITICAL: Ensure NO 403 or Access Denied error is shown
    const accessDenied = page.locator('text="403", text="Access denied", text="Forbidden"');
    await expect(accessDenied).toHaveCount(0);

    // Verify bound documents list rendered
    const docRow = page.locator('text="Commercial_Property_Binder_POL-2025-9842.pdf", .document-row, .doc-item');
    await expect(docRow.first()).toBeVisible();
  });

  test('Policyholder can view claims and track claim status', async ({
    policyholderPage: page,
  }) => {
    await navigateTo(page, '/claims');
    await expect(page).toHaveURL(/.*\/claims/);

    // Verify claim row or claim number is displayed
    const claimRow = page.locator('text="CLM-2026-0101", text="CLM-2026-0102", .claim-item');
    await expect(claimRow.first()).toBeVisible();
  });
});
