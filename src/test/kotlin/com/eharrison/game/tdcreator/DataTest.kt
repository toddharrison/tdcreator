package com.eharrison.game.tdcreator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataTest {
    @Nested
    inner class TestAddTower {
        val game = Game(10, 10)

        @Test
        fun `add tower outside of the game`() {
            assertFalse(addTower(game, Tower(-1, -1)))
            assertFalse(addTower(game, Tower(0, -1)))
            assertFalse(addTower(game, Tower(-1, 0)))
            assertFalse(addTower(game, Tower(10, 10)))
            assertFalse(addTower(game, Tower(9, 10)))
            assertFalse(addTower(game, Tower(10, 9)))

            assertTrue(game.towers.isEmpty())
        }

        @Test
        fun `add tower inside of the game`() {
            assertTrue(addTower(game, Tower(0,0)))
            assertTrue(addTower(game, Tower(4,4)))
            assertTrue(addTower(game, Tower(9,9)))

            assertEquals(3, game.towers.size)
        }

        @Test
        fun `add large tower outside of the game`() {
            assertFalse(addTower(game, Tower(9,9,2,2)))
            assertFalse(addTower(game, Tower(0,9,2,2)))
            assertFalse(addTower(game, Tower(9,0,2,2)))

            assertTrue(game.towers.isEmpty())
        }

        @Test
        fun `add large tower inside of the game`() {
            assertTrue(addTower(game, Tower(4,4,2,2)))

            assertEquals(1, game.towers.size)
        }
    }
}
