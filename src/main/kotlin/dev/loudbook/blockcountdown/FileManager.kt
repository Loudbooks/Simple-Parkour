package dev.loudbook.blockcountdown

import org.bukkit.Location
import java.io.File

class FileManager(private val plugin: BlockCountdown) {
    var enableCountdown: Boolean = true

    companion object {
        private const val FILE_NAME = "cache.db"
    }

    fun checkFile() {
        val file = plugin.dataFolder

        if (!file.exists()) {
            file.mkdir()
        }

        val dataFile = File(file.toURI().resolve(FILE_NAME))

        if (!dataFile.exists()) {
            dataFile.createNewFile()
        }
    }

    fun saveFile(courses: List<ParkourCourse>) {
        val file = File(plugin.dataFolder.toURI().resolve(FILE_NAME))

        println("Saving file...")

        file.writeText(
            courses.joinToString("\n") { "${it.worldName}: ${it.start.x},${it.start.y},${it.start.z},${it.end.x},${it.end.y},${it.end.z}" } +
                    "\n\n" +
                    enableCountdown.toString()
        )

        println("File saved.")
    }

    fun loadFile(): List<ParkourCourse> {
        val file = File(plugin.dataFolder.toURI().resolve(FILE_NAME))
        val courses = mutableListOf<ParkourCourse>()

        val string = file.readText().split("\n\n")

        if (string.size != 2) {
            return courses
        }

        enableCountdown = string[1].toBoolean()

        val finalString = string[0].split("\n")

        finalString.forEach {
            val split = it.split(": ")
            val worldName = split[0]
            val locations = split[1].split(",")

            val start = Location(plugin.server.getWorld(worldName), locations[0].toDouble(), locations[1].toDouble(), locations[2].toDouble())
            val end = Location(plugin.server.getWorld(worldName), locations[3].toDouble(), locations[4].toDouble(), locations[5].toDouble())

            courses.add(ParkourCourse(worldName, start, end))
        }

        return courses
    }
}