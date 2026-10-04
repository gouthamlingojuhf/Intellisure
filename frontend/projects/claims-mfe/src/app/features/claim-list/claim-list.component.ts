import { CurrencyPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ClaimRecord } from '../../models/claim.models';
import { ClaimsService } from '../../services/claims.service';

@Component({
  selector: 'claims-list',
  standalone: true,
  imports: [RouterLink, CurrencyPipe],
  template: `
    <section class="card">
      <h1>Claim queue</h1>
      <a routerLink="new">File new claim</a>
      <table>
        <thead>
          <tr>
            <th>Claim</th>
            <th>Policy</th>
            <th>Claimant</th>
            <th>Type</th>
            <th>Status</th>
            <th>Amount</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          @for (claim of claims; track claim.claimId) {
            <tr>
              <td>{{ claim.claimId }}</td>
              <td>{{ claim.policyNumber }}</td>
              <td>{{ claim.claimantName }}</td>
              <td>{{ claim.claimType }}</td>
              <td>{{ claim.status }}</td>
              <td>{{ claim.claimedAmount | currency:'USD':'symbol':'1.0-0' }}</td>
              <td><a [routerLink]="['/', claim.claimId]">Open</a></td>
            </tr>
          }
        </tbody>
      </table>
    </section>
  `,
})
export class ClaimListComponent implements OnInit {
  claims: ClaimRecord[] = [];

  constructor(private readonly claimsService: ClaimsService) {}

  ngOnInit(): void {
    this.claims = this.claimsService.getClaims();
  }
}
