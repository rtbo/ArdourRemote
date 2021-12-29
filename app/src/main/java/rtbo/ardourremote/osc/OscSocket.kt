package rtbo.ardourremote.osc

data class OscSocketParams(
    val address: String,
    val sendPort: Int,
    val rcvPort: Int,
)

class OscSocket {
}