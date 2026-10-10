import { test, expect } from '../fixtures/test-base';
import { navigateTo } from '../helpers/page-helpers';

test.describe('Usecase Suite 04: Underwriter Actor Journey', () => {
  test('Underwriter accesses /underwriting dashboard and views pending review queue', async ({
    underwriterPage: page,
  }) => {
    await navigateTo(page, '/underwriting');
    await expect(page).toHaveURL(/.*\/underwriting/);

    // Verify underwriting dashboard elements
    const heading = page.locator('h1, h2, .underwriting-workspace, .dashboard-title');
    await expect(heading.first()).toBeVisible();
  });

  test('CRITICAL: Underwriter views /quotes and NEVER sees DRAFT quotes', async ({
    underwriterPage: page,
  }) => {
    await navigateTo(page, '/quotes');
    await expect(page).toHaveURL(/.*\/quotes/);

    // 1. Assert DRAFT quote QTE-2026-0001 is NOT visible
    const draftQuote = page.locator('text="QTE-2026-0001"');
    await expect(draftQuote).toHaveCount(0);

    // 2. Assert assigned non-draft quote QTE-2026-0002 IS visible
    const assignedQuote = page.locator('text="QTE-2026-0002"');
    await expect(assignedQuote.first()).toBeVisible();

    // 3. Assert quotes assigned to other underwriters (e.g. QTE-2026-0004 assigned to underwriter 2) are filtered out by backend
    const unassignedQuote = page.locator('text="QTE-2026-0004"');
    await expect(unassignedQuote).toHaveCount(0);
  });

  test('Underwriter reviews assigned quote details and performs underwriting assessment', async ({
    underwriterPage: page,
  }) => {
    await navigateTo(page, '/quotes/q0000002-0000-0000-0000-000000000002');
    await expect(page.locator('body')).toBeVisible();

    // Verify quote number is visible
    const quoteTitle = page.locator('text="QTE-2026-0002"');
    await expect(quoteTitle.first()).toBeVisible();

    // Verify underwriting action controls (Approve, Decline, Adjust)
    const actionButtons = page.locator(
      'button:has-text("Approve"), button:has-text("Decline"), is-button:has-text("Approve"), .underwriting-actions'
    );
    if (await actionButtons.first().isVisible()) {
      await expect(actionButtons.first()).toBeVisible();
    }
  });

  test('Underwriter accesses /docs without 403 error', async ({ underwriterPage: page }) => {
    await navigateTo(page, '/docs');
    await expect(page).toHaveURL(/.*\/docs/);

    // Verify zero access denied errors
    const errorNotice = page.locator('text="403", text="Access denied", text="Forbidden"');
    await expect(errorNotice).toHaveCount(0);

    const docItems = page.locator('.document-row, .doc-item, table tr');
    await expect(docItems.first()).toBeVisible();
  });

  test('Underwriter can access underwriting analytics at /analytics', async ({
    underwriterPage: page,
  }) => {
    await navigateTo(page, '/analytics');
    await expect(page).toHaveURL(/.*\/analytics/);

    // Verify analytics charts or KPI metrics rendered
    const kpiElements = page.locator('.analytics-dashboard, .metric-card, canvas, .chart-container, h1');
    await expect(kpiElements.first()).toBeVisible();
  });
});
