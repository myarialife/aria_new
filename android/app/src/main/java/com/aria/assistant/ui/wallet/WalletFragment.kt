package com.aria.assistant.ui.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aria.assistant.R
import com.aria.assistant.databinding.FragmentWalletBinding
import kotlinx.coroutines.launch

class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: WalletViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(WalletViewModel::class.java)
        
        setupUI()
        setupObservers()
        
        // Load wallet status
        viewModel.checkWalletStatus()
    }
    
    private fun setupUI() {
        // Setup create wallet button click event
        binding.createWalletButton.setOnClickListener {
            if (!viewModel.hasWallet()) {
                createWallet()
            } else {
                Toast.makeText(context, "Wallet already exists", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Setup import wallet button click event
        binding.importWalletButton.setOnClickListener {
            // In a real application, this should open a dialog for the user to input the mnemonic
            // Simplified version just shows a toast
            Toast.makeText(context, "Please complete wallet import in settings", Toast.LENGTH_SHORT).show()
        }
        
        // Setup send tokens button click event
        binding.sendTokensButton.setOnClickListener {
            // In a real application, this should open a dialog for the user to input recipient address and amount
            Toast.makeText(context, "Send tokens feature coming soon", Toast.LENGTH_SHORT).show()
        }
        
        // Setup receive tokens button click event
        binding.receiveTokensButton.setOnClickListener {
            // In a real application, this should display a QR code of the current wallet address
            val address = viewModel.getWalletAddress()
            if (address != null) {
                Toast.makeText(context, "Your wallet address: $address", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Please create a wallet first", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupObservers() {
        // Observe wallet status
        viewModel.walletStatus.observe(viewLifecycleOwner) { hasWallet ->
            updateWalletUI(hasWallet)
        }
        
        // Observe wallet balance
        viewModel.walletBalance.observe(viewLifecycleOwner) { balance ->
            binding.balanceTextView.text = "$balance ARI"
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateWalletUI(hasWallet: Boolean) {
        if (hasWallet) {
            // Show wallet info area
            binding.walletInfoLayout.visibility = View.VISIBLE
            binding.walletSetupLayout.visibility = View.GONE
            
            // Refresh wallet balance
            lifecycleScope.launch {
                viewModel.refreshBalance()
            }
            
            // Display wallet address
            val address = viewModel.getWalletAddress()
            binding.addressTextView.text = address?.let { 
                val formatted = if (it.length > 20) {
                    it.substring(0, 10) + "..." + it.substring(it.length - 10)
                } else {
                    it
                }
                "Address: $formatted"
            } ?: "Address: Unknown"
        } else {
            // Show wallet setup area
            binding.walletInfoLayout.visibility = View.GONE
            binding.walletSetupLayout.visibility = View.VISIBLE
        }
    }
    
    private fun createWallet() {
        lifecycleScope.launch {
            try {
                val mnemonic = viewModel.createWallet()
                
                // In a real application, this should display the mnemonic and remind the user to back it up
                // Simplified version just shows a toast
                Toast.makeText(context, "Wallet created successfully! Please keep your mnemonic safe", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to create wallet: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 