/*
 * Public API Surface of ui-core
 */

// Design tokens
export * from './lib/design-tokens';

// Styles - CSS is handled via angular.json styles array

// Components
export { ButtonComponent } from './lib/button/button.component';
export type { ButtonVariant, ButtonSize } from './lib/button/button.component';

export { CardComponent } from './lib/card/card.component';

export { BadgeComponent } from './lib/badge/badge.component';
export type { BadgeVariant, BadgeSize } from './lib/badge/badge.component';

export { TableComponent } from './lib/table/table.component';
export type { TableColumn, TableAction } from './lib/table/table.component';

export { InputComponent } from './lib/form/input.component';
export { SelectComponent } from './lib/form/select.component';
export { TextareaComponent } from './lib/form/textarea.component';

export { SkeletonComponent } from './lib/skeleton/skeleton.component';
export type { SkeletonVariant } from './lib/skeleton/skeleton.component';

export { ToastComponent } from './lib/toast/toast.component';
export { ToastStackComponent } from './lib/toast/toast-stack.component';
export type { Toast, ToastKind } from './lib/toast/toast.component';

export { EmptyStateComponent } from './lib/empty-state/empty-state.component';
export type { EmptyStateSize } from './lib/empty-state/empty-state.component';

export { ModalComponent } from './lib/modal/modal.component';
export type { ModalSize } from './lib/modal/modal.component';

export { SidebarComponent } from './lib/layout/sidebar.component';
export type { NavItem } from './lib/layout/sidebar.component';

export { TopbarComponent } from './lib/layout/topbar.component';
export type { BreadcrumbItem, Notification, UserMenuItem } from './lib/layout/topbar.component';

export { BreadcrumbComponent } from './lib/layout/breadcrumb.component';
export type { BreadcrumbItem as BreadcrumbItemType } from './lib/layout/breadcrumb.component';

export { StepperComponent } from './lib/stepper/stepper.component';
export type { StepperStep } from './lib/stepper/stepper.component';