package jh.exp.auth.service.autoData;

import jh.exp.auth.core.entity.Account;
import jh.exp.auth.core.mapper.AccountMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化器：在应用启动时自动创建测试数据
 * 如果数据库中没有数据，则自动插入测试账号
 * 
 * 注意：如果使用 data.sql 方式初始化数据，可以禁用此类
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AccountMapper accountMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有数据，避免重复插入
        if (accountMapper.selectCount(null) > 0) {
            System.out.println("数据库已有数据，跳过初始化");
            return;
        }

        System.out.println("开始初始化测试数据...");

        // 创建管理员账号
        Account admin = new Account();
        admin.setAccountName("admin");
        admin.setAccountDisplay("系统管理员");
        admin.setPasswordHash(passwordEncoder.encode("123456")); // 密码：123456
        admin.setStatus("ENABLED");
        admin.setLoginFailCount(0);
        admin.setNeedChangePwd(false);
        admin.setCreatedTime(LocalDateTime.now());
        admin.setUpdatedTime(LocalDateTime.now());
        accountMapper.insert(admin);
        System.out.println("创建管理员账号: admin / 123456");

        // 创建测试用户1
        Account testUser1 = new Account();
        testUser1.setAccountName("testuser1");
        testUser1.setAccountDisplay("测试用户1");
        testUser1.setPasswordHash(passwordEncoder.encode("123456"));
        testUser1.setMobile("13800138001");
        testUser1.setEmail("testuser1@example.com");
        testUser1.setStatus("ENABLED");
        testUser1.setLoginFailCount(0);
        testUser1.setNeedChangePwd(false);
        testUser1.setOrgId(1L);
        testUser1.setCreatedTime(LocalDateTime.now());
        testUser1.setUpdatedTime(LocalDateTime.now());
        accountMapper.insert(testUser1);
        System.out.println("创建测试用户1: testuser1 / 123456");

        // 创建测试用户2
        Account testUser2 = new Account();
        testUser2.setAccountName("testuser2");
        testUser2.setAccountDisplay("测试用户2");
        testUser2.setPasswordHash(passwordEncoder.encode("123456"));
        testUser2.setMobile("13800138002");
        testUser2.setEmail("testuser2@example.com");
        testUser2.setStatus("ENABLED");
        testUser2.setLoginFailCount(0);
        testUser2.setNeedChangePwd(false);
        testUser2.setOrgId(2L);
        testUser2.setCreatedTime(LocalDateTime.now());
        testUser2.setUpdatedTime(LocalDateTime.now());
        accountMapper.insert(testUser2);
        System.out.println("创建测试用户2: testuser2 / 123456");

        // 创建已禁用账号（用于测试）
        Account disabled = new Account();
        disabled.setAccountName("disabled");
        disabled.setAccountDisplay("已禁用账号");
        disabled.setPasswordHash(passwordEncoder.encode("123456"));
        disabled.setStatus("DISABLED");
        disabled.setLoginFailCount(0);
        disabled.setNeedChangePwd(false);
        disabled.setCreatedTime(LocalDateTime.now());
        disabled.setUpdatedTime(LocalDateTime.now());
        accountMapper.insert(disabled);
        System.out.println("创建已禁用账号: disabled / 123456");

        System.out.println("测试数据初始化完成！");
        System.out.println("可用账号：");
        System.out.println("  - admin / 123456 (管理员)");
        System.out.println("  - testuser1 / 123456 (测试用户1)");
        System.out.println("  - testuser2 / 123456 (测试用户2)");
        System.out.println("  - disabled / 123456 (已禁用，用于测试)");
    }
}

