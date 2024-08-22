package com.mnvpatni.teamsync.auth

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mnvpatni.teamsync.R
import com.mnvpatni.teamsync.admin.AdminDashboard
import com.mnvpatni.teamsync.databinding.ActivitySignInBinding
import com.mnvpatni.teamsync.network.RetrofitInstance
import com.mnvpatni.teamsync.sharedPrefs.AuthSharedPref
import com.mnvpatni.teamsync.volunteer.VolunteerDashboard
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var authSharedPref: AuthSharedPref
    private lateinit var pd: ProgressDialog
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Auth
        authSharedPref = AuthSharedPref(this)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        pd = ProgressDialog(this).apply {
            setMessage("Please wait..")
            setCancelable(false)
        }

        if (!authSharedPref.isSignedIn()) {
            // First time login, show login buttons
            binding.llButtons.visibility = View.VISIBLE
            binding.llWaitingForApproval.visibility = View.GONE
        } else {
            // If user is signed in, always check with the server to verify status
            binding.llButtons.visibility = View.GONE
            binding.llWaitingForApproval.visibility = View.VISIBLE
        }

        //buttons
        binding.btnGoogleAdmin.setOnClickListener {
            authSharedPref.setUserType("Admin")
            signIn()
        }

        binding.btnGoogleVolunteer.setOnClickListener {
            authSharedPref.setUserType("Volunteer")
            signIn()
        }

        binding.btnCheckStatus.setOnClickListener {
            verifyMember(auth.currentUser!!.uid)
        }

    }

    private fun signIn() {
        if (authSharedPref.isSignedIn()) {
            // User is already signed in, verify immediately
            showWelcomeMessage()
            verifyMember(auth.uid!!)
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
                pd.dismissIfShowing()
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            task.result?.let { updateUI(it) } ?: pd.dismissIfShowing()
        } else {
            showToastError(task.exception.toString())
            pd.dismissIfShowing()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                authSharedPref.setAuthStatus(true)
                verifyMember(auth.uid!!)
            } else {
                showToastError(it.exception.toString())
            }
            pd.dismissIfShowing()
        }
    }

    private fun saveAuthStatus() {
        authSharedPref.setAuthStatus(true)
        authSharedPref.setUID(auth.currentUser!!.uid)
    }

    private fun showWelcomeMessage() {
        Toast.makeText(this, "Welcome ${auth.currentUser?.displayName!!} 👋👋!!", Toast.LENGTH_SHORT)
            .show()
    }

    private fun verifyMember(uid: String) {
        pd.setMessage("Verifying account...")
        pd.show()

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.verifyCommitteeMember(
                    uid = uid,
                    name = auth.currentUser?.displayName.toString(),
                    userType = authSharedPref.userType().toString()
                )

                if (response.status == 1) {  // Status 1 means approved
                    authSharedPref.setUserPost(response.post)
                    authSharedPref.setAccessTo(response.access_to)
                    saveAuthStatus()
                    navigateToDashboard()
                } else {
                    // Status 0 means awaiting approval, stay on the waiting screen
                    binding.llButtons.visibility = View.GONE
                    binding.llWaitingForApproval.visibility = View.VISIBLE
                    Toast.makeText(this@SignInActivity, "Please wait for approval.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d("Api error", e.message.toString())
                Toast.makeText(this@SignInActivity, "An unexpected error occurred!!", Toast.LENGTH_SHORT).show()
            } finally {
                pd.dismissIfShowing()
            }
        }
    }

    private fun navigateToDashboard() {
        val targetActivity = when (authSharedPref.userType()) {
            "Admin" -> AdminDashboard::class.java
            else -> VolunteerDashboard::class.java
        }
        exit(targetActivity)
    }

    private fun exit(activityClass: Class<out Activity>) {
        startActivity(Intent(this@SignInActivity, activityClass))
        finishAffinity()
    }

    private fun ProgressDialog.dismissIfShowing() {
        if (isShowing) dismiss()
    }

    private fun showToastError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

