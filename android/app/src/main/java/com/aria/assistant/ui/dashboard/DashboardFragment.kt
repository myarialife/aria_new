package com.aria.assistant.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aria.assistant.R
import com.aria.assistant.databinding.FragmentDashboardBinding
import com.aria.assistant.utils.formatAriaTokenAmount
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main dashboard Fragment
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
    }
    
    /**
     * Set up UI elements
     */
    private fun setupUI() {
        // Set wallet card click event
        binding.walletCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_walletFragment)
        }
        
        // Set data card click event
        binding.dataCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_dataFragment)
        }
        
        // Set assistant card click event
        binding.assistantCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_assistantFragment)
        }
        
        // Set community card click event
        binding.communityCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_communityFragment)
        }
        
        // Refresh button
        binding.refreshButton.setOnClickListener {
            viewModel.refreshDashboardData()
        }
    }
    
    /**
     * Set up observers
     */
    private fun setupObservers() {
        // Observe wallet status
        viewModel.walletStatus.observe(viewLifecycleOwner) { walletStatus ->
            binding.walletStatusText.text = walletStatus
            binding.walletStatusIcon.setImageResource(
                if (walletStatus == "Connected") R.drawable.ic_wallet_connected
                else R.drawable.ic_wallet_disconnected
            )
        }
        
        // Observe token balance
        viewModel.tokenBalance.observe(viewLifecycleOwner) { balance ->
            binding.tokenBalanceText.text = "$balance ARI"
        }
        
        // Observe data collection status
        viewModel.dataCollectionEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.dataCollectionStatusText.text = if (enabled) "Active" else "Inactive"
            binding.dataCollectionStatusIcon.setImageResource(
                if (enabled) R.drawable.ic_data_active
                else R.drawable.ic_data_inactive
            )
        }
        
        // Observe collected data count
        viewModel.dataPointsCollected.observe(viewLifecycleOwner) { count ->
            binding.dataPointsText.text = "$count"
        }
        
        // Observe assistant dialog count
        viewModel.assistantInteractions.observe(viewLifecycleOwner) { count ->
            binding.assistantInteractionsText.text = "$count"
        }
        
        // Observe total rewards
        viewModel.totalRewards.observe(viewLifecycleOwner) { rewards ->
            binding.totalRewardsText.text = "$rewards ARI"
        }
        
        // Observe user level
        viewModel.userLevel.observe(viewLifecycleOwner) { level ->
            binding.userLevelText.text = "Level $level"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 