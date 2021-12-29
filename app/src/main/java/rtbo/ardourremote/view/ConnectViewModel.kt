package rtbo.ardourremote.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rtbo.ardourremote.repository.ConnectionRepo
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel @Inject constructor(private val repo: ConnectionRepo) : ViewModel() {
    private val _data = MutableLiveData<List<ConnectionItemViewModel>>()

    val data: LiveData<List<ConnectionItemViewModel>>
        get() = _data

    init {
        loadData()
    }

    val hasConnections: Boolean
        get() = data.value?.isNotEmpty() ?: false

    private fun loadData() {
        viewModelScope.launch {
            val connections = repo.getAll().map {
                ConnectionItemViewModel(
                    if (it.name.isNotEmpty()) { it.name } else { it.host },
                    "${it.host}\u2191${it.sendPort}\u2193${it.rcvPort}",
                    lastUsedString(it.lastUsed),
                )
            }
            _data.postValue(connections)
        }
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
}

data class ConnectionItemViewModel(
    val name: String,
    val humanDesc: String,
    val lastUsed: String,
)
