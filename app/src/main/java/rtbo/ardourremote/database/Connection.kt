package rtbo.ardourremote.database

import androidx.room.*
import java.util.*

@Entity
data class Connection(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "host") val host: String,
    @ColumnInfo(name = "send_port") val sendPort: Int = 3819,
    @ColumnInfo(name = "rcv_port") val rcvPort: Int = 8000,
    @ColumnInfo(name = "last_used") val lastUsed: Date = Date(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
) {
}

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connection")
    suspend fun getAll(): List<Connection>

    @Insert
    suspend fun insert(connection: Connection): Long
}
