package rtbo.ardourremote

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import rtbo.ardourremote.database.ArdourRemoteDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ArdourRemoteModule {
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(app, ArdourRemoteDatabase::class.java, "ardour_remote").build()

    @Singleton
    @Provides
    fun provideConnectionDao(
        db: ArdourRemoteDatabase,
    ) = db.connectionDao()
}
