package com.eharrison.game.tdcreator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DataTest {
    val game = Game(10, 10,10)

    @Nested
    inner class TestGetTowersAt {
        @Test
        fun `get towers at a valid location`() {
            val tower = Tower(4,4)
            addTower(game, tower)

            var towers = getTowersAt(game,4,4)
            assertEquals(1, towers.size)
            assertTrue(towers.contains(tower))

            val tower2 = Tower(4,4,1)
            addTower(game, tower2)

            towers = getTowersAt(game,4,4,0,4,4,1)
            assertEquals(2, towers.size)
            assertTrue(towers.contains(tower))
            assertTrue(towers.contains(tower2))

            towers = getTowersAt(game,4,4,1,3,3,0)
            assertEquals(2, towers.size)
            assertTrue(towers.contains(tower))
            assertTrue(towers.contains(tower2))
        }
    }

    @Nested
    inner class TestAddTower {
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

            assertTrue(addTower(game, Tower(4,4,8,2,2,2)))

            assertEquals(4, game.towers.size)
        }

        @Test
        fun `add large tower outside of the game`() {
            assertFalse(addTower(game, Tower(9,9, sizeX=2, sizeY=2)))
            assertFalse(addTower(game, Tower(0,9, sizeX=2, sizeY=2)))
            assertFalse(addTower(game, Tower(9,0, sizeX=2, sizeY=2)))

            assertFalse(addTower(game, Tower(4,4,9,2,2,2)))

            assertTrue(game.towers.isEmpty())
        }

        @Test
        fun `add large tower inside of the game`() {
            assertTrue(addTower(game, Tower(4,4, sizeX=2, sizeY=2)))

            assertEquals(1, game.towers.size)
        }

        @Test
        fun `try to add overlapping towers`() {
            assertTrue(addTower(game, Tower(4,4)))
            assertFalse(addTower(game, Tower(4,4)))

            assertEquals(1, game.towers.size)

            assertTrue(addTower(game, Tower(4,4,1)))
            assertEquals(2, game.towers.size)

            assertFalse(addTower(game, Tower(4,4,1)))
            assertEquals(2, game.towers.size)
        }

        @Test
        fun `try to add overlapping large towers`() {
            assertTrue(addTower(game, Tower(4,4,0,2,2,2)))
            assertFalse(addTower(game, Tower(4,4,1,2,2,2)))
            assertTrue(addTower(game, Tower(4,4,2,2,2,2)))
            assertFalse(addTower(game, Tower(4,4,3,2,2,2)))
            assertTrue(addTower(game, Tower(4,4,4,2,2,2)))
            assertFalse(addTower(game, Tower(4,4,5,2,2,2)))
            assertTrue(addTower(game, Tower(4,4,6,2,2,2)))
        }
    }

    @Nested
    inner class TestRender {
        @Test
        fun `render all layer`() {
            val game = Game(10,10,3)

            addTower(game, Tower(1,8))
            addTower(game, Tower(4,4,0,2,2,2))

            println(render(game))
        }
    }
}
