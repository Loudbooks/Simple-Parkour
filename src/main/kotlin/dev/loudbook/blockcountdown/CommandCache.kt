package dev.loudbook.blockcountdown

import org.bukkit.Location

data class CommandCache(
    var worldName: String,
    var start: Location? = null,
    var end: Location? = null
)