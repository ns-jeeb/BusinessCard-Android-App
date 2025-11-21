package dev.najeeb.businesscard.cardwallet.screens

import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object ListRoute
@Serializable object CreateCardRoute
@Serializable data class EditCardRoute(val cardId: Int)
@Serializable object ScannerRoute
@Serializable data class DetailRoute(val cardId: Int)
@Serializable object BrochureRoute
@Serializable object ServiceRout