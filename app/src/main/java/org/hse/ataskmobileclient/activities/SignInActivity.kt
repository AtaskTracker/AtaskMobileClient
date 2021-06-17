package org.hse.ataskmobileclient.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import org.hse.ataskmobileclient.R
import org.hse.ataskmobileclient.databinding.ActivitySignInBinding
import org.hse.ataskmobileclient.services.SessionManager
import org.hse.ataskmobileclient.viewmodels.SignInViewModel


class SignInActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    private val viewModel : SignInViewModel by lazy { ViewModelProvider(this).get(SignInViewModel::class.java) }
    private val binding : ActivitySignInBinding by lazy {
        val binding : ActivitySignInBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        binding.lifecycleOwner = this@SignInActivity
        binding.viewModel = viewModel
        binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.loginButton.setOnClickListener { googleSingIn() }

        trySilentSignIn(googleSignInClient)

        viewModel.onAuthorizedOnBackendEvent.observe(this, { account ->
            if (account == null){
                val errorMessage = getString(R.string.could_not_authorize_with_google_on_backend)
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                return@observe
            }

            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.FULL_NAME_EXTRA, "${account.givenName} ${account.familyName}")
                putExtra(MainActivity.PHOTO_URL_EXTRA, account.photoUrl.toString())
            }
            startActivity(intent)
        })
    }

    private fun trySilentSignIn(googleSignInClient: GoogleSignInClient) {
        val task = googleSignInClient.silentSignIn()
        val immediateResultAvailable = task.isSuccessful
        if (immediateResultAvailable) {
            handleSignInResult(task)
        }
        else {
            viewModel.isLoading.value = true
            task.addOnCompleteListener {
                viewModel.isLoading.value = false
                handleSignInResult(task)
                // Если не получилось авторизоваться, то мы останемся на этом экране
                // пользователю придется нажать на кнопку самостоятельно
            }
        }
    }

    private fun googleSingIn(){
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            loginWithAccount(account)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun loginWithAccount(account: GoogleSignInAccount) {
        val idToken = account.idToken
        if (idToken == null){
            Log.e(TAG, "Id token was null :O")
            return
        }

        Log.i(TAG, "Singed in with Google! $idToken")

        val sessionManager = SessionManager(this)
        sessionManager.saveAuthToken(idToken)

        viewModel.authWithGoogleOnBackend(account)
    }

    companion object {
        const val RC_SIGN_IN = 777
        const val TAG = "SignInActivity"
    }
}