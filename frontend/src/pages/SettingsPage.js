import React, { useState } from 'react';
import { useWallet } from '@solana/wallet-adapter-react';

const SettingsPage = () => {
  const { publicKey, connected } = useWallet();
  
  // Privacy settings state
  const [privacySettings, setPrivacySettings] = useState({
    shareUsageData: true,
    shareLocationData: false,
    shareChatHistory: true,
    enableNotifications: true
  });
  
  // Token earnings settings
  const [tokenSettings, setTokenSettings] = useState({
    autoStake: false,
    dataContributionRewards: true,
    participateInGrowthProgram: true
  });
  
  // AI preferences
  const [aiPreferences, setAiPreferences] = useState({
    enableVoiceResponse: false,
    personalityType: 'balanced',
    responseLength: 'medium'
  });
  
  const [isSaving, setIsSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState(null);
  
  const handlePrivacyChange = (e) => {
    const { name, checked } = e.target;
    setPrivacySettings({
      ...privacySettings,
      [name]: checked
    });
  };
  
  const handleTokenChange = (e) => {
    const { name, checked } = e.target;
    setTokenSettings({
      ...tokenSettings,
      [name]: checked
    });
  };
  
  const handleAiPreferenceChange = (e) => {
    const { name, value } = e.target;
    setAiPreferences({
      ...aiPreferences,
      [name]: value
    });
  };
  
  const handleSaveSettings = (e) => {
    e.preventDefault();
    setIsSaving(true);
    setSaveMessage(null);
    
    // Simulate API call to save settings
    setTimeout(() => {
      setIsSaving(false);
      setSaveMessage('Settings saved successfully!');
      
      // Clear success message after 3 seconds
      setTimeout(() => {
        setSaveMessage(null);
      }, 3000);
    }, 1000);
    
    // Once backend is implemented, use this:
    /*
    try {
      const response = await axios.post('/api/settings', {
        privacySettings,
        tokenSettings,
        aiPreferences,
        walletAddress: publicKey.toString()
      });
      
      setIsSaving(false);
      setSaveMessage('Settings saved successfully!');
      
      // Clear success message after 3 seconds
      setTimeout(() => {
        setSaveMessage(null);
      }, 3000);
    } catch (error) {
      setIsSaving(false);
      setSaveMessage('Error saving settings. Please try again.');
    }
    */
  };
  
  if (!connected) {
    return (
      <div className="settings-page">
        <h1>Settings</h1>
        <div className="card">
          <p>Please connect your wallet to access settings.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="settings-page">
      <h1>Settings</h1>
      
      <form onSubmit={handleSaveSettings}>
        {/* Privacy Settings */}
        <div className="card">
          <h2 className="card-title">Privacy Settings</h2>
          <p className="card-description">
            Control what data you share with ARIA and how it's used. Sharing more data helps improve ARIA and can earn you more ARI tokens.
          </p>
          
          <div className="settings-group">
            <div className="setting-item">
              <label className="toggle-label">
                <input
                  type="checkbox"
                  name="shareUsageData"
                  checked={privacySettings.shareUsageData}
                  onChange={handlePrivacyChange}
                />
                Share Usage Data
              </label>
              <p className="setting-description">
                Allow ARIA to collect anonymous usage data to improve the service
              </p>
            </div>
            
            <div className="setting-item">
              <label className="toggle-label">
                <input
                  type="checkbox"
                  name="shareLocationData"
                  checked={privacySettings.shareLocationData}
                  onChange={handlePrivacyChange}
                />
                Share Location Data
              </label>
              <p className="setting-description">
                Allow ARIA to access your location for contextual recommendations
              </p>
            </div>
            
            <div className="setting-item">
              <label className="toggle-label">
                <input
                  type="checkbox"
                  name="shareChatHistory"
                  checked={privacySettings.shareChatHistory}
                  onChange={handlePrivacyChange}
                />
                Store Chat History
              </label>
              <p className="setting-description">
                Save your conversations to improve responses and provide continuity
              </p>
            </div>
            
            <div className="setting-item">
              <label className="toggle-label">
                <input
                  type="checkbox"
                  name="enableNotifications"
                  checked={privacySettings.enableNotifications}
                  onChange={handlePrivacyChange}
                />
                Enable Notifications
              </label>
              <p className="setting-description">
                Receive updates, reminders, and important alerts from ARIA
              </p>
            </div>
          </div>
        </div>
        
        {/* Token Settings */}
        <div className="card">
          <h2 className="card-title">Token Earnings</h2>
          <p className="card-description">
            Configure how you earn and use ARI tokens on the platform
          </p>
          
          <div className="settings-group">
            <div className="setting-item">
              <label className="toggle-label">
                <input
                  type="checkbox"
                  name="autoStake"
                  checked={tokenSettings.autoStake}
                  onChange={handleTokenChange}
                />
                Auto-Stake Earnings
              </label>
              <p className="setting-description">
                Automatically stake earned tokens for additional rewards
              </p>
            </div>
            
            <div className="setting-item">
              <label className="toggle-label">
                <input
                  type="checkbox"
                  name="dataContributionRewards"
                  checked={tokenSettings.dataContributionRewards}
                  onChange={handleTokenChange}
                />
                Data Contribution Rewards
              </label>
              <p className="setting-description">
                Earn tokens by contributing anonymized data to improve ARIA
              </p>
            </div>
            
            <div className="setting-item">
              <label className="toggle-label">
                <input
                  type="checkbox"
                  name="participateInGrowthProgram"
                  checked={tokenSettings.participateInGrowthProgram}
                  onChange={handleTokenChange}
                />
                Growth Program Participation
              </label>
              <p className="setting-description">
                Join our growth program to earn bonus tokens for active usage
              </p>
            </div>
          </div>
        </div>
        
        {/* AI Preferences */}
        <div className="card">
          <h2 className="card-title">AI Preferences</h2>
          <p className="card-description">
            Customize how ARIA interacts with you
          </p>
          
          <div className="settings-group">
            <div className="setting-item">
              <label className="toggle-label">
                <input
                  type="checkbox"
                  name="enableVoiceResponse"
                  checked={aiPreferences.enableVoiceResponse}
                  onChange={(e) => setAiPreferences({...aiPreferences, enableVoiceResponse: e.target.checked})}
                />
                Enable Voice Responses
              </label>
              <p className="setting-description">
                Allow ARIA to respond using voice output when available
              </p>
            </div>
            
            <div className="setting-item">
              <label className="select-label">
                Personality Type
                <select
                  name="personalityType"
                  value={aiPreferences.personalityType}
                  onChange={handleAiPreferenceChange}
                >
                  <option value="professional">Professional</option>
                  <option value="friendly">Friendly</option>
                  <option value="balanced">Balanced</option>
                  <option value="concise">Concise</option>
                </select>
              </label>
              <p className="setting-description">
                Choose how ARIA communicates with you
              </p>
            </div>
            
            <div className="setting-item">
              <label className="select-label">
                Response Length
                <select
                  name="responseLength"
                  value={aiPreferences.responseLength}
                  onChange={handleAiPreferenceChange}
                >
                  <option value="short">Short</option>
                  <option value="medium">Medium</option>
                  <option value="detailed">Detailed</option>
                </select>
              </label>
              <p className="setting-description">
                Preferred level of detail in ARIA's responses
              </p>
            </div>
          </div>
        </div>
        
        {/* Save Button */}
        <div className="settings-actions">
          <button type="submit" className="button" disabled={isSaving}>
            {isSaving ? 'Saving...' : 'Save Settings'}
          </button>
          
          {saveMessage && (
            <div className="settings-message">
              {saveMessage}
            </div>
          )}
        </div>
      </form>
    </div>
  );
};

export default SettingsPage; 