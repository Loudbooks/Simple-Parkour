package dev.loudbook.blockcountdown

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class BlockCountdown : JavaPlugin() {
    val fileManager = FileManager(this)
    var courses = mutableListOf<ParkourCourse>()

    override fun onEnable() {
        fileManager.checkFile()

        courses = fileManager.loadFile().toMutableList()

        Bukkit.getPluginManager().registerEvents(MovementListener(this), this)
        getCommand("blockcountdown")?.setExecutor(CommandHandler(this))
    }

    override fun onDisable() {
        fileManager.saveFile(courses)
    }

    fun addCourse(course: ParkourCourse) {
        courses.add(course)
    }

    fun removeCourse(course: ParkourCourse) {
        courses.remove(course)
    }
}