package local.sop.sopinfo.infrastructure.validation.compositekeys;

import org.springframework.stereotype.Service;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.compositekey.validate.ValidateCompositeKey;

@Service
public class TestService {

    @ValidateCompositeKey(ports = {"key1Port", "key2Port"})
    public void execute(TestCommand command) {}

    @ValidateCompositeKey(ports = {"key1Port", "key2Port"})
    public void executeWithDirectKey(CompositeKey id) {}

    @ValidateCompositeKey(ports = {"key1Port", "key2Port"})
    public void executeWithNoId(TestCommandNoId command) {}
}