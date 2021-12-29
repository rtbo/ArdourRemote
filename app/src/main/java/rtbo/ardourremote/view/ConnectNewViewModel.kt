package rtbo.ardourremote.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rtbo.ardourremote.database.Connection
import rtbo.ardourremote.repository.ConnectionRepo
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ConnectNewViewModel @Inject constructor(private val repo: ConnectionRepo): ViewModel() {

    val name = MutableLiveData<String>("")
    val host = MutableLiveData<String>("192.168.1.1")
    val sendPort = MutableLiveData<Int>(3819)
    val rcvPort= MutableLiveData<Int>(8000)

    private val _connected = MutableLiveData<Boolean>(false)
    val connected: LiveData<Boolean> = _connected

    fun connect() {
        viewModelScope.launch {
            val connection = Connection(name.value!!, host.value!!, sendPort.value!!, rcvPort.value!!)
            val newId = repo.insert(connection)
            if (newId != 0L) {
                _connected.postValue(true)
            }
        }
    }

}