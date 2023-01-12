package dev.aaa1115910.hsodv2.faction.widget

import android.content.Context
import dev.aaa1115910.hsodv2.faction.PreferencesKeys
import dev.aaa1115910.hsodv2.faction.UserPreferences
import dev.aaa1115910.hsodv2.faction.dataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

object FactionRepo {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            header("referer", "https://redbean.tech")
        }
        BrowserUserAgent()
    }

    suspend fun getFactionInfo(context: Context): FactionInfo {
        println("getFactionInfo")
        return runCatching {
            //var xpId = 1
            //var score = 0
            val p = context.dataStore.data.map { preferences ->
                val xpId = preferences[PreferencesKeys.XP_ID] ?: 1
                val score = preferences[PreferencesKeys.SCORE] ?: 1
                UserPreferences(xpId, score)
            }.first()
            println("-xp: ${p.xpId}")
            println("-score: ${p.score}")
            FactionInfo.Available(
                currentData = getFactionData(),
                yourXpId = p.xpId,
                yourScore = p.score
            )
        }
            .onFailure {
                println(it)
                return FactionInfo.Unavailable(it.message ?: "")
            }
            .getOrDefault(FactionInfo.Unavailable("get data failed"))
    }

    suspend fun getFactionData(
        id: Int = 20231
    ): FactionData =
        client
            .get("https://api-1256168079.cos.ap-chengdu.myqcloud.com/faction/$id/data.json")
            .body()
}

@Serializable
data class FactionData(
    val factions: Factions = Factions(),
    val counts: Counts = Counts()
) {
    @Serializable
    data class Factions(
        val faction1: FactionItem = FactionItem(1),
        val faction2: FactionItem = FactionItem(2),
        val faction3: FactionItem = FactionItem(3),
        val faction4: FactionItem = FactionItem(4),
        val faction5: FactionItem = FactionItem(5),
        val faction6: FactionItem = FactionItem(6)
    ) {
        @Serializable
        data class FactionItem(
            val factionId: Int,
            val factionName: String = "",
            val data: List<FactionItemData> = emptyList()
        ) {
            @Serializable
            data class FactionItemData(
                val time: String,
                val point: Int
            )
        }
    }

    @Serializable
    data class Counts(
        val top1k: List<Int> = emptyList(),
        val top1w: List<Int> = emptyList(),
        val user: List<Int> = emptyList()
    )
}

@Serializable
sealed interface FactionInfo {
    @Serializable
    object Loading : FactionInfo

    @Serializable
    data class Available(
        val currentData: FactionData,
        val yourXpId: Int = 1,
        val yourScore: Int = 0
    ) : FactionInfo

    @Serializable
    data class Unavailable(val message: String) : FactionInfo
}