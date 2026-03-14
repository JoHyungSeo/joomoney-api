package jooyung.com.joomoney_api.auth.dto

import jooyung.com.joomoney_api.enum.theme.Theme

data class UserConfigurationDto(
    val language: String,
    val theme: Theme
)
