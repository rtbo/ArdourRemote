package rtbo.ardourremote.osc

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext

class OscRemote {
    private var _socket: OscSocket? = null
    private var _receiveChannel: ReceiveChannel<OscMessage>? = null
    private val _connected = MutableLiveData<Boolean>(false)

    val connected: LiveData<Boolean> = _connected

    suspend fun connect(params: OscSocketParams) {
        if (_socket != null) return
        val socket = OscSocketUDP(params)
        _socket = socket

        withContext(Dispatchers.Main) {
            try {
                _connected.postValue(true)
                Log.d("OSC", "Connected")

                sendInitialStateQuery()

                receiveLoop(socket)
            } finally {
                _socket = null
                _connected.postValue(false)
                Log.d("OSC", "Disconnected")
            }
        }
    }

    fun disconnect() {
        Log.d("OSC", "Disconnecting...")
        _receiveChannel?.cancel()
        _receiveChannel = null
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
        sendMessage(OscMessage("/transport_frame"))
        sendMessage(OscMessage("/transport_speed"))
        sendMessage(OscMessage("/record_enabled"))
    }

    private suspend fun sendMessage(msg: OscMessage) {
        Log.d("OSC", "Sending msg $msg")
        _socket!!.sendMessage(msg)
    }

    private suspend fun receiveLoop(socket: OscSocket) {
        val channel = Channel<OscMessage>(16)
        _receiveChannel = channel
        socket.receiveMessages(channel)
        for (msg in channel) {
            dispatchRcvMessage(msg)
        }
    }

    private fun dispatchRcvMessage(msg: OscMessage) {
        Log.d("OSC", "Received msg $msg")
    }


}