package com.polotika.tsfsocialmediaintegration

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.polotika.tsfsocialmediaintegration.databinding.ActivityLoginBinding
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.log


class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"

    private var loader: ProgressDialog? = null
    private lateinit var binding: ActivityLoginBinding


    private val loginViewModel: LoginViewModel by viewModels()

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            loginViewModel.onGoogleLauncherResult(it,this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.vm = loginViewModel
        observers()

        binding.goLoginBtn.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        loginViewModel.signInWithGoogleClient(this){
            googleSignInLauncher.launch(it)

        }
    }

    private fun observers() {
        lifecycleScope.launchWhenResumed {

            loginViewModel.loginFlow.collectLatest {
                when (it) {
                    true -> Snackbar.make(binding.root, "Logged in", Snackbar.LENGTH_SHORT).show()
                    false -> Snackbar.make(binding.root, "Failed", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            loginViewModel.loaderFlow.collectLatest {
                when (it) {
                    true -> showLoader()
                    false -> hideLoader()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            loginViewModel.uiFlow.collectLatest { loginFlow ->
                when (loginFlow){
                    is LoginScreenFlow.Success ->{
                        startActivity(Intent(this@LoginActivity,HomeActivity::class.java))
                    }
                    is LoginScreenFlow.Failed ->{
                        Snackbar.make(binding.root, loginFlow.message,Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoader() {
        loader = ProgressDialog(this)
        loader?.setTitle("Loading...")
        loader?.create()
        loader?.show()

    }

    private fun hideLoader() {
        loader?.hide()
    }

    override fun onStop() {
        loginViewModel.logout()
        super.onStop()

    }


}