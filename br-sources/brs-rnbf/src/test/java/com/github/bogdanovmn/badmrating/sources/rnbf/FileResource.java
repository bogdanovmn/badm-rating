package com.github.bogdanovmn.badmrating.sources.rnbf;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class FileResource {
    private final Class<?> targetClass;

    public String content(String fileName) throws IOException {
        return IOUtils.toString(this.targetClass.getResourceAsStream(fileName), StandardCharsets.UTF_8);
    }

    public InputStream stream(String fileName) {
        return this.targetClass.getResourceAsStream(fileName);
    }

    public Path path(String fileName) {
        return Paths.get(
            this.targetClass.getResource(fileName).getPath()
        );
    }
}
