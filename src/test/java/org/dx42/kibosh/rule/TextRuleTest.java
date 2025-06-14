package org.dx42.kibosh.rule;

import static org.assertj.core.api.Assertions.*;
import static org.dx42.kibosh.rule.Violation.Severity.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.dx42.kibosh.test.AbstractKiboshTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TextRuleTest extends AbstractKiboshTest {

    private static final String NAME = "MyRule";
    private static final String DESCRIPTION = "Description123";
    private static final String FILE_NAME = "SomeFile.java";
    private static final Path PATH = Paths.get("some", FILE_NAME);

    private Violation.Severity expectedSeverity = ERROR;

    @Nested
    class ApplyToFile {

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
            void NoOccurrences_NoViolations() {
                TextRule.readFile = p -> "12345";
                assertNoViolation(rule);
            }

            @Test
            void SingleOccurrence_SingleViolation() {
                TextRule.readFile = p -> "abc";
                assertSingleViolation(rule, "illegal string \"abc\"", 1);
            }

            @Test
            void MultipleOccurrences_MultipleViolations() {
                TextRule.readFile = p -> "xy\n     xxx   abc\n other";
                assertViolations(rule,
                        "illegal string \"abc\"", 2,
                        "illegal string \"xy\"", 1);
            }

            @Test
            void SameString_MultipleOccurrences_MultipleViolations() {
                TextRule.readFile = p -> "xabcx \n   \n Some Other Line \n  abc";
                assertViolations(rule,
                        "illegal string \"abc\"", 1,
                        "illegal string \"abc\"", 4);
            }
        }

        @Nested
        class IllegalRegularExpression {

            TextRule rule = textRuleBuilder()
                    .illegalRegularExpression("abc")
                    .illegalRegularExpression("begin.*end")
                    .build();

            @Test
            void NoOccurrences_NoViolations() {
                TextRule.readFile = p -> "12345";
                assertNoViolation(rule);
            }

            @Test
            void SingleOccurrence_SingleViolation() {
                TextRule.readFile = p -> "other.. abc ^&*$%#";
                assertSingleViolation(rule, "illegal regular expression /abc/", 1);
            }

            @Test
            void MultipleStrings_MultipleViolations() {
                TextRule.readFile = p -> "b 999 end       abc\n     begin$$$$end";
                assertViolations(rule,
                        "illegal regular expression /abc/", 1,
                        "illegal regular expression /begin.*end/", 2);
            }

            @Test
            void SameString_MultipleOccurrences_MultipleViolations() {
                TextRule.readFile = p -> "xabcx \n   \n Some Other Line \n  abc";
                assertViolations(rule,
                        "illegal regular expression /abc/", 1,
                        "illegal regular expression /abc/", 4);
            }
        }

        @Nested
        class RequiredString {

            TextRule rule = textRuleBuilder()
                    .requiredString("abc")
                    .requiredString("xy")
                    .build();

            @Test
            void AllRequiredStringsPresent() {
                TextRule.readFile = p -> "abc\n     xy   xy";
                assertNoViolation(rule);
            }

            @Test
            void OnlyOneRequiredStringPresent_SingleViolation() {
                TextRule.readFile = p -> "abc";
                assertSingleViolation(rule, "required string \"xy\"", 0);
            }

            @Test
            void NoRequiredStringsPresent_OneViolationForEachMissingString() {
                TextRule.readFile = p -> "12345";
                assertViolations(rule,
                        "required string \"abc\"", 0,
                        "required string \"xy\"", 0);
            }
        }

        @Nested
        class RequiredRegularExpression {

            TextRule rule = textRuleBuilder()
                    .requiredRegularExpression("abc")
                    .requiredRegularExpression("begin.*end")
                    .build();

            @Test
            void AllRequiredRegularExpressionPresent() {
                TextRule.readFile = p -> "begin 999 end       abc\n     begin$$$$end";
                assertNoViolation(rule);
            }

            @Test
            void OnlyOneRequiredRegularExpressionPresent_SingleViolation() {
                TextRule.readFile = p -> "other.. abc ^&*$%#";
                assertSingleViolation(rule, "required regular expression /begin.*end/", 0);
            }

            @Test
            void NoRequiredRegularExpressionsPresent_OneViolationForEachMissingRegularExpression() {
                TextRule.readFile = p -> "12345";
                assertViolations(rule,
                        "required regular expression /abc/", 0,
                        "required regular expression /begin.*end/", 0);
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
                assertSingleViolation(rule, "abc", 1);
            }

        }

        @Nested
        class Severity {

            @Test
            void FailOnViolations_false() {
                TextRule rule = textRuleBuilder()
                        .illegalString("abc")
                        .severity(WARNING)
                        .build();
                TextRule.readFile = p -> "abc";
                expectedSeverity = WARNING;
                assertSingleViolation(rule, "abc", 1);
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

        private void assertSingleViolation(Rule rule, String violationMessage, int lineNumber) {
            List<Violation> violations = rule.applyToFile(PATH);
            log.info("violations={}", violations);

            assertThat(violations).hasSize(1);
            assertThat(violations.get(0).getRule()).isEqualTo(rule);
            assertThat(violations.get(0).getSeverity()).isEqualTo(expectedSeverity);
            assertThat(violations.get(0).getMessage()).contains(NAME, DESCRIPTION, violationMessage);
            assertThat(violations.get(0).getLineNumber()).isEqualTo(lineNumber);
        }

        private void assertViolations(Rule rule, String message1, int lineNumber1, String message2, int lineNumber2) {
            List<Violation> violations = rule.applyToFile(PATH);
            log.info("violations={}", violations);

            assertThat(violations).hasSize(2);

            assertThat(violations.get(0).getRule()).isEqualTo(rule);
            assertThat(violations.get(0).getSeverity()).isEqualTo(expectedSeverity);
            assertThat(violations.get(0).getMessage()).contains(NAME, DESCRIPTION, message1);
            assertThat(violations.get(0).getLineNumber()).isEqualTo(lineNumber1);

            assertThat(violations.get(1).getRule()).isEqualTo(rule);
            assertThat(violations.get(1).getSeverity()).isEqualTo(expectedSeverity);
            assertThat(violations.get(1).getMessage()).contains(NAME, DESCRIPTION, message2);
            assertThat(violations.get(1).getLineNumber()).isEqualTo(lineNumber2);
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