package local.sop.datawarehouse.auditlog.domain.service;

import local.sop.datawarehouse.auditlog.domain.model.Log;
public interface AuditLogDomain {
    Log createLog(Log log);
}
