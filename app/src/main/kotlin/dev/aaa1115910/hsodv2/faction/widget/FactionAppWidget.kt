package dev.aaa1115910.hsodv2.faction.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dev.aaa1115910.hsodv2.faction.AppWidgetBox
import dev.aaa1115910.hsodv2.faction.AppWidgetColumn
import dev.aaa1115910.hsodv2.faction.appWidgetBackgroundCornerRadius
import dev.aaa1115910.hsodv2.faction.ui.theme.HSoDv2FactionGlanceTheme
import dev.aaa1115910.hsodv2.faction.xpMap


class FactionAppWidget : GlanceAppWidget() {

    companion object {
        private val thinProMode = DpSize(60.dp, 100.dp)
        private val thinMode = DpSize(100.dp, 100.dp)
        private val smallBannerMode = DpSize(200.dp, 100.dp)
        private val largeBannerMode = DpSize(300.dp, 100.dp)
        private val largeMode = DpSize(300.dp, 200.dp)
    }

    override val stateDefinition = FactionInfoStateDefinition

    //override val sizeMode: SizeMode get() = SizeMode.Exact
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(thinProMode, thinMode, smallBannerMode, largeBannerMode, largeMode)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        val factionInfo = currentState<FactionInfo>()
        val size = LocalSize.current

        HSoDv2FactionGlanceTheme {
            when (factionInfo) {
                is FactionInfo.Loading -> {
                    AppWidgetBox(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is FactionInfo.Available -> {
                    Box {
                        when (size) {
                            thinProMode -> FactionProThin(factionInfo)
                            thinMode -> FactionThin(factionInfo)
                            smallBannerMode -> FactionSmallBanner(factionInfo)
                            largeBannerMode -> FactionLargeBanner(factionInfo)
                            largeMode -> FactionLarge(factionInfo)
                        }
                        //Text(text = "$size")
                    }
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(actionRunCallback<UpdateFactionCallback>())
                    ) {}
                }

                is FactionInfo.Unavailable -> {
                    AppWidgetColumn(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Data not available")
                        Text(text = factionInfo.message)
                        Button("Refresh", actionRunCallback<UpdateFactionCallback>())
                    }
                }
            }
        }
    }
}

class UpdateFactionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        FactionWorker.enqueue(context = context, force = true)
    }
}

class FactionAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = FactionAppWidget()
}

//1x1
@Composable
fun FactionProThin(factionInfo: FactionInfo.Available) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(4.dp)
            .appWidgetBackground()
            .background(GlanceTheme.colors.background)
            .appWidgetBackgroundCornerRadius(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = xpMap[factionInfo.yourXpId] ?: "???",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        val currentScore = when (factionInfo.yourXpId) {
            1 -> factionInfo.currentData.factions.faction1.data.last().point
            2 -> factionInfo.currentData.factions.faction2.data.last().point
            3 -> factionInfo.currentData.factions.faction3.data.last().point
            4 -> factionInfo.currentData.factions.faction4.data.last().point
            5 -> factionInfo.currentData.factions.faction5.data.last().point
            6 -> factionInfo.currentData.factions.faction6.data.last().point
            else -> 0
        }
        Text(text = "$currentScore")
        Text(text = "${factionInfo.yourScore}")
    }
}

//2x1
@Composable
fun FactionThin(factionInfo: FactionInfo.Available) {
    AppWidgetColumn(
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            text = xpMap[factionInfo.yourXpId] ?: "???",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        val currentScore = when (factionInfo.yourXpId) {
            1 -> factionInfo.currentData.factions.faction1.data.last().point
            2 -> factionInfo.currentData.factions.faction2.data.last().point
            3 -> factionInfo.currentData.factions.faction3.data.last().point
            4 -> factionInfo.currentData.factions.faction4.data.last().point
            5 -> factionInfo.currentData.factions.faction5.data.last().point
            6 -> factionInfo.currentData.factions.faction6.data.last().point
            else -> 0
        }
        Text(text = "当前：$currentScore")
        Text(text = "你的：${factionInfo.yourScore}")
    }
}

//3x1
@Composable
fun FactionSmallBanner(factionInfo: FactionInfo.Available) {
    AppWidgetColumn {
        Text(
            modifier = GlanceModifier.fillMaxWidth(),
            text = xpMap[factionInfo.yourXpId] ?: "???",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {

            val currentScore = when (factionInfo.yourXpId) {
                1 -> factionInfo.currentData.factions.faction1.data.last().point
                2 -> factionInfo.currentData.factions.faction2.data.last().point
                3 -> factionInfo.currentData.factions.faction3.data.last().point
                4 -> factionInfo.currentData.factions.faction4.data.last().point
                5 -> factionInfo.currentData.factions.faction5.data.last().point
                6 -> factionInfo.currentData.factions.faction6.data.last().point
                else -> 0
            }
            Column {
                Text(text = "实物线")
                Text(text = "$currentScore")
            }
            Spacer(
                modifier = GlanceModifier.width(24.dp)
            )
            Column {
                Text(text = "你的分数")
                Text(text = "${factionInfo.yourScore}")
            }
        }
    }
}

//4x1
@Composable
fun FactionLargeBanner(factionInfo: FactionInfo.Available) {
    AppWidgetColumn {
        Text(
            modifier = GlanceModifier.fillMaxWidth(),
            text = "超日常夏日之旅",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FactionLargeBannerItem(factionInfo.currentData.factions.faction1)
            FactionLargeBannerItem(factionInfo.currentData.factions.faction2)
            FactionLargeBannerItem(factionInfo.currentData.factions.faction3)
            FactionLargeBannerItem(factionInfo.currentData.factions.faction4)
            FactionLargeBannerItem(factionInfo.currentData.factions.faction5)
            FactionLargeBannerItem(factionInfo.currentData.factions.faction6)
        }
    }
}

@Composable
fun FactionLargeBannerItem(
    factionItem: FactionData.Factions.FactionItem
) {
    Column(
        modifier = GlanceModifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(text = factionItem.factionName)
        Text(text = "${factionItem.data.last().point}")
        Text(text = "+${
            factionItem.data.last().point -
                    runCatching { factionItem.data[factionItem.data.size - 144].point }
                        .getOrDefault(0)
        }")
    }
}

//4x2+
@Composable
fun FactionLarge(factionInfo: FactionInfo.Available) {

    //val list = remember { mutableStateListOf<FactionData.Factions.FactionItem>() }
    //var maxIncrease by remember { mutableStateOf(0) }

    val list = mutableListOf<FactionData.Factions.FactionItem>()
        .apply {
            add(factionInfo.currentData.factions.faction1)
            add(factionInfo.currentData.factions.faction2)
            add(factionInfo.currentData.factions.faction3)
            add(factionInfo.currentData.factions.faction4)
            add(factionInfo.currentData.factions.faction5)
            add(factionInfo.currentData.factions.faction6)
            sortByDescending {
                it.data.last().point -
                        runCatching { it.data[it.data.size - 144].point }.getOrDefault(0)
            }
        }
    val maxIncrease =
        list.first().data.last().point -
                runCatching { list.first().data[list.first().data.size - 144].point }.getOrDefault(0)

    AppWidgetColumn(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(4.dp),
    ) {
        Text(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            text = "超日常夏日之旅 - 看看大佬 24h 卷了多少",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                list.forEach {
                    FactionLargeItem(factionItem = it, maxIncrease = maxIncrease)
                }
            }
        }
    }
}

@Composable
fun FactionLargeItem(
    factionItem: FactionData.Factions.FactionItem,
    maxIncrease: Int
) {
    val add = factionItem.data.last().point -
            runCatching { factionItem.data[factionItem.data.size - 144].point }.getOrDefault(0)
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = GlanceModifier.width(70.dp),
            text = factionItem.factionName
        )
        Box(
            modifier = GlanceModifier
                .width(((add.toFloat() / maxIncrease) * 200).dp)
                .height(8.dp)
                .cornerRadius(4.dp)
                .background(GlanceTheme.colors.primary)
        ) {}
        Text(
            modifier = GlanceModifier.padding(start = 4.dp),
            text = "+${
                factionItem.data.last().point -
                        runCatching { factionItem.data[factionItem.data.size - 144].point }
                            .getOrDefault(0)
            }"
        )
    }
}