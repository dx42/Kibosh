package org.dx42.kibosh.runner;

import org.dx42.kibosh.rule.Violation;

import java.util.List;

public class KiboshViolationsException extends RuntimeException {

    private final List<Violation> violations;

    public KiboshViolationsException(List<Violation> violations) {
        super("Violations: " + violations);
        this.violations = violations;
    }

    public List<Violation> getViolations() {
        return violations;
    }
}
