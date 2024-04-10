package org.kibosh.runner;

import lombok.extern.slf4j.Slf4j;
import org.kibosh.rule.Rule;
import org.kibosh.rule.Violation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class KiboshRunner {

    private final String baseDirectory;

    public KiboshRunner(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public void applyRules(List<Rule> rules) throws IOException {
        Path startingDir = Paths.get(baseDirectory);

        KiboshFileVisitor visitor = new KiboshFileVisitor(rules);
        Files.walkFileTree(startingDir, visitor);

        if (!visitor.getViolations().isEmpty()) {
            String violationsOnePerLine = visitor.getViolations().stream()
                    .map(Violation::toString)
                    .collect(Collectors.joining("\n    "));
            log.error("There were violations: \n    {}", violationsOnePerLine);
            throw new KiboshViolationsException(visitor.getViolations());
        }
    }
}
