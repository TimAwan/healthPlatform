package com.health.platform.security.permission;

import java.util.Set;

public interface PermissionProvider {

    Set<String> getPermissions(Long userId);
}
