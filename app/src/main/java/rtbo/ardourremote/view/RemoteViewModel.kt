package rtbo.ardourremote.view

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rtbo.ardourremote.database.Connection
import rtbo.ardourremote.osc.OscRemote
import rtbo.ardourremote.osc.OscSocketParams
import rtbo.ardourremote.repository.ConnectionRepo
import javax.inject.Inject

enum class RecordBtnStyle {
    OFF,
    BLINK,
    SOLID,
}

@HiltViewModel
class RemoteViewModel @Inject constructor(private val repo: ConnectionRepo) : ViewModel() {
    companion object {
        private const val TAG = "REMOTE"
    }

    private val remote = OscRemote(viewModelScope)

    private val _connection = MutableLiveData<Connection>()
    private var _connectionRequested = false
    private var _connId = -1L

    val connectionDesc: LiveData<String> = _connection.map {
        it.humanDesc
    }

    //    val connected = remote.connected
    val playing = remote.playing
    val stopped = remote.stopped

    //    val speed = remote.speed
//    val recordEnabled = remote.recordEnabled
    val timecode = remote.timecode
    val bbt = remote.bbt

    val speedTxt: LiveData<String> = remote.speed.map {
        Log.d(TAG, "speed update $it")
        if (it != 0.0f && it != 1.0f) {
            "x $it"
        } else {
            ""
        }
    }

    val recordBtnStyle = MediatorLiveData<RecordBtnStyle>().apply {
        fun update() {
            Log.d(TAG, "RecordBtnStyle update")
            val play = remote.playing.value ?: return
            val record = remote.recordEnabled.value ?: return

            value = if (!record) {
                RecordBtnStyle.OFF
            } else {
                if (play) {
                    RecordBtnStyle.SOLID
                } else {
                    RecordBtnStyle.BLINK
                }
            }
        }

        addSource(remote.playing) { update() }
        addSource(remote.recordEnabled) { update() }

        value = RecordBtnStyle.OFF
    }

    val stopTrashEnabled = MediatorLiveData<Boolean>().apply {
        fun update() {
            Log.d(TAG, "Stop trash update")
            val play = remote.playing.value ?: return
            val record = remote.recordEnabled.value ?: return

            value = play && record
        }
        addSource(remote.playing) { update() }
        addSource(remote.recordEnabled) { update() }
        value = false
    }

    fun setConnectionId(id: Long) {
        if (id == _connId)
            return
        viewModelScope.launch {
            val conn = repo.getById(id)!!
            _connection.postValue(conn)
            _connId = conn.id

            if (_connectionRequested) {
                _connectionRequested = false
                doConnect(conn)
            }
        }
    }

    fun connect() {
        viewModelScope.launch {
            val conn = _connection.value
            if (conn != null) {
                doConnect(conn)
            } else {
                // even if setConnectionId() is called before connect(), it can be raced out
                // because fetching connection from repo is long.
                // So we need a deferred connection mechanism.
                _connectionRequested = true
            }
        }
    }

    private suspend fun doConnect(conn: Connection) {
        Log.d(TAG, "Connection to ${conn.humanDesc}")
        val params = OscSocketParams(conn.host, conn.sendPort, conn.rcvPort)
        remote.connect(params)
        repo.updateLastUsedById(conn.id)
    }

    fun disconnect() {
        viewModelScope.launch {
            remote.disconnect()
            repo.updateLastUsedById(_connId)
        }
    }

    fun play() {
        viewModelScope.launch {
            remote.transportPlay()
        }
    }

    fun stop() {
        viewModelScope.launch {
            remote.transportStop()
        }
    }

    fun recordToggle() {
        viewModelScope.launch {
            remote.recordToggle()
        }
    }

    fun stopTrash() {
        viewModelScope.launch {
            remote.stopAndForget()
        }
    }

}