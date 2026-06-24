package local.sop.sopinfo.infrastructure.validation.compositekeys;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

public record TestCommand(CompositeKey id, boolean active) {

}
