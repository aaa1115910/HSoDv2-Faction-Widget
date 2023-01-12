package dev.aaa1115910.hsodv2.faction.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration

class FactionWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object{
        private val uniqueWorkName=FactionWorker::class.java.simpleName

        fun enqueue(context: Context,force:Boolean=false){
            val manager = WorkManager.getInstance(context)
            val requestBuilder = PeriodicWorkRequestBuilder<FactionWorker>(
                Duration.ofMinutes(30)
            )
            var workPolicy = ExistingPeriodicWorkPolicy.KEEP

            // Replace any enqueued work and expedite the request
            if (force) {
                workPolicy = ExistingPeriodicWorkPolicy.REPLACE
            }

            manager.enqueueUniquePeriodicWork(
                uniqueWorkName,
                workPolicy,
                requestBuilder.build()
            )
        }
    }

    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(FactionAppWidget::class.java)

        runCatching {
            //loading
            setWidgetState(glanceIds, FactionInfo.Loading)
            //load data
            setWidgetState(glanceIds, FactionRepo.getFactionInfo(context))

            return Result.success()
        }.onFailure {
            return if (runAttemptCount < 10) {
                setWidgetState(glanceIds, FactionInfo.Unavailable(it.message ?: "unknown error"))
                Result.retry()
            } else {
                Result.failure()
            }
        }
        return Result.failure()
    }

    private suspend fun setWidgetState(glanceIds: List<GlanceId>, newState: FactionInfo) {
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                definition = FactionInfoStateDefinition,
                glanceId = glanceId,
                updateState = { newState }
            )
        }
        FactionAppWidget().updateAll(context)
    }
}
