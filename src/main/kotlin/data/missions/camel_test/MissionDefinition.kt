package data.missions.camel_test

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetGoal
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.mission.MissionDefinitionAPI
import com.fs.starfarer.api.mission.MissionDefinitionPlugin

class MissionDefinition : MissionDefinitionPlugin {
    companion object {
        val LOGGER = Global.getLogger(MissionDefinition::class.java)!!
    }

    override fun defineMission(api: MissionDefinitionAPI) {
        api.initFleet(FleetSide.PLAYER, "CES", FleetGoal.ATTACK, false, 7)
        api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true, 7)

        api.setFleetTagline(FleetSide.PLAYER, "Camel  simulated training fleet")
        api.setFleetTagline(FleetSide.ENEMY, "Tritachyon Carrier Fleet")


        val settings = Global.getSettings()
        val faction = Global.getSector().getFaction("camel")!!
        var flagshipNeeded = true
        for (variant in settings.allVariantIds.filter {id -> id.startsWith("camel_")}.filter {id -> !id.endsWith("_Hull")}) {
            LOGGER.debug("Adding variant '$variant'")
            api.addToFleet(FleetSide.PLAYER, variant, FleetMemberType.SHIP, faction.pickRandomShipName(), flagshipNeeded)
            flagshipNeeded = false
        }

        api.addToFleet(FleetSide.ENEMY, "astral_Attack", FleetMemberType.SHIP, false)

        // Set up the map.
        val width = 15000f
        val height = 35000f
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f)
    }
}
