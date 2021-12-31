package rtbo.ardourremote.database

import androidx.room.*
import java.util.*

@Entity(tableName = "connection")
data class Connection(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "host") val host: String,
    @ColumnInfo(name = "send_port") val sendPort: Int = 3819,
    @ColumnInfo(name = "rcv_port") val rcvPort: Int = 8000,
    @ColumnInfo(name = "last_used") val lastUsed: Date = Date(),
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
) {
    val humanDesc
        get() = "${host}\u2191${sendPort}\u2193${rcvPort}"
}

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connection")
    suspend fun getAll(): List<Connection>

    @Query("SELECT * FROM connection WHERE id = :id")
    suspend fun getById(id: Long): Connection?

    @Query("UPDATE connection SET last_used = :lastUsed WHERE id = :id")
    suspend fun updateLastUsedById(id: Long, lastUsed: Date)

    @Insert
    suspend fun insert(connection: Connection): Long

    @Query("DELETE FROM connection WHERE id = :id")
    suspend fun deleteById(id: Long)
}
