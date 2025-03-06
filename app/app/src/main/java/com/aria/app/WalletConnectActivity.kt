package com.aria.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Activity for connecting to Solana wallets
 * This is a separate activity to handle wallet connection flow
 */
class WalletConnectActivity : ComponentActivity() {

    private lateinit var solanaService: SolanaService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Solana service
        solanaService = SolanaService()
        solanaService.initializeWalletAdapter(this)
        
        setContent {
            MaterialTheme {
                WalletConnectScreen(
                    onConnected = { finish() },
                    onBack = { finish() },
                    solanaService = solanaService
                )
            }
        }
    }
}

/**
 * Wallet connect screen composable
 */
@Composable
fun WalletConnectScreen(
    onConnected: () -> Unit,
    onBack: () -> Unit,
    solanaService: SolanaService
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.connect_wallet)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.connect_wallet_description),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    solanaService.connectWallet()
                                    Toast.makeText(context, "Wallet connected successfully", Toast.LENGTH_SHORT).show()
                                    onConnected()
                                } catch (e: Exception) {
                                    isLoading = false
                                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text(stringResource(R.string.connect_wallet))
                    }
                }
            }
        }
    }
} 