package com.dfi.spike.log

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Append-only local spike event log for accidental-activation and session
 * measurement. Intentionally NOT the DFI evidence schema: one line per event,
 * greppable, shareable by reading the file from the in-app viewer.
 *
 * Format: <epochMs>|<EVENT_NAME>|k=v|k=v
 */
class SpikeEventLog(context: Context) {

    private val file = logFile(context)

    @Synchronized
    fun log(name: String, fields: Map<String, String> = emptyMap()) {
        val line = buildString {
            append(System.currentTimeMillis())
            append('|').append(name)
            for ((k, v) in fields) {
                append('|').append(k).append('=')
                append(v.replace('\n', ' ').replace('|', '/'))
            }
            append('\n')
        }
        try {
            file.appendText(line)
        } catch (t: Throwable) {
            Log.e(TAG, "log append failed", t)
        }
        Log.d(TAG, line.trim())
    }

    companion object {
        private const val TAG = "DFISpike"
        private const val FILE_NAME = "spike_events.log"

        private fun logFile(context: Context) = File(context.filesDir, FILE_NAME)

        fun read(context: Context): String {
            val f = logFile(context)
            return if (f.exists()) f.readText() else "(no spike events logged yet)"
        }

        fun clear(context: Context) {
            logFile(context).delete()
        }
    }
}
