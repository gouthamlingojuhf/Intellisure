import { Component, Input } from '@angular/core';

@Component({
  selector: 'is-remote-placeholder',
  standalone: true,
  template: `
    <section class="card">
      <h1 class="text-xl font-bold text-blue-900">{{ title }}</h1>
      <p class="mt-2 text-gray-700">
        This remote microfrontend is not mounted yet. It will load via Module Federation
        from its dev server once implemented.
      </p>
    </section>
  `,
})
export class RemotePlaceholderComponent {
  @Input() title = 'Remote MFE';
}
