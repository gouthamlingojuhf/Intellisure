import { Routes } from '@angular/router';
import { RemotePlaceholderComponent } from '../remote-placeholder/remote-placeholder.component';

export const ANALYTICS_SHELL_ROUTES: Routes = [{ path: '**', component: RemotePlaceholderComponent }];
