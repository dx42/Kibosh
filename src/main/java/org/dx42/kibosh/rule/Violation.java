package org.dx42.kibosh.rule;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Violation {

    public enum Severity { ERROR, WARNING }

    Rule rule;
    String message;
    int lineNumber;

    @Builder.Default
    Severity severity = Severity.ERROR;

}
