package org.dx42.kibosh.rule;

import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.dx42.kibosh.rule.Violation.Severity;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Builder
public class TextRule implements Rule {

    private static final String QUOTES = "\"";

    // For testing
    protected static Function<Path, String> readFile = TextRule::readFileContents;

    private final String name;
    private final String description;

    @Builder.Default
    private Severity severity = Severity.ERROR;

    @Singular
    List<String> illegalStrings;

    @Singular
    List<String> illegalRegularExpressions;

    @Singular
    List<String> requiredStrings;

    @Singular
    List<String> requiredRegularExpressions;

    /** Filenames to skip applying this rule. May contain wildcards ('*' or '?'). */
    @Singular
    List<String> excludeFilenames;

    private final Map<String, Pattern> illegalRegularExpressionPatterns = new LinkedHashMap<>();

    @Override
    public List<Violation> applyToFile(Path path) {
        List<Violation> violations = new ArrayList<>();

        if (shouldExcludeFile(path)) {
            return violations;
        }

        String fileContents = readFile.apply(path);
        checkForIllegalStrings(path, fileContents, violations);
        checkForIllegalRegularExpressions(path, fileContents, violations);
        checkForRequiredStrings(path, fileContents, violations);
        checkForRequiredRegularExpressions(path, fileContents, violations);

        return violations;
    }

    private boolean shouldExcludeFile(Path path) {
        for (String excludeFilename: excludeFilenames) {
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + excludeFilename);
            if (pathMatcher.matches(path.getFileName())) {
                return true;
            }
        }
        return false;
    }

    private void checkForIllegalStrings(Path path, String fileContents, List<Violation> violations) {
        for (String illegalString: illegalStrings) {
            if (fileContents.contains(illegalString)) {
                String message = messagePrefix(path) + "contains illegal string " +  quoted(illegalString);
                addViolation(violations, message);
            }
        }
    }

    private void checkForIllegalRegularExpressions(Path path, String fileContents, List<Violation> violations) {
        for (String illegalRegex: illegalRegularExpressions) {
            Pattern pattern = patternForRegex(illegalRegex);
            Matcher matcher = pattern.matcher(fileContents);
            while (matcher.find()) {
                int startIndex = matcher.start();
                int lineNumber = getLineNumber(fileContents, startIndex);
                String message = messagePrefix(path) + "contains illegal regular expression /" +  illegalRegex + "/";
                addViolation(violations, message, lineNumber);
            }
        }
    }

    private void checkForRequiredStrings(Path path, String fileContents, List<Violation> violations) {
        for (String requiredString: requiredStrings) {
            if (!fileContents.contains(requiredString)) {
                String message = messagePrefix(path) + "does not contain required string " +  quoted(requiredString);
                addViolation(violations, message);
            }
        }
    }

    private void checkForRequiredRegularExpressions(Path path, String fileContents, List<Violation> violations) {
        for (String requiredRegex: requiredRegularExpressions) {
            Pattern pattern = patternForRegex(requiredRegex);
            Matcher matcher = pattern.matcher(fileContents);
            if (!matcher.find()) {
                String message = messagePrefix(path) + "does not contain required regular expression /" +  requiredRegex + "/";
                addViolation(violations, message);
            }
        }
    }

    private String messagePrefix(Path path) {
        return name + ": " + quoted(description) + "; " + "File=.(" + path.getFileName() + ":1) ";
    }

    private Pattern patternForRegex(String regex) {
        if (!illegalRegularExpressionPatterns.containsKey(regex)) {
            illegalRegularExpressionPatterns.put(regex, Pattern.compile(regex));
        }
        return illegalRegularExpressionPatterns.get(regex);
    }

    private void addViolation(List<Violation> violations, String message) {
        addViolation(violations, message, 0);
    }

    private void addViolation(List<Violation> violations, String message, int lineNumber) {
        Violation violation = Violation.builder()
                .rule(this)
                .severity(severity)
                .message(message)
                .lineNumber(lineNumber)
                .build();
        violations.add(violation);
    }

    private String quoted(String string) {
        return QUOTES + string + QUOTES;
    }

    private static String readFileContents(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int getLineNumber(String content, int index) {
        if (index < 0 || index >= content.length()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return content.substring(0, index).split("\n", -1).length;
    }
}
