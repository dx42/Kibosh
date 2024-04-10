package org.kibosh.rule;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Violation {

    Rule rule;
    String message;
    int lineNumber;

}
