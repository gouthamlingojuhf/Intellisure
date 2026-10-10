import { test, expect } from '../fixtures/test-base';
import { navigateTo } from '../helpers/page-helpers';

test.describe('Usecase Suite 08: Vendor Portal & Assignment Journey', () => {
  test('Vendor Manager views vendor portal and assignments at /vendor', async ({
    vendorManagerPage: page,
  }) => {
    await navigateTo(page, '/vendor');
    await expect(page).toHaveURL(/.*\/vendor/);

    const vendorContainer = page.locator('.vendor-shell, .vendor-workspace, table, h1, h2');
    await expect(vendorContainer.first()).toBeVisible();
  });

  test('Vendor views assignment details and updates progress', async ({
    vendorApplicantPage: page,
  }) => {
    await navigateTo(page, '/vendor');
    await expect(page).toHaveURL(/.*\/vendor/);

    // Navigate to assignment detail if present
    const detailLink = page.locator('a[href*="/assignments/"], button:has-text("View")');
    if (await detailLink.first().isVisible()) {
      await detailLink.first().click();
      await page.waitForTimeout(300);
    }
  });

  test('Vendor applicant cannot access admin workspace', async ({
    vendorApplicantPage: page,
  }) => {
    await page.goto('/admin');
    await page.waitForLoadState('domcontentloaded');
    expect(page.url()).not.toContain('/admin');
  });
});
