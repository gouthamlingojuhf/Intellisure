import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { ButtonComponent } from 'ui-core';
import { selectIsAuthenticated } from '../../core/store/auth/auth.selectors';

import { LandingHeaderComponent } from './components/landing-header.component';
import { HeroVisualComponent } from './components/hero-visual.component';
import { TrustStripComponent } from './components/trust-strip.component';
import { LifecycleFlowComponent } from './components/lifecycle-flow.component';
import { CoreCapabilitiesComponent } from './components/core-capabilities.component';
import { HowItWorksComponent } from './components/how-it-works.component';
import { IntelligenceDemoComponent } from './components/intelligence-demo.component';
import { RoleExperienceComponent } from './components/role-experience.component';
import { FinalCtaComponent } from './components/final-cta.component';
import { LandingFooterComponent } from './components/landing-footer.component';

@Component({
  selector: 'is-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ButtonComponent,
    LandingHeaderComponent,
    HeroVisualComponent,
    TrustStripComponent,
    LifecycleFlowComponent,
    CoreCapabilitiesComponent,
    HowItWorksComponent,
    IntelligenceDemoComponent,
    RoleExperienceComponent,
    FinalCtaComponent,
    LandingFooterComponent,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent {
  private readonly store = inject(Store);
  readonly isAuthenticated$ = this.store.select(selectIsAuthenticated);
}
