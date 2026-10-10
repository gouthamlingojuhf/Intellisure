import { test, expect } from '../fixtures/test-base';
import { navigateTo } from '../helpers/page-helpers';

test.describe('Usecase Suite 05: Risk Engineer Actor Journey', () => {
  test('Risk Engineer accesses /underwriting inspection queue', async ({
    riskEngineerPage: page,
  }) => {
    await navigateTo(page, '/underwriting');
    await expect(page).toHaveURL(/.*\/underwriting/);

    const workspace = page.locator('h1, h2, .underwriting-workspace, .risk-queue');
    await expect(workspace.first()).toBeVisible();
  });

  test('CRITICAL: Risk Engineer accesses /docs without 403 Access Denied error', async ({
    riskEngineerPage: page,
  }) => {
    // This explicitly verifies the resolution of the bug where RISK_ENGINEER
    // was missing from DOCUMENT_STAFF_ROLES causing a 403 error on /docs
    await navigateTo(page, '/docs');
    await expect(page).toHaveURL(/.*\/docs/);

    // Verify no 403 Access Denied banner appears
    const accessDeniedBanner = page.locator('text="403", text="Access denied", text="Forbidden"');
    await expect(accessDeniedBanner).toHaveCount(0);

    // Verify documents are listed
    const docList = page.locator('.document-row, .doc-item, table, .documents-container');
    await expect(docList.first()).toBeVisible();
  });

  test('Risk Engineer accesses technical risk analytics at /analytics', async ({
    riskEngineerPage: page,
  }) => {
    await navigateTo(page, '/analytics');
    await expect(page).toHaveURL(/.*\/analytics/);

    const analyticsView = page.locator('.analytics-dashboard, .metric-card, canvas, h1');
    await expect(analyticsView.first()).toBeVisible();
  });
});
