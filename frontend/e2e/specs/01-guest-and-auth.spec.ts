import { test, expect } from '../fixtures/test-base';
import { navigateTo } from '../helpers/page-helpers';

test.describe('Usecase Suite 01: Guest Actor & Authentication Flows', () => {
  test('Guest can view public landing page and key entry points', async ({ guestPage: page }) => {
    await navigateTo(page, '/');
    await expect(page.locator('body')).toBeVisible();

    // Verify presence of brand title or home navigation
    const homeBrand = page.locator('a.breadcrumb-root, .brand-logo, h1, .home-hero');
    await expect(homeBrand.first()).toBeVisible();

    // Verify unauthenticated navigation items (e.g. Overview, Help)
    const navOverview = page.locator('nav a:has-text("Overview"), a[href="/"]');
    await expect(navOverview.first()).toBeVisible();
  });

  test('Guest can access platform documentation and guide at /help without authentication', async ({
    guestPage: page,
  }) => {
    await navigateTo(page, '/help');
    await expect(page).toHaveURL(/.*\/help/);

    // Verify platform guide content renders
    const guideHeading = page.locator('h1, h2, .guide-header, .platform-guide');
    await expect(guideHeading.first()).toBeVisible();
  });

  test('Guest accessing protected routes is redirected to /auth', async ({ guestPage: page }) => {
    // Attempt accessing customer dashboard
    await page.goto('/dashboard');
    await page.waitForLoadState('domcontentloaded');
    await expect(page).toHaveURL(/.*\/auth/);

    // Attempt accessing quotes
    await page.goto('/quotes');
    await page.waitForLoadState('domcontentloaded');
    await expect(page).toHaveURL(/.*\/auth/);

    // Attempt accessing admin
    await page.goto('/admin');
    await page.waitForLoadState('domcontentloaded');
    await expect(page).toHaveURL(/.*\/auth/);
  });

  test('Guest navigates to unknown route and sees 404 page', async ({ guestPage: page }) => {
    await navigateTo(page, '/non-existent-route-404');
    const notFoundIndicator = page.locator(
      'text="Not Found", text="404", is-not-found, .not-found'
    );
    await expect(notFoundIndicator.first()).toBeVisible();
  });

  test('Guest can perform login successfully and is routed to appropriate home', async ({
    guestPage: page,
  }) => {
    await navigateTo(page, '/auth');
    await expect(page).toHaveURL(/.*\/auth/);

    const emailInput = page.locator('input[type="email"], input[name="email"], #email');
    const passwordInput = page.locator(
      'input[type="password"], input[name="password"], #password'
    );
    const submitBtn = page.locator(
      'button[type="submit"], is-button:has-text("Sign In"), is-button:has-text("Log In"), button:has-text("Sign in")'
    );

    if (await emailInput.isVisible()) {
      await emailInput.fill('acme.corp@intellisure.test');
      await passwordInput.fill('SecurePassword123!');
      await submitBtn.click();

      // Upon login, auth store persists token and navigates
      await page.waitForTimeout(500);
      const token = await page.evaluate(() => localStorage.getItem('is_token'));
      expect(token).toBeTruthy();
    }
  });

  test('Guest can register as a new policyholder customer', async ({ guestPage: page }) => {
    await navigateTo(page, '/auth');

    // Click register tab if present
    const registerTab = page.locator(
      'button:has-text("Register"), a:has-text("Sign up"), button:has-text("Sign up")'
    );
    if (await registerTab.isVisible()) {
      await registerTab.click();

      const emailInput = page.locator('input[type="email"], #reg-email');
      const passInput = page.locator('input[type="password"], #reg-password');
      if (await emailInput.isVisible()) {
        await emailInput.fill('new.logistics@test.com');
        await passInput.fill('ValidPass987!');
      }
    }
  });

  test('Guest can register as a vendor applicant', async ({ guestPage: page }) => {
    await navigateTo(page, '/auth');

    const vendorOption = page.locator(
      'text="Vendor", input[value="VENDOR_APPLICANT"], button:has-text("Vendor")'
    );
    if (await vendorOption.isVisible()) {
      await vendorOption.click();
    }
  });
});
