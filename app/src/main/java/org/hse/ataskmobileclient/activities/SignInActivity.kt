package org.hse.ataskmobileclient.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import org.hse.ataskmobileclient.R


class SignInActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val loginButton = findViewById<SignInButton>(R.id.login_button)
        loginButton.setOnClickListener {
            googleSingIn()
        }
    }

    private fun googleSingIn(){
        val task = googleSignInClient.silentSignIn()
        if (task.isSuccessful){
            val signInAccount = task.result!!
            loginWithAccount(signInAccount)
        }
        else {
            task.addOnCompleteListener { task ->
                try {
                    val signInAccount = task.getResult(ApiException::class.java)!!
                    loginWithAccount(signInAccount)
                } catch (apiException: ApiException) {
                    Log.e(TAG, apiException.message ?: apiException.statusCode.toString())

                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
            }
        }
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
        Log.i(TAG, "Singed in with Google!")

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.FULL_NAME_EXTRA, "${account.givenName} ${account.familyName}")
            putExtra(MainActivity.PHOTO_URL_EXTRA, account.photoUrl.toString())
        }
        startActivity(intent)
    }

    companion object {
        const val RC_SIGN_IN = 777
        const val TAG = "SignInActivity"
    }
}