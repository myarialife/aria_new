import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Button, Alert } from 'react-bootstrap';
import { Line } from 'react-chartjs-2';
import { Chart, registerables } from 'chart.js';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faWallet, faExchangeAlt, faChartLine, faBell } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';
import NavBar from '../components/NavBar';
import TransactionModal from '../components/TransactionModal';
import TokenStatsCard from '../components/TokenStatsCard';
import ActivityFeed from '../components/ActivityFeed';

Chart.register(...registerables);

const DashboardPage = () => {
  const [balance, setBalance] = useState(0);
  const [tokenStats, setTokenStats] = useState({});
  const [transactions, setTransactions] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Fetch data from backend API
    const fetchData = async () => {
      try {
        setLoading(true);
        // In a real app, these would be actual API calls
        // For demo, we'll simulate them
        
        // Get token balance
        const balanceResponse = await axios.get('/api/token/balance');
        setBalance(balanceResponse.data.balance);
        
        // Get token statistics
        const statsResponse = await axios.get('/api/token/statistics');
        setTokenStats(statsResponse.data);
        
        // Get transaction history
        const transactionsResponse = await axios.get('/api/token/transactions');
        setTransactions(transactionsResponse.data);
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
        setError('Failed to load dashboard data. Please try again later.');
        setLoading(false);
        
        // For demo purposes, set mock data
        setBalance(1000.75);
        setTokenStats({
          totalSupply: '100,000,000',
          circulatingSupply: '22,500,000',
          holders: 1245,
          price: { usd: 0.012, solana: 0.00008 },
          marketCap: '$270,000',
          volume24h: '$12,500'
        });
        setTransactions([
          { id: 1, type: 'Received', amount: 100, timestamp: '2024-11-15T10:30:00Z', status: 'Confirmed' },
          { id: 2, type: 'Sent', amount: -25.5, timestamp: '2024-11-13T15:45:00Z', status: 'Confirmed' },
          { id: 3, type: 'Staking Reward', amount: 5.25, timestamp: '2024-11-10T09:15:00Z', status: 'Confirmed' },
          { id: 4, type: 'Data Contribution', amount: 10, timestamp: '2024-11-05T14:20:00Z', status: 'Confirmed' }
        ]);
      }
    };

    fetchData();
  }, []);

  // Chart data for token price history
  const chartData = {
    labels: ['Oct 25', 'Nov 1', 'Nov 8', 'Nov 15', 'Nov 22', 'Nov 29', 'Dec 6'],
    datasets: [
      {
        label: 'ARI Price (USD)',
        data: [0.008, 0.009, 0.011, 0.010, 0.012, 0.014, 0.012],
        fill: false,
        borderColor: 'rgba(75, 192, 192, 1)',
        tension: 0.1
      }
    ]
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
      },
      title: {
        display: true,
        text: 'ARI Token Price History'
      }
    }
  };

  // Recent activities for the activity feed
  const recentActivities = [
    { id: 1, type: 'Transaction', description: 'Received 100 ARI', timestamp: '2024-12-05T10:30:00Z' },
    { id: 2, type: 'System', description: 'New feature available: AI Staking', timestamp: '2024-12-03T14:15:00Z' },
    { id: 3, type: 'Governance', description: 'New proposal: Community Fund', timestamp: '2024-11-29T09:45:00Z' },
    { id: 4, type: 'Transaction', description: 'Staking reward: 5.25 ARI', timestamp: '2024-11-25T16:20:00Z' }
  ];

  return (
    <>
      <NavBar />
      <Container fluid className="dashboard-container my-4">
        {error && <Alert variant="danger">{error}</Alert>}
        
        <Row className="mb-4">
          <Col md={6} lg={3} className="mb-3">
            <Card className="h-100 shadow-sm">
              <Card.Body>
                <div className="d-flex justify-content-between align-items-center">
                  <div>
                    <h6 className="text-muted">ARI Balance</h6>
                    <h3>{loading ? 'Loading...' : `${balance.toLocaleString()} ARI`}</h3>
                  </div>
                  <FontAwesomeIcon icon={faWallet} size="2x" className="text-primary" />
                </div>
                <Button 
                  variant="primary" 
                  size="sm" 
                  className="mt-2"
                  onClick={() => setShowModal(true)}
                >
                  <FontAwesomeIcon icon={faExchangeAlt} className="me-1" /> Send / Receive
                </Button>
              </Card.Body>
            </Card>
          </Col>
          
          <Col md={6} lg={3} className="mb-3">
            <TokenStatsCard 
              title="Market Price"
              value={loading ? 'Loading...' : `$${tokenStats.price?.usd || 0}`}
              subtitle={`${tokenStats.price?.solana || 0} SOL`}
              icon={faChartLine}
              change="+5.2%"
              isPositive={true}
            />
          </Col>
          
          <Col md={6} lg={3} className="mb-3">
            <TokenStatsCard 
              title="Market Cap"
              value={loading ? 'Loading...' : tokenStats.marketCap || '$0'}
              subtitle={`Supply: ${tokenStats.circulatingSupply || 0} / ${tokenStats.totalSupply || 0}`}
              icon={faChartLine}
              change="+2.8%"
              isPositive={true}
            />
          </Col>
          
          <Col md={6} lg={3} className="mb-3">
            <TokenStatsCard 
              title="24h Volume"
              value={loading ? 'Loading...' : tokenStats.volume24h || '$0'}
              subtitle={`Holders: ${tokenStats.holders?.toLocaleString() || 0}`}
              icon={faExchangeAlt}
              change="-1.5%"
              isPositive={false}
            />
          </Col>
        </Row>
        
        <Row className="mb-4">
          <Col lg={8} className="mb-3">
            <Card className="shadow-sm h-100">
              <Card.Body>
                <Card.Title>Price History</Card.Title>
                {loading ? (
                  <div className="text-center my-5">Loading chart data...</div>
                ) : (
                  <Line data={chartData} options={chartOptions} height={80} />
                )}
              </Card.Body>
            </Card>
          </Col>
          
          <Col lg={4} className="mb-3">
            <Card className="shadow-sm h-100">
              <Card.Body>
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <Card.Title>Activity Feed</Card.Title>
                  <FontAwesomeIcon icon={faBell} className="text-primary" />
                </div>
                <ActivityFeed activities={recentActivities} />
              </Card.Body>
            </Card>
          </Col>
        </Row>
        
        <Row>
          <Col>
            <Card className="shadow-sm">
              <Card.Body>
                <Card.Title>Recent Transactions</Card.Title>
                {loading ? (
                  <div className="text-center my-3">Loading transactions...</div>
                ) : transactions.length > 0 ? (
                  <Table responsive striped hover>
                    <thead>
                      <tr>
                        <th>Type</th>
                        <th>Amount</th>
                        <th>Date</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {transactions.map(tx => (
                        <tr key={tx.id}>
                          <td>{tx.type}</td>
                          <td className={tx.amount >= 0 ? 'text-success' : 'text-danger'}>
                            {tx.amount >= 0 ? '+' : ''}{tx.amount} ARI
                          </td>
                          <td>{new Date(tx.timestamp).toLocaleString()}</td>
                          <td>
                            <span className={`badge bg-${tx.status === 'Confirmed' ? 'success' : 'warning'}`}>
                              {tx.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                ) : (
                  <div className="text-center my-3">No transactions found</div>
                )}
                
                <div className="text-center mt-3">
                  <Button variant="outline-primary" size="sm">View All Transactions</Button>
                </div>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
      
      <TransactionModal 
        show={showModal}
        onHide={() => setShowModal(false)}
        balance={balance}
      />
    </>
  );
};

export default DashboardPage; 