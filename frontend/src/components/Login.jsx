import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { User, Mail, Lock, UserPlus, LogIn, Sparkles, RefreshCw, ArrowRight, ShieldCheck } from 'lucide-react';
import { getAvatarUrl } from '../utils/media';

const DEFAULT_DEMO_USERS = [
  {
    id: 1,
    username: 'alex.morgan',
    email: 'alex.morgan@example.com',
    firstName: 'Alex',
    lastName: 'Morgan',
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80',
    work: 'Principal Architect @ CloudScale',
    location: 'San Francisco, CA',
    bio: 'Lead Full-Stack Architect & Open-Source enthusiast.'
  },
  {
    id: 2,
    username: 'sarah.jenkins',
    email: 'sarah.jenkins@example.com',
    firstName: 'Sarah',
    lastName: 'Jenkins',
    avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop&q=80',
    work: 'Visual Journalist @ Jenkins Studios',
    location: 'Seattle, WA',
    bio: 'Landscape & Travel Photographer 📸'
  },
  {
    id: 3,
    username: 'david.chen',
    email: 'david.chen@example.com',
    firstName: 'David',
    lastName: 'Chen',
    avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80',
    work: 'Senior Audio Designer @ Waveform',
    location: 'Austin, TX',
    bio: 'Sound Designer & Electronic Music Producer 🎧'
  },
  {
    id: 4,
    username: 'elena.rostova',
    email: 'elena.rostova@example.com',
    firstName: 'Elena',
    lastName: 'Rostova',
    avatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80',
    work: 'Lead Product Designer @ Studio Neo',
    location: 'New York, NY',
    bio: 'Design Director & Creative Technologist ✨'
  },
  {
    id: 5,
    username: 'marcus.vance',
    email: 'marcus.vance@example.com',
    firstName: 'Marcus',
    lastName: 'Vance',
    avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80',
    work: 'Head Trainer @ Peak Performance',
    location: 'Denver, CO',
    bio: 'Ultra-marathoner & Mountain Guide 🏔️'
  },
  {
    id: 6,
    username: 'priya.patel',
    email: 'priya.patel@example.com',
    firstName: 'Priya',
    lastName: 'Patel',
    avatarUrl: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&auto=format&fit=crop&q=80',
    work: 'Research Scientist @ DeepMind',
    location: 'Boston, MA',
    bio: 'AI Researcher & Keynote Speaker 🤖'
  }
];

const Login = () => {
  const { login, apiFetch } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [demoUsers, setDemoUsers] = useState(DEFAULT_DEMO_USERS);
  const [isSeeding, setIsSeeding] = useState(false);
  const [seedMessage, setSeedMessage] = useState('');

  // Form states
  const [emailOrUsername, setEmailOrUsername] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

  useEffect(() => {
    loadDemoUsers();
  }, []);

  const loadDemoUsers = async () => {
    try {
      const users = await apiFetch('/api/demo/users');
      if (users && users.length > 0) {
        setDemoUsers(users);
      }
    } catch (e) {
      // Keep defaults if server is initializing
    }
  };

  const handleDemoLogin = async (demoUser) => {
    setError('');
    setLoading(true);
    try {
      // 1. Try login
      const loginData = await apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({
          emailOrUsername: demoUser.username,
          password: 'demo123'
        })
      });
      login(loginData);
    } catch (err) {
      // If demo user hasn't been seeded yet, auto-seed and retry login
      try {
        await apiFetch('/api/demo/seed', { method: 'POST' });
        const loginData = await apiFetch('/api/auth/login', {
          method: 'POST',
          body: JSON.stringify({
            emailOrUsername: demoUser.username,
            password: 'demo123'
          })
        });
        login(loginData);
      } catch (retryErr) {
        setError(`Failed to sign in as ${demoUser.firstName}: ${retryErr.message || err.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleManualSeed = async () => {
    setIsSeeding(true);
    setSeedMessage('');
    try {
      await apiFetch('/api/demo/seed', { method: 'POST' });
      await loadDemoUsers();
      setSeedMessage('Demo profiles and content successfully loaded!');
      setTimeout(() => setSeedMessage(''), 4000);
    } catch (e) {
      setError(`Seed failed: ${e.message}`);
    } finally {
      setIsSeeding(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isRegister) {
        if (!username || !email || !password || !firstName || !lastName) {
          throw new Error('Please fill in all registration fields');
        }
        // 1. Call Register
        await apiFetch('/api/auth/register', {
          method: 'POST',
          body: JSON.stringify({ username, email, password, firstName, lastName })
        });
        
        // 2. Call Auto-login on success
        const loginData = await apiFetch('/api/auth/login', {
          method: 'POST',
          body: JSON.stringify({ emailOrUsername: email, password })
        });
        login(loginData);
      } else {
        if (!emailOrUsername || !password) {
          throw new Error('Please enter credentials');
        }
        // Call Login
        const loginData = await apiFetch('/api/auth/login', {
          method: 'POST',
          body: JSON.stringify({ emailOrUsername, password })
        });
        login(loginData);
      }
    } catch (err) {
      setError(err.message || 'An error occurred during authentication');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.glowBox1}></div>
      <div style={styles.glowBox2}></div>
      
      <div style={styles.mainWrapper}>
        {/* Left Side: Quick 1-Click Demo Profiles Selection */}
        <div className="glass-card" style={styles.demoCard}>
          <div style={styles.demoHeader}>
            <div style={styles.demoTitleRow}>
              <Sparkles size={22} style={{ color: '#ec4899' }} />
              <h3 style={styles.demoTitle}>Explore Demo Profiles</h3>
            </div>
            <p style={styles.demoSubtitle}>
              Click any profile below to immediately explore LinkUp as that user with realistic posts, friends, stories & groups.
            </p>
          </div>

          {seedMessage && <div style={styles.successAlert}>{seedMessage}</div>}

          <div style={styles.demoGrid}>
            {demoUsers.map((u) => (
              <div 
                key={u.username} 
                style={styles.demoProfileItem}
                onClick={() => handleDemoLogin(u)}
                title={`Click to log in as ${u.firstName} ${u.lastName}`}
              >
                <div style={styles.demoAvatarWrapper}>
                  <img 
                    src={getAvatarUrl(u.avatarUrl)} 
                    alt={u.firstName} 
                    style={styles.demoAvatar} 
                  />
                  <span style={styles.onlineBadge}></span>
                </div>
                <div style={styles.demoProfileInfo}>
                  <div style={styles.demoProfileName}>{u.firstName} {u.lastName}</div>
                  <div style={styles.demoProfileRole}>{u.work || u.location}</div>
                  <div style={styles.demoProfileUsername}>@{u.username}</div>
                </div>
                <button 
                  type="button" 
                  className="btn btn-primary" 
                  style={styles.demoLoginBtn}
                  disabled={loading}
                >
                  <ArrowRight size={14} />
                </button>
              </div>
            ))}
          </div>

          <div style={styles.demoFooter}>
            <div style={styles.demoHint}>
              <ShieldCheck size={16} color="#10b981" />
              <span>All demo profiles use password: <strong>demo123</strong></span>
            </div>
            <button 
              type="button" 
              onClick={handleManualSeed}
              className="btn btn-secondary"
              style={styles.reseedBtn}
              disabled={isSeeding}
            >
              <RefreshCw size={14} className={isSeeding ? 'animate-spin' : ''} />
              {isSeeding ? 'Seeding...' : 'Reset Demo Data'}
            </button>
          </div>
        </div>

        {/* Right Side: Traditional Login / Register Card */}
        <div className="glass-card" style={styles.card}>
          <div style={styles.header}>
            <div style={styles.logoContainer}>
              <span style={styles.logoText}>LinkUp</span>
            </div>
            <p style={styles.subtitle}>Connect and share with people in your life.</p>
          </div>

          {error && <div style={styles.errorAlert}>{error}</div>}

          <form onSubmit={handleSubmit} style={styles.form}>
            {isRegister && (
              <div style={styles.row}>
                <div style={styles.inputWrapper}>
                  <input
                    type="text"
                    placeholder="First Name"
                    className="input-field"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    required
                  />
                </div>
                <div style={styles.inputWrapper}>
                  <input
                    type="text"
                    placeholder="Last Name"
                    className="input-field"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    required
                  />
                </div>
              </div>
            )}

            {isRegister && (
              <div style={styles.inputContainer}>
                <User size={18} style={styles.icon} />
                <input
                  type="text"
                  placeholder="Username"
                  className="input-field"
                  style={styles.inputWithIcon}
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              </div>
            )}

            {isRegister ? (
              <div style={styles.inputContainer}>
                <Mail size={18} style={styles.icon} />
                <input
                  type="email"
                  placeholder="Email Address"
                  className="input-field"
                  style={styles.inputWithIcon}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            ) : (
              <div style={styles.inputContainer}>
                <User size={18} style={styles.icon} />
                <input
                  type="text"
                  placeholder="Username or Email"
                  className="input-field"
                  style={styles.inputWithIcon}
                  value={emailOrUsername}
                  onChange={(e) => setEmailOrUsername(e.target.value)}
                  required
                />
              </div>
            )}

            <div style={styles.inputContainer}>
              <Lock size={18} style={styles.icon} />
              <input
                type="password"
                placeholder="Password"
                className="input-field"
                style={styles.inputWithIcon}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="btn btn-primary" style={styles.submitBtn} disabled={loading}>
              {loading ? (
                'Authenticating...'
              ) : isRegister ? (
                <>
                  <UserPlus size={18} /> Register Account
                </>
              ) : (
                <>
                  <LogIn size={18} /> Sign In
                </>
              )}
            </button>
          </form>

          <div style={styles.divider}>
            <span style={styles.dividerText}>or</span>
          </div>

          <button
            type="button"
            className="btn btn-secondary"
            style={styles.toggleBtn}
            onClick={() => {
              setIsRegister(!isRegister);
              setError('');
            }}
          >
            {isRegister ? 'Already have an account? Sign In' : 'Create New LinkUp Account'}
          </button>
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    width: '100vw',
    backgroundColor: '#0b0f19',
    position: 'relative',
    overflow: 'hidden',
    padding: '30px 20px'
  },
  mainWrapper: {
    display: 'flex',
    gap: '30px',
    maxWidth: '1050px',
    width: '100%',
    alignItems: 'stretch',
    zIndex: 10,
    flexWrap: 'wrap',
    justifyContent: 'center'
  },
  glowBox1: {
    position: 'absolute',
    width: '500px',
    height: '500px',
    borderRadius: '50%',
    background: 'radial-gradient(circle, rgba(59,130,246,0.15) 0%, rgba(0,0,0,0) 70%)',
    top: '-10%',
    left: '-10%',
    zIndex: 1
  },
  glowBox2: {
    position: 'absolute',
    width: '600px',
    height: '600px',
    borderRadius: '50%',
    background: 'radial-gradient(circle, rgba(236,72,153,0.12) 0%, rgba(0,0,0,0) 70%)',
    bottom: '-10%',
    right: '-10%',
    zIndex: 1
  },
  demoCard: {
    flex: '1 1 480px',
    maxWidth: '520px',
    padding: '24px',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    gap: '16px'
  },
  demoHeader: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px'
  },
  demoTitleRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px'
  },
  demoTitle: {
    fontSize: '20px',
    fontWeight: '800',
    color: '#f3f4f6'
  },
  demoSubtitle: {
    fontSize: '13px',
    color: '#9ca3af',
    lineHeight: '1.4'
  },
  demoGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(210px, 1fr))',
    gap: '12px'
  },
  demoProfileItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    backgroundColor: '#162032',
    border: '1px solid #273b5c',
    borderRadius: '12px',
    padding: '10px 12px',
    cursor: 'pointer',
    transition: 'all 0.2s ease',
    '&:hover': {
      backgroundColor: '#1f2d47',
      borderColor: '#3b82f6'
    }
  },
  demoAvatarWrapper: {
    position: 'relative'
  },
  demoAvatar: {
    width: '42px',
    height: '42px',
    borderRadius: '50%',
    objectFit: 'cover',
    border: '2px solid #3b82f6'
  },
  onlineBadge: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: '11px',
    height: '11px',
    borderRadius: '50%',
    backgroundColor: '#10b981',
    border: '2px solid #162032'
  },
  demoProfileInfo: {
    display: 'flex',
    flexDirection: 'column',
    flex: 1,
    minWidth: 0
  },
  demoProfileName: {
    fontSize: '13px',
    fontWeight: '700',
    color: '#f3f4f6',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis'
  },
  demoProfileRole: {
    fontSize: '11px',
    color: '#9ca3af',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis'
  },
  demoProfileUsername: {
    fontSize: '10.5px',
    color: '#60a5fa'
  },
  demoLoginBtn: {
    padding: '6px',
    borderRadius: '8px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center'
  },
  demoFooter: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTop: '1px solid #273b5c',
    paddingTop: '14px',
    gap: '12px',
    flexWrap: 'wrap'
  },
  demoHint: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    fontSize: '12px',
    color: '#9ca3af'
  },
  reseedBtn: {
    padding: '6px 12px',
    fontSize: '12px',
    display: 'flex',
    alignItems: 'center',
    gap: '6px'
  },
  successAlert: {
    backgroundColor: 'rgba(16, 185, 129, 0.15)',
    border: '1px solid rgba(16, 185, 129, 0.4)',
    color: '#10b981',
    padding: '10px',
    borderRadius: '8px',
    fontSize: '12.5px',
    textAlign: 'center'
  },
  card: {
    flex: '1 1 380px',
    maxWidth: '440px',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'stretch',
    padding: '28px'
  },
  header: {
    textAlign: 'center',
    marginBottom: '20px'
  },
  logoContainer: {
    marginBottom: '8px'
  },
  logoText: {
    fontSize: '34px',
    fontWeight: '800',
    background: 'linear-gradient(to right, #3b82f6, #ec4899)',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    letterSpacing: '-1px'
  },
  subtitle: {
    color: '#9ca3af',
    fontSize: '13.5px',
    lineHeight: '1.4'
  },
  errorAlert: {
    backgroundColor: 'rgba(239, 68, 68, 0.1)',
    border: '1px solid rgba(239, 68, 68, 0.4)',
    color: '#ef4444',
    padding: '12px',
    borderRadius: '8px',
    marginBottom: '16px',
    fontSize: '13px',
    textAlign: 'center'
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '14px'
  },
  row: {
    display: 'flex',
    gap: '10px'
  },
  inputWrapper: {
    flex: 1
  },
  inputContainer: {
    position: 'relative',
    display: 'flex',
    alignItems: 'center'
  },
  icon: {
    position: 'absolute',
    left: '12px',
    color: '#6b7280',
    pointerEvents: 'none'
  },
  inputWithIcon: {
    paddingLeft: '40px'
  },
  submitBtn: {
    width: '100%',
    marginTop: '6px',
    padding: '12px'
  },
  divider: {
    display: 'flex',
    alignItems: 'center',
    textAlign: 'center',
    margin: '18px 0',
    color: '#4b5563'
  },
  dividerText: {
    padding: '0 10px',
    fontSize: '12px',
    textTransform: 'uppercase',
    letterSpacing: '1px'
  },
  toggleBtn: {
    width: '100%',
    padding: '12px'
  }
};

export default Login;
