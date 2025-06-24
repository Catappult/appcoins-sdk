package com.appcoins.sdk.core.analytics.matomo

@Suppress("MagicNumber")
enum class SdkGeneralProperties(
    override val key: String,
    override val eventName: String,
    override val id: Int
) : Property {
    SDK_VERSION_CODE("version_code", "general_properties", 1),
    GAME_PACKAGE_NAME("package_name", "general_properties", 10),

    LANGUAGE("language", "general_properties", 20),
    IS_EMULATOR("probably_emulator", "general_properties", 30),
}
