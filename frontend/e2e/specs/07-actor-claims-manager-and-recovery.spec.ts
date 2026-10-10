import { test, expect } from '../fixtures/test-base';
import { navigateTo } from '../helpers/page-helpers';

test.describe('Usecase Suite 07: Claims Manager & Recovery Journey', () => {
  test('Claims Manager accesses /claims for enterprise claim oversight', async ({
    claimsManagerPage: page,
  }) => {
    await navigateTo(page, '/claims');
    await expect(page).toHaveURL(/.*\/claims/);

    const claimsList = page.locator('.claims-container, table, .claims-list, h1');
    await expect(claimsList.first()).toBeVisible();
  });

  test('Claims Manager accesses /recovery portal for subrogation and salvage', async ({
    claimsManagerPage: page,
  }) => {
    await navigateTo(page, '/recovery');
    await expect(page).toHaveURL(/.*\/recovery/);

    const recoveryHeader = page.locator('h1, h2, .recovery-workspace, .recovery-header');
    await expect(recoveryHeader.first()).toBeVisible();
  });

  test('Claims Manager views claims cycle times and reserve analytics at /analytics', async ({
    claimsManagerPage: page,
  }) => {
    await navigateTo(page, '/analytics');
    await expect(page).toHaveURL(/.*\/analytics/);

    const analyticsCards = page.locator('.metric-card, canvas, .analytics-dashboard, h1');
    await expect(analyticsCards.first()).toBeVisible();
  });
});
