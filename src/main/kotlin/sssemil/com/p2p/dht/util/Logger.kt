package sssemil.com.p2p.dht.util

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class Logger {
    companion object {
        private const val DEBUG = false

        private const val logFileName = "dht.log"

        private var logOut = PrintWriter(File(logFileName))
        private val logLock = Object()

        var writeLogsToFile = false

        fun d(msg: String) {
            if (DEBUG) {
                val msgWithInfo = "[DEBUG][${getDateString()}]: $msg"

                System.out.println(msgWithInfo)

                if (writeLogsToFile) {
                    synchronized(logLock) {
                        logOut.println(msgWithInfo)
                        logOut.flush()
                    }
                }
            }
        }

        fun i(msg: String) {
            val msgWithInfo = "[INFO][${getDateString()}]: $msg"

            System.out.println(msgWithInfo)

            if (writeLogsToFile) {
                synchronized(logLock) {
                    logOut.println(msgWithInfo)
                    logOut.flush()
                }
            }
        }

        fun w(msg: String) {
            val msgWithInfo = "[WARNING][${getDateString()}]: $msg"

            System.out.println(msgWithInfo)

            if (writeLogsToFile) {
                synchronized(logLock) {
                    logOut.println(msgWithInfo)
                    logOut.flush()
                }
            }
        }

        fun e(msg: String) {
            val msgWithInfo = "[ERROR][${getDateString()}]: $msg"

            System.err.println(msgWithInfo)

            if (writeLogsToFile) {
                synchronized(logLock) {
                    logOut.println(msgWithInfo)
                    logOut.flush()
                }
            }
            //throw RuntimeException(msgWithInfo)
        }

        private fun getDateString(): String {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            return dateFormat.format(calendar.time)
        }

        fun getTimeString(deltaTime: Long = System.currentTimeMillis()): String {
            var holder = deltaTime
            val second = holder / 1000 % 60
            holder -= second * 1000
            val minute = holder / (1000 * 60) % 60
            holder -= minute * 1000 * 60
            val hour = holder / (1000 * 60 * 60) % 24
            holder -= hour * 1000 * 60 * 24

            return String.format("%02dh:%02dm:%02ds:%04dms", hour, minute, second, holder)
        }

        fun setOutputDirectory(rootFolder: File) {
            rootFolder.mkdirs()
            val logFile = File(rootFolder, logFileName)
            logOut = PrintWriter(BufferedWriter(FileWriter(logFile, true)))
        }
    }
}