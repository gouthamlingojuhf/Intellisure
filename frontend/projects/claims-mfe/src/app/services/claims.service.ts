import { Injectable } from '@angular/core';
import { ClaimRecord } from '../models/claim.models';

@Injectable({ providedIn: 'root' })
export class ClaimsService {
  private readonly claims: ClaimRecord[] = [
    {
      claimId: 'CLM-1001',
      policyNumber: 'POL-4421',
      claimantName: 'Ava Thompson',
      claimType: 'AUTO_COLLISION',
      lossDate: '2026-09-18',
      claimedAmount: 18450,
      status: 'UNDER_REVIEW',
      adjuster: 'M. Romero',
      reserveAmount: 15000,
      description: 'Rear-end collision with structural damage and towing service required.',
    },
    {
      claimId: 'CLM-1002',
      policyNumber: 'POL-8037',
      claimantName: 'Marcus Hill',
      claimType: 'PROPERTY_DAMAGE',
      lossDate: '2026-09-22',
      claimedAmount: 32000,
      status: 'APPROVED',
      adjuster: 'J. Patel',
      reserveAmount: 27000,
      description: 'Storm damage to roof and exterior siding with temporary mitigation.'
    },
    {
      claimId: 'CLM-1003',
      policyNumber: 'POL-1189',
      claimantName: 'Noah Patel',
      claimType: 'MEDICAL',
      lossDate: '2026-09-10',
      claimedAmount: 9600,
      status: 'FILED',
      adjuster: 'S. Park',
      reserveAmount: 8200,
      description: 'Emergency treatment following a slip and fall at insured property.',
    },
    {
      claimId: 'CLM-1004',
      policyNumber: 'POL-7710',
      claimantName: 'Elena Garcia',
      claimType: 'AUTO_THEFT',
      lossDate: '2026-08-30',
      claimedAmount: 23840,
      status: 'SETTLED',
      adjuster: 'R. Khan',
      reserveAmount: 23840,
      description: 'Recovered vehicle total loss settlement with finance balance offset.',
    },
  ];

  getClaims(): ClaimRecord[] {
    return [...this.claims];
  }

  getClaim(claimId: string): ClaimRecord | undefined {
    return this.claims.find((claim) => claim.claimId === claimId);
  }

  addClaim(payload: Omit<ClaimRecord, 'claimId' | 'status' | 'adjuster' | 'reserveAmount'>): ClaimRecord {
    const nextId = `CLM-${String(this.claims.length + 1000)}`;
    const newClaim: ClaimRecord = {
      claimId: nextId,
      policyNumber: payload.policyNumber,
      claimantName: payload.claimantName,
      claimType: payload.claimType,
      lossDate: payload.lossDate,
      claimedAmount: payload.claimedAmount,
      status: 'FILED',
      adjuster: 'Unassigned',
      reserveAmount: Math.round(payload.claimedAmount * 0.8),
      description: payload.description,
    };
    this.claims.unshift(newClaim);
    return newClaim;
  }
}
