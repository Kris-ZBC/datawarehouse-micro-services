package local.sop.sopinfo.sharedkernel.exceptions;

import java.util.Map;

public class NotFoundException extends DomainException {
    public NotFoundException(String messageKey, Map<String,Object> args) {
        super(ErrorCode.NOT_FOUND, messageKey, args);
    }
    public static NotFoundException student(Long id) {
        return new NotFoundException("student.notFound", Map.of("id", id));
    }
}
