package org.dx42.kibosh.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractKiboshTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static <T> List<T> list(T... elements) {
        return Arrays.stream(elements).collect(Collectors.toList());
    }

}
