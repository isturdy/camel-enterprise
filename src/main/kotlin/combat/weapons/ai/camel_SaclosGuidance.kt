package com.github.isturdy.camelenterprise.combat.weapons.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MissileAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.WeaponAPI.AIHints
import com.github.isturdy.camelenterprise.util.PDController
import com.gthishub.isturdy.camelenterprise.util.clampAngle
import org.lazywizard.lazylib.ext.getAngle
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

/*
 * This guidance roughly replicates the guidance law of SACLOS systems. It
 * attempts to keep the missile traveling along a straight line between the
 * launcher and the target based on the angle between the lines to the
 * missile and the target and its rate of change using a simple PD law.
 *
 * In practice, this has advantages and disadvantages in SS. It is strictly
 * incapable of second-chance attacks, and the constraint to remain on the
 * line between launcher and target forces less efficient flight paths than
 * missile-based guidance laws (especially when the launcher is moving rapidly,
 * which has no effect on typical missiles). On the other hand, it avoids
 * overcorrecting to target motion while still avoiding tail chases--the flight
 * path changes from near-direct pursuit to near-perfect leading as the missile
 * approaches the target. It is also immune to flares, as they are presumed not
 * to confuse the better shipboard sensors.
 */
class camel_SaclosGuidance(
    private val missile: MissileAPI,
    private val launchingShip: ShipAPI,
) : MissileAIPlugin {
    companion object {
        val LOGGER = Global.getLogger(camel_SaclosGuidance::class.java)
    }

    // The desired delta/angle. (It's the inverse of the number of seconds it
    // would take to correct if no further changes are made.)
    private val deltaTarget = 2.0f

    private val launcher = missile.weapon
    private val weaponGroup = launchingShip.getWeaponGroupFor(launcher)
    private val target = run {
        if (launchingShip.shipAI != null || weaponGroup.isAutofiring) {
            weaponGroup.getAutofirePlugin(launcher).targetShip ?: launchingShip.shipTarget
        } else {
            null
        }
    }
    private var lastAngle = 0.0f
    private val angleController = PDController(1.0f, 0.25f)

    override fun advance(amount: Float) {
        missile.giveCommand(ShipCommand.ACCELERATE)
        if (target?.location != null) {
            LOGGER.debug("Using ship target location")
        } else {
            LOGGER.debug("Using mouse target")
        }
        val targetPoint = target?.location ?: launchingShip.mouseTarget
        val origin = launcher.location
        val missileLoS = missile.location.minus(origin)
        val missileFacingFromLoS = missileLoS.getFacing()
        LOGGER.debug("Origin: $origin, targetPoint: $targetPoint, missile: ${missile.location}")
        LOGGER.debug("Target: ${targetPoint.minus(origin)}, missile: ${missile.location.minus(origin)}")
        val angle = (targetPoint.minus(origin).getFacing() - missileFacingFromLoS).clampAngle()
        // When we have a target ship we could directly compute the delta (it's the
        // difference between the angular velocities); using the difference between
        // frames is necessary when using a mouse target whose velocity is unknown.
        val delta = (angle - lastAngle) / amount
        val distanceFactor = missileLoS.length() / 1000
        val deltaTargetForAngle = -angle * deltaTarget * distanceFactor
        val control = angleController.advance(amount, deltaTargetForAngle - delta)
        val missileAngleFromLoS = (missile.facing - missileFacingFromLoS).clampAngle()
        LOGGER.debug("Angle: $angle, delta: $delta, target: $deltaTargetForAngle, control: $control, LoS angle: $missileAngleFromLoS")
        missile.giveCommand (if(missileAngleFromLoS < -90) {
            LOGGER.debug("Correcting right")
            ShipCommand.TURN_LEFT
        } else if (missileAngleFromLoS > 90) {
            LOGGER.debug("Correcting left")
            ShipCommand.TURN_RIGHT
        } else {
            if (control < 0) {
                ShipCommand.TURN_LEFT
            } else {
                ShipCommand.TURN_RIGHT
            }
        })
        lastAngle = angle
    }
}