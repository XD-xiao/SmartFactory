package com.example.smartfactoryservice.Utils;

import java.util.regex.Pattern;

public final class RequestParamValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

    private RequestParamValidator() {
    }

}
