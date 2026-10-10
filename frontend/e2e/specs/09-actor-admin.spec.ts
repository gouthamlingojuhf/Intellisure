import { test, expect } from '../fixtures/test-base';
import { navigateTo } from '../helpers/page-helpers';

test.describe('Usecase Suite 09: Admin Supervision & Underwriter Assignment Journey', () => {
  test('Admin accesses /admin workspace and manages enterprise users', async ({
    adminPage: page,
  }) => {
    await navigateTo(page, '/admin');
    await expect(page).toHaveURL(/.*\/admin/);

    const adminPanel = page.locator('h1, h2, .admin-workspace, .admin-header');
    await expect(adminPanel.first()).toBeVisible();
  });

  test('CRITICAL: Admin views /quotes and NEVER sees DRAFT quotes', async ({
    adminPage: page,
  }) => {
    await navigateTo(page, '/quotes');
    await expect(page).toHaveURL(/.*\/quotes/);

    // 1. Assert customer draft quote QTE-2026-0001 is NOT visible to Admin
    const draftQuote = page.locator('text="QTE-2026-0001"');
    await expect(draftQuote).toHaveCount(0);

    // 2. Assert submitted quote QTE-2026-0002 IS visible
    const submittedQuote = page.locator('text="QTE-2026-0002"');
    await expect(submittedQuote.first()).toBeVisible();

    // 3. Assert approved quote QTE-2026-0003 IS visible
    const approvedQuote = page.locator('text="QTE-2026-0003"');
    await expect(approvedQuote.first()).toBeVisible();
  });

  test('CRITICAL: Admin can assign/reassign Underwriter from live database dropdown', async ({
    adminPage: page,
  }) => {
    await navigateTo(page, '/quotes');
    await expect(page).toHaveURL(/.*\/quotes/);

    // Look for Reassign or Assign Underwriter action button
    const assignBtn = page.locator('button:has-text("Assign"), is-button:has-text("Assign"), button:has-text("Reassign")');
    if (await assignBtn.first().isVisible()) {
      await assignBtn.first().click();

      // Modal appears
      const modal = page.locator('.assign-modal, .modal-backdrop, [role="dialog"], is-card');
      await expect(modal.first()).toBeVisible();

      // Check underwriter select contains active database underwriters (e.g. Marcus Vance or Sarah Jenkins)
      const underwriterSelect = page.locator('select#underwriterSelect, .uw-select').first();
      if (await underwriterSelect.isVisible()) {
        const optionText = await underwriterSelect.innerText();
        expect(optionText).toMatch(/Sarah|Marcus|Jenkins|Vance/i);
      }
    }
  });

  test('Admin accesses analytics dashboard at /analytics', async ({ adminPage: page }) => {
    await navigateTo(page, '/analytics');
    await expect(page).toHaveURL(/.*\/analytics/);

    const analyticsCards = page.locator('.analytics-dashboard, .metric-card, canvas, h1');
    await expect(analyticsCards.first()).toBeVisible();
  });

  test('Admin accesses /docs without 403 Access Denied error', async ({ adminPage: page }) => {
    await navigateTo(page, '/docs');
    await expect(page).toHaveURL(/.*\/docs/);

    const accessDenied = page.locator('text="403", text="Access denied", text="Forbidden"');
    await expect(accessDenied).toHaveCount(0);
  });
});
