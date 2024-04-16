package org.dx42.kibosh.runner;

import lombok.Builder;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dx42.kibosh.rule.Rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Builder
public class KiboshRunner {

    @Singular
    private final List<String> baseDirectories;

    @SneakyThrows(IOException.class)
    public void applyRules(List<Rule> rules) {
        KiboshFileVisitor visitor = new KiboshFileVisitor(rules);
        walkFileTree(visitor);
        checkForViolations(visitor);
    }

    private void walkFileTree(KiboshFileVisitor visitor) throws IOException {
        for (String baseDirectory: baseDirectories) {
            Path startingDir = Paths.get(baseDirectory);
            Files.walkFileTree(startingDir, visitor);
        }
    }

    private void checkForViolations(KiboshFileVisitor visitor) {
        if (!visitor.getViolations().isEmpty()) {
            String violationsOnePerLine = visitor.getViolations().stream()
                    .map(v -> "- " + v.getMessage())
                    .collect(Collectors.joining("\n    "));
            log.error("There were violations: \n    {}", violationsOnePerLine);
            throw new KiboshViolationsException(visitor.getViolations());
        }
    }
}
