package com.user.UserService.user.domain.entity;

import com.user.UserService.user.domain.value.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_roles")
public class UserRole {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRole userRole = (UserRole) o;
        return user != null && user.getId() != null && 
               user.getId().equals(userRole.user != null ? userRole.user.getId() : null) && 
               role == userRole.role;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(user != null ? user.getId() : null, role);
    }
}

