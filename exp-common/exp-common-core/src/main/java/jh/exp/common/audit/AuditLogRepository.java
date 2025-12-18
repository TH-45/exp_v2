package jh.exp.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 审计日志仓储。
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}


