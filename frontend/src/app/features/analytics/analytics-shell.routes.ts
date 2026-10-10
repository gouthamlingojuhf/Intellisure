import { Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/module-federation';

/** Shell mounts the intelligence MFE remote (dev: http://localhost:4203/remoteEntry.js). */
export const ANALYTICS_SHELL_ROUTES: Routes = [
  {
    path: '',
    loadChildren: () =>
      loadRemoteModule({
        type: 'module',
        remoteEntry: 'http://localhost:4203/remoteEntry.js',
        exposedModule: './Routes',
      }).then((m) => m.ANALYTICS_REMOTE_ROUTES),
  },
];
