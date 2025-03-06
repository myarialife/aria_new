import React from 'react';
import { Link } from 'react-router-dom';
import { useWallet } from '@solana/wallet-adapter-react';
import { WalletMultiButton } from '@solana/wallet-adapter-react-ui';

const HomePage = () => {
  const { connected } = useWallet();

  return (
    <div className="home-page">
      <section className="hero">
        <div className="hero-content">
          <h1>ARIA - AI Personal Assistant on Solana</h1>
          <p>A decentralized AI personal assistant built on the Solana blockchain. Privacy-focused, token-incentivized, and designed to make your life easier.</p>
          
          {!connected ? (
            <div className="hero-cta">
              <WalletMultiButton className="button" />
              <Link to="/chat" className="button button-secondary">Try AI Chat</Link>
            </div>
          ) : (
            <div className="hero-cta">
              <Link to="/dashboard" className="button">Go to Dashboard</Link>
              <Link to="/chat" className="button button-secondary">Start Chatting</Link>
            </div>
          )}
        </div>
        <div className="hero-image">
          <img src="/hero-image.png" alt="ARIA AI Assistant" />
        </div>
      </section>

      <section className="features">
        <h2>Key Features</h2>
        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-icon">🔒</div>
            <h3>Privacy First</h3>
            <p>Your data stays under your control with blockchain-based privacy protection.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">💰</div>
            <h3>ARI Token</h3>
            <p>Earn rewards for your data contributions with the ARI token on Solana.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">🤖</div>
            <h3>AI Assistant</h3>
            <p>Get personalized help with daily tasks, information, and more.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">⚡</div>
            <h3>Fast & Efficient</h3>
            <p>Built on Solana for lightning-fast transactions and low fees.</p>
          </div>
        </div>
      </section>

      <section className="how-it-works">
        <h2>How It Works</h2>
        <div className="steps">
          <div className="step">
            <div className="step-number">1</div>
            <h3>Connect Your Wallet</h3>
            <p>Link your Solana wallet to get started with ARIA.</p>
          </div>
          <div className="step">
            <div className="step-number">2</div>
            <h3>Configure Privacy Settings</h3>
            <p>Choose what data you want to share and earn ARI tokens.</p>
          </div>
          <div className="step">
            <div className="step-number">3</div>
            <h3>Start Using ARIA</h3>
            <p>Chat with your AI assistant and enjoy the benefits!</p>
          </div>
        </div>
      </section>

      <section className="cta-section">
        <h2>Ready to Experience the Future of AI Assistants?</h2>
        <p>Connect your wallet now and start earning ARI tokens while enjoying a privacy-focused AI assistant.</p>
        {!connected ? (
          <WalletMultiButton className="button" />
        ) : (
          <Link to="/dashboard" className="button">Go to Dashboard</Link>
        )}
      </section>
    </div>
  );
};

export default HomePage; 