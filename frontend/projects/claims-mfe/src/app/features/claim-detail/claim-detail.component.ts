import { CurrencyPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ClaimRecord } from '../../models/claim.models';
import { ClaimsService } from '../../services/claims.service';

@Component({
  selector: 'claim-detail',
  standalone: true,
  imports: [CurrencyPipe],
  template: `
    @if (claim) {
      <section class="card">
        <h1>{{ claim.claimId }}</h1>
        <p>Policy: {{ claim.policyNumber }}</p>
        <p>Claimant: {{ claim.claimantName }}</p>
        <p>Type: {{ claim.claimType }}</p>
        <p>Loss date: {{ claim.lossDate }}</p>
        <p>Status: {{ claim.status }}</p>
        <p>Adjuster: {{ claim.adjuster }}</p>
        <p>Claimed: {{ claim.claimedAmount | currency:'USD':'symbol':'1.0-0' }}</p>
        <p>Reserve: {{ claim.reserveAmount | currency:'USD':'symbol':'1.0-0' }}</p>
        <p>Description: {{ claim.description }}</p>
      </section>
    } @else {
      <section class="card">Claim not found.</section>
    }
  `,
})
export class ClaimDetailComponent implements OnInit {
  claim: ClaimRecord | undefined;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly claimsService: ClaimsService,
  ) {}

  ngOnInit(): void {
    const claimId = this.route.snapshot.paramMap.get('claimId') ?? '';
    this.claim = this.claimsService.getClaim(claimId);
  }
}
