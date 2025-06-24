package com.appcoins.sdk.core.analytics.matomo

interface Property {
    val key: String
    val eventName: String
    val id: Int
    val skip: Boolean
}

@Suppress("MagicNumber")
enum class SdkGeneralProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int,
    override val skip: Boolean = false
) : Property {
    SDK_VERSION_CODE("version_code", "general_properties", 1),
    GAME_PACKAGE_NAME("package_name", "general_properties", 10),

    LANGUAGE("language", "general_properties", 20, true),

    IS_EMULATOR("probably_emulator", "general_properties", 30, true),
}
