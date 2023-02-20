package com.github.isturdy.camelenterprise.combat.weapons.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MissileAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
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
    private val deltaTarget = 0.5f

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

    override fun advance(amount: Float) {
        missile.giveCommand(ShipCommand.ACCELERATE)
        if (target?.location != null) {
            LOGGER.info("Using ship target location")
        } else {
            LOGGER.info("Using mouse target")
        }
        val targetPoint = target?.location ?: launchingShip.mouseTarget
        val origin = launcher.location
        LOGGER.info("Origin: $origin, targetPoint: $targetPoint, missile: ${missile.location}")
        LOGGER.info("Target: ${targetPoint.minus(origin)}, missile: ${missile.location.minus(origin)}")
        val angle = targetPoint.minus(origin).getFacing() - missile.location.minus(origin).getFacing()
        // When we have a target ship we could directly compute the delta (it's the
        // difference between the angular velocities); using the difference between
        // frames is necessary when using a mouse target whose velocity is unknown.
        val delta = (lastAngle - angle) / amount
        val deltaTargetForAngle = (angle * deltaTarget).coerceIn(-90.0f, 90.0f)
        LOGGER.info("Angle: $angle, delta: $delta, target: $deltaTargetForAngle")
        if (abs(delta - deltaTargetForAngle) > 1) {
            missile.giveCommand(
                if (delta < deltaTargetForAngle) {
                    ShipCommand.TURN_LEFT
                } else {
                    ShipCommand.TURN_RIGHT
                }
            )
        }
        lastAngle = angle
    }
}