package rtbo.ardourremote.osc

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

const val FEEDBACK_STRIP_BUTTONS = 1
const val FEEDBACK_STRIP_CONTROLS = 2
const val FEEDBACK_PATH_SSID = 4
const val FEEDBACK_HEARTBEAT = 8
const val FEEDBACK_MASTER_SECTION = 16
const val FEEDBACK_PLAYHEAD_BBT = 32
const val FEEDBACK_PLAYHEAD_SMPTE = 64
const val FEEDBACK_METERING_FLOAT = 128
const val FEEDBACK_METERING_LED_STRIP = 256
const val FEEDBACK_SIGNAL_PRESENT = 512
const val FEEDBACK_PLAYHEAD_SAMPLES = 1024
const val FEEDBACK_PLAYHEAD_TIME = 2048
const val FEEDBACK_PLAYHEAD_GUI = 4096
const val FEEDBACK_EXTRA_SELECT_FEEDBACK = 8192
const val FEEDBACK_LEGACY_REPLY = 16384

class OscRemote(private val scope: CoroutineScope) {
    private var _socket: OscSocket? = null
    private var _receiveJob: Job? = null
    private var _receiveChannel: ReceiveChannel<OscMessage>? = null
    private val _connected = MutableLiveData(false)
    private val _sessionName = MutableLiveData("")
    private val _heartbeat = MutableLiveData<Boolean>(false)
    private val _playing = MutableLiveData(false)
    private val _stopped = MutableLiveData(false)
    private val _speed = MutableLiveData(0.0f)
    private val _recordEnabled = MutableLiveData(false)
    private val _bbt = MutableLiveData<String>()
    private val _timecode = MutableLiveData<String>()

    val connected: LiveData<Boolean> = _connected
    val sessionName: LiveData<String> = _sessionName
    val heartbeat: LiveData<Boolean> = _heartbeat
    val playing: LiveData<Boolean> = _playing
    val stopped: LiveData<Boolean> = _stopped
    val speed: LiveData<Float> = _speed
    val recordEnabled: LiveData<Boolean> = _recordEnabled
    val bbt: LiveData<String> = _bbt
    val timecode: LiveData<String> = _timecode

    suspend fun connect(params: OscSocketParams) {
        if (_socket != null) return
        val socket = OscSocketUDP(params)
        _socket = socket

        try {

            val channel = Channel<OscMessage>(16)
            _receiveChannel = channel

            _receiveJob = scope.launch(Dispatchers.IO) {
                receiveProduceLoop(socket, channel)
            }

            delay(500)

            sendInitialStateQuery()
            _connected.postValue(true)
            Log.d("OSC", "Connected")

            withContext(Dispatchers.Main) {
                receiveConsumeLoop(channel)
            }
        } finally {
            socket.close()
            _socket = null
            _connected.postValue(false)
            Log.d("OSC", "Disconnected")
        }
    }

    suspend fun disconnect() {
        Log.d("OSC", "Disconnecting...")
        _receiveJob?.cancelAndJoin()
        _receiveChannel?.cancel()
        _receiveJob = null
        _receiveChannel = null
    }

    suspend fun transportToStart() {
        sendMessage(OscMessage("/goto_start"))
    }

    suspend fun transportToEnd() {
        sendMessage(OscMessage("/goto_end"))
    }

    suspend fun transportJumpBars(bars: Int) {
        sendMessage(OscMessage("/jump_bars", OscInt(bars)))
    }

    suspend fun transportPlay() {
        sendMessage(OscMessage("/transport_play"))
    }

    suspend fun transportStop() {
        sendMessage(OscMessage("/transport_stop"))
    }

    suspend fun stopAndForget() {
        sendMessage(OscMessage("/stop_forget"))
    }

    suspend fun recordToggle() {
        sendMessage(OscMessage("/rec_enable_toggle"))
    }

    private suspend fun sendInitialStateQuery() {
        val feedback =
            FEEDBACK_MASTER_SECTION or
                    FEEDBACK_HEARTBEAT or
                    FEEDBACK_PLAYHEAD_BBT or
                    FEEDBACK_PLAYHEAD_TIME
        sendMessage(OscMessage("/set_surface/feedback", OscInt(feedback)))
        sendMessage(OscMessage("/set_surface/port", OscInt(_socket!!.params.rcvPort)))
    }

    private suspend fun sendMessage(msg: OscMessage) {
        Log.d("OSC", "Sending msg $msg")
        withContext(Dispatchers.IO) {
            _socket!!.sendMessage(msg)
        }
    }

    private suspend fun receiveConsumeLoop(channel: ReceiveChannel<OscMessage>) {
        for (msg in channel) {
            dispatchRcvMessage(msg)
        }
    }

    private suspend fun receiveProduceLoop(socket: OscSocket, channel: SendChannel<OscMessage>) {
        try {
            while (true) {
                yield()
                val msg = socket.receiveMessage()
                if (msg != null) {
                    channel.send(msg)
                }
            }
        } catch (ex: CancellationException) {
        }
    }

    private fun dispatchRcvMessage(msg: OscMessage) {
        Log.d("OSC", "Received msg $msg")
        when (msg.address.value) {
            "/heartbeat" -> _heartbeat.postValue(msg.args[0].float!! > 0.5f)
            "/position/bbt" -> _bbt.postValue(msg.args[0].string)
            "/position/time" -> _timecode.postValue(msg.args[0].string)
            "/transport_play" -> _playing.postValue(msg.args[0].int == 1)
            "/transport_stop" -> _stopped.postValue(msg.args[0].int == 1)
            "/transport_speed" -> {
                val speed = msg.args[0].float
                _speed.postValue(speed)
                if (speed != null) {
                    _stopped.postValue(speed == 0.0f)
                    _playing.postValue(speed == 1.0f)
                }
            }

            "/rec_enable_toggle" -> _recordEnabled.postValue(msg.args[0].int != 0)
            "/session_name" -> _sessionName.postValue(msg.args[0].string)
        }
    }
}