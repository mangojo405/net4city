package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.netcity.data.NetCityRepository
import com.example.netcity.data.authStateFlow
import com.example.netcity.ui.AuthScreen
import com.example.netcity.ui.NetCityShell
import com.example.netcity.viewmodel.NetCityViewModel
import com.example.ui.theme.NetCityTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetCityTheme {
                NetCityRoot()
            }
        }
    }
}

@Composable
fun NetCityRoot() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            val viewModel: NetCityViewModel = viewModel(
                key = user.uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        NetCityViewModel(
                            repository = NetCityRepository(db, auth),
                            currentUserId = user.uid,
                            currentUserDisplayName = user.displayName ?: user.email ?: "Pengguna NET4CITY"
                        )
                    }
                }
            )
            NetCityShell(
                viewModel = viewModel,
                onSignedOut = {}
            )
        }
    }
}
