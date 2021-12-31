package rtbo.ardourremote.osc

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext

class OscConnection(params: OscSocketParams) {
    private val socket: OscSocket = OscSocketUDP(params)
    private var receiveChannel: ReceiveChannel<OscMessage>? = null
    private val _connected = MutableLiveData<Boolean>(false)
    private var _inConnectLoop = false

    val connected: LiveData<Boolean> = _connected

    suspend fun connect() {
        if (_inConnectLoop) return
        _inConnectLoop = true
        withContext(Dispatchers.Main) {
            try {
                _connected.postValue(true)
                Log.d("OSC", "Connected")
                val channel = Channel<OscMessage>(16)
                receiveChannel = channel
                socket.receiveMessages(channel)
                for (msg in channel) {
                    dispatchRcvMessage(msg)
                }
            } finally {
                _inConnectLoop = false
                _connected.postValue(false)
                Log.d("OSC", "Disconnected")
            }
        }
    }

    fun disconnect() {
        Log.d("OSC", "Disconnecting...")
        receiveChannel?.cancel()
        receiveChannel = null
    }

    private suspend fun sendMessage(msg: OscMessage) {
        Log.d("OSC", "Sending msg $msg")
        socket.sendMessage(msg)
    }

    private fun dispatchRcvMessage(msg: OscMessage) {
        Log.d("OSC", "Received msg $msg")
    }


}