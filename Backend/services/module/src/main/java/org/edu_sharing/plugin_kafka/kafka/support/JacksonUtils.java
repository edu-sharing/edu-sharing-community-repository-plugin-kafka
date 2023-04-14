package org.edu_sharing.plugin_kafka.kafka.support;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.util.ClassUtils;

public final class JacksonUtils {
    private static final boolean JDK8_MODULE_PRESENT =
            ClassUtils.isPresent("com.fasterxml.jackson.datatype.jdk8.Jdk8Module", null);

    private static final boolean JAVA_TIME_MODULE_PRESENT =
            ClassUtils.isPresent("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule", null);

    private static final boolean JODA_MODULE_PRESENT =
            ClassUtils.isPresent("com.fasterxml.jackson.datatype.joda.JodaModule", null);

    private static final boolean KOTLIN_MODULE_PRESENT =
            ClassUtils.isPresent("kotlin.Unit", null) &&
                    ClassUtils.isPresent("com.fasterxml.jackson.module.kotlin.KotlinModule", null);

    public static ObjectMapper enhancedObjectMapper() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        registerWellKnownModulesIfAvailable(objectMapper);
        return objectMapper;
    }

    private static void registerWellKnownModulesIfAvailable(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JacksonMimeTypeModule());
        if (JDK8_MODULE_PRESENT) {
            objectMapper.registerModule(Jdk8ModuleProvider.MODULE);
        }

        if (JAVA_TIME_MODULE_PRESENT) {
            objectMapper.registerModule(JavaTimeModuleProvider.MODULE);
        }

        if (JODA_MODULE_PRESENT) {
            objectMapper.registerModule(JodaModuleProvider.MODULE);
        }

        if (KOTLIN_MODULE_PRESENT) {
            objectMapper.registerModule(KotlinModuleProvider.MODULE);
        }
    }

    private static final class Jdk8ModuleProvider {

        static final com.fasterxml.jackson.databind.Module MODULE =
                new com.fasterxml.jackson.datatype.jdk8.Jdk8Module();

    }

    private static final class JavaTimeModuleProvider {

        static final com.fasterxml.jackson.databind.Module MODULE =
                new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule();

    }

    private static final class JodaModuleProvider {

        static final com.fasterxml.jackson.databind.Module MODULE =
                new com.fasterxml.jackson.datatype.joda.JodaModule();

    }

    private static final class KotlinModuleProvider {

        @SuppressWarnings("deprecation")
        static final com.fasterxml.jackson.databind.Module MODULE =
                new com.fasterxml.jackson.module.kotlin.KotlinModule();

    }
}
