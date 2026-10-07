# IntelliSure - Agent Instructions

## Architecture Overview

Angular 17 monorepo with Module Federation (MFE). Single shell app + 4 remote MFEs + shared libraries.

```
frontend/
├── src/                    # shell-app (host)
├── projects/
│   ├── auth-mfe           # Port 4201 (remote)
│   ├── claims-mfe         # Port 4202 (remote)
│   ├── intelligence-mfe   # Port 4203 (remote)
│   └── vendor-mfe         # Port 4205 (remote)
└── libs/
    ├── ui-core            # Design system (Hartford theme)
    ├── auth               # Auth state/effects
    ├── charting           # Chart components
    ├── data-access        # API services
    ├── shared-models      # TypeScript interfaces
    └── store              # NgRx store
```

## Key Commands

Run from `frontend/` directory:

```bash
# Build all (production)
npm run build                # shell-app only
npm run build:auth
npm run build:claims
npm run build:intelligence
npm run build:vendor

# Dev servers (run separately in different terminals)
npm run start:shell          # http://localhost:4200
npm run start:auth           # http://localhost:4201
npm run start:claims         # http://localhost:4202
npm run start:intelligence   # http://localhost:4203
npm run start:vendor         # http://localhost:4205

# Run all MFEs together (module federation dev server)
npm run run:all

# Test
npm run test

# Watch mode (dev)
npm run watch
```

## Build System Quirks

- **ngx-build-plus** used instead of standard Angular builder (custom webpack config per project)
- **Common chunk disabled** (`commonChunk: false`) - each MFE bundles its own dependencies
- **TypeScript paths** map to `./dist/<lib>` - must build libraries before consuming apps
- **Budget limits**: component styles max 10kb warning / 20kb error (increased from default 2kb/4kb)

## Library Development

```bash
# Build a library
ng build ui-core

# Library output goes to dist/ui-core (referenced by TS paths)
# Always rebuild library after changes before testing in apps
```

## Design System (ui-core)

Hartford-inspired enterprise insurance theme in `libs/ui-core/src/lib/design-tokens.ts`:

```typescript
// Primary colors
claret:     #75013F  // Primary brand
fuchsia:    #FE3082  // Accent only
warm:       #EAE5DF  // Borders, dividers
warm-light: #F7F5F3  // Background
ink:        #000000  // Text
```

**Import from ui-core** (not local copies):
```typescript
import { ButtonComponent, CardComponent, TableComponent } from 'ui-core';
```

Global styles loaded via `angular.json` → `src/styles.css` (imports Tailwind + design tokens).

## State Management

- **NgRx** for global state (`libs/store`, `libs/auth`)
- Selectors in `core/store/*/selectors.ts`
- Actions in `core/store/*/actions.ts`
- Effects in `core/store/*/effects.ts`

## Module Federation

- Shell app loads MFEs via `loadRemoteModule()`
- Remote entries defined in `webpack.config.js` per project
- Dev: `http://localhost:420X/remoteEntry.js`
- Route config in `src/app/features/*/shell.routes.ts`

## Testing

```bash
ng test                          # All projects
ng test ui-core                  # Single library
ng test shell-app                # Shell app only
```

Karma + Jasmine. No e2e configured.

## Common Gotchas

| Issue | Fix |
|-------|-----|
| `Cannot find module 'ui-core'` | Run `ng build ui-core` first |
| MFE not loading in dev | Ensure all `npm run start:*` servers running |
| Style budget exceeded | Increase `anyComponentStyle` budget in angular.json or move styles to global |
| TS path not resolving | Rebuild library (`ng build <lib>`) |
| `inject()` not found | Import from `@angular/core` |

## File Conventions

- Standalone components only (no NgModules)
- Components in `libs/ui-core` are the canonical UI primitives
- Feature dashboards use `FeatureDashboardComponent` with `DashboardConfig` input
- Routes are lazy-loaded; MFE routes loaded via `loadRemoteModule`