import React from 'react';
import { Link } from 'react-router-dom';
import { useWallet } from '@solana/wallet-adapter-react';
import { WalletMultiButton } from '@solana/wallet-adapter-react-ui';

const Header = () => {
  const { connected } = useWallet();

  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="logo">
          <img src="/logo.png" alt="ARIA Logo" />
          ARIA
        </Link>
        
        <nav>
          <ul className="nav-links">
            <li><Link to="/">Home</Link></li>
            {connected && <li><Link to="/dashboard">Dashboard</Link></li>}
            <li><Link to="/chat">AI Chat</Link></li>
            {connected && <li><Link to="/settings">Settings</Link></li>}
          </ul>
        </nav>
        
        <div className="wallet-button">
          <WalletMultiButton />
        </div>
      </div>
    </header>
  );
};

export default Header; 