package dev.loudbook.blockcountdown

import org.bukkit.Location

data class ParkourCourse(
    val worldName: String,
    val start: Location,
    val end: Location
)
