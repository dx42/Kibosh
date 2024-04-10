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

    private static final String BASE_DIRECTORY = "src/test/resources/sampledir";
    private static final Violation VIOLATION1 = Violation.builder().message("m1").build();
    private static final Violation VIOLATION2 = Violation.builder().message("m2").build();
    private static final Violation VIOLATION3 = Violation.builder().message("m3").build();

    @TempDir
    Path tempDir;

    private KiboshRunner kiboshRunner;
    private Path filePath1, filePath2, filePath3, filePath4;
    private Path subdirectory;

    private Rule rule = mock(Rule.class);
    private List<Rule> rules = list(rule);

    @BeforeEach
    void beforeEach() throws IOException {
        log.info("tempDir={}", tempDir);
        kiboshRunner = new KiboshRunner(tempDir.toString());
        filePath1 = Paths.get(tempDir.toString(), "File1.java");
        filePath2 = Paths.get(tempDir.toString(), "File2.java");
        subdirectory = Paths.get(tempDir.toString(), "subdir");
        Files.createDirectory(subdirectory);
        filePath3 = Paths.get(subdirectory.toString(), "File3.java");
        filePath4 = Paths.get(subdirectory.toString(), "File4.java");
    }

    @Nested
    class applyRules {

        @Test
        void NoFiles_NoViolations() throws IOException {
            KiboshRunner kiboshRunner = new KiboshRunner(tempDir.toString());
            kiboshRunner.applyRules(rules);
        }

        @Test
        void Files_NoViolations() throws IOException {
            Files.createFile(Paths.get(tempDir.toString(), "SomeFile.java"));
            KiboshRunner kiboshRunner = new KiboshRunner(tempDir.toString());
            kiboshRunner.applyRules(rules);
        }

        @Test
        void SingleViolation() throws IOException {
            Files.createFile(filePath1);
            when(rule.applyToFile(filePath1)).thenReturn(list(VIOLATION1));
            assertViolations(VIOLATION1);
        }

        @Test
        void MultipleViolation() throws IOException {
            Files.createFile(filePath1);
            Files.createFile(filePath2);
            Files.createFile(filePath3);
            Files.createFile(filePath4);
            when(rule.applyToFile(filePath1)).thenReturn(list(VIOLATION1));
            when(rule.applyToFile(filePath2)).thenReturn(list(VIOLATION2));
            when(rule.applyToFile(filePath3)).thenReturn(list(VIOLATION3));
            assertViolations(VIOLATION1, VIOLATION2, VIOLATION3);
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