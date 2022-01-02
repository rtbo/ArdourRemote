package rtbo.ardourremote.osc

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
    abstract fun sendMessage(msg: OscMessage)
    abstract fun receiveMessage(): OscMessage?
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

    private val rcvBuf: ByteArray by lazy {
        ByteArray(MAX_MSG_SIZE)
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

    override fun sendMessage(msg: OscMessage) {
        val arr = oscMessageToPacket(msg)
        sendSocket.send(DatagramPacket(arr, arr.size, hostAddress, params.sendPort))
    }

    override fun receiveMessage(): OscMessage? {
        val buf = rcvBuf
        val pkt = DatagramPacket(buf, buf.size)
        return try {
            rcvSocket.receive(pkt)
            oscPacketToMessage(buf, 0, pkt.length)
        } catch (ex: SocketTimeoutException) {
            null
        }
    }
}

private const val MAX_MSG_SIZE = 4096
