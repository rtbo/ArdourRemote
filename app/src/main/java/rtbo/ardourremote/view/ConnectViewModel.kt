package rtbo.ardourremote.view

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rtbo.ardourremote.database.Connection
import rtbo.ardourremote.repository.ConnectionRepo
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel @Inject constructor(private val repo: ConnectionRepo) : ViewModel(),
    ConnectionItemActions {

    private val _connections = MutableLiveData<MutableList<ConnectionItemViewModel>>()
    private val _newConn = MutableLiveData<Connection>()

    val connections: LiveData<List<ConnectionItemViewModel>> = _connections.map {
        it
    }

    val newName = MutableLiveData<String>("Laptop")
    val newHost = MutableLiveData<String>("192.168.1.64")
    val newSendPort = MutableLiveData<Int>(3819)
    val newRcvPort = MutableLiveData<Int>(8000)

    val newConn: LiveData<Connection> = _newConn

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        val connections = repo.getAll()
        val connectionModels = ArrayList<ConnectionItemViewModel>()
        for ((idx, c) in connections.withIndex()) {
            connectionModels.add(mapConnectionModel(idx, c))
        }
        _connections.postValue(connectionModels)
    }

    private fun mapConnectionModel(idx: Int, conn: Connection): ConnectionItemViewModel =
        ConnectionItemViewModel(
            idx,
            conn.id,
            conn.name.ifEmpty {
                conn.host
            },
            conn.humanDesc,
            lastUsedString(conn.lastUsed),
            this,
        )

    private val dateFormat: DateFormat by lazy {
        SimpleDateFormat("MMM d", Locale.getDefault())
    }

    private fun lastUsedString(lastUsed: Date?): String {
        return if (lastUsed == null) {
            "never\nused"
        } else {
            val date = dateFormat.format(lastUsed)
            "used\n${date}"
        }
    }

    fun connectNew() {
        val connection = Connection(
            newName.value ?: "",
            newHost.value!!,
            newSendPort.value!!,
            newRcvPort.value!!
        )
        viewModelScope.launch {
            val newId = repo.insert(connection)
            val newConn = repo.getById(newId)!!
            _newConn.postValue(newConn)
            val conns = _connections.value!!
            val connModel = mapConnectionModel(conns.size, newConn)
            conns.add(connModel)
            _connections.postValue(conns)
        }
    }

    override fun deleteItem(itemPos: Int) {
        viewModelScope.launch {
            val conns = _connections.value!!
            val conn = conns[itemPos]
            repo.deleteById(conn.id)
            conns.removeAt(itemPos)
            _connections.postValue(conns)
        }
    }
}

data class ConnectionItemViewModel(
    val pos: Int,
    val id: Long,
    val name: String,
    val humanDesc: String,
    val lastUsed: String,
    val actions: ConnectionItemActions,
)

interface ConnectionItemActions {
    fun deleteItem(itemPos: Int)
}
