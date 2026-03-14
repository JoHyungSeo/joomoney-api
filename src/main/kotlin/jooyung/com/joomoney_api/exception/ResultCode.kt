package jooyung.com.joomoney_api.exception

enum class ResultCode(
    val code: String,
    val status: String,
    val message: String
) {
    SUCCESS("SUCCESS", "200", "Success"),
    ERR_BAD_REQUEST("ERR_BAD_REQUEST", "400", "Bad request"),
    ERR_SYSTEM("ERR_SYSTEM", "500", "Internal server error"),

    // USER
    ERR_USER_ID_IS_EMPTY("ERR_USER_ID_IS_EMPTY", "400.01", "User ID is required"),
    ERR_USER_ID_IS_OVER_50_AND_UNDER_2_CHAR("ERR_USER_ID_IS_OVER_50_AND_UNDER_2_CHAR", "400.02", "User ID must be between 2 and 50 characters"),
    ERR_USER_ID_IS_INVALID("ERR_USER_ID_IS_INVALID", "400.03", "Invalid user ID format"),
    ERR_USER_ID_IS_DUPLICATE("ERR_USER_ID_IS_DUPLICATE", "400.04", "User ID already exists"),

    // NAME
    ERR_NAME_IS_EMPTY("ERR_NAME_IS_EMPTY", "400.05", "Name is required"),
    ERR_NAME_IS_INVALID("ERR_NAME_IS_INVALID", "400.06", "Invalid name format"),
    ERR_NAME_IS_OVER_50_CHAR("ERR_NAME_IS_OVER_50_CHAR", "400.07", "Name cannot exceed 50 characters"),

    // EMAIL
    ERR_EMAIL_IS_EMPTY("ERR_EMAIL_IS_EMPTY", "400.08", "Email is required"),
    ERR_EMAIL_IS_OVER_255_CHAR("ERR_EMAIL_IS_OVER_255_CHAR", "400.09", "Email cannot exceed 255 characters"),
    ERR_EMAIL_IS_INVALID("ERR_EMAIL_IS_INVALID", "400.10", "Invalid email format"),
    ERR_EMAIL_IS_DUPLICATE("ERR_EMAIL_IS_DUPLICATE", "400.11", "Email already exists"),

    ERR_EMAIL_SMTP_SEND_FAILED("ERR_EMAIL_SMTP_SEND_FAILED", "500.01", "Failed to send verification email"),

    // PASSWORD
    ERR_PASSWORD_IS_EMPTY("ERR_PASSWORD_IS_EMPTY", "400.12", "Password is required"),
    ERR_PASSWORD_IS_OVER_128_CHAR_AND_UNDER_8_CHAR("ERR_PASSWORD_IS_OVER_128_CHAR_AND_UNDER_8_CHAR", "400.13", "Password must be between 8 and 128 characters"),
    ERR_PASSWORD_NEEDS_UPPERCASE("ERR_PASSWORD_NEEDS_UPPERCASE", "400.14", "Password must contain at least one uppercase letter"),
    ERR_PASSWORD_NEEDS_NUMBER("ERR_PASSWORD_NEEDS_NUMBER", "400.15", "Password must contain at least one number"),
    ERR_PASSWORD_NEEDS_SPECIAL_CHAR("ERR_PASSWORD_NEEDS_SPECIAL_CHAR", "400.16", "Password must contain at least one special character"),

    // BIRTHDAY
    ERR_BIRTHDAY_IS_FUTURE("ERR_BIRTHDAY_IS_FUTURE", "400.17", "Birthday cannot be in the future"),

    // GENDER
    ERR_GENDER_IS_INVALID("ERR_GENDER_IS_INVALID", "400.18", "Invalid gender"),

    // LANGUAGE
    ERR_LANGUAGE_IS_EMPTY("ERR_LANGUAGE_IS_EMPTY", "400.23", "Language is required"),
    ERR_LANGUAGE_IS_INVALID("ERR_LANGUAGE_IS_INVALID", "400.24", "Invalid language"),

    // THEME
    ERR_THEME_IS_EMPTY("ERR_THEME_IS_EMPTY", "400.25", "Theme is required"),
    ERR_THEME_IS_INVALID("ERR_THEME_IS_INVALID", "400.26", "Invalid theme"),

    // DEVICE
    ERR_DEVICE_ID_IS_EMPTY("ERR_DEVICE_ID_IS_EMPTY", "400.27", "Device ID is required"),
    ERR_DEVICE_TYPE_IS_EMPTY("ERR_DEVICE_TYPE_IS_EMPTY", "400.28", "Device type is required"),
    ERR_DEVICE_TYPE_IS_INVALID("ERR_DEVICE_TYPE_IS_INVALID", "400.29", "Invalid device type"),
    ERR_OS_IS_EMPTY("ERR_OS_IS_EMPTY", "400.30", "OS is required"),
    ERR_OS_IS_INVALID("ERR_OS_IS_INVALID", "400.31", "Invalid OS"),
    ERR_PLATFORM_IS_EMPTY("ERR_PLATFORM_IS_EMPTY", "400.32", "Platform is required"),
    ERR_PLATFORM_IS_INVALID("ERR_PLATFORM_IS_INVALID", "400.33", "Invalid platform"),

    // IP
    ERR_IP_IS_EMPTY("ERR_IP_IS_EMPTY", "400.34", "IP address is required"),
    ERR_IP_IS_INVALID("ERR_IP_IS_INVALID", "400.35", "Invalid IP address"),

    // EMAIL VERIFICATION
    ERR_EMAIL_VERIFY_COOLDOWN("ERR_EMAIL_VERIFY_COOLDOWN", "400.56", "Please wait 1 minute before requesting a new code"),
    ERR_EMAIL_VERIFY_IN_PROGRESS("ERR_EMAIL_VERIFY_IN_PROGRESS", "400.36", "Verification code already sent"),
    ERR_EMAIL_VERIFY_ALREADY_COMPLETED("ERR_EMAIL_VERIFY_ALREADY_COMPLETED", "400.37", "Email verification already completed"),

    // EMAIL CODE
    ERR_EMAIL_CODE_NOT_FOUND("ERR_EMAIL_CODE_NOT_FOUND", "400.38", "Verification code not found"),
    ERR_EMAIL_CODE_EXPIRED("ERR_EMAIL_CODE_EXPIRED", "400.39", "Verification code has expired"),
    ERR_EMAIL_CODE_INVALID("ERR_EMAIL_CODE_INVALID", "400.40", "Invalid verification code"),

    // FAIL LIMIT
    ERR_EMAIL_CODE_FAIL_LIMIT_EXCEEDED("ERR_EMAIL_CODE_FAIL_LIMIT_EXCEEDED", "400.41", "Too many incorrect attempts"),
    ERR_EMAIL_CODE_LOCKED("ERR_EMAIL_CODE_LOCKED", "400.42", "Verification temporarily locked"),

    // EMAIL VERIFY REQUIRED
    ERR_EMAIL_VERIFY_REQUIRED("ERR_EMAIL_VERIFY_REQUIRED", "400.43", "Email verification is required"),

    // EMAIL VERIFICATION TOKEN
    ERR_EMAIL_VERIFICATION_TOKEN_EXPIRED("ERR_EMAIL_VERIFICATION_TOKEN_EXPIRED", "400.44", "Email verification token has expired"),
    ERR_EMAIL_VERIFICATION_TOKEN_INVALID("ERR_EMAIL_VERIFICATION_TOKEN_INVALID", "400.45", "Invalid email verification token"),
    ERR_EMAIL_VERIFICATION_TOKEN_ALREADY_CONSUMED("ERR_EMAIL_VERIFICATION_TOKEN_ALREADY_CONSUMED", "400.58", "Verification token has already been used"),

    // LOGIN
    ERR_USER_NOT_FOUND("ERR_USER_NOT_FOUND", "400.46", "User not found"),
    ERR_PASSWORD_MISMATCH("ERR_PASSWORD_MISMATCH", "400.47", "Password does not match"),
    ERR_USER_INACTIVE("ERR_USER_INACTIVE", "400.48", "User account is inactive"),

    // USER CONFIGURATION
    ERR_USER_CONFIG_NOT_FOUND("ERR_USER_CONFIG_NOT_FOUND", "400.49", "User configuration not found"),

    // USER STATUS
    ERR_USER_BANNED("ERR_USER_BANNED", "400.52", "Account has been permanently banned"),
    ERR_USER_DORMANT("ERR_USER_DORMANT", "400.50", "Account is dormant"),
    ERR_USER_SUSPENDED("ERR_USER_SUSPENDED", "400.51", "Account is temporarily suspended"),

    // EMAIL NOT REGISTERED
    ERR_EMAIL_NOT_REGISTERED("ERR_EMAIL_NOT_REGISTERED", "400.57", "Email is not registered"),

    // REFRESH TOKEN
    ERR_REFRESH_TOKEN_INVALID("ERR_REFRESH_TOKEN_INVALID", "400.53", "Invalid refresh token"),
    ERR_REFRESH_TOKEN_EXPIRED("ERR_REFRESH_TOKEN_EXPIRED", "400.54", "Refresh token has expired"),
    ERR_REFRESH_TOKEN_NOT_FOUND("ERR_REFRESH_TOKEN_NOT_FOUND", "400.55", "Refresh token not found"),
}
