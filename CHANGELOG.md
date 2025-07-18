# Kibosh Change Log

## Version 1.1.1 (Jun 2025)
 - #3: TextRule: Include actual line number in violation message logged to console.


## Version 1.1.0 (Jun 2025)
 - #1: TextRule: For illegalRegularExpression, include line number; create violation for each occurrence.
 - #2: TextRule: For illegalString, include line number; create violation for each occurrence.
 - build.gradle: Use constants for other duplicated values.


## Version 1.0.0
 - `TextRule`: Add support for `requiredString` and `requiredRegularExpression`.
 - `KiboshRunner`: Make files to apply to configurable using new *applyToFileNames* property.
 - `KiboshRunner`: Show number of warning and error violations.

## Version 0.3.0

 - `KiboshRunner`: Include link to source files when listing violations.
 - `KiboshRunner`: Specify rule list as a varargs parameter. Add `applyRules(Rule… rules)`.


## Version 0.2.0

 - `README`: Add Maven dependency info. Add separate docs page for TextRule and KiboshRunner.
 - `KiboshRunner`: Log WARNING and ERROR violations separately. Only throw exception for ERROR violations.
 - `TextRule`: Add severity field for created Violations..
 - `Violation`: Add Severity enum with ERROR and WARNING.
 - `KiboshRunner`: Enable setting multiple baseDirectory.
 - `build.gradle`: Use constants for version, artifactId, groupId. Do not export slf4j-simple as a jar dependency.