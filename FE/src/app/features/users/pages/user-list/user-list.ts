import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../../core/services/user';
import { UserDto, UserListParams } from '../../../../core/models/user.model';
import { PageResponse } from '../../../../core/models/api.model';

@Component({
  selector: 'app-user-list',
  imports: [CommonModule],
  templateUrl: './user-list.html',
})
export class UserList implements OnInit {
  private readonly userService = inject(UserService);

  readonly data = signal<PageResponse<UserDto> | null>(null);
  readonly params = signal<UserListParams>({ page: 0, size: 20 });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.userService.getUsers(this.params()).subscribe({
      next: res => this.data.set(res),
    });
  }
}
