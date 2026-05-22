package com.toni.wings.util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Util {
    private Util() {
    }

    private static final Converter<String, String> UNDERSCORE_TO_CAMEL = CaseFormat.LOWER_UNDERSCORE
        .converterTo(CaseFormat.LOWER_CAMEL);

    @Nonnull
    public static String underScoreToCamel(@Nonnull String value) {
        return requireNonnull(UNDERSCORE_TO_CAMEL.convert(value), "Converted string cannot be null");
    }

    @Nonnull
    public static <T> T requireNonnull(@Nullable T value, @Nonnull String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

}
