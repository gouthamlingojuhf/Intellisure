import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { authActions } from './core/store/auth/auth.actions';
import { selectIsAuthenticated, selectUserRole } from './core/store/auth/auth.selectors';
import { selectGlobalLoading, selectToasts } from './core/store/ui/ui.selectors';
import { uiActions } from './core/store/ui/ui.actions';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, AsyncPipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  private readonly store = inject(Store);
  readonly title = 'IntelliSure Shell';
  readonly isAuthenticated$ = this.store.select(selectIsAuthenticated);
  readonly role$ = this.store.select(selectUserRole);
  readonly loading$ = this.store.select(selectGlobalLoading);
  readonly toasts$ = this.store.select(selectToasts);

  logout(): void {
    this.store.dispatch(authActions.logout());
  }

  dismiss(id: number): void {
    this.store.dispatch(uiActions.dismissToast({ id }));
  }
}
