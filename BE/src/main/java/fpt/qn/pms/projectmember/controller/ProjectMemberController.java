package fpt.qn.pms.projectmember.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.projectmember.dto.request.AddProjectMemberRequest;
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
    @Operation(summary = "Get project members", description = "Available to ADMIN or an active PM of this project")
    public ResponseEntity<ApiResponse<PageResponse<ProjectMemberDto>>> getMembers(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        PageResponse<ProjectMemberDto> members = projectMemberService
                .getMembers(projectId, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(members, null));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current project membership", description = "Available to an active project member")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> getCurrentMember(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(
                projectMemberService.getCurrentMember(projectId), null));
    }

    @GetMapping("/candidates")
    @Operation(
            summary = "Get available project member candidates",
            description = "Returns active users who are not active members; available to ADMIN or an active PM")
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
    @Operation(summary = "Add a project member", description = "Available to ADMIN or an active PM of this project")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> addMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request) {
        ProjectMemberDto member = projectMemberService.addMember(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(member, "Project member added successfully"));
    }

    @DeleteMapping("/{memberId}")
    @Operation(summary = "Remove a project member", description = "Soft-deletes membership; ADMIN or active project PM only")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId) {
        projectMemberService.removeMember(projectId, memberId);
        return ResponseEntity.noContent().build();
    }
}
