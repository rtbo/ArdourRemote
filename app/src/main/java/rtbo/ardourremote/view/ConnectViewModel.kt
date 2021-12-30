package rtbo.ardourremote.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val _connections = MutableLiveData<List<ConnectionItemViewModel>>()

    val newName = MutableLiveData<String>("Laptop")
    val newHost = MutableLiveData<String>("192.168.1.40")
    val newSendPort = MutableLiveData<Int>(3819)
    val newRcvPort = MutableLiveData<Int>(8000)


    val connections: LiveData<List<ConnectionItemViewModel>>
        get() = _connections

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        val connections = repo.getAll().mapIndexed { idx, it ->
            ConnectionItemViewModel(
                idx,
                it.id,
                if (it.name.isNotEmpty()) {
                    it.name
                } else {
                    it.host
                },
                "${it.host}\u2191${it.sendPort}\u2193${it.rcvPort}",
                lastUsedString(it.lastUsed),
                this,
            )
        }
        _connections.postValue(connections)
    }

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

    override fun deleteItem(itemPos: Int) {
        val conn = _connections.value?.get(itemPos)!!
        viewModelScope.launch {
            repo.deleteById(conn.id)
            loadData()
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
            repo.insert(connection)
            loadData()
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
