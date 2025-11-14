package dev.najeeb.businesscard.cardwallet

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData

class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val cardDao: CardDao
    private val userRepository: UserRepository // Add this

    val allCards: LiveData<List<BusinessCard>> // This will now ONLY be collected cards
    val userCard = mutableStateOf<BusinessCard?>(null) // State for the user's own card

    init {

        val database = CardDatabase.getDatabase(application)
        cardDao = database.cardDao()
        userRepository = UserRepository(application) // Initialize it

        // Get all cards EXCEPT the user's own (which we assume has id=1 if it were in the DB)
        allCards = cardDao.getAllCards().asLiveData()// This is now correct as it only fetches from the DB

        // Load the user's card from SharedPreferences
        loadUserCard()
    }

    private fun loadUserCard() {
        viewModelScope.launch {
            userCard.value = userRepository.getUserCard()
        }
    }

    // This function now ONLY saves to SharedPreferences
    fun saveOrUpdateUserCard(card: BusinessCard) {
        viewModelScope.launch {
            userRepository.saveUserCard(card)
            // Reload the state after saving
            userCard.value = card
        }
    }
    fun insert(card: BusinessCard){
        viewModelScope.launch(Dispatchers.IO) {
            cardDao.insertCard(card)
        }
    }

    /**
     * This function is for saving another person's card (e.g., from a QR scan)
     * into the Room database. It is not used for the user's own card.
     */
    fun collectNewCard(card: BusinessCard) {
        viewModelScope.launch(Dispatchers.IO) {
            // We must ensure the ID is 0 so Room can auto-generate a new one,
            // preventing any conflict with the original card's ID from another user's device.
            cardDao.insertCard(card.copy(id = 0))
        }
    }

    // You would also have functions here for deleting collected cards, etc.
    fun deleteCollectedCard(card: BusinessCard) {
        viewModelScope.launch(Dispatchers.IO) {
            cardDao.deleteCards(card)
        }
    }
}
class CardViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}