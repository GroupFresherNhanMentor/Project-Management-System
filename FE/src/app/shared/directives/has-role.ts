import { Directive, Input, OnInit, TemplateRef, ViewContainerRef, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth';
import { SystemRole } from '../../core/models/api.model';

@Directive({
  selector: '[appHasRole]',
})
export class HasRole implements OnInit {
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainer = inject(ViewContainerRef);
  private readonly authService = inject(AuthService);

  @Input({ required: true }) appHasRole!: SystemRole | SystemRole[];

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    const roles = Array.isArray(this.appHasRole) ? this.appHasRole : [this.appHasRole];
    this.viewContainer.clear();
    if (user && roles.includes(user.role)) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    }
  }
}
