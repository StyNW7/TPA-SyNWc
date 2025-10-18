package edu.bluejack25_1.synwc.domain.service

import edu.bluejack25_1.synwc.data.repository.StreakRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class StreakService {
    private val streakRepository = StreakRepository()
    private var streakCheckJob: Job? = null

    fun startPeriodicStreakCheck() {
        // Stop any existing job
        streakCheckJob?.cancel()

        // Start new job to check streaks every 6 hours
        streakCheckJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                checkAndResetStreaks()
                delay(TimeUnit.HOURS.toMillis(6)) // Check every 6 hours
            }
        }
    }

    fun stopPeriodicStreakCheck() {
        streakCheckJob?.cancel()
        streakCheckJob = null
    }

    suspend fun checkAndResetStreaks() {
        streakRepository.checkAndResetStreaks()
    }

    suspend fun checkStreaksOnAppStart() {
        // This should be called when the app starts or user logs in
        checkAndResetStreaks()
    }
}