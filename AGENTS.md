# IntelliSure Agent Instructions

## Repository map

- `frontend/` is an Angular 17 Module Federation workspace with a shell, four active remote MFEs, and shared libraries.
- `api-gateway/`, `eureka/`, and the service directories under the repository root are independent Spring Boot 4.1.1 applications targeting Java 17.
- Keep business rules in service classes, HTTP concerns in controllers, persistence in repositories, and API shapes in DTOs/entities/mappers.
- Use the existing service documentation for domain-specific behavior; do not duplicate it here.

Relevant documentation:

- [Frontend architecture](Docs/MICROFRONTEND_ARCHITECTURE_DOCUMENTATION.md)
- [API gateway](Docs/API_GATEWAY_DOCUMENTATION.md)
- [Service documentation](Docs/CLAIMS_SERVICE_DOCUMENTATION.md)
- [Business blueprint](Docs/IntelliSure_Insurance_Lifecycle_Business_Blueprint.md)

## Backend conventions

- Services use Spring WebFlux, Reactor `Mono`/`Flux`, Spring Security, and Eureka discovery. Preserve reactive behavior; avoid blocking calls in request paths.
- Each service has its own Maven module with a wrapper. Run commands from the service directory unless the task explicitly targets the root orchestration scripts.
- Follow the existing package layout: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `config`, `security`, `filter`, `exception`, and `client`.
- Use MapStruct for entity/DTO mapping and Lombok for conventional boilerplate only when the surrounding module already uses it.
- Validate request bodies with Jakarta Validation and return domain-specific exceptions through the module's global exception handler.
- Reuse the existing `SecurityActorService` and role checks; do not bypass authorization or expose caller-specific data.
- Preserve correlation IDs, JWT propagation, and authorization headers across gateway, service, and WebClient flows.
- Treat database identifiers and UUID byte conversion as service-local concerns; keep schemas and migrations aligned with the existing persistence model.
- Use the module's existing tests and add focused tests for changed behavior. Prefer real service/repository behavior where practical; mock only the lowest external boundary needed.

Maven commands:

```powershell
# Build and test one service
cd claims-service
./mvnw test

# Build all backend modules without running tests
./build_all.ps1

# Run all backend services in separate Windows terminals
./run_all_services.ps1
```

The root build script invokes Maven `clean install -DskipTests` for every backend module. Use targeted module tests for changes and run the relevant full test command before completion.

## Frontend conventions

- The shell and each MFE are separate Angular projects. Keep remote route exports in `*-remote.routes.ts` and mount them through the shell's lazy route loaders.
- Use standalone Angular components and the existing `ui-core` library for canonical design-system primitives. Do not copy UI components into feature modules.
- Keep shared interfaces in `libs/shared-models`; keep frontend API clients in the owning feature or `libs/data-access`.
- Use NgRx actions, reducers, selectors, and effects for application state. Keep effects responsible for side effects and reducers responsible for state transitions.
- Use `inject()` from `@angular/core`; keep dependency injection constructor-based where the surrounding code does so.
- Preserve Module Federation entry points in each project's `webpack.config.js`; `commonChunk: false` is intentional.
- Do not change the configured ports or remote entry URLs without updating the shell routes, webpack configuration, and startup documentation.

Frontend commands from `frontend/`:

```powershell
npm install
npm run build                 # shell only
npm run build:auth
npm run build:claims
npm run build:intelligence
npm run build:vendor
npm run test
npm run start:shell           # 4200
npm run start:auth            # 4201
npm run start:claims          # 4202
npm run start:intelligence    # 4203
npm run start:vendor          # 4205
npm run run:all               # all MFEs through the Module Federation dev server
```

Build and test rules:

- Build shared libraries before applications that consume them. In particular, build `ui-core` before dependent frontend verification when its public API changes.
- `ngx-build-plus` is required for the shell and MFEs; do not replace it with the standard Angular builder without updating the custom webpack integration.
- Preserve the configured production budgets in `angular.json`; if a component style exceeds the limit, prefer moving styles to the shared/global layer or justify a scoped budget change.
- Karma + Jasmine is the current frontend test stack; no e2e suite is configured.
- Run `npm run test` for frontend changes and the relevant Maven test command for backend changes. Do not claim a build or test passed without running it.

## Change workflow

1. Identify the owning module and inspect its existing tests and documentation before editing.
2. Make the smallest behavior-preserving change. Update API documentation only when the external contract changes.
3. Preserve existing security, role, error, and correlation behavior.
4. Run the narrowest relevant test/build command, then run broader verification when the change affects a shared library, API contract, or Module Federation boundary.
5. Review the diff for accidental generated files, secrets, logs, and stale port or route references.

## Common pitfalls

- A frontend library change can fail in consumers when `dist/` has not been rebuilt.
- MFE changes are not complete when only the remote project builds; verify the shell route and remote entry integration.
- Backend changes that only compile may still break reactive request flows, authorization, or WebClient token propagation.
- Existing service docs may contain stale ports or routes; verify them against the current code and configuration before relying on them.
- Never commit credentials, tokens, private keys, or local environment values. The repository's `Credentials.txt` is not a template for new secrets.
