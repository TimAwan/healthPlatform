package com.health.platform.system.config;

import com.health.platform.system.entity.SysUser;
import com.health.platform.system.service.SysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动播种系统管理员（表为空时）。初始密码经环境变量注入并 BCrypt 加密存储。
 */
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_INITIAL_PASSWORD:Admin@123456}")
    private String adminInitialPassword;

    public AdminUserInitializer(SysUserService sysUserService, PasswordEncoder passwordEncoder) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (sysUserService.count() > 0) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminInitialPassword));
        admin.setRealName("系统管理员");
        admin.setStatus("ENABLED");
        sysUserService.create(admin);
        log.warn("已初始化系统管理员账号 [{}]，初始密码来自 ADMIN_INITIAL_PASSWORD，请尽快修改", adminUsername);
    }
}
