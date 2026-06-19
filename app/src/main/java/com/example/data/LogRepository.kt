package com.example.data

import kotlinx.coroutines.flow.Flow

class LogRepository(private val logDao: LogDao) {
    val allLogs: Flow<List<LogEntry>> = logDao.getAllLogs()

    suspend fun insert(log: LogEntry) {
        logDao.insertLog(log)
    }

    suspend fun deleteById(id: Int) {
        logDao.deleteLogById(id)
    }

    suspend fun clearAll() {
        logDao.clearAllLogs()
    }
}
