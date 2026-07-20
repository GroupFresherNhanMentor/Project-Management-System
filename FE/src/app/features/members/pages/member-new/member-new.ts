import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProjectRole } from '../../../../core/models/api.model';

const MOCK_AVAILABLE_USERS = [
  { id: 'u6', fullName: 'Nam Vo' },
  { id: 'u7', fullName: 'Trang Nguyen' },
  { id: 'u8', fullName: 'Duc Phan' },
];

@Component({
  selector: 'app-member-new',
  imports: [FormsModule],
  templateUrl: './member-new.html',
})
export class MemberNew {
  private readonly router = inject(Router);

  readonly availableUsers = MOCK_AVAILABLE_USERS;
  readonly roles: ProjectRole[] = ['PM', 'DEV', 'TESTER'];

  userId     = '';
  role: ProjectRole = 'DEV';
  submitting = false;

  submit(): void { void this.router.navigate(['/members']); }
  cancel(): void { void this.router.navigate(['/members']); }
}
