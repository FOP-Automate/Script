package org.example

fun main(args: Array<String>) {

    val settings = Settings(
        SettingsJoin(
            listOf(
                CommandLineArgumentSettings(args),
                EnvironmentSettings(),
                PropertiesSettings(loadPropertiesFile("settings.properties"))
            )
        )
    )

}
