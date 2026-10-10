import { test, expect } from '../fixtures/test-base';
import { navigateTo } from '../helpers/page-helpers';

test.describe('Usecase Suite 02: Role Access Control & Employee Profile Guarding', () => {
  test('Policyholder cannot access administrative or underwriting workspaces', async ({
    policyholderPage: page,
  }) => {
    // Attempt accessing /admin
    await page.goto('/admin');
    await page.waitForLoadState('domcontentloaded');
    // Role guard redirects unauthorized users to /
    expect(page.url()).not.toContain('/admin');

    // Attempt accessing /underwriting
    await page.goto('/underwriting');
    await page.waitForLoadState('domcontentloaded');
    expect(page.url()).not.toContain('/underwriting');

    // Attempt accessing /analytics
    await page.goto('/analytics');
    await page.waitForLoadState('domcontentloaded');
    expect(page.url()).not.toContain('/analytics');
  });

  test('CRITICAL: Underwriter is NEVER shown Business Profile link and cannot access /profile', async ({
    underwriterPage: page,
  }) => {
    await navigateTo(page, '/dashboard');

    // 1. Assert navigation sidebar DOES NOT contain 'Business Profile'
    const profileNavLink = page.locator('nav a:has-text("Business Profile"), a[href="/profile"]');
    await expect(profileNavLink).toHaveCount(0);

    // 2. Directly navigating to /profile should be BLOCKED by roleGuard
    await page.goto('/profile');
    await page.waitForLoadState('domcontentloaded');

    // URL must not remain at /profile
    expect(page.url()).not.toContain('/profile');
  });

  test('CRITICAL: Risk Engineer is NEVER shown Business Profile link and cannot access /profile', async ({
    riskEngineerPage: page,
  }) => {
    await navigateTo(page, '/dashboard');

    const profileNavLink = page.locator('nav a:has-text("Business Profile"), a[href="/profile"]');
    await expect(profileNavLink).toHaveCount(0);

    await page.goto('/profile');
    await page.waitForLoadState('domcontentloaded');
    expect(page.url()).not.toContain('/profile');
  });

  test('CRITICAL: Claims Adjuster is NEVER shown Business Profile link and cannot access /profile', async ({
    claimsAdjusterPage: page,
  }) => {
    await navigateTo(page, '/dashboard');

    const profileNavLink = page.locator('nav a:has-text("Business Profile"), a[href="/profile"]');
    await expect(profileNavLink).toHaveCount(0);

    await page.goto('/profile');
    await page.waitForLoadState('domcontentloaded');
    expect(page.url()).not.toContain('/profile');
  });

  test('CRITICAL: Admin is NEVER shown Business Profile link and cannot access /profile', async ({
    adminPage: page,
  }) => {
    await navigateTo(page, '/dashboard');

    const profileNavLink = page.locator('nav a:has-text("Business Profile"), a[href="/profile"]');
    await expect(profileNavLink).toHaveCount(0);

    await page.goto('/profile');
    await page.waitForLoadState('domcontentloaded');
    expect(page.url()).not.toContain('/profile');
  });

  test('Policyholder IS allowed to access Business Profile at /profile', async ({
    policyholderPage: page,
  }) => {
    await navigateTo(page, '/dashboard');

    // Navigation contains Business Profile for Policyholder
    const profileNavLink = page.locator('nav a:has-text("Business Profile"), a[href="/profile"]');
    await expect(profileNavLink.first()).toBeVisible();

    // Navigating directly to /profile succeeds
    await page.goto('/profile');
    await page.waitForLoadState('domcontentloaded');
    await expect(page).toHaveURL(/.*\/profile/);
  });

  test('Claims Adjuster cannot access /underwriting or /admin', async ({
    claimsAdjusterPage: page,
  }) => {
    await page.goto('/underwriting');
    await page.waitForLoadState('domcontentloaded');
    expect(page.url()).not.toContain('/underwriting');

    await page.goto('/admin');
    await page.waitForLoadState('domcontentloaded');
    expect(page.url()).not.toContain('/admin');
  });

  test('Vendor Manager can access /vendor but cannot access /admin', async ({
    vendorManagerPage: page,
  }) => {
    await navigateTo(page, '/vendor');
    await expect(page).toHaveURL(/.*\/vendor/);

    await page.goto('/admin');
    await page.waitForLoadState('domcontentloaded');
    expect(page.url()).not.toContain('/admin');
  });
});
