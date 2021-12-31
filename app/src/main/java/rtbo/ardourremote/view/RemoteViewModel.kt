package rtbo.ardourremote.view

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rtbo.ardourremote.database.Connection
import rtbo.ardourremote.osc.OscConnection
import rtbo.ardourremote.osc.OscSocketParams
import rtbo.ardourremote.repository.ConnectionRepo
import javax.inject.Inject

@HiltViewModel
class RemoteViewModel @Inject constructor(private val repo: ConnectionRepo) : ViewModel() {
    companion object {
        private const val TAG = "REMOTE"
    }

    private val _connection = MutableLiveData<Connection>()
    private val _connected = MediatorLiveData<Boolean>()
    private var _oscConnection: OscConnection? = null
    private var _connectionRequested = false
    private var _connId = -1L

    val humanDesc: LiveData<String> = _connection.map {
        it.humanDesc
    }
    val connected: LiveData<Boolean> = _connected

    fun connect() {
        viewModelScope.launch {
            val oscConn = _oscConnection
            if (oscConn != null) {
                Log.d(TAG, "Connection to ${humanDesc.value}")
                oscConn.connect()
            } else {
                // even if setConnectionId() is called before connect(), it can be raced out
                // because fetching connection from repo is long.
                // So we need a deferred connection mechanism.
                _connectionRequested = true
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _oscConnection?.disconnect()
        }
    }

    fun setConnectionId(id: Long) {
        if (id == _connId)
            return
        _connId = id
        viewModelScope.launch {
            val conn = repo.getById(id)!!
            _connection.postValue(conn)

            val oldOscConn = _oscConnection
            if (oldOscConn != null) {
                _connected.removeSource(oldOscConn.connected)
                oldOscConn.disconnect()
            }

            val params = OscSocketParams(conn.host, conn.sendPort, conn.rcvPort)
            val oscConn = OscConnection(params)
            _oscConnection = oscConn
            _connected.addSource(oscConn.connected) {
                _connected.value = it
            }

            if (_connectionRequested) {
                Log.d(TAG, "Connection to ${conn.humanDesc}")
                oscConn.connect()
                _connectionRequested = false
            }
        }
    }

}