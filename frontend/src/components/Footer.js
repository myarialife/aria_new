import React from 'react';
import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="footer">
      <ul className="footer-links">
        <li><Link to="/">Home</Link></li>
        <li><a href="https://github.com/myarialife/aria_new" target="_blank" rel="noopener noreferrer">GitHub</a></li>
        <li><a href="/docs/whitepaper.pdf" target="_blank" rel="noopener noreferrer">Whitepaper</a></li>
        <li><a href="/docs/tokenomics.pdf" target="_blank" rel="noopener noreferrer">Tokenomics</a></li>
      </ul>
      <p>&copy; {new Date().getFullYear()} ARIA. All rights reserved.</p>
    </footer>
  );
};

export default Footer; 