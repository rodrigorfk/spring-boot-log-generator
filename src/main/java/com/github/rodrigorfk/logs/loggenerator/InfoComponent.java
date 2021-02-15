package com.github.rodrigorfk.logs.loggenerator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class InfoComponent implements InfoContributor {

    private final Manifest manifest;

    public InfoComponent(@Value("classpath:/META-INF/MANIFEST.MF") Resource resource) {
        Manifest manifest = new Manifest();
        if (resource.exists()) {
            try (final InputStream stream = resource.getInputStream()) {
                manifest = new Manifest(stream);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        this.manifest = manifest;
    }

    @Override
    public void contribute(Info.Builder builder) {
        String classPath = manifest.getMainAttributes().getValue("Class-Path");

        if (StringUtils.isNotEmpty(classPath)) {

            classPath = Stream.of(classPath.split(" "))
                .filter(t -> t.toLowerCase().endsWith(".jar"))
                .map(t -> {
                    String[] parts = t.split("/");
                    String artifact = parts[parts.length-3];
                    String version = parts[parts.length-2];

                    StringBuilder group = new StringBuilder();
                    for (int i=0; i<parts.length-3; i++) {
                        if (i > 0) {
                            group.append(".");
                        }
                        group.append(parts[i]);
                    }

                    return String.format("%s:%s:%s", group.toString(), artifact, version);
                })
                .collect(Collectors.joining(", "));

            builder.withDetail("classpath", classPath);
        }

        Properties properties = System.getProperties();
        builder.withDetail("java.vendor", properties.getProperty("java.vendor"));
        builder.withDetail("java.vm.version", properties.getProperty("java.vm.version"));
        builder.withDetail("java.version.date", properties.getProperty("java.version.date"));

    }
}
