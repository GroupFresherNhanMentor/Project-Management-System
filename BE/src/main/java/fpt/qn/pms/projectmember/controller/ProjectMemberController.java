package fpt.qn.pms.projectmember.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.projectmember.dto.request.AddProjectMemberRequest;
import fpt.qn.pms.projectmember.dto.request.UpdateProjectMemberRoleRequest;
import fpt.qn.pms.projectmember.dto.response.ProjectMemberCandidateDto;
import fpt.qn.pms.projectmember.dto.response.ProjectMemberDto;
import fpt.qn.pms.projectmember.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Tag(name = "ProjectMember", description = "Project member management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectMemberController {

    ProjectMemberService projectMemberService;

    @GetMapping
    @PreAuthorize("@projectSecurityEvaluator.isMember(#projectId) or hasAuthority('ADMIN')")
    @Operation(summary = "Get project members")
    public ResponseEntity<ApiResponse<PageResponse<ProjectMemberDto>>> getMembers(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        PageResponse<ProjectMemberDto> members = projectMemberService.getMembers(projectId, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(members, null));
    }

    @GetMapping("/me")
    @PreAuthorize("@projectSecurityEvaluator.isMember(#projectId) or hasAuthority('ADMIN')")
    @Operation(summary = "Get current user's membership in this project")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> getCurrentMember(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectMemberService.getCurrentMember(projectId), null));
    }

    @GetMapping("/candidates")
    @PreAuthorize("@projectSecurityEvaluator.requireRole(#projectId, {T(fpt.qn.pms.jooq.enums.ProjectRole).PM}) or hasAuthority('ADMIN')")
    @Operation(summary = "Get available member candidates", description = "Active users not already in the project. PM only.")
    public ResponseEntity<ApiResponse<PageResponse<ProjectMemberCandidateDto>>> getMemberCandidates(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        PageResponse<ProjectMemberCandidateDto> candidates = projectMemberService
                .getMemberCandidates(projectId, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(candidates, null));
    }

    @PostMapping
    @PreAuthorize("@projectSecurityEvaluator.requireRole(#projectId, {T(fpt.qn.pms.jooq.enums.ProjectRole).PM}) or hasAuthority('ADMIN')")
    @Operation(summary = "Add a project member", description = "PM only.")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> addMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request) {
        ProjectMemberDto member = projectMemberService.addMember(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(member, "Project member added successfully"));
    }

    @PatchMapping("/{memberId}/role")
    @PreAuthorize("@projectSecurityEvaluator.requireRole(#projectId, {T(fpt.qn.pms.jooq.enums.ProjectRole).PM}) or hasAuthority('ADMIN')")
    @Operation(
        summary = "Change a project member's role",
        description = "Admin can change any member to any role. PM can only change DEV↔TESTER — cannot touch PM members or promote to PM.")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> updateMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateProjectMemberRoleRequest request) {
        ProjectMemberDto updated = projectMemberService.updateMemberRole(projectId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Member role updated successfully"));
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("@projectSecurityEvaluator.requireRole(#projectId, {T(fpt.qn.pms.jooq.enums.ProjectRole).PM}) or hasAuthority('ADMIN')")
    @Operation(summary = "Remove a project member", description = "PM only.")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId) {
        projectMemberService.removeMember(projectId, memberId);
        return ResponseEntity.noContent().build();
    }
}
