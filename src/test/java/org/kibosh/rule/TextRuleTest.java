package org.kibosh.rule;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kibosh.test.AbstractKiboshTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class TextRuleTest extends AbstractKiboshTest {

    private static final String NAME = "MyRule";
    private static final String DESCRIPTION = "Description123";
    private static final String FILE_NAME = "SomeFile.java";
    private static final Path PATH = Paths.get("some", FILE_NAME);

    @Nested
    class applyToFile {

        @Test
        void DefaultConfiguration_NoViolations() {
            TextRule rule = textRuleBuilder().build();
            TextRule.readFile = p -> "";
            assertThat(rule.applyToFile(PATH)).isEmpty();
        }

        @Nested
        class IllegalString {

            TextRule rule = textRuleBuilder()
                    .illegalString("abc")
                    .illegalString("xy")
                    .build();

            @Test
            void NoOccurrences() {
                TextRule.readFile = p -> "12345";
                assertNoViolation(rule);
            }

            @Test
            void SingleOccurrence() {
                TextRule.readFile = p -> "abc";
                assertViolation(rule, "illegal string \"abc\"");
            }

            @Test
            void MultipleOccurrences_SingleViolation() {
                TextRule.readFile = p -> "abc\n     xy   xy";
                assertViolation(rule,
                        "illegal string \"abc\"",
                        "illegal string \"xy\"");
            }
        }

        @Nested
        class IllegalRegularExpression {

            TextRule rule = textRuleBuilder()
                    .illegalRegularExpression("abc")
                    .illegalRegularExpression("begin.*end")
                    .build();

            @Test
            void NoOccurrences() {
                TextRule.readFile = p -> "12345";
                assertNoViolation(rule);
            }

            @Test
            void SingleOccurrence() {
                TextRule.readFile = p -> "other.. abc ^&*$%#";
                assertViolation(rule, "illegal regular expression /abc/");
            }

            @Test
            void MultipleOccurrences_SingleViolation() {
                TextRule.readFile = p -> "begin 999 end       abc\n     begin$$$$end";
                assertViolation(rule,
                        "illegal regular expression /abc/",
                        "illegal regular expression /begin.*end/");
            }
        }

        @Nested
        class ExcludeFilenames {

            @Test
            void ExcludedFile_ExactFilename_NoViolations() {
                TextRule rule = textRuleBuilder()
                        .illegalString("abc")
                        .excludeFilename("other.txt")
                        .excludeFilename(FILE_NAME)
                        .build();
                TextRule.readFile = p -> "abc";
                assertNoViolation(rule);
            }

            @Test
            void ExcludedFile_Wildcards_NoViolations() {
                TextRule rule = textRuleBuilder()
                        .illegalString("abc")
                        .excludeFilename("S*.java")
                        .build();
                TextRule.readFile = p -> "abc";
                assertNoViolation(rule);
            }

            @Test
            void NotExcludedFile_Violation() {
                TextRule rule = textRuleBuilder()
                        .illegalString("abc")
                        .excludeFilename("*.txt")
                        .build();
                TextRule.readFile = p -> "abc";
                assertViolation(rule, "abc");
            }

        }

        private TextRule.TextRuleBuilder textRuleBuilder() {
            return TextRule.builder()
                    .name(NAME)
                    .description(DESCRIPTION);
        }

        private void assertNoViolation(Rule rule) {
            List<Violation> violations = rule.applyToFile(PATH);
            assertThat(violations).isEmpty();
        }

        private void assertViolation(Rule rule, String... violationMessages) {
            List<Violation> violations = rule.applyToFile(PATH);
            log.info("violations={}", violations);

            assertThat(violations).hasSize(violationMessages.length);
            int messageIndex = 0;
            for (Violation violation: violations) {
                assertThat(violation.getRule()).isEqualTo(rule);
                assertThat(violation.getMessage()).contains(NAME, DESCRIPTION, violationMessages[messageIndex]);
                messageIndex++;
            }
        }

    }

    @Test
    void readFile(@TempDir Path tempDir) throws IOException {
        String contents = "abc12345";
        Path file = tempDir.resolve("TempFile.txt");
        Files.write(file, contents.getBytes());
        assertThat(TextRule.readFile.apply(file)).isEqualTo(contents);
    }
}