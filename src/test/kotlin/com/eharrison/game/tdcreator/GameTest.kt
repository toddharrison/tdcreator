package com.eharrison.game.tdcreator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GameTest {
    val game = Game(10, 10,10)

    @Nested
    inner class TestAddCreep {
        @Test
        fun `Try to spawn a creep on a tower`() {
            val creep = Creep(1.5,1.2)
            val tower = Tower(1,1)
            addTower(game,tower)
            assertFalse(addCreep(game,creep))
            assertEquals(0,game.creeps.size)
        }

        @Test
        fun `Spawn a creep normally`() {
            val creep = Creep(4.3,2.9)
            assertTrue(addCreep(game,creep))
            assertEquals(1,game.creeps.size)
        }

        @Test
        fun `Try to Spawn a creep outside of the map`(){
            val creep1 = Creep(-2.3,3.4)
            val creep2 = Creep(1.3,-3.4)
            val creep3 = Creep(11.3,3.4)
            val creep4 = Creep(1.3,13.4)
            val creep5 = Creep(1.3,3.4,13.3)
            assertFalse(addCreep(game,creep1))
            assertFalse(addCreep(game,creep2))
            assertFalse(addCreep(game,creep3))
            assertFalse(addCreep(game,creep4))
            assertFalse(addCreep(game,creep5))
            assertEquals(0,game.creeps.size)
        }

        @Test
        fun `Spawn several creeps inside one space`(){
            val creep1 = Creep(3.3,4.4)
            val creep2 = Creep(3.3,4.4)
            addCreep(game,creep1)
            assertTrue(addCreep(game,creep1))
            assertEquals(2,game.creeps.size)
        }
    }

    @Nested
    inner class TestRemoveCreep {
        @Test
        fun `remove a creep`() {
            val creep = Creep(4.2,6.3)
            addCreep(game,creep)
            removeCreep(game,creep)
            assertEquals(0,game.creeps.size)
        }
    }

    @Nested
    inner class TestAddProjectile {
       @Test
       fun `add a projectile`() {
           val projectile = Projectile(3.3,4.4)
           assertTrue(addProjectile(game, projectile))
           assertEquals(1,game.projectiles.size)
       }

       @Test
       fun `Add a projectile on a tower`() {
           addTower(game, Tower(3,4))
           val projectile = Projectile(3.3,4.4)
           assertTrue(addProjectile(game, projectile))
           assertEquals(1,game.projectiles.size)
       }
    }

    @Nested
    inner class TestGetTowersAt {
        @Test
        fun `get towers at a valid location`() {
            val tower = Tower(4,4)
            addTower(game, tower)

            var towers = getTowersAt(game, Region(4,4))
            assertEquals(1, towers.size)
            assertTrue(towers.contains(tower))

            val tower2 = Tower(4,4,1)
            addTower(game, tower2)

            towers = getTowersAt(game, Region(4,4,0,4,4,1))
            assertEquals(2, towers.size)
            assertTrue(towers.contains(tower))
            assertTrue(towers.contains(tower2))

            towers = getTowersAt(game, Region(4,4,1,3,3,0))
            assertEquals(2, towers.size)
            assertTrue(towers.contains(tower))
            assertTrue(towers.contains(tower2))
        }
    }

    @Nested
    inner class TestGetCreepsAt {
        @Test
        fun `get creeps at a valid location`() {
            val creep = Creep(4.3,4.2)
            addCreep(game, creep)

            var creeps = getCreepsAt(game, Region(4,4))
            assertEquals(1, creeps.size)
            assertTrue(creeps.contains(creep))

            val creep2 = Creep(4.3,4.6,1.0)
            addCreep(game, creep2)

            creeps = getCreepsAt(game, Region(4,4,0,4,4,1))
            assertEquals(2, creeps.size)
            assertTrue(creeps.contains(creep))
            assertTrue(creeps.contains(creep2))

            creeps = getCreepsAt(game, Region(4,4,1,3,3,0))
            assertEquals(2, creeps.size)
            assertTrue(creeps.contains(creep))
            assertTrue(creeps.contains(creep2))
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
    inner class TestRemoveTower {
        @Test
        fun `remove tower not in game`() {
            assertTrue(addTower(game, Tower(4,4)))

            assertEquals(1, game.towers.size)

            val tower = Tower(5,5)

            assertFalse(removeTower(game, tower))

            assertEquals(1, game.towers.size)
        }

        @Test
        fun `remove tower in game`() {
            assertTrue(addTower(game, Tower(4,4)))
            assertTrue(addTower(game, Tower(5,5)))

            assertEquals(2, game.towers.size)

            assertTrue(removeTower(game, game.towers[0]))

            assertEquals(1, game.towers.size)
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
