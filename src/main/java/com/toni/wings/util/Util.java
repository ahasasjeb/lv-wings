package com.toni.wings.util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

public final class Util {
    private Util() {
    }

    private static final Converter<String, String> UNDERSCORE_TO_CAMEL = CaseFormat.LOWER_UNDERSCORE
        .converterTo(CaseFormat.LOWER_CAMEL);

    public static String underScoreToCamel(String value) {
        return UNDERSCORE_TO_CAMEL.convert(value);
    }

}
