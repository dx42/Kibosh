package org.kibosh.runner;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kibosh.rule.Rule;
import org.kibosh.rule.Violation;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class KiboshFileVisitor extends SimpleFileVisitor<Path> {

    private final List<Rule> rules;
    private final String fileNamePattern = "*.java";
    private PathMatcher pathMatcher;

    @Getter
    private final List<Violation> violations = new ArrayList<>();

    public KiboshFileVisitor(List<Rule> rules) {
        this.rules = rules;

        // See https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-
        this.pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + fileNamePattern);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        log.info("visitFile: file={}", file);
        if (pathMatcher.matches(file.getFileName())) {
            violations.addAll(rules.get(0).applyToFile(file));
        }
        return FileVisitResult.CONTINUE;
    }

}
