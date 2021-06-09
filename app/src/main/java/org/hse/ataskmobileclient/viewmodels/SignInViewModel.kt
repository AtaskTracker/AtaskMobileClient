package org.hse.ataskmobileclient.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.launch
import org.hse.ataskmobileclient.SingleLiveEvent
import org.hse.ataskmobileclient.apis.AuthApi

class SignInViewModel : ViewModel() {

    val onAuthorizedOnBackendEvent : SingleLiveEvent<GoogleSignInAccount?> = SingleLiveEvent()

    fun authWithGoogleOnBackend(account: GoogleSignInAccount) {
        viewModelScope.launch {
            val idToken = account.idToken
            if (idToken == null || idToken.isEmpty())
                return@launch

            val authorizedOnBackend = AuthApi().authWithGoogle(idToken)

            if (authorizedOnBackend) {
                onAuthorizedOnBackendEvent.call(account)
            }
            else {
                onAuthorizedOnBackendEvent.call(null)
            }
        }
    }
}