package dev.loudbook.blockcountdown

import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z")

    val systemTime: String
        get() = simpleDateFormat.format(Date(System.currentTimeMillis()))

    fun getFormattedTime(time: Long): String {
        val sdf = SimpleDateFormat("mm:ss.SSS")
        return sdf.format(Date(time))
    }
}