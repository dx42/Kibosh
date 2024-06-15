# Kibosh Change Log

## TODO: Version 0.4.0
 - `TextRule`: Add support for `requiredString` and `requiredRegularExpression`.
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