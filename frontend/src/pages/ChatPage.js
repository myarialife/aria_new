import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';

const ChatPage = () => {
  const [messages, setMessages] = useState([
    { id: 1, sender: 'aria', content: 'Hello! I am ARIA, your AI personal assistant. How can I help you today?' }
  ]);
  const [newMessage, setNewMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);

  // Auto-scroll to the bottom when messages update
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    
    if (!newMessage.trim()) return;
    
    // Add user message to chat
    const userMessage = {
      id: messages.length + 1,
      sender: 'user',
      content: newMessage
    };
    
    setMessages(prevMessages => [...prevMessages, userMessage]);
    setNewMessage('');
    setIsLoading(true);
    
    try {
      // Call backend API for AI response
      // For MVP, we'll simulate a response after a delay
      setTimeout(() => {
        const ariaResponse = {
          id: messages.length + 2,
          sender: 'aria',
          content: getAIResponse(newMessage)
        };
        
        setMessages(prevMessages => [...prevMessages, ariaResponse]);
        setIsLoading(false);
      }, 1000);
      
      // Once backend is implemented, use this:
      /*
      const response = await axios.post('/api/chat', { message: newMessage });
      
      const ariaResponse = {
        id: messages.length + 2,
        sender: 'aria',
        content: response.data.message
      };
      
      setMessages(prevMessages => [...prevMessages, ariaResponse]);
      */
    } catch (error) {
      console.error('Error getting AI response:', error);
      
      // Add error message
      const errorMessage = {
        id: messages.length + 2,
        sender: 'aria',
        content: 'Sorry, I encountered an error. Please try again later.'
      };
      
      setMessages(prevMessages => [...prevMessages, errorMessage]);
      setIsLoading(false);
    }
  };
  
  // Simple AI response simulation for MVP
  const getAIResponse = (userMessage) => {
    const lowerCaseMessage = userMessage.toLowerCase();
    
    if (lowerCaseMessage.includes('hello') || lowerCaseMessage.includes('hi')) {
      return 'Hello there! How can I assist you today?';
    } else if (lowerCaseMessage.includes('name')) {
      return 'My name is ARIA, your AI personal assistant powered by Solana blockchain.';
    } else if (lowerCaseMessage.includes('token') || lowerCaseMessage.includes('ari')) {
      return 'ARI is our utility token built on Solana. You can earn ARI tokens by contributing data and using the platform.';
    } else if (lowerCaseMessage.includes('privacy')) {
      return 'ARIA is designed with privacy at its core. Your data remains under your control on the Solana blockchain, and you choose what to share.';
    } else if (lowerCaseMessage.includes('help')) {
      return 'I can help with information about ARIA, assist with daily tasks, answer questions, and more. What would you like to know?';
    } else {
      return 'I understand your message. In the full version, I would provide a more specific response based on advanced AI processing.';
    }
  };

  return (
    <div className="chat-page">
      <h1>Chat with ARIA</h1>
      <p>Your AI personal assistant powered by Solana blockchain</p>
      
      <div className="chat-container">
        <div className="chat-messages">
          {messages.map((message) => (
            <div 
              key={message.id} 
              className={`message ${message.sender === 'user' ? 'message-user' : 'message-aria'}`}
            >
              {message.content}
            </div>
          ))}
          {isLoading && (
            <div className="message message-aria">
              <div className="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>
        
        <form className="chat-input" onSubmit={handleSendMessage}>
          <input
            type="text"
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder="Type your message..."
            disabled={isLoading}
          />
          <button 
            type="submit" 
            className="button"
            disabled={isLoading || !newMessage.trim()}
          >
            Send
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChatPage; 