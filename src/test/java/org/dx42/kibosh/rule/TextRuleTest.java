package org.dx42.kibosh.rule;

import static org.assertj.core.api.Assertions.*;
import static org.dx42.kibosh.rule.Violation.Severity.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.dx42.kibosh.test.AbstractKiboshTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TextRuleTest extends AbstractKiboshTest {

    private static final String NAME = "MyRule";
    private static final String DESCRIPTION = "Description123";
    private static final String FILE_NAME = "SomeFile.java";
    private static final Path PATH = Paths.get("some", FILE_NAME);

    private Violation.Severity expectedSeverity = ERROR;
    private TextRule rule;

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

            @BeforeEach
            void setUp() {
                rule = textRuleBuilder()
                        .illegalString("abc")
                        .illegalString("xy")
                        .build();
            }

            @Test
            void NoOccurrences_NoViolations() {
                TextRule.readFile = p -> "12345";
                assertNoViolation(rule);
            }

            @Test
            void SingleOccurrence_SingleViolation() {
                TextRule.readFile = p -> "abc";
                assertViolations(
                        violation("illegal string \"abc\"", 1));
            }

            @Test
            void MultipleOccurrences_MultipleViolations() {
                TextRule.readFile = p -> "xy\n     xxx   abc\n other";
                assertViolations(
                        violation("illegal string \"abc\"", 2),
                        violation("illegal string \"xy\"", 1));
            }

            @Test
            void SameString_MultipleOccurrences_MultipleViolations() {
                TextRule.readFile = p -> "xabcx \n   \n Some Other Line \n  abc";
                assertViolations(
                        violation("illegal string \"abc\"", 1),
                        violation("illegal string \"abc\"", 4));
            }
        }

        @Nested
        class IllegalRegularExpression {

            @BeforeEach
            void setUp() {
                rule = textRuleBuilder()
                        .illegalRegularExpression("abc")
                        .illegalRegularExpression("begin.*end")
                        .build();
            }

            @Test
            void NoOccurrences_NoViolations() {
                TextRule.readFile = p -> "12345";
                assertNoViolation(rule);
            }

            @Test
            void SingleOccurrence_SingleViolation() {
                TextRule.readFile = p -> "other.. abc ^&*$%#";
                assertViolations(
                        violation("illegal regular expression /abc/", 1));
            }

            @Test
            void MultipleStrings_MultipleViolations() {
                TextRule.readFile = p -> "b 999 end       abc\n     begin$$$$end";
                assertViolations(
                        violation("illegal regular expression /abc/", 1),
                        violation("illegal regular expression /begin.*end/", 2));
            }

            @Test
            void SameString_MultipleOccurrences_MultipleViolations() {
                TextRule.readFile = p -> "xabcx \n   \n Some Other Line \n  abc  \n beginend";
                assertViolations(
                        violation("illegal regular expression /abc/", 1),
                        violation("illegal regular expression /abc/", 4),
                        violation("illegal regular expression /begin.*end/", 5));
            }
        }

        @Nested
        class RequiredString {

            @BeforeEach
            void setUp() {
                rule = textRuleBuilder()
                        .requiredString("abc")
                        .requiredString("xy")
                        .build();
            }

            @Test
            void AllRequiredStringsPresent() {
                TextRule.readFile = p -> "abc\n     xy   xy";
                assertNoViolation(rule);
            }

            @Test
            void OnlyOneRequiredStringPresent_SingleViolation() {
                TextRule.readFile = p -> "abc";
                assertViolations(
                        violation("required string \"xy\"", 1));
            }

            @Test
            void NoRequiredStringsPresent_OneViolationForEachMissingString() {
                TextRule.readFile = p -> "12345";
                assertViolations(
                        violation("required string \"abc\"", 1),
                        violation("required string \"xy\"", 1));
            }
        }

        @Nested
        class RequiredRegularExpression {

            @BeforeEach
            void setUp() {
                rule = textRuleBuilder()
                        .requiredRegularExpression("abc")
                        .requiredRegularExpression("begin.*end")
                        .build();

            }

            @Test
            void AllRequiredRegularExpressionPresent() {
                TextRule.readFile = p -> "begin 999 end       abc\n     begin$$$$end";
                assertNoViolation(rule);
            }

            @Test
            void OnlyOneRequiredRegularExpressionPresent_SingleViolation() {
                TextRule.readFile = p -> "other.. abc ^&*$%#";
                assertViolations(violation("required regular expression /begin.*end/", 1));
            }

            @Test
            void NoRequiredRegularExpressionsPresent_OneViolationForEachMissingRegularExpression() {
                TextRule.readFile = p -> "12345";
                assertViolations(
                        violation("required regular expression /abc/", 1),
                        violation("required regular expression /begin.*end/", 1));
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
                rule = textRuleBuilder()
                        .illegalString("abc")
                        .excludeFilename("*.txt")
                        .build();
                TextRule.readFile = p -> "abc";
                assertViolations(violation("abc", 1));
            }

        }

        @Nested
        class Severity {

            @Test
            void FailOnViolations_false() {
                rule = textRuleBuilder()
                        .illegalString("abc")
                        .severity(WARNING)
                        .build();
                TextRule.readFile = p -> "abc";
                expectedSeverity = WARNING;
                assertViolations(violation("abc", 1));
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

        private Violation violation(String message, int lineNumber) {
            return Violation.builder()
                    .rule(rule)
                    .severity(expectedSeverity)
                    .message(message)
                    .lineNumber(lineNumber)
                    .build();
        }

        private void assertViolations(Violation... expectedViolations) {
            List<Violation> violations = rule.applyToFile(PATH);
            log.info("actualViolations={}", violations);

            assertThat(violations).hasSize(expectedViolations.length);
            for (int i = 0; i < expectedViolations.length; i++) {
                Violation expected = expectedViolations[i];
                String expectedLineNumber = ":" + expected.getLineNumber();
                assertThat(violations.get(i).getRule()).isEqualTo(rule);
                assertThat(violations.get(i).getSeverity()).isEqualTo(expected.getSeverity());
                assertThat(violations.get(i).getMessage()).contains(NAME, DESCRIPTION, expected.getMessage(), expectedLineNumber);
                assertThat(violations.get(i).getLineNumber()).isEqualTo(expected.getLineNumber());
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