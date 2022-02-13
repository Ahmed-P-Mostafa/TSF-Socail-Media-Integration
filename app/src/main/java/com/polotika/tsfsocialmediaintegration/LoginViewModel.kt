package com.polotika.tsfsocialmediaintegration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Message
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val TAG = "LoginViewModel"
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uieFlow = MutableSharedFlow<LoginScreenFlow>()
    val uiFlow = _uieFlow.asSharedFlow()

    fun signInWithGoogleClient(context: Context,onClientIntent : (intent: Intent)-> Unit){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val googleSignInClient :GoogleSignInClient = GoogleSignIn.getClient(context, gso)

        onClientIntent(googleSignInClient.signInIntent)
    }

    fun onGoogleLauncherResult(result:ActivityResult, activity: Activity){
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            viewModelScope.launch { _uieFlow.emit(LoginScreenFlow.Success(account)) }
            if (account.idToken != null) {
                firebaseAuthWithGoogle(account.idToken!!,activity)
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
            viewModelScope.launch { _uieFlow.emit(LoginScreenFlow.Failed("Connection Error!")) }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String,activity: Activity) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //updateUI(null)
                }
            }
    }

    fun logout()
    {
        auth.signOut()
    }

    private val loginChannel = Channel<Boolean>()
    val loginFlow = loginChannel.receiveAsFlow()
    val loaderFlow  = MutableSharedFlow<Boolean>()
    fun loginWithFacebook(){
        viewModelScope.launch {
            loaderFlow.emit(true)
            delay(2000)
            loginChannel.send(true)
            loaderFlow.emit(false)
        }
    }

    fun loginWithTwitter(){

    }

    fun loginWithGoogle(){

    }


}

sealed class LoginScreenFlow{
    data class Success(val account: GoogleSignInAccount):LoginScreenFlow()
    data class Failed(val message: String):LoginScreenFlow()
}