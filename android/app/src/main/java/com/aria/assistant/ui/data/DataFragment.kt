package com.aria.assistant.ui.data

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aria.assistant.R
import com.aria.assistant.services.DataCollectionService
import com.aria.assistant.utils.LocationUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataFragment : Fragment() {
    
    // Permission callback
    private val permissionCallback = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            // All permissions granted
            updatePermissionStatus()
            checkStartDataCollection()
        } else {
            // Some permissions denied
            updatePermissionStatus()
        }
    }
    
    private val viewModel: DataViewModel by viewModels()
    
    private lateinit var switchDataCollection: Switch
    private lateinit var btnRequestPermissions: Button
    private lateinit var btnPrivacyPolicy: Button
    private lateinit var tvPermissionStatus: TextView
    private lateinit var tvRewards: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data, container, false)
        
        // Initialize ViewModel
        viewModel.dataCollectionEnabled.observe(viewLifecycleOwner) { enabled ->
            updateCollectionStatus(enabled)
        }
        
        // Initialize permission status
        tvPermissionStatus = view.findViewById(R.id.tv_permission_status)
        updatePermissionStatus()
        
        // Set up data collection switch
        switchDataCollection = view.findViewById(R.id.switch_data_collection)
        switchDataCollection.setOnCheckedChangeListener { _, isChecked ->
            
            // When turning on data collection, check and request required permissions
            if (isChecked && !allPermissionsGranted()) {
                requestPermissions()
            }
            // Start data collection service
            if (isChecked) {
                startDataCollectionService()
            } else {
                // When turning off data collection, stop the service
                stopDataCollectionService()
            }
            viewModel.setDataCollectionEnabled(isChecked)
        }
        
        // Set up permission request button click event
        btnRequestPermissions = view.findViewById(R.id.btn_request_permissions)
        btnRequestPermissions.setOnClickListener {
            requestPermissions()
        }
        
        // Set up privacy policy button click event
        btnPrivacyPolicy = view.findViewById(R.id.btn_privacy_policy)
        btnPrivacyPolicy.setOnClickListener {
            // Show privacy policy
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aria.ai/privacy-policy"))
            startActivity(intent)
        }
        
        // Observe data collection status
        viewModel.dataCollectionEnabled.observe(viewLifecycleOwner) { enabled ->
            switchDataCollection.isChecked = enabled
            
            // Update UI status
            if (enabled) {
                tvPermissionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                if (allPermissionsGranted()) {
                    tvPermissionStatus.setText(R.string.data_collection_enabled)
                } else {
                    tvPermissionStatus.setText(R.string.permissions_needed)
                }
            } else {
                tvPermissionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
                tvPermissionStatus.setText(R.string.data_collection_disabled)
            }
        }
        
        // Observe total rewards
        tvRewards = view.findViewById(R.id.tv_rewards)
        viewModel.totalRewards.observe(viewLifecycleOwner) { rewards ->
            tvRewards.text = String.format("%.2f ARI", rewards)
        }
        
        return view
    }
    
    /**
     * Update permission status text
     */
    private fun updatePermissionStatus() {
        // Update permission status text
        if (allPermissionsGranted()) {
            btnRequestPermissions.visibility = View.GONE
            if (viewModel.dataCollectionEnabled.value == true) {
                tvPermissionStatus.setText(R.string.data_collection_enabled)
                tvPermissionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            } else {
                tvPermissionStatus.setText(R.string.data_collection_disabled)
                tvPermissionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            }
        } else {
            // If permissions needed but not granted, show request button
            if (viewModel.dataCollectionEnabled.value == true) {
                btnRequestPermissions.visibility = View.VISIBLE
                tvPermissionStatus.setText(R.string.permissions_needed)
                tvPermissionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow))
            } else {
                btnRequestPermissions.visibility = View.GONE
                tvPermissionStatus.setText(R.string.data_collection_disabled)
                tvPermissionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            }
        }
    }
    
    /**
     * Check if all necessary permissions are granted
     */
    private fun allPermissionsGranted(): Boolean {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALENDAR
        )
        
        return requiredPermissions.all {
            LocationUtils.hasPermission(requireContext(), it)
        }
    }
    
    /**
     * Request necessary permissions
     */
    private fun requestPermissions() {
        permissionCallback.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_CALENDAR
            )
        )
    }
    
    /**
     * Start data collection service
     */
    private fun startDataCollectionService() {
        if (allPermissionsGranted()) {
            val intent = Intent(requireContext(), DataCollectionService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        }
    }
    
    /**
     * Stop data collection service
     */
    private fun stopDataCollectionService() {
        val intent = Intent(requireContext(), DataCollectionService::class.java)
        requireContext().stopService(intent)
    }
    
    /**
     * Update collection status UI
     */
    private fun updateCollectionStatus(enabled: Boolean) {
        switchDataCollection.isChecked = enabled
        checkStartDataCollection()
    }
    
    /**
     * Check and start data collection if enabled and permissions granted
     */
    private fun checkStartDataCollection() {
        if (viewModel.dataCollectionEnabled.value == true && allPermissionsGranted()) {
            startDataCollectionService()
        }
    }
} 