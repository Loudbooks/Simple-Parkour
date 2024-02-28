package dev.loudbook.blockcountdown

import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.meta.FireworkMeta
import java.util.*

class MovementListener(private val plugin: BlockCountdown) : Listener {
    private val endTasks = mutableMapOf<UUID, Int>()
    private val timerTasks = mutableMapOf<UUID, Int>()
    private val elapsedTimes = mutableMapOf<UUID, Long>()
    private val justFinished = mutableListOf<UUID>()

    private val colors = arrayOf(
        org.bukkit.Color.AQUA,
        org.bukkit.Color.BLACK,
        org.bukkit.Color.BLUE,
        org.bukkit.Color.FUCHSIA,
        org.bukkit.Color.GRAY,
        org.bukkit.Color.GREEN,
        org.bukkit.Color.LIME,
        org.bukkit.Color.MAROON,
        org.bukkit.Color.NAVY,
        org.bukkit.Color.OLIVE,
        org.bukkit.Color.ORANGE,
        org.bukkit.Color.PURPLE,
        org.bukkit.Color.RED,
        org.bukkit.Color.SILVER,
        org.bukkit.Color.TEAL,
        org.bukkit.Color.WHITE,
        org.bukkit.Color.YELLOW
    )

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val location = player.location

        plugin.courses.forEach { course ->
            if (location.block.location.distance(course.start.block.location) < 2.0) {
                tryStart(player)
            } else if (justFinished.contains(player.uniqueId)) {
                justFinished.remove(player.uniqueId)
            }

            if (location.block.location.distance(course.end.block.location) < 2.0) {
                tryEnd(player, course)
            }
        }
    }

    private fun tryStart(player: Player) {
        if (timerTasks.containsKey(player.uniqueId)) {
            return
        }

        if (justFinished.contains(player.uniqueId)) {
            return
        }

        val startTime = System.currentTimeMillis()

        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (plugin.fileManager.enableCountdown && timerTasks.containsKey(player.uniqueId)) {
                player.spigot().sendMessage(
                    net.md_5.bungee.api.ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(
                        "${ChatColor.GREEN}${ChatColor.BOLD}Time: ${ChatColor.DARK_GREEN}${TimeUtil.getFormattedTime(System.currentTimeMillis() - startTime)}"
                    )
                )
            }

            elapsedTimes[player.uniqueId] = System.currentTimeMillis() - startTime
        }, 0, 1).taskId

        timerTasks[player.uniqueId] = task
    }

    private fun tryEnd(player: Player, course: ParkourCourse) {
        if (!endTasks.containsKey(player.uniqueId)) {
            var timesRun = 0

            val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
                if (timesRun == 10) {
                    Bukkit.getScheduler().cancelTask(timerTasks[player.uniqueId] ?: 0)
                    timerTasks.remove(player.uniqueId)
                    player.teleport(course.start)

                    player.sendTitle(
                        "${ChatColor.GREEN}${ChatColor.BOLD}Congratulations!",
                        "${ChatColor.GRAY}Time: ${ChatColor.DARK_GRAY}${TimeUtil.getFormattedTime(elapsedTimes[player.uniqueId] ?: 0)}",
                        10,
                        70,
                        20
                    )

                    elapsedTimes.remove(player.uniqueId)

                    Bukkit.getScheduler().cancelTask(endTasks[player.uniqueId] ?: 0)
                    endTasks.remove(player.uniqueId)

                    player.playSound(player.location, "entity.player.levelup", 1.0f, 1.0f)
                    spawnFirework(player)

                    justFinished.add(player.uniqueId)
                } else {
                    if (player.location.block != course.end.block) {
                        Bukkit.getScheduler().cancelTask(endTasks[player.uniqueId] ?: 0)
                        endTasks.remove(player.uniqueId)

                        return@Runnable
                    }

                    if (elapsedTimes[player.uniqueId] == null) {
                        Bukkit.getScheduler().cancelTask(endTasks[player.uniqueId] ?: 0)
                        endTasks.remove(player.uniqueId)

                        return@Runnable
                    }

                    player.playSound(player.location, "entity.experience_orb.pickup", 1.0f, 1.0f)

                    player.sendTitle(
                        "${ChatColor.WHITE}${ChatColor.BOLD}${10 - timesRun}",
                        "",
                        10,
                        70,
                        20
                    )

                    timesRun++
                }
            }, 0, 20).taskId

            endTasks[player.uniqueId] = task
        }
    }

    private fun spawnFirework(player: Player) {
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val xOffset = Random().nextInt(11) - 5
            val zOffset = Random().nextInt(11) - 5

            val fireworkMeta: FireworkMeta = Bukkit.getItemFactory().getItemMeta(Material.FIREWORK_ROCKET) as FireworkMeta

            val effect = FireworkEffect.builder().withColor(colors.random())
                .trail(true)
                .withFade(colors.random())
                .flicker(true).build()

            fireworkMeta.power = Random().nextInt(127) + 1
            fireworkMeta.addEffect(effect)

            val newFirework =
                player.world.spawn(player.location.clone().add(xOffset.toDouble(), 10.0, zOffset.toDouble()), org.bukkit.entity.Firework::class.java)
            newFirework.fireworkMeta = fireworkMeta

            newFirework.detonate()
        }, 1, 10)

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            Bukkit.getScheduler().cancelTask(task.taskId)
        }, 150)
    }
}