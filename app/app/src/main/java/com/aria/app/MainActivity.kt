package com.aria.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solana.core.PublicKey
import kotlinx.coroutines.launch

/**
 * Main Activity for the ARIA app
 */
class MainActivity : ComponentActivity() {

    private lateinit var solanaService: SolanaService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Solana service
        solanaService = SolanaService()
        solanaService.initializeWalletAdapter(this)
        
        setContent {
            MaterialTheme {
                MainScreen(solanaService)
            }
        }
    }
}

/**
 * Main screen composable
 */
@Composable
fun MainScreen(solanaService: SolanaService) {
    val navController = rememberNavController()
    
    // Wallet connection state
    var isWalletConnected by remember { mutableStateOf(false) }
    var walletPublicKey by remember { mutableStateOf<PublicKey?>(null) }
    
    // Bottom navigation state
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    if (isWalletConnected) {
                        // Disconnect wallet button
                        IconButton(onClick = {
                            solanaService.disconnectWallet()
                            isWalletConnected = false
                            walletPublicKey = null
                        }) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = stringResource(R.string.disconnect_wallet))
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Wallet") },
                    label = { Text("Wallet") },
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0
                        navController.navigate("wallet") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Chat, contentDescription = "Chat") },
                    label = { Text("Chat") },
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        navController.navigate("chat") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "wallet",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("wallet") {
                WalletScreen(
                    solanaService = solanaService,
                    isWalletConnected = isWalletConnected,
                    walletPublicKey = walletPublicKey,
                    onWalletConnected = { publicKey ->
                        isWalletConnected = true
                        walletPublicKey = publicKey
                    }
                )
            }
            
            composable("chat") {
                ChatScreen()
            }
            
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

/**
 * Wallet screen composable
 */
@Composable
fun WalletScreen(
    solanaService: SolanaService,
    isWalletConnected: Boolean,
    walletPublicKey: PublicKey?,
    onWalletConnected: (PublicKey) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    
    var ariBalance by remember { mutableStateOf(0.0) }
    var solBalance by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf<List<AriaTransaction>>(emptyList()) }
    
    LaunchedEffect(isWalletConnected) {
        if (isWalletConnected && walletPublicKey != null) {
            isLoading = true
            try {
                ariBalance = solanaService.getAriTokenBalance(walletPublicKey)
                solBalance = solanaService.getSolBalance(walletPublicKey)
                transactions = solanaService.getTransactionHistory()
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isWalletConnected) {
            // Wallet connection UI
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.connect_wallet_description),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val publicKey = solanaService.connectWallet()
                                onWalletConnected(publicKey)
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.connect_wallet))
                }
            }
        } else {
            // Wallet content UI
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Wallet address
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.wallet_address),
                                style = MaterialTheme.typography.subtitle1
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = walletPublicKey?.toBase58()?.take(16) + "..." + walletPublicKey?.toBase58()?.takeLast(4),
                                    modifier = Modifier.weight(1f)
                                )
                                
                                IconButton(
                                    onClick = {
                                        walletPublicKey?.toBase58()?.let { address ->
                                            clipboardManager.setText(AnnotatedString(address))
                                            Toast.makeText(context, context.getString(R.string.address_copied), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.copy_address))
                                }
                            }
                        }
                    }
                    
                    // Token balances
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.token_balance),
                                style = MaterialTheme.typography.subtitle1
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.ari_token),
                                        style = MaterialTheme.typography.body2
                                    )
                                    
                                    Text(
                                        text = "$ariBalance ARI",
                                        style = MaterialTheme.typography.h5
                                    )
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "SOL",
                                        style = MaterialTheme.typography.body2
                                    )
                                    
                                    Text(
                                        text = "$solBalance SOL",
                                        style = MaterialTheme.typography.h5
                                    )
                                }
                            }
                        }
                    }
                    
                    // Recent transactions
                    Text(
                        text = stringResource(R.string.transactions),
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_transactions),
                                color = Color.Gray
                            )
                        }
                    } else {
                        Column {
                            transactions.forEach { transaction ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = transaction.type,
                                                style = MaterialTheme.typography.subtitle2
                                            )
                                            
                                            Text(
                                                text = transaction.timestamp.take(10),
                                                style = MaterialTheme.typography.caption
                                            )
                                        }
                                        
                                        Text(
                                            text = "${if (transaction.amount > 0) "+" else ""}${transaction.amount} ARI",
                                            color = if (transaction.amount > 0) Color(0xFF4CAF50) else Color(0xFFE91E63)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chat screen composable (simplified for MVP)
 */
@Composable
fun ChatScreen() {
    val messages = remember { mutableStateListOf(
        Message("ARIA", stringResource(R.string.welcome_message))
    ) }
    
    var newMessage by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat messages
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                messages.forEach { message ->
                    MessageBubble(message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        
        // Message input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.type_message)) }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = {
                    if (newMessage.isNotEmpty()) {
                        messages.add(Message("User", newMessage))
                        
                        // Simulate AI response for MVP
                        messages.add(Message("ARIA", "I'm here to help with your AI and crypto needs! This is a simplified MVP demonstration."))
                        
                        newMessage = ""
                    }
                }
            ) {
                Icon(Icons.Filled.Send, contentDescription = stringResource(R.string.send))
            }
        }
    }
}

/**
 * Message bubble composable
 */
@Composable
fun MessageBubble(message: Message) {
    val isFromUser = message.sender == "User"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            backgroundColor = if (isFromUser) Color(0xFF2196F3) else Color(0xFFE0E0E0)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.caption,
                    color = if (isFromUser) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                )
                
                Text(
                    text = message.content,
                    color = if (isFromUser) Color.White else Color.Black
                )
            }
        }
    }
}

/**
 * Settings screen composable
 */
@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // About section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.about),
                    style = MaterialTheme.typography.subtitle1
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "ARIA is a decentralized AI personal assistant built on the Solana blockchain. This MVP demonstrates the token functionality and wallet integration.",
                    style = MaterialTheme.typography.body2
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.version, "0.1.0"),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

/**
 * Data class representing a chat message
 */
data class Message(
    val sender: String,
    val content: String
) 