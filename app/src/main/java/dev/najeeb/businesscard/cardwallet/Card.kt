package dev.najeeb.businesscard.cardwallet

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity
data class BusinessCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Unique ID for each card
    val name: String,
    val title: String,
    val phone: String,
    val email: String,
    val website: String,
    val address: String
)
@Dao
interface CardDao {
    // Command to insert a new card into the database
    @Insert
    suspend fun insertCard(card: BusinessCard)

    // Command to get all cards from the database, ordered by name.
    // Flow makes it automatically update the UI when the data changes.
    @Query("SELECT * FROM businesscard ORDER BY name ASC")
    fun getAllCards(): Flow<List<BusinessCard>>
}
@Database(entities = [BusinessCard::class], version = 1)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        // @Volatile ensures the instance is always up-to-date
        @Volatile
        private var INSTANCE: CardDatabase? = null

        fun getDatabase(context: Context): CardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CardDatabase::class.java,
                    "card_database" // Name of the database file
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}