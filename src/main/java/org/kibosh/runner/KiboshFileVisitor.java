package org.kibosh.runner;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kibosh.rule.Rule;
import org.kibosh.rule.Violation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class KiboshFileVisitor extends SimpleFileVisitor<Path> {

    private final List<Rule> rules;

    @Getter
    private final List<Violation> violations = new ArrayList<>();

    public KiboshFileVisitor(List<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        log.info("preVisitDirectory: dir={}", dir);
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        log.info("visitFile: file={}", file);
        violations.addAll(rules.get(0).applyToFile(file));
        return FileVisitResult.CONTINUE;
    }

//    @Override
//    public FileVisitResult visitFileFailed(Path file, IOException exception) throws IOException {
//        log.info("visitFileFailed: file={}; exception={}", file, exception);
//        return super.visitFileFailed(file, exception);
//    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        log.info("postVisitDirectory: dir={}", dir);
        return super.postVisitDirectory(dir, exc);
    }

}
