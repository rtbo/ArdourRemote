package rtbo.ardourremote.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}

@Database(entities = [Connection::class], exportSchema = true, version = 1)
@TypeConverters(Converters::class)
abstract class ArdourRemoteDatabase: RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao
}
