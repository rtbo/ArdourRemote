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

@HiltViewModel
class RemoteViewModel @Inject constructor(private val repo: ConnectionRepo) : ViewModel() {
    companion object {
        private const val TAG = "REMOTE"
    }

    private val remote = OscRemote()

    private val _connection = MutableLiveData<Connection>()
    private val _connected: LiveData<Boolean> = remote.connected
    private var _connectionRequested = false
    private var _connId = -1L

    val humanDesc: LiveData<String> = _connection.map {
        it.humanDesc
    }
    val connected: LiveData<Boolean> = _connected

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

    fun transportPlay() {
        viewModelScope.launch {
            remote.transportPlay()
        }
    }

    fun recordToggle() {
        viewModelScope.launch {
            remote.recordToggle()
        }
    }

}