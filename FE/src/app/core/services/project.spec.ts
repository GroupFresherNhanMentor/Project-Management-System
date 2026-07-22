import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ProjectService } from './project';
import { PageResponse } from '../models/api.model';
import { ProjectDto } from '../models/project.model';
import { ProjectMemberCandidateDto, ProjectMemberDto } from '../models/project-member.model';

describe('ProjectService', () => {
  let service: ProjectService;
  let http: HttpTestingController;

  const project: ProjectDto = {
    id: '5ef81d32-1426-46ad-b086-80b1c7c97020',
    projectCode: 'WEB',
    projectName: 'Web Project',
    description: null,
    startDate: '2026-07-01',
    endDate: '2026-12-31',
    status: 'ACTIVE',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProjectService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProjectService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads projects with filters and unwraps the page response', () => {
    const page: PageResponse<ProjectDto> = {
      items: [project], totalElements: 1, totalPages: 1, pageNumber: 0, pageSize: 20,
    };
    let actual: PageResponse<ProjectDto> | undefined;

    service.getProjects({ keyword: 'web', status: 'ACTIVE', page: 0, size: 20 })
      .subscribe(result => actual = result);

    const request = http.expectOne(req => req.url === '/api/projects');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('keyword')).toBe('web');
    expect(request.request.params.get('status')).toBe('ACTIVE');
    request.flush({ success: true, message: null, data: page });
    expect(actual).toEqual(page);
  });

  it('creates a project and unwraps the API response', () => {
    let actual: ProjectDto | undefined;
    service.createProject({
      projectCode: 'WEB', projectName: 'Web Project', startDate: '2026-07-01',
      endDate: '2026-12-31', status: 'ACTIVE',
    }).subscribe(result => actual = result);

    const request = http.expectOne('/api/projects');
    expect(request.request.method).toBe('POST');
    request.flush({ success: true, message: null, data: project });
    expect(actual).toEqual(project);
  });

  it('loads project members with paging', () => {
    const member: ProjectMemberDto = {
      id: '598a36a2-4957-44c8-85d3-2a75badbdb70',
      projectId: project.id,
      userId: '76606760-8893-4461-986c-41e6784e3595',
      userFullName: 'Project Member',
      projectRole: 'DEV',
      status: 'ACTIVE',
    };
    const page: PageResponse<ProjectMemberDto> = {
      items: [member], totalElements: 1, totalPages: 1, pageNumber: 0, pageSize: 20,
    };
    let actual: PageResponse<ProjectMemberDto> | undefined;

    service.getMembers(project.id, 0, 20, 'Project Member').subscribe(result => actual = result);

    const request = http.expectOne(req => req.url === `/api/projects/${project.id}/members`);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('20');
    expect(request.request.params.get('keyword')).toBe('Project Member');
    request.flush({ success: true, message: null, data: page });
    expect(actual).toEqual(page);
  });

  it('loads the current project membership', () => {
    const member: ProjectMemberDto = {
      id: '598a36a2-4957-44c8-85d3-2a75badbdb70',
      projectId: project.id,
      userId: '76606760-8893-4461-986c-41e6784e3595',
      userFullName: 'Project Manager',
      projectRole: 'PM',
      status: 'ACTIVE',
    };
    let actual: ProjectMemberDto | undefined;

    service.getCurrentMember(project.id).subscribe(result => actual = result);

    const request = http.expectOne(`/api/projects/${project.id}/members/me`);
    expect(request.request.method).toBe('GET');
    request.flush({ success: true, message: null, data: member });
    expect(actual).toEqual(member);
  });

  it('loads available project member candidates', () => {
    const candidate: ProjectMemberCandidateDto = {
      id: '76606760-8893-4461-986c-41e6784e3595',
      employeeId: 'EMP002',
      fullName: 'Available User',
      email: 'available@test.com',
    };
    const page: PageResponse<ProjectMemberCandidateDto> = {
      items: [candidate], totalElements: 1, totalPages: 1, pageNumber: 0, pageSize: 100,
    };
    let actual: PageResponse<ProjectMemberCandidateDto> | undefined;

    service.getMemberCandidates(project.id, 0, 10, 'Available').subscribe(result => actual = result);

    const request = http.expectOne(
      req => req.url === `/api/projects/${project.id}/members/candidates`,
    );
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.get('keyword')).toBe('Available');
    request.flush({ success: true, message: null, data: page });
    expect(actual).toEqual(page);
  });

  it('soft-removes a member through the member endpoint', () => {
    const memberId = '598a36a2-4957-44c8-85d3-2a75badbdb70';
    let completed = false;

    service.removeMember(project.id, memberId).subscribe(() => completed = true);

    const request = http.expectOne(`/api/projects/${project.id}/members/${memberId}`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
    expect(completed).toBe(true);
  });
});
