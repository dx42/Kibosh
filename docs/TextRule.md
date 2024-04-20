---
layout: default
title: FakeFtpServer Filesystems
---

# TextRule

The `TextRule` class implements the `Rule` interface, and enables checking for illegal strings or illegal regular expressions within a source file.

It includes the following fields:

| **Field (property)**     | **Description and Usage**                                                                                                                       |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| name                     | The name of the rule; included in violation messages.                                                                                           |
| description              | The description for the rule; included in violation messages.                                                                                   |
| severity                 | The rule severity. Can be either ERROR or WARNING. Defaults to ERROR. WARNING violations do not fail (throw an exception) in `KiboshRunner`.    |
| illegalString            | If the specified string is contained within a source file, it causes a violation. You can specify more than one of these.                       |
| illegalRegularExpression | If the specified regular expression is matched (contained) within a source file, it causes a violation. You can specify more than one of these. |

The `TextRule` class provides a *Builder* API. Here is an example instantiation:

```java
        TextRule.builder()
                .name("NoTabs")
                .description("Use spaces rather than tabs")
                .illegalString("\t")
                .build();

```
