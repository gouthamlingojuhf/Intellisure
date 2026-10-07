import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { CardComponent, ButtonComponent, BadgeComponent, TableComponent, TableColumn, TableAction, EmptyStateComponent } from 'ui-core';

interface Vendor {
  id: string;
  name: string;
  category: string;
  status: string;
  rating: number;
  contact: string;
  lastActive: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CardComponent, ButtonComponent, BadgeComponent, TableComponent, EmptyStateComponent],
  template: `
    <div class="vendor-shell">
      <header class="vendor-header">
        <div class="header-content">
          <h1>Vendor & Partner Management</h1>
          <p>Manage vendor relationships, performance, and compliance across the network.</p>
        </div>
        <is-button variant="primary" routerLink="/vendors/new">Add vendor</is-button>
      </header>

      <main class="vendor-main">
        <is-card title="Vendor directory" subtitle="Active vendors in the network">
          <is-table
            [columns]="columns"
            [data]="vendors"
            [actions]="actions"
            [trackByFn]="trackById"
            [emptyMessage]="'No vendors found'"
          />
        </is-card>
      </main>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background: var(--warm-light);
    }
    
    .vendor-shell {
      max-width: 1540px;
      margin: 0 auto;
      padding: 32px 24px;
    }
    
    .vendor-header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 24px;
      margin-bottom: 32px;
      padding-bottom: 16px;
      border-bottom: 1px solid var(--border);
    }
    
    .header-content h1 {
      margin: 0 0 8px;
      color: var(--ink);
      font-size: clamp(25px, 2.5vw, 34px);
      line-height: 1.08;
      letter-spacing: -0.045em;
    }
    
    .header-content p {
      margin: 0;
      color: var(--muted);
      font-size: 12px;
      line-height: 1.6;
    }
    
    .vendor-main {
      display: grid;
      gap: 20px;
    }
  `],
})
export class AppComponent {
  vendors: Vendor[] = [
    { id: 'V-001', name: 'Crawford & Company', category: 'Claims Adjusting', status: 'Active', rating: 4.8, contact: 'john.smith@crawford.com', lastActive: '2 hours ago' },
    { id: 'V-002', name: 'Sedgwick CMS', category: 'TPA Services', status: 'Active', rating: 4.6, contact: 'sarah.jones@sedgwick.com', lastActive: '5 hours ago' },
    { id: 'V-003', name: 'Crawford TPA Solutions', category: 'Claims Management', status: 'Active', rating: 4.7, contact: 'mike.wilson@crawford.com', lastActive: '1 day ago' },
    { id: 'V-004', name: 'GAB Robins', category: 'Adjusting', status: 'Pending Review', rating: 4.3, contact: 'lisa.brown@gabrobins.com', lastActive: '3 days ago' },
    { id: 'V-005', name: 'EFI Global', category: 'Engineering & Investigations', status: 'Active', rating: 4.9, contact: 'david.lee@efiglobal.com', lastActive: '4 hours ago' },
    { id: 'V-006', name: 'J.S. Held', category: 'Forensic Accounting', status: 'Inactive', rating: 4.2, contact: 'karen.davis@jsheld.com', lastActive: '2 weeks ago' },
  ];

  columns: TableColumn<Vendor>[] = [
    { key: 'name', header: 'Vendor', width: '200px' },
    { key: 'category', header: 'Category', width: '180px' },
    { key: 'status', header: 'Status', width: '140px', render: this.renderStatus.bind(this) },
    { key: 'rating', header: 'Rating', width: '80px', align: 'center', render: this.renderRating.bind(this) },
    { key: 'contact', header: 'Primary contact', width: '220px' },
    { key: 'lastActive', header: 'Last active', width: '140px' },
  ];

  actions: TableAction<Vendor>[] = [
    { label: 'View', variant: 'text', handler: (row) => {} },
    { label: 'Edit', variant: 'text', handler: (row) => {} },
  ];

  trackById(index: number, item: Vendor): string {
    return item.id;
  }

  renderStatus(row: Vendor, value: string): string {
    const statusMap: Record<string, 'success' | 'warning' | 'danger' | 'info' | 'neutral'> = {
      'Active': 'success',
      'Pending Review': 'warning',
      'Inactive': 'neutral',
    };
    const variant = statusMap[value] || 'neutral';
    return `<is-badge variant="${variant}" size="sm">${value}</is-badge>`;
  }

  renderRating(row: Vendor, value: number): string {
    const stars = '★'.repeat(Math.floor(value)) + '☆'.repeat(5 - Math.floor(value));
    return `<span style="color: var(--warning); font-size: 12px;">${stars}</span> ${value.toFixed(1)}`;
  }
}