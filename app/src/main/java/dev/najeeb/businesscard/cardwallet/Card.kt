package dev.najeeb.businesscard.cardwallet

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "businessCard")
data class BusinessCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Unique ID for each card
    val name: String,
    val title: String,
    val phone: String,
    val email: String,
    val website: String,
    val address: String,
    val profilePictureUri: String? = null,
    val isFavorite: Boolean = false
)
@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: BusinessCard)

    @Delete
    fun deleteCards(card: BusinessCard)

    @Query("SELECT * FROM businesscard ORDER BY name ASC")
    fun getAllCards(): Flow<List<BusinessCard>>
}

@Database(entities = [BusinessCard::class], version = 2) // <-- 1. Increment version
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        // 2. Define the migration
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // This is where you write the SQL to alter the table
                db.execSQL("ALTER TABLE businessCard ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile
        private var INSTANCE: CardDatabase? = null

        fun getDatabase(context: Context): CardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CardDatabase::class.java,
                    "card_database"
                )
                    .addMigrations(MIGRATION_1_2) // <-- 3. Add the migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}