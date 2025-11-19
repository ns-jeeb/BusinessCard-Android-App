package dev.najeeb.businesscard.cardwallet

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class CardViewModel @Inject constructor(
    private val repository: UserRepository,
    private val cardDao: CardDao ) : ViewModel() {

    val allCards = cardDao.getAllCards().asLiveData()
    val userCard = mutableStateOf<BusinessCard?>(null)

    init {
        loadUserCard()
    }

    private fun loadUserCard() {
        viewModelScope.launch {
            userCard.value = repository.getUserCard()
        }
    }
    fun saveOrUpdateUserCard(card: BusinessCard) {
        viewModelScope.launch {
            repository.saveUserCard(card)
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

    fun deleteCollectedCard(card: BusinessCard) {
        viewModelScope.launch(Dispatchers.IO) {
            cardDao.deleteCards(card)
        }
    }
}