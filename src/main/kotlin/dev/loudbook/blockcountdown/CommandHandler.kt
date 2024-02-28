package dev.loudbook.blockcountdown

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandHandler(private val plugin: BlockCountdown) : CommandExecutor {
    private val cache = mutableMapOf<Player, CommandCache>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!command.name.equals("blockcountdown", true)) return false

        if (!sender.isOp) {
            sender.sendMessage("${ChatColor.RED}You do not have permission to use this command.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${ChatColor.GREEN}dev.loudbook.blockcountdown.BlockCountdown v1.0")
            return true
        }

        val player = sender as? Player ?: run {
            sender.sendMessage("${ChatColor.RED}You must be a player to use this command.")
            return true
        }

        when (args[0]) {
            "timer" -> {
                if (args.size > 2) {
                    sender.sendMessage("${ChatColor.RED}Invalid usage. /blockcountdown timer [true/false]")
                    return true
                }

                val enable = args.getOrNull(1)?.toBooleanStrictOrNull() ?: run {
                    sender.sendMessage("${ChatColor.RED}Invalid usage. /blockcountdown timer [true/false]")
                    return true
                }

                plugin.fileManager.enableCountdown = enable
                sender.sendMessage("${ChatColor.GREEN}Timer has been set to $enable.")
            }

            "reset" -> {
                cache.remove(player)
                player.sendMessage("${ChatColor.RED}Cache has been reset.")
            }
            "start" -> {
                if (args.size > 1) {
                    player.sendMessage("${ChatColor.RED}Invalid usage. /blockcountdown start")
                    return true
                }

                val location = player.location.block.location.clone().add(0.5, 0.0, 0.5)

                val cache = cache[player] ?: CommandCache(location.world?.name ?: "world")
                cache.start = location

                player.sendMessage("${ChatColor.GREEN}Start location has been set at ${ChatColor.BOLD}${location.x}, ${location.y}, ${location.z}.")

                this.cache[player] = cache
                finishCache(player)
            }
            "end" -> {
                if (args.size > 1) {
                    sender.sendMessage("${ChatColor.RED}Invalid usage. /blockcountdown end")
                    return true
                }

                val location = player.location.block.location.clone().add(0.5, 0.0, 0.5)

                val cache = cache[player] ?: CommandCache(location.world?.name ?: "world")
                cache.end = location

                player.sendMessage("${ChatColor.GREEN}End location has been set at ${ChatColor.BOLD}${location.x}, ${location.y}, ${location.z}.")

                this.cache[player] = cache
                finishCache(player)
            }
        }

        return true
    }

    private fun finishCache(player: Player) {
        val cache = cache[player] ?: return

        val start = cache.start ?: run {
            return
        }

        val end = cache.end ?: run {
            return
        }

        val course = ParkourCourse(cache.worldName, start, end)
        plugin.addCourse(course)

        this.cache.remove(player)

        player.sendMessage("${ChatColor.GREEN}${ChatColor.BOLD}Course has been added.")
    }
}