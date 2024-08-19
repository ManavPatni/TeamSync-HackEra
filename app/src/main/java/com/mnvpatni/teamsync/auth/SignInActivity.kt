package com.mnvpatni.teamsync.auth

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.databinding.ActivitySignInBinding
import com.mnvpatni.teamsync.sharedPrefs.AuthSharedPref

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    private lateinit var authSharedPref: AuthSharedPref
    private lateinit var pd: ProgressDialog

    //login
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //auth shared pref
        authSharedPref = AuthSharedPref(this)

        // Initialize ProgressDialog here
        pd = ProgressDialog(this).apply {
            setMessage("Please wait..")
            setCancelable(false)
        }

        //login
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        /*Sign In Btn*/
        binding.btnGoogleAdmin.setOnClickListener {
            signIn()
        }

        binding.btnGoogleVolunteer.setOnClickListener{
            signIn()
        }

    }

    private fun signIn() {
        if (authSharedPref.isSignedIn()) {
            Snackbar.make(
                binding.main,
                "Welcome ${authSharedPref.userName()!!} 👋👋!!",
                Snackbar.LENGTH_SHORT
            ).show()
            //startActivity(Intent(this, MainActivity::class.java))
            exit()
        } else {
            pd.show()
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            } else {
                pd.dismissIfShowing() // Ensure pd is dismissed if sign-in was cancelled
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            } else {
                pd.dismissIfShowing()
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
            pd.dismissIfShowing()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                // Save onboarding status and login type in SharedPreferences
                authSharedPref.setAuthStatus(true)
                authSharedPref.setUserName(auth.currentUser?.displayName!!)
                authSharedPref.setUID(auth.currentUser!!.uid)
                Snackbar.make(
                    binding.main,
                    "Welcome ${authSharedPref.userName()!!} 👋👋!!",
                    Snackbar.LENGTH_SHORT
                ).show()
                exit()
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
            pd.dismissIfShowing()
        }
    }

    private fun ProgressDialog.dismissIfShowing() {
        if (isShowing) {
            dismiss()
        }
    }

    private fun exit() {
        //val intent = Intent(this, MainActivity::class.java)
        //startActivity(intent)
        finishAffinity()
    }
}
