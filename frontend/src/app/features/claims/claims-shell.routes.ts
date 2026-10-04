import { Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/module-federation';

/** Shell mounts the claims-ops MFE remote (dev: http://localhost:4202/remoteEntry.js). */
export const CLAIMS_SHELL_ROUTES: Routes = [
  {
    path: '**',
    loadChildren: () =>
      loadRemoteModule({
        type: 'module',
        remoteEntry: 'http://localhost:4202/remoteEntry.js',
        exposedModule: './Routes',
      }).then((m) => m.CLAIMS_REMOTE_ROUTES),
  },
];
