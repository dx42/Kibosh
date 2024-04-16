package org.dx42.kibosh.runner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.dx42.kibosh.rule.Violation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.dx42.kibosh.rule.Rule;
import org.dx42.kibosh.test.AbstractKiboshTest;

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
    private Path filePath1, filePath2, filePath3, filePath4, subdir1, subdir2;
    private Rule rule1 = mock(Rule.class);
    private Rule rule2 = mock(Rule.class);
    private List<Rule> rules = list(rule1, rule2);

    @BeforeEach
    void beforeEach() throws IOException {
        tempDir = tempDirPath.toString();
        kiboshRunner = KiboshRunner.builder().baseDirectory(tempDir).build();
        filePath1 = Paths.get(tempDir, "File1.java");
        filePath2 = Paths.get(tempDir, "File2.java");
        subdir1 = Paths.get(tempDir, "subdir1");
        Files.createDirectory(subdir1);
        filePath3 = Paths.get(subdir1.toString(), "File3.java");
        filePath4 = Paths.get(subdir1.toString(), "File4.java");
        subdir2 = Paths.get(tempDir, "subdir2");
        Files.createDirectory(subdir2);

        Files.createFile(filePath1);
        Files.createFile(filePath2);
        Files.createFile(filePath3);
        Files.createFile(filePath4);
    }

    @Nested
    class applyRules {

        @Test
        void NoFiles_NoViolations() {
            kiboshRunner = KiboshRunner.builder().baseDirectory(subdir2.toString()).build();
            kiboshRunner.applyRules(rules);
        }

        @Test
        void Files_NoViolations() throws IOException {
            Files.createFile(Paths.get(tempDir, "SomeFile.java"));
            KiboshRunner kiboshRunner = KiboshRunner.builder().baseDirectory(tempDir).build();
            kiboshRunner.applyRules(rules);
        }

        @Test
        void SingleViolation() {
            when(rule1.applyToFile(filePath1)).thenReturn(list(VIOLATION1));
            assertViolations(VIOLATION1);
        }

        @Test
        void MultipleViolation() {
            when(rule1.applyToFile(filePath1)).thenReturn(list(VIOLATION1));
            when(rule2.applyToFile(filePath2)).thenReturn(list(VIOLATION2));
            when(rule1.applyToFile(filePath3)).thenReturn(list(VIOLATION3));
            assertViolations(VIOLATION1, VIOLATION2, VIOLATION3);
        }

        @Test
        void MultipleBaseDirectories() throws IOException {
            Path filePath5 = Paths.get(subdir2.toString(), "SomeFile.java");
            Files.createFile(filePath5);

            when(rule1.applyToFile(filePath3)).thenReturn(list(VIOLATION1));
            when(rule2.applyToFile(filePath4)).thenReturn(list(VIOLATION2));
            when(rule1.applyToFile(filePath5)).thenReturn(list(VIOLATION3));

            kiboshRunner = KiboshRunner.builder()
                    .baseDirectory(subdir1.toString())
                    .baseDirectory(subdir2.toString())
                    .build();

            assertViolations(VIOLATION1, VIOLATION2, VIOLATION3);
        }

        @Test
        void IgnoresNonJavaFiles() throws IOException {
            Path path = Paths.get(tempDir, "SomeFile.txt");
            Files.createFile(path);
            when(rule1.applyToFile(path)).thenThrow(new RuntimeException());
            kiboshRunner.applyRules(rules);
        }

        private void assertViolations(Violation... expectedViolations) {
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