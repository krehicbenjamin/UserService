package com.user.UserService.user.web.controller;

import com.user.UserService.security.CurrentUser;
import com.user.UserService.user.service.UserService;
import com.user.UserService.user.web.dto.DeviceSessionResponse;
import com.user.UserService.user.web.dto.ErrorResponse;
import com.user.UserService.user.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and session management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user", description = "Returns the profile of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @Parameter(hidden = true) @CurrentUser UUID userId) {
        UserResponse response = userService.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get active sessions", description = "Returns all active device sessions for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me/sessions")
    public ResponseEntity<List<DeviceSessionResponse>> getActiveSessions(
            @Parameter(hidden = true) @CurrentUser UUID userId) {
        List<DeviceSessionResponse> sessions = userService.getActiveSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @Operation(summary = "Revoke session", description = "Revokes a specific device session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session revoked successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/me/sessions/{sessionId}")
    public ResponseEntity<Void> revokeSession(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @Parameter(description = "Session ID to revoke") @PathVariable UUID sessionId) {
        userService.revokeSession(userId, sessionId);
        return ResponseEntity.noContent().build();
    }
}
