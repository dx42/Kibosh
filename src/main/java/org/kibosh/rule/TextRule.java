package org.kibosh.rule;

import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Builder
public class TextRule implements Rule {

    public static final String QUOTES = "\"";
    private final String name;
    private final String description;

    @Singular
    List<String> illegalStrings;

    // For testing
    protected static Function<Path, String> readFile = TextRule::readFileContents;

    @Override
    public List<Violation> applyToFile(Path path) {
        String fileContents = readFile.apply(path);
        List<Violation> violations = new ArrayList<>();

        for (String illegalString: illegalStrings) {
            if (fileContents.contains(illegalString)) {
                String message = name + ": " + quoted(description) + "; " + "File=[" + path.getFileName() + "] contains illegal string " +  quoted(illegalString);
                Violation violation = Violation.builder()
                        .rule(this)
                        .message(message)
                        .build();
                violations.add(violation);
            }
        }

        return violations;
    }

    private String quoted(String string) {
        return QUOTES + string + QUOTES;
    }

    private static String readFileContents(Path path) {
        log.info("readFileContents: path={}", path);
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
