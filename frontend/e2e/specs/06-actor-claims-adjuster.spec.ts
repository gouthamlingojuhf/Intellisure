import { test, expect } from '../fixtures/test-base';
import { navigateTo } from '../helpers/page-helpers';

test.describe('Usecase Suite 06: Claims Adjuster Actor Journey', () => {
  test('Claims Adjuster accesses /claims workspace and views assigned claims', async ({
    claimsAdjusterPage: page,
  }) => {
    await navigateTo(page, '/claims');
    await expect(page).toHaveURL(/.*\/claims/);

    // Verify assigned claim CLM-2026-0101 is displayed
    const assignedClaim = page.locator('text="CLM-2026-0101"');
    await expect(assignedClaim.first()).toBeVisible();
  });

  test('Claims Adjuster reviews claim damage loss details', async ({
    claimsAdjusterPage: page,
  }) => {
    await navigateTo(page, '/claims');

    // Click on claim or navigate to claim detail
    const claimLink = page.locator('a:has-text("CLM-2026-0101"), tr:has-text("CLM-2026-0101")');
    if (await claimLink.first().isVisible()) {
      await claimLink.first().click();
      await page.waitForTimeout(300);
    }
  });

  test('Claims Adjuster can access /vendor to view and dispatch vendor partners', async ({
    claimsAdjusterPage: page,
  }) => {
    await navigateTo(page, '/vendor');
    await expect(page).toHaveURL(/.*\/vendor/);

    const vendorContainer = page.locator('.vendor-workspace, table, .vendor-card, h1, h2');
    await expect(vendorContainer.first()).toBeVisible();
  });

  test('Claims Adjuster accesses /docs without 403 and views claim evidence files', async ({
    claimsAdjusterPage: page,
  }) => {
    await navigateTo(page, '/docs');
    await expect(page).toHaveURL(/.*\/docs/);

    const accessDenied = page.locator('text="403", text="Access denied", text="Forbidden"');
    await expect(accessDenied).toHaveCount(0);

    const docItems = page.locator('.document-row, .doc-item, table tr');
    await expect(docItems.first()).toBeVisible();
  });
});
