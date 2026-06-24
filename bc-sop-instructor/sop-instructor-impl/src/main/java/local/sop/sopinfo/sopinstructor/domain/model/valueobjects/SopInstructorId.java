package local.sop.sopinfo.sopinstructor.domain.model.valueobjects;

public record SopInstructorId(SopRef sopRef, InstructorRef instructorRef) {
    public SopInstructorId {
        if (sopRef == null) {
            throw new NullPointerException("sopRef is required");
        }
        if (instructorRef == null) {
            throw new NullPointerException("instructorRef is required");
        }
    }
}
