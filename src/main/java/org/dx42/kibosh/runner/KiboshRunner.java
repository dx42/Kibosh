package org.dx42.kibosh.runner;

import static org.dx42.kibosh.rule.Violation.Severity.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dx42.kibosh.rule.Rule;
import org.dx42.kibosh.rule.Violation;

@Slf4j
@Builder
public class KiboshRunner {

    @Singular
    private final List<String> baseDirectories;

    private final String applyToFileNames;

    @SneakyThrows(IOException.class)
    public void applyRules(List<Rule> rules) {
        KiboshFileVisitor visitor = new KiboshFileVisitor(rules, "glob:" + fileNamePattern());
        walkFileTree(visitor);
        checkForViolations(visitor);
    }

    public void applyRules(Rule... rules) {
        applyRules(Arrays.asList(rules));
    }

    private void walkFileTree(KiboshFileVisitor visitor) throws IOException {
        for (String baseDirectory: baseDirectories) {
            Path startingDir = Paths.get(baseDirectory);
            Files.walkFileTree(startingDir, visitor);
        }
    }

    private void checkForViolations(KiboshFileVisitor visitor) {
        List<Violation> warningViolations = getViolationsBySeverity(visitor.getViolations(), WARNING);
        logViolations(warningViolations, WARNING);

        List<Violation> errorViolations = getViolationsBySeverity(visitor.getViolations(), ERROR);
        logViolations(errorViolations, ERROR);

        if (!errorViolations.isEmpty()) {
            throw new KiboshViolationsException(errorViolations);
        }
    }

    private String fileNamePattern() {
        return applyToFileNames == null ? "*.java" : applyToFileNames;
    }

    private static void logViolations(List<Violation> violations, Violation.Severity severity) {
        if (!violations.isEmpty()) {
            String violationsOnePerLine = violations.stream()
                    .map(v -> "- " + v.getMessage())
                    .collect(Collectors.joining("\n    "));
            log.warn("There were {} violations: \n    {}", violations.size() + " " + severity, violationsOnePerLine);
        }
    }

    private static List<Violation> getViolationsBySeverity(List<Violation> violations, Violation.Severity severity) {
        return violations.stream().filter(v -> v.getSeverity() == severity).collect(Collectors.toList());
    }

}
