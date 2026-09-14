package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.game.castlewar.entities.MovingCastle
import com.game.castlewar.game.GameManager
import com.game.castlewar.game.GameState
import com.game.castlewar.resources.ResourceType
import com.game.castlewar.upgrades.UpgradeManager
import com.game.castlewar.utils.CollisionUtils
import com.game.castlewar.weapons.CannonWeapon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Castle War", appName)
    }

    @Test
    fun `mainActivity launches successfully`() {
        val controller = org.robolectric.Robolectric.buildActivity(com.game.castlewar.MainActivity::class.java)
        val activity = controller.create().start().resume().get()
        assertTrue(activity != null)
    }

    @Test
    fun `gameManager transitions states correctly`() {
        val manager = GameManager()
        assertEquals(GameState.MAIN_MENU, manager.currentState)

        manager.startGame()
        assertEquals(GameState.PLAYING, manager.currentState)
        assertTrue(manager.currentState.isSimulationActive)

        manager.pauseGame()
        assertEquals(GameState.PAUSED, manager.currentState)
        assertFalse(manager.currentState.isSimulationActive)

        manager.resumeGame()
        assertEquals(GameState.PLAYING, manager.currentState)

        manager.triggerLevelUp()
        assertEquals(GameState.LEVEL_UP, manager.currentState)

        manager.completeLevelUp()
        assertEquals(GameState.PLAYING, manager.currentState)

        manager.startBossFight()
        assertEquals(GameState.BOSS_FIGHT, manager.currentState)
        assertTrue(manager.currentState.isSimulationActive)

        manager.triggerGameOver()
        assertEquals(GameState.GAME_OVER, manager.currentState)
        assertFalse(manager.currentState.isSimulationActive)

        manager.returnToMainMenu()
        assertEquals(GameState.MAIN_MENU, manager.currentState)
    }

    @Test
    fun `movingCastle takes damage and collects resources`() {
        val castle = MovingCastle(0f, 0f)
        assertEquals(1000f, castle.currentHealth, 0.01f)

        // Damage calculation factoring armor (armor = 15)
        castle.takeDamage(100f)
        // effective damage = 100 * (100 / (100 + 15)) = 86.95
        assertTrue(castle.currentHealth < 1000f)
        assertTrue(castle.currentHealth > 900f)

        // Repair
        castle.repair(50f)
        assertTrue(castle.currentHealth <= castle.maximumHealth)

        // Add resource
        castle.addResource(ResourceType.GOLD, 50)
        assertEquals(50, castle.getResource(ResourceType.GOLD))

        // Experience and level up
        val initialLvl = castle.level
        val leveledUp = castle.addExperience(castle.experienceRequired)
        assertTrue(leveledUp)
        assertEquals(initialLvl + 1, castle.level)
    }

    @Test
    fun `collisionUtils calculates accurate physics`() {
        assertTrue(CollisionUtils.circleVsCircle(0f, 0f, 10f, 15f, 0f, 10f))
        assertFalse(CollisionUtils.circleVsCircle(0f, 0f, 5f, 50f, 0f, 5f))

        assertTrue(CollisionUtils.circleVsRectangle(10f, 10f, 5f, 0f, 0f, 20f, 20f))
        assertFalse(CollisionUtils.circleVsRectangle(50f, 50f, 5f, 0f, 0f, 20f, 20f))
    }

    @Test
    fun `upgradeManager provides valid choices and applies successfully`() {
        val manager = UpgradeManager()
        val choices = manager.getRandomChoices(3)
        assertEquals(3, choices.size)

        val castle = MovingCastle(0f, 0f)
        val initialHp = castle.maximumHealth
        val hpUpgrade = choices.firstOrNull { it.id == "reinforced_hull" }
            ?: manager.allUpgrades.first { it.id == "reinforced_hull" }

        hpUpgrade.apply(castle)
        assertTrue(castle.maximumHealth > initialHp)
    }
}
