package org.kibosh.runner;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kibosh.rule.Rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Builder
public class KiboshRunner {

    private final String baseDirectory;

    @SneakyThrows(IOException.class)
    public void applyRules(List<Rule> rules) {
        Path startingDir = Paths.get(baseDirectory);

        KiboshFileVisitor visitor = new KiboshFileVisitor(rules);
        Files.walkFileTree(startingDir, visitor);

        if (!visitor.getViolations().isEmpty()) {
            String violationsOnePerLine = visitor.getViolations().stream()
                    .map(v -> "- " + v.getMessage())
                    .collect(Collectors.joining("\n    "));
            log.error("There were violations: \n    {}", violationsOnePerLine);
            throw new KiboshViolationsException(visitor.getViolations());
        }
    }
}
