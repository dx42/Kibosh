package org.dx42.kibosh.runner;

import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dx42.kibosh.rule.Rule;
import org.dx42.kibosh.rule.Violation;

@Slf4j
public class KiboshFileVisitor extends SimpleFileVisitor<Path> {

    private final List<Rule> rules;
    private final PathMatcher pathMatcher;

    @Getter
    private final List<Violation> violations = new ArrayList<>();

    public KiboshFileVisitor(List<Rule> rules, String fileNamePattern) {
        this.rules = rules;

        // See https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-
        this.pathMatcher = FileSystems.getDefault().getPathMatcher(fileNamePattern);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (pathMatcher.matches(file.getFileName())) {
            rules.forEach(rule -> violations.addAll(rule.applyToFile(file)));
        }
        return FileVisitResult.CONTINUE;
    }

}
