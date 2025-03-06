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
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize services
        solanaService = SolanaService()
        solanaService.initializeWalletAdapter(this)
        apiService = ApiService()
        
        setContent {
            MaterialTheme {
                MainScreen(solanaService, apiService)
            }
        }
    }
}

/**
 * Main screen composable
 */
@Composable
fun MainScreen(solanaService: SolanaService, apiService: ApiService) {
    val navController = rememberNavController()
    
    // Wallet connection state
    var isWalletConnected by remember { mutableStateOf(false) }
    var walletPublicKey by remember { mutableStateOf<PublicKey?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }
    
    // Bottom navigation state
    var selectedTab by remember { mutableStateOf(0) }
    
    // Handle wallet verification and token setting
    LaunchedEffect(isWalletConnected, walletPublicKey) {
        if (isWalletConnected && walletPublicKey != null && authToken == null) {
            try {
                val token = apiService.verifyWallet(walletPublicKey!!.toBase58())
                if (token != null) {
                    authToken = token
                    apiService.setAuthToken(token)
                }
            } catch (e: Exception) {
                // Handle error, but don't block the UI
            }
        }
    }
    
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
                            authToken = null
                            apiService.setAuthToken(null)
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
                ChatScreen(apiService, isWalletConnected)
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
 * Chat screen composable with API integration
 */
@Composable
fun ChatScreen(apiService: ApiService, isWalletConnected: Boolean) {
    val messages = remember { mutableStateListOf(
        Message("ARIA", stringResource(R.string.welcome_message))
    ) }
    
    var newMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val messagesEndRef = remember { mutableStateOf<Unit?>(null) }
    
    // Auto-scroll to bottom when messages change
    LaunchedEffect(messages.size) {
        messagesEndRef.value = Unit // Trigger recomposition of the referenced element
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat header with login prompt if not connected
        if (!isWalletConnected) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                backgroundColor = Color(0xFFF8F0E3)
            ) {
                Text(
                    text = "Connect your wallet for a personalized chat experience and to earn ARI tokens!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.body2
                )
            }
        }
        
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
                
                // Show typing indicator when loading
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 16.dp, bottom = 8.dp)
                    ) {
                        TypingIndicator()
                    }
                }
                
                // Reference for auto-scrolling
                Box(modifier = Modifier.onGloballyPositioned {
                    // This gets called after layout
                    messagesEndRef.value?.let {
                        it // Access to trigger recomposition
                    }
                })
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
                placeholder = { Text(stringResource(R.string.type_message)) },
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = {
                    if (newMessage.isNotEmpty()) {
                        // Add user message
                        val userMessage = Message("User", newMessage)
                        messages.add(userMessage)
                        
                        // Store message to send and clear input field
                        val messageToSend = newMessage
                        newMessage = ""
                        isLoading = true
                        
                        // Call API to get AI response
                        coroutineScope.launch {
                            try {
                                val response = apiService.sendChatMessage(messageToSend)
                                messages.add(Message("ARIA", response))
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                messages.add(Message("ARIA", "Sorry, I encountered an error. Please try again."))
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading && newMessage.isNotEmpty()
            ) {
                Icon(Icons.Filled.Send, contentDescription = stringResource(R.string.send))
            }
        }
    }
}

/**
 * Typing indicator animation for chat
 */
@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .background(
                color = Color(0xFFE0E0E0),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val dotSize = 8.dp
        val dotColor = Color.Gray
        
        // First dot with animation
        val firstDotAlpha by animateFloatAsState(
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1500
                    0.7f at 0
                    0.9f at 300
                    0.3f at 600
                }
            )
        )
        
        // Second dot with animation
        val secondDotAlpha by animateFloatAsState(
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1500
                    0.3f at 0
                    0.7f at 300
                    0.9f at 600
                    0.3f at 900
                }
            )
        )
        
        // Third dot with animation
        val thirdDotAlpha by animateFloatAsState(
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1500
                    0.3f at 0
                    0.3f at 300
                    0.7f at 600
                    0.9f at 900
                    0.3f at 1200
                }
            )
        )
        
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(dotColor.copy(alpha = firstDotAlpha), shape = CircleShape)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(dotColor.copy(alpha = secondDotAlpha), shape = CircleShape)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(dotColor.copy(alpha = thirdDotAlpha), shape = CircleShape)
        )
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
            backgroundColor = if (isFromUser) Color(0xFF2196F3) else Color(0xFFE0E0E0),
            shape = if (isFromUser) 
                MaterialTheme.shapes.medium.copy(
                    topEnd = ZeroCornerSize,
                    bottomStart = ZeroCornerSize,
                    bottomEnd = ZeroCornerSize
                )
            else 
                MaterialTheme.shapes.medium.copy(
                    topStart = ZeroCornerSize,
                    bottomStart = ZeroCornerSize,
                    bottomEnd = ZeroCornerSize
                )
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