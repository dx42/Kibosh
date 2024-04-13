package org.kibosh.runner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kibosh.rule.Rule;
import org.kibosh.rule.Violation;
import org.kibosh.test.AbstractKiboshTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class KiboshRunnerTest extends AbstractKiboshTest {

    private static final Violation VIOLATION1 = Violation.builder().message("m1").build();
    private static final Violation VIOLATION2 = Violation.builder().message("m2").build();
    private static final Violation VIOLATION3 = Violation.builder().message("m3").build();

    @TempDir
    Path tempDirPath;

    private KiboshRunner kiboshRunner;
    private String tempDir;
    private Path filePath1, filePath2, filePath3, filePath4;

    private Rule rule1 = mock(Rule.class);
    private Rule rule2 = mock(Rule.class);
    private List<Rule> rules = list(rule1, rule2);

    @BeforeEach
    void beforeEach() throws IOException {
        tempDir = tempDirPath.toString();
        log.info("tempDir={}", tempDir);
        kiboshRunner = new KiboshRunner(tempDir);
        filePath1 = Paths.get(tempDir, "File1.java");
        filePath2 = Paths.get(tempDir, "File2.java");
        Path subdirectory = Paths.get(tempDir, "subdir");
        Files.createDirectory(subdirectory);
        filePath3 = Paths.get(subdirectory.toString(), "File3.java");
        filePath4 = Paths.get(subdirectory.toString(), "File4.java");

        Files.createFile(filePath1);
        Files.createFile(filePath2);
        Files.createFile(filePath3);
        Files.createFile(filePath4);
    }

    @Nested
    class applyRules {

        @Test
        void NoFiles_NoViolations() throws IOException {
            KiboshRunner kiboshRunner = new KiboshRunner(tempDir);
            kiboshRunner.applyRules(rules);
        }

        @Test
        void Files_NoViolations() throws IOException {
            Files.createFile(Paths.get(tempDir, "SomeFile.java"));
            KiboshRunner kiboshRunner = new KiboshRunner(tempDir);
            kiboshRunner.applyRules(rules);
        }

        @Test
        void SingleViolation() throws IOException {
            when(rule1.applyToFile(filePath1)).thenReturn(list(VIOLATION1));
            assertViolations(VIOLATION1);
        }

        @Test
        void MultipleViolation() throws IOException {
            when(rule1.applyToFile(filePath1)).thenReturn(list(VIOLATION1));
            when(rule2.applyToFile(filePath2)).thenReturn(list(VIOLATION2));
            when(rule1.applyToFile(filePath3)).thenReturn(list(VIOLATION3));
            assertViolations(VIOLATION1, VIOLATION2, VIOLATION3);
        }

        @Test
        void IgnoresNonJavaFiles() throws IOException {
            Path path = Paths.get(tempDir, "SomeFile.txt");
            Files.createFile(path);
            when(rule1.applyToFile(path)).thenThrow(new RuntimeException());

            KiboshRunner kiboshRunner = new KiboshRunner(tempDir);
            kiboshRunner.applyRules(rules);
        }

        private void assertViolations(Violation... expectedViolations) throws IOException {
            try {
                kiboshRunner.applyRules(rules);
                fail("Expected KiboshViolationsException");
            } catch(KiboshViolationsException e) {
                List<Violation> violations = e.getViolations();
                assertThat(violations).containsExactlyInAnyOrder(expectedViolations);
            }
        }
    }

}