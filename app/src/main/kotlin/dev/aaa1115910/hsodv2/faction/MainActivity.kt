package dev.aaa1115910.hsodv2.faction

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import dev.aaa1115910.hsodv2.faction.service.FactionWarningService
import dev.aaa1115910.hsodv2.faction.ui.theme.HSoDv2FactionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

val xpMap = mapOf(
    1 to "丽贝卡",
    2 to "眠梦",
    3 to "Yoko",
    4 to "丝诺",
    5 to "罗蕾莱",
    6 to "恰卡纳"
)

class MainActivity : ComponentActivity() {
    private lateinit var mService: FactionWarningService
    private var mBound: Boolean = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as FactionWarningService.FactionWarningServiceBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Intent(this, FactionWarningService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        startForegroundService(Intent(this, FactionWarningService::class.java))

        setContent {
            HSoDv2FactionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userPreferences = runBlocking { context.getUserPreferences() }

    var scoreText by remember { mutableStateOf("${userPreferences.score}") }
    var warningLineText by remember { mutableStateOf("${userPreferences.warningLine}") }
    var expandedXpDropdownMenu by remember { mutableStateOf(false) }

    var yourXp by remember { mutableStateOf(userPreferences.xpId) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("HSoDv2 阵营战微件") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "你的 XP: ${xpMap[yourXp]}")
                    Box {
                        IconButton(onClick = { expandedXpDropdownMenu = true }) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedXpDropdownMenu)
                        }
                        DropdownMenu(
                            expanded = expandedXpDropdownMenu,
                            onDismissRequest = { expandedXpDropdownMenu = false }
                        ) {
                            xpMap.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(text = name) },
                                    onClick = {
                                        yourXp = id
                                        expandedXpDropdownMenu = false
                                    })
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = scoreText,
                    onValueChange = { scoreText = it },
                    label = { Text(text = "你的崩坏能") },
                    supportingText = { Text(text = "又没有接口去获取分数，只能你自己填了呗") }
                )
                OutlinedTextField(
                    value = warningLineText,
                    onValueChange = { warningLineText = it },
                    label = { Text(text = "实物预警线") },
                    supportingText = { Text(text = "当你的崩坏能与实物线间的差距低于预警线时则发送通知\n例如 120000(实物线) 3000(预警线) 122999(你的崩坏能)\n此时会发送通知进行提醒\n注意: 实物线每 10 分钟才刷新一次") }
                )
                Button(onClick = {
                    var scoreNum = 0
                    var warningLineNum = 0
                    runCatching {
                        scoreNum = scoreText.toInt()
                        warningLineNum = warningLineText.toInt()
                    }.onFailure {
                        Toast.makeText(context, "请检查你填写的数据", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch(Dispatchers.Default) {
                        context.dataStore.edit { preferences ->
                            preferences[PreferencesKeys.XP_ID] = yourXp
                            preferences[PreferencesKeys.SCORE] = scoreNum
                            preferences[PreferencesKeys.WARNING_LINE] = warningLineNum

                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Text(text = "好就这样")
                }
                Text(text = "数据来源：搞事学园")
            }
            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .clickable {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/aaa1115910/HSoDv2-Faction-Widget")
                            )
                        )
                    },
                text = "github.com/aaa1115910/HSoDv2-Faction-Widget"
            )
        }
    }
}
