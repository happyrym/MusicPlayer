package com.rymin.common.utils

object TimeUtils {
    /**
     * Converts milliseconds to a formatted time string in the format "mm:ss".
     * @param milliseconds The time in milliseconds.
     * @return A formatted time string (e.g., "03:45").
     */
    fun formatTime(milliseconds: Long): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
