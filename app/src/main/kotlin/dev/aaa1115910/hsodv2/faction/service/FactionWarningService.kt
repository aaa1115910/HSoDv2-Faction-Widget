package dev.aaa1115910.hsodv2.faction.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.datastore.preferences.core.edit
import dev.aaa1115910.hsodv2.faction.App
import dev.aaa1115910.hsodv2.faction.PreferencesKeys
import dev.aaa1115910.hsodv2.faction.R
import dev.aaa1115910.hsodv2.faction.dataStore
import dev.aaa1115910.hsodv2.faction.getUserPreferences
import dev.aaa1115910.hsodv2.faction.widget.FactionRepo
import dev.aaa1115910.hsodv2.faction.xpMap
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

@OptIn(DelicateCoroutinesApi::class)
class FactionWarningService : Service() {
    companion object {
        private const val TAG = "FactionWarningService"
        private const val KEEP_ALIVE_NOTIFICATION_ID = 1
        private const val KEEP_ALIVE_NOTIFICATION_CHANNEL_ID = "keep-alive"
        private const val SCORE_WARNING_NOTIFICATION_ID = 2
        private const val SCORE_WARNING_NOTIFICATION_CHANNEL_ID = "score-warning"
        private const val KEY_TEXT_REPLY = "key_text_reply"
    }

    private val mBinder = FactionWarningServiceBinder()
    private lateinit var manager: NotificationManager
    private val timer = Timer()

    private var keepAliveNotification =
        NotificationCompat.Builder(App.context, KEEP_ALIVE_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("title")
            .setContentText("text")
            //.setContentIntent(PendingIntent.getService(this,0,Intent(App.context,MainActivity::class.java),PendingIntent.FLAG_ONE_SHOT))
            .build()

    private lateinit var scoreWarningNotification: Notification

    private var remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
        setLabel("在此输入我的崩坏能")
        build()
    }

    private var replyPendingIntent: PendingIntent =
        if (Build.VERSION.SDK_INT >= 34) {
            PendingIntent.getBroadcast(
                App.context,
                1,
                Intent().apply { action = "quick.reply.input" },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT
            )
        } else {
                PendingIntent.getBroadcast(
                    App.context,
                    1,
                    Intent().apply { action = "quick.reply.input" },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
        }


    private fun getMessageText(intent: Intent): String {
        return RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT_REPLY).toString()
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val replyMessage = getMessageText(intent)
            Log.d(TAG, "Receive replay message: $replyMessage")
            runCatching {
                GlobalScope.launch(Dispatchers.Default) {
                    context.dataStore.edit { preferences ->
                        preferences[PreferencesKeys.SCORE] = replyMessage.toInt()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "更新崩坏能成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                    runCatching {
                        updateData()
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }.onFailure {
                Toast.makeText(context, "更新崩坏能失败，请检查自己输入的内容", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    init {
        updateScoreWarningNotification("", 0, 0)
    }

    private fun updateScoreWarningNotification(xp: String, userScore: Int, currentScore: Int) {
        val notification =
            NotificationCompat.Builder(App.context, SCORE_WARNING_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)

        if (userScore < currentScore) {
            notification.setContentTitle("分数低于实物线 - $xp")
            notification.setContentText(
                """
                低于实物线 ${currentScore - userScore} 分
                实物线: $currentScore, 崩坏能: $userScore
            """.trimIndent()
            )
        } else {
            notification.setContentTitle("实物线安全预警 - $xp")
            notification.setContentText(
                """
                仅比实物线高出 ${userScore - currentScore} 分
                实物线: $currentScore, 崩坏能: $userScore
            """.trimIndent()
            )
        }

        val action: NotificationCompat.Action =
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_edit, "更新我的崩坏能记录", replyPendingIntent
            ).addRemoteInput(remoteInput).build()

        notification.addAction(action)

        scoreWarningNotification = notification.build()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val keepAliveChannel =
            NotificationChannel(
                KEEP_ALIVE_NOTIFICATION_CHANNEL_ID,
                "实时监控",
                NotificationManager.IMPORTANCE_NONE
            )
        val scoreWarningChannel =
            NotificationChannel(
                SCORE_WARNING_NOTIFICATION_CHANNEL_ID,
                "实物线危机",
                NotificationManager.IMPORTANCE_HIGH
            )
        manager.createNotificationChannels(listOf(keepAliveChannel, scoreWarningChannel))

        timer.schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "Update data task")
                GlobalScope.launch(Dispatchers.Default) {
                    runCatching {
                        updateData()
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }

        }, 0, 1000 * 60)

        val filter = IntentFilter()
        filter.addCategory(this.packageName)
        filter.addAction("quick.reply.input")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }

        super.onCreate()
    }

    private suspend fun updateData() {
        val response = FactionRepo.getFactionData(id = 20231)
        val userPreferences = runBlocking { App.context.getUserPreferences() }

        val faction = when (userPreferences.xpId) {
            1 -> response.factions.faction1
            2 -> response.factions.faction2
            3 -> response.factions.faction3
            4 -> response.factions.faction4
            5 -> response.factions.faction5
            6 -> response.factions.faction6
            else -> response.factions.faction1
        }

        if (faction.data.last().point + userPreferences.warningLine > userPreferences.score) {
            updateScoreWarningNotification(
                xpMap[userPreferences.xpId] ?: "未知",
                userPreferences.score,
                faction.data.last().point
            )
            manager.notify(SCORE_WARNING_NOTIFICATION_ID, scoreWarningNotification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(KEEP_ALIVE_NOTIFICATION_ID, keepAliveNotification)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    inner class FactionWarningServiceBinder : Binder() {
        fun getService(): FactionWarningService = this@FactionWarningService
    }
}

