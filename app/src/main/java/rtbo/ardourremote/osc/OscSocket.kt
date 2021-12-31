package rtbo.ardourremote.osc

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

data class OscSocketParams(
    val address: String,
    val sendPort: Int,
    val rcvPort: Int,
)

sealed class OscSocket(val params: OscSocketParams) {
    abstract fun close()
    abstract suspend fun sendMessage(msg: OscMessage)
    abstract suspend fun receiveMessages(channel: SendChannel<OscMessage>)
}

class OscSocketUDP(params: OscSocketParams) : OscSocket(params) {
    private val sendSocket: DatagramSocket by lazy {
        DatagramSocket()
    }

    private val rcvSocket: DatagramSocket by lazy {
        val sock = DatagramSocket(params.rcvPort)
        sock.reuseAddress = true
        sock.soTimeout = 1000
        sock
    }

    private val hostAddress: InetAddress by lazy {
        InetAddress.getByName(params.address)
    }

    override fun close() {
        sendSocket.disconnect()
        sendSocket.close()
        rcvSocket.disconnect()
        rcvSocket.close()
    }

    override suspend fun sendMessage(msg: OscMessage) {
        val arr = oscMessageToPacket(msg)

        withContext(Dispatchers.IO) {
            sendSocket.send(DatagramPacket(arr, arr.size, hostAddress, params.sendPort))
        }
    }

    override suspend fun receiveMessages(channel: SendChannel<OscMessage>) {
        withContext(Dispatchers.IO) {
            val buf = ByteArray(MAX_MSG_SIZE)
            while (true) {
                val pkt = DatagramPacket(buf, buf.size)
                try {
                    if (channel.isClosedForSend) {
                        break
                    }
                    rcvSocket.receive(pkt)
                    Log.d("SOCKET", "received ${pkt.length}")
                    val msg = oscPacketToMessage(buf, 0, pkt.length)
                    channel.send(msg)
                } catch (ex: SocketTimeoutException) {
                } catch (ex: CancellationException) {
                    break
                }
            }
        }
    }
}

private const val MAX_MSG_SIZE = 4096
