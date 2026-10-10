import { test as base, Page } from '@playwright/test';
import { TEST_USERS, TestUser } from './mock-data';
import { setupApiMocks } from './api-mocks';

export interface ActorFixtures {
  policyholderPage: Page;
  underwriterPage: Page;
  riskEngineerPage: Page;
  claimsAdjusterPage: Page;
  claimsManagerPage: Page;
  vendorManagerPage: Page;
  vendorApplicantPage: Page;
  adminPage: Page;
  guestPage: Page;
}

/**
 * Seeds session tokens and role metadata into localStorage before Angular hydrates.
 */
export async function authenticateActor(page: Page, user: TestUser): Promise<void> {
  await page.addInitScript(
    (userData) => {
      localStorage.setItem('is_token', userData.token);
      localStorage.setItem('is_role', userData.role);
      localStorage.setItem('is_user_id', userData.userId);
      localStorage.setItem('is_email', userData.email);
      if (userData.customerId) {
        localStorage.setItem('is_customer_id', userData.customerId);
      } else {
        localStorage.removeItem('is_customer_id');
      }
    },
    user
  );
  await setupApiMocks(page, user);
}

export const test = base.extend<ActorFixtures>({
  guestPage: async ({ page }, use) => {
    await page.addInitScript(() => {
      localStorage.clear();
    });
    await setupApiMocks(page, TEST_USERS['POLICYHOLDER']);
    await use(page);
  },

  policyholderPage: async ({ page }, use) => {
    await authenticateActor(page, TEST_USERS['POLICYHOLDER']);
    await use(page);
  },

  underwriterPage: async ({ page }, use) => {
    await authenticateActor(page, TEST_USERS['UNDERWRITER']);
    await use(page);
  },

  riskEngineerPage: async ({ page }, use) => {
    await authenticateActor(page, TEST_USERS['RISK_ENGINEER']);
    await use(page);
  },

  claimsAdjusterPage: async ({ page }, use) => {
    await authenticateActor(page, TEST_USERS['CLAIMS_ADJUSTER']);
    await use(page);
  },

  claimsManagerPage: async ({ page }, use) => {
    await authenticateActor(page, TEST_USERS['CLAIMS_MANAGER']);
    await use(page);
  },

  vendorManagerPage: async ({ page }, use) => {
    await authenticateActor(page, TEST_USERS['VENDOR_MANAGER']);
    await use(page);
  },

  vendorApplicantPage: async ({ page }, use) => {
    await authenticateActor(page, TEST_USERS['VENDOR_APPLICANT']);
    await use(page);
  },

  adminPage: async ({ page }, use) => {
    await authenticateActor(page, TEST_USERS['ADMIN']);
    await use(page);
  },
});

export { expect } from '@playwright/test';
