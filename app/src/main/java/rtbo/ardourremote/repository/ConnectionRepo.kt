package rtbo.ardourremote.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rtbo.ardourremote.database.Connection
import rtbo.ardourremote.database.ConnectionDao
import javax.inject.Inject

class ConnectionRepo @Inject constructor(private val dao: ConnectionDao) {
    suspend fun getAll() = withContext(Dispatchers.IO) {
        dao.getAll()
    }

    suspend fun insert(connection: Connection) = withContext(Dispatchers.IO) {
        dao.insert(connection)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }
}
