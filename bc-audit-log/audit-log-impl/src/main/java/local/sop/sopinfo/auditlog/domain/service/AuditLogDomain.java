package local.sop.sopinfo.auditlog.domain.service;

import local.sop.sopinfo.auditlog.domain.model.Log;
public interface AuditLogDomain {
    Log createLog(Log log);
}
