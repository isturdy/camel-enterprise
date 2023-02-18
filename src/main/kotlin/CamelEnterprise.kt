package com.github.isturdy.camelenterprise

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import data.missions.camel_test.MissionDefinition
import org.apache.log4j.Level
import org.json.JSONObject

class CamelEnterprise : BaseModPlugin() {
    companion object {
        val LOGGER = Global.getLogger(CamelEnterprise::class.java)!!
        const val SETTINGS_FILE: String = "camel-enterprise-settings.json"
        var SETTINGS: Settings = Settings(JSONObject())
    }

    override fun onApplicationLoad() {
        SETTINGS = Settings(Global.getSettings().loadJSON(SETTINGS_FILE))
        LOGGER.info("Camel enterprise settings: $SETTINGS")
        setLogLevel(SETTINGS.LOG_LEVEL)
    }

    private fun setLogLevel(level: Level) {
        CamelEnterprise.LOGGER.level = level
        MissionDefinition.LOGGER.level = level
    }
}