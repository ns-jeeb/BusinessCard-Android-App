package dev.najeeb.businesscard.cardwallet.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.najeeb.businesscard.cardwallet.CardDatabase
import dev.najeeb.businesscard.cardwallet.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideCardDatabase(@ApplicationContext context: Context): CardDatabase {
        return Room.databaseBuilder(
            context = context,
            CardDatabase::class.java,
            "card_database").fallbackToDestructiveMigration(true).build()
    }
    @Provides
    @Singleton
    fun provideCardDao(cardDatabase: CardDatabase) = cardDatabase.cardDao()

}