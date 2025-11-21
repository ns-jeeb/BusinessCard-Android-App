package dev.najeeb.businesscard.cardwallet
import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserRepository @Inject constructor(cardDao: CardDao, @ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val PREF_USER_CARD = "user_card_json"

    // 2. Function to save the user's card
    fun saveUserCard(card: BusinessCard) {
        val cardJson = gson.toJson(card) // Convert the BusinessCard object to a JSON string
        prefs.edit().putString(PREF_USER_CARD, cardJson)?.apply()
    }

    // 3. Function to load the user's card
    fun getUserCard(): BusinessCard? {
        val cardJson = prefs?.getString(PREF_USER_CARD, null)
        return if (cardJson != null) {
            gson.fromJson(cardJson, BusinessCard::class.java) // Convert JSON string back to a BusinessCard object
        } else {
            null
        }
    }
}