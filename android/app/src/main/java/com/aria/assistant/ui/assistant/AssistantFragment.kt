package com.aria.assistant.ui.assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aria.assistant.R
import com.aria.assistant.data.entities.AssistantMessage
import com.aria.assistant.databinding.FragmentAssistantBinding

class AssistantFragment : Fragment() {

    private var _binding: FragmentAssistantBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AssistantViewModel
    private lateinit var adapter: AssistantAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(AssistantViewModel::class.java)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup send button click event
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageInput.setText("")
            }
        }
        
        // Observe message list changes
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.recyclerView.smoothScrollToPosition(messages.size - 1)
            }
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.sendButton.isEnabled = !isLoading
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
        
        // Add greeting message if this is a new conversation
        if (viewModel.isNewConversation) {
            viewModel.addMessage(
                AssistantMessage(
                    content = getString(R.string.assistant_greeting),
                    isFromUser = false,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
    
    private fun setupRecyclerView() {
        adapter = AssistantAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
    }
    
    private fun sendMessage(message: String) {
        viewModel.sendMessage(message)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 