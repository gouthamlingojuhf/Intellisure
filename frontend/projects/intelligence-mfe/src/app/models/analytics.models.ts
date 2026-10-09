export interface ExecutiveDashboardSummary {
  summaryId: string;
  totalWrittenPremium: number;
  totalEarnedPremium: number;
  totalIncurredLosses: number;
  lossRatioPercentage: number;
  claimsFrequency: number;
  netSubrogationYield: number;
  activePolicyCount: number;
  totalClaimsFiled: number;
  openClaimsCount: number;
  closedClaimsCount: number;
  calculatedAt: string;
  periodStart: string;
  periodEnd: string;
}
