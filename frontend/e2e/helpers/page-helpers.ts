import { Page, expect } from '@playwright/test';

/**
 * Common test helper functions for IntelliSure UI interactions.
 */

export async function navigateTo(page: Page, path: string): Promise<void> {
  await page.goto(path);
  // Wait for Angular shell to hydrate
  await page.waitForLoadState('domcontentloaded');
}

export async function toggleTheme(page: Page): Promise<boolean> {
  const themeToggle = page.locator('.theme-toggle-btn');
  await expect(themeToggle).toBeVisible();
  await themeToggle.click();
  const isDark = await page.evaluate(() => document.documentElement.classList.contains('dark'));
  return isDark;
}

export async function triggerSearchShortcut(page: Page): Promise<void> {
  const isMac = process.platform === 'darwin';
  if (isMac) {
    await page.keyboard.press('Meta+k');
  } else {
    await page.keyboard.press('Control+k');
  }
}

export async function verifyStickyHeader(page: Page): Promise<void> {
  const header = page.locator('header.topbar');
  await expect(header).toBeVisible();

  // Scroll down 500px
  await page.evaluate(() => window.scrollTo(0, 500));
  await page.waitForTimeout(200);

  // Assert topbar bounding box top is still at or near 0
  const box = await header.boundingBox();
  expect(box).not.toBeNull();
  if (box) {
    expect(box.y).toBeLessThanOrEqual(5);
  }
}
