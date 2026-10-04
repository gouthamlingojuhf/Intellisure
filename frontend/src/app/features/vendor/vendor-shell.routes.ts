import { Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/module-federation';

/** Shell mounts the vendorMfe remote (dev: http://localhost:4205/remoteEntry.js). */
export const VENDOR_SHELL_ROUTES: Routes = [
  {
    path: '**',
    loadChildren: () =>
      loadRemoteModule({
        type: 'module',
        remoteEntry: 'http://localhost:4205/remoteEntry.js',
        exposedModule: './Routes',
      }).then((m) => m.VENDOR_REMOTE_ROUTES),
  },
];
