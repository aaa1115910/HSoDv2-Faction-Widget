package dev.aaa1115910.hsodv2.faction

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import dev.aaa1115910.hsodv2.faction.ui.theme.HSoDv2FactionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HSoDv2FactionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val scope = rememberCoroutineScope()

                    val p = runBlocking {
                        this@MainActivity.dataStore.data.map { preferences ->
                            val xpId = preferences[PreferencesKeys.XP_ID] ?: 1
                            val score = preferences[PreferencesKeys.SCORE] ?: 0
                            UserPreferences(xpId, score)
                        }.first()
                    }

                    var scoreText by remember { mutableStateOf("${p.score}") }
                    var expandedXpDropdownMenu by remember { mutableStateOf(false) }

                    var yourXp by remember { mutableStateOf(p.xpId) }

                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text("HSoDv2 阵营战微件") }
                            )
                        }
                    ) { innerPadding ->
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
                                label = { Text(text = "你的分数") },
                                supportingText = { Text(text = "又没有接口去获取分数，只能你自己填了呗") }
                            )
                            Button(onClick = {
                                val scoreNum = runCatching { scoreText.toInt() }
                                    .onFailure {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "请检查你填写的数据",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                    .getOrDefault(0)
                                scope.launch(Dispatchers.Default) {
                                    /*GlanceAppWidgetManager(this@MainActivity)
                                        .getGlanceIds(FactionAppWidget::class.java)
                                        .forEach { glanceId ->
                                            updateAppWidgetState(
                                                context = this@MainActivity,
                                                glanceId = glanceId,
                                            ) {
                                                it[intPreferencesKey("xp_id")] = yourXp
                                                it[PreferencesKeys.SCORE] = scoreNum
                                            }
                                        }*/

                                    this@MainActivity.dataStore.edit { preferences ->
                                        preferences[PreferencesKeys.XP_ID] = yourXp
                                        preferences[PreferencesKeys.SCORE] = scoreNum

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "保存成功",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }) {
                                Text(text = "好就这样")
                            }
                            Text(text = "数据来源：搞事学园")
                            Text(
                                modifier = Modifier.clickable {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://github.com/aaa1115910/HSoDv2-Faction-Widget")
                                        )
                                    )
                                },
                                text = "项目地址：https://github.com/aaa1115910/HSoDv2-Faction-Widget"
                            )
                        }
                    }
                }
            }
        }
    }
}
