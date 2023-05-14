package rtbo.ardourremote.ui.remote

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import rtbo.ardourremote.R
import rtbo.ardourremote.ui.theme.ArdourRemoteTheme
import rtbo.ardourremote.ui.theme.blueOff
import rtbo.ardourremote.ui.theme.blueOn
import rtbo.ardourremote.ui.theme.greenOff
import rtbo.ardourremote.ui.theme.greenOn
import rtbo.ardourremote.ui.theme.redOff
import rtbo.ardourremote.ui.theme.redOn
import rtbo.ardourremote.view.RecordBtnStyle
import rtbo.ardourremote.view.RemoteViewModel

const val REMOTE_CONN_ID_KEY = "REMOTE_CONN_ID"
const val TAG = "REMOTE UI"

@AndroidEntryPoint
class RemoteActivity : ComponentActivity() {

    private val viewModel: RemoteViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.extras?.getLong(REMOTE_CONN_ID_KEY)!!
        viewModel.setConnectionId(id)

        viewModel.sessionName.observe(this) {
            title = if (it.isNotEmpty()) "Ardour Remote - $it" else "Ardour Remote"
        }

        // WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            RemoteScreen(viewModel, widthSizeClass)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.connect()
    }

    override fun onStop() {
        viewModel.disconnect()
        super.onStop()
    }
}

data class RemoteState(
    val connectionDesc: String,
    val timecode: String,
    val bbt: String,
    val speed: String,
    val heartbeat: Boolean,
    val recordBtnStyle: RecordBtnStyle,
    val stopped: Boolean,
    val playing: Boolean,
    val stopTrashEnabled: Boolean,

    val toStart: () -> Unit,
    val toEnd: () -> Unit,
    val jumpBars: (bars: Int) -> Unit,
    val recordToggle: () -> Unit,
    val stop: () -> Unit,
    val play: () -> Unit,
    val stopTrash: () -> Unit,
)

@Composable
fun RemoteScreen(viewModel: RemoteViewModel, widthSizeClass: WindowWidthSizeClass) {
    val connectionDesc by viewModel.connectionDesc.observeAsState("")
    val timecode by viewModel.timecode.observeAsState("")
    val bbt by viewModel.bbt.observeAsState("")
    val speed by viewModel.speedTxt.observeAsState("")
    val heartbeat by viewModel.heartbeat.observeAsState(false)
    val recordBtnStyle by viewModel.recordBtnStyle.observeAsState(RecordBtnStyle.OFF)
    val playing by viewModel.playing.observeAsState(false)
    val stopped by viewModel.playing.observeAsState(false)
    val stopTrashEnabled by viewModel.stopTrashEnabled.observeAsState(false)

    val state = RemoteState(
        connectionDesc,
        timecode,
        bbt,
        speed,
        heartbeat,
        recordBtnStyle,
        playing,
        stopped,
        stopTrashEnabled,
        toStart = { viewModel.toStart() },
        toEnd = { viewModel.toEnd() },
        jumpBars = { bars -> viewModel.jumpBars(bars) },
        recordToggle = { viewModel.recordToggle() },
        stop = { viewModel.stop() },
        play = { viewModel.play() },
        stopTrash = { viewModel.stopTrash() },
    )

    RemoteContent(state, widthSizeClass)
}

@Preview(
    name = "Screen (phone portrait - light)",
    widthDp = 360,
    heightDp = 722,
)
@Preview(
    name = "Screen (phone portrait - dark)",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 360,
    heightDp = 722,
)
@Composable
fun RemotePreview() {
    val state = RemoteState(
        connectionDesc = "192.168.1.64\u21913819\u21938000",
        timecode = "00:00:21:29",
        bbt = "007|03|0734",
        speed = "x1.5",
        heartbeat = false,
        recordBtnStyle = RecordBtnStyle.BLINK,
        playing = false,
        stopped = true,
        stopTrashEnabled = true,

        toStart = {},
        toEnd = {},
        jumpBars = {},
        recordToggle = {},
        stop = {},
        play = {},
        stopTrash = {},
    )
    val widthSizeClass = WindowWidthSizeClass.Compact

    RemoteContent(state, widthSizeClass)
}

@Composable
fun RemoteContent(state: RemoteState, widthSizeClass: WindowWidthSizeClass) {
//    val config = LocalConfiguration.current
//    val width = config.screenWidthDp
//    val height = config.screenHeightDp
//    Log.d(TAG, "screen $width x $height")
    ArdourRemoteTheme {
        Surface {
            Column {
                TransportTimeRow(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                JumpButtonsRow(
                    state = state, modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                RecordingButtonsRow(
                    state = state, modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(16.dp))
                ConnectionRow(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                )
            }
        }
    }
}

@Composable
fun TransportTimeRow(state: RemoteState, modifier: Modifier) {
    val col = MaterialTheme.colorScheme.greenOn
    val family = FontFamily.Monospace
    val size = 18.sp
    Box(modifier) {
        Row(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            val space = Modifier.width(10.dp)
            Text(
                state.timecode,
                color = col,
                fontFamily = family,
                fontSize = size,
            )
            Spacer(modifier = space)
            Text(
                state.bbt,
                color = col,
                fontFamily = family,
                fontSize = size,
            )

        }
        if (state.speed.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // bbt and timecode are always the same dp width
                    .absoluteOffset(148.dp),
            ) {
                Text(
                    state.speed,
                    color = col,
                    fontFamily = family,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
fun JumpButtonsRow(
    state: RemoteState,
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
) {
    Row(modifier, horizontalArrangement, verticalAlignment) {

        val sz = 64.dp
        val iconSz = 32.dp
        val colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
        )
        val space = 8.dp

        FilledIconButton(modifier = Modifier.size(sz),
            colors = colors,
            onClick = { state.toStart() }) {
            Icon(
                modifier = Modifier.size(iconSz),
                painter = painterResource(id = R.drawable.ic_baseline_double_bar_arrow_left),
                contentDescription = "to start"
            )
        }
        Spacer(Modifier.size(space))
        FilledIconButton(modifier = Modifier.size(sz),
            colors = colors,
            onClick = { state.jumpBars(-1) }) {
            Icon(
                modifier = Modifier.size(iconSz),
                painter = painterResource(id = R.drawable.ic_baseline_bar_arrow_left),
                contentDescription = "previous bar"
            )
        }
        Spacer(Modifier.size(space))
        FilledIconButton(modifier = Modifier.size(sz),
            colors = colors,
            onClick = { state.jumpBars(+1) }) {
            Icon(
                modifier = Modifier.size(iconSz),
                painter = painterResource(id = R.drawable.ic_baseline_bar_arrow_right),
                contentDescription = "next bar"
            )
        }
        Spacer(Modifier.size(space))
        FilledIconButton(modifier = Modifier.size(sz),
            colors = colors,
            onClick = { state.toEnd() }) {
            Icon(
                modifier = Modifier.size(iconSz),
                painter = painterResource(id = R.drawable.ic_baseline_double_bar_arrow_right),
                contentDescription = "to end"
            )
        }
    }
}

@Composable
fun RecordingButtonsRow(
    state: RemoteState,
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
) {
    Row(modifier, horizontalArrangement, verticalAlignment) {
        val sz = 64.dp
        val iconSz = 32.dp
        val colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
        )
        val space = 8.dp

        FilledIconButton(modifier = Modifier.size(sz),
            colors = colors,
            onClick = { state.recordToggle() }) {
            Icon(
                modifier = Modifier.size(iconSz),
                painter = painterResource(id = R.drawable.ic_baseline_record_24),
                contentDescription = "record",
                tint = recordIconColor(state.recordBtnStyle),
            )
        }
        Spacer(Modifier.size(space))
        FilledIconButton(modifier = Modifier.size(sz),
            colors = colors,
            onClick = { state.stop() }) {
            Icon(
                modifier = Modifier.size(iconSz),
                painter = painterResource(id = R.drawable.ic_baseline_stop_24),
                contentDescription = "stop",
                tint = stopIconColor(stopped = state.stopped),
            )
        }
        Spacer(Modifier.size(space))
        FilledIconButton(modifier = Modifier.size(sz),
            colors = colors,
            onClick = { state.play() }) {
            Icon(
                modifier = Modifier.size(iconSz),
                painter = painterResource(id = R.drawable.ic_baseline_play_24),
                contentDescription = "play",
                tint = playIconColor(playing = state.playing),
            )
        }
        Spacer(Modifier.size(space))
        FilledIconButton(modifier = Modifier.size(sz),
            colors = colors,
            enabled = state.stopTrashEnabled,
            onClick = { state.stopTrash() }) {
            Icon(
                modifier = Modifier.size(iconSz),
                painter = painterResource(id = R.drawable.ic_baseline_stop_trash_24),
                contentDescription = "stop and trash",
                tint = MaterialTheme.colorScheme.redOn.copy(
                    alpha = LocalContentColor.current.alpha
                ),
            )
        }
    }
}

@Composable
fun recordIconColor(recordBtnStyle: RecordBtnStyle): Color {
    return when (recordBtnStyle) {
        RecordBtnStyle.BLINK -> {
            val blinkAnimation = rememberInfiniteTransition()
            val blinkColor by blinkAnimation.animateColor(
                initialValue = MaterialTheme.colorScheme.redOff,
                targetValue = MaterialTheme.colorScheme.redOn,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        easing = FastOutSlowInEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
            )
            blinkColor
        }

        RecordBtnStyle.OFF -> MaterialTheme.colorScheme.redOff
        RecordBtnStyle.SOLID -> MaterialTheme.colorScheme.redOn
    }
}

@Composable
fun stopIconColor(stopped: Boolean): Color {
    return when (stopped) {
        false -> MaterialTheme.colorScheme.blueOff
        true -> MaterialTheme.colorScheme.blueOn
    }
}

@Composable
fun playIconColor(playing: Boolean): Color {
    return when (playing) {
        false -> MaterialTheme.colorScheme.greenOn
        true -> MaterialTheme.colorScheme.greenOff
    }
}

@Composable
fun ConnectionRow(
    state: RemoteState,
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
) {
    Row(modifier, horizontalArrangement, verticalAlignment = Alignment.CenterVertically) {
        val color: Color by animateColorAsState(
            when (state.heartbeat) {
                true -> MaterialTheme.colorScheme.blueOn
                false -> MaterialTheme.colorScheme.blueOff
            }
        )
        Icon(
            modifier = Modifier.size(12.dp),
            painter = painterResource(id = R.drawable.ic_baseline_record_24),
            contentDescription = "heartbeat icon",
            tint = color,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            state.connectionDesc,
            color = MaterialTheme.colorScheme.greenOn,
            fontFamily = FontFamily.Monospace,
        )
    }
}
