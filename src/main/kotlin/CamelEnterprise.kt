package com.github.isturdy.camelenterprise

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.combat.MissileAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.github.isturdy.camelenterprise.combat.weapons.ai.camel_SaclosGuidance
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

    override fun pickMissileAI(missile: MissileAPI, launchingShip: ShipAPI?): PluginPick<MissileAIPlugin>? {
        return when (missile.projectileSpecId) {
            in listOf("camel_klein_missile", "camel_klein_hs2_missile") -> PluginPick(
                camel_SaclosGuidance(missile, launchingShip!!),
                CampaignPlugin.PickPriority.MOD_SET
            )
            else -> null
        }
    }

    private fun setLogLevel(level: Level) {
        CamelEnterprise.LOGGER.level = level
        MissionDefinition.LOGGER.level = level
        camel_SaclosGuidance
    }
}