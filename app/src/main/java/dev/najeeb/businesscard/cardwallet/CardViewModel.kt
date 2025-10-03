package dev.najeeb.businesscard.cardwallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CardViewModel(application: Application) : AndroidViewModel(application) {

    // Get a reference to the DAO from our database
    private val cardDao = CardDatabase.getDatabase(application).cardDao()

    // Get the list of all cards from the DAO. This is a Flow.
    val allCards: Flow<List<BusinessCard>> = cardDao.getAllCards()

    // Function to insert a new card. This will be called when the user saves.
    fun insertCard(card: BusinessCard) {
        // viewModelScope launches a coroutine to do the work off the main thread.
         viewModelScope.launch {
            cardDao.insertCard(card)
        }
    }
}
class CardViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    // This 'create' function is the one that gets called by the system.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel is our CardViewModel
        if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
            // If it is, create and return an instance of it, passing the application.
            @Suppress("UNCHECKED_CAST")
            return CardViewModel(application) as T
        }
        // If it's some other ViewModel, throw an error.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}