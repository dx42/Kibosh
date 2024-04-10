package org.kibosh.rule;

import java.nio.file.Path;
import java.util.List;

public interface Rule {

    List<Violation> applyToFile(Path path);

}
