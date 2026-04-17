package com.example.config;

import com.example.entity.admin.AdminRole;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModuleRoleMapping {
    private static final Map<String, Set<String>> MODULE_ROLES = new HashMap<>();

    static {
        MODULE_ROLES.put("admin_manager", Set.of(AdminRole.SUPER_ADMIN));
        MODULE_ROLES.put("course_admin", Set.of(AdminRole.SUPER_ADMIN, AdminRole.COURSE_ADMIN));
        MODULE_ROLES.put("community_admin", Set.of(AdminRole.SUPER_ADMIN, AdminRole.COMMUNITY_ADMIN));
        MODULE_ROLES.put("announcement_admin", Set.of(AdminRole.SUPER_ADMIN, AdminRole.ANNOUNCEMENT_ADMIN));
    }

    public static boolean hasAccess(String module, String role) {
        if (role == null) return false;
        if (AdminRole.SUPER_ADMIN.equals(role)) return true;
        Set<String> allowed = MODULE_ROLES.get(module);
        return allowed != null && allowed.contains(role);
    }
}
