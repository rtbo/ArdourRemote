package rtbo.ardourremote.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rtbo.ardourremote.database.Connection
import rtbo.ardourremote.database.ConnectionDao
import java.util.*
import javax.inject.Inject

class ConnectionRepo @Inject constructor(private val dao: ConnectionDao) {
    suspend fun getAll() = withContext(Dispatchers.IO) {
        dao.getAll()
    }

    suspend fun getById(id: Long) = withContext(Dispatchers.IO) {
        dao.getById(id)
    }

    suspend fun insert(connection: Connection) = withContext(Dispatchers.IO) {
        dao.insert(connection)
    }

    suspend fun updateLastUsedById(id: Long, lastUsed: Date = Date()) =
        withContext(Dispatchers.IO) {
            dao.updateLastUsedById(id, lastUsed)
        }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }
}
