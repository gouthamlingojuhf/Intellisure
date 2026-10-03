import { Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/module-federation';

/** Shell mounts the authMfe remote (dev: http://localhost:4201/remoteEntry.js). */
export const AUTH_SHELL_ROUTES: Routes = [
  {
    path: '**',
    loadChildren: () =>
      loadRemoteModule({
        type: 'module',
        remoteEntry: 'http://localhost:4201/remoteEntry.js',
        exposedModule: './Routes',
      }).then((m) => m.AUTH_REMOTE_ROUTES),
  },
];
