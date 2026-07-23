package fpt.qn.pms.projectmember.service;

import java.util.UUID;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.projectmember.dto.request.AddProjectMemberRequest;
import fpt.qn.pms.projectmember.dto.request.UpdateProjectMemberRoleRequest;
import fpt.qn.pms.projectmember.dto.response.ProjectMemberCandidateDto;
import fpt.qn.pms.projectmember.dto.response.ProjectMemberDto;

public interface ProjectMemberService {

    PageResponse<ProjectMemberDto> getMembers(
            UUID projectId, String keyword, int page, int size);

    ProjectMemberDto getCurrentMember(UUID projectId);

    PageResponse<ProjectMemberCandidateDto> getMemberCandidates(
            UUID projectId, String keyword, int page, int size);

    ProjectMemberDto addMember(UUID projectId, AddProjectMemberRequest request);

    ProjectMemberDto updateMemberRole(UUID projectId, UUID memberId, UpdateProjectMemberRoleRequest request);

    void removeMember(UUID projectId, UUID memberId);
}
