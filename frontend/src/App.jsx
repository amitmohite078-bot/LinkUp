import React, { useState } from 'react';
import { useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import LeftSidebar from './components/LeftSidebar';
import RightSidebar from './components/RightSidebar';
import Login from './components/Login';
import StoriesPanel from './components/StoriesPanel';
import Feed from './components/Feed';
import Messenger from './components/Messenger';
import Profile from './components/Profile';
import GroupsView from './components/GroupsView';
import EventsView from './components/EventsView';
import SavedPostsView from './components/SavedPostsView';
import MemoriesView from './components/MemoriesView';

import { Home, MessageCircle, Users, Calendar, User } from 'lucide-react';

const App = () => {
  const { user } = useAuth();
  const [view, setView] = useState('home'); // home, messenger, profile, groups, events, saved, memories
  const [targetProfileId, setTargetProfileId] = useState(null);

  // If user is not logged in, force Login screen
  if (!user) {
    return <Login />;
  }

  return (
    <div className="app-container">
      {/* Top Navigation */}
      <Navbar 
        currentView={view} 
        setCurrentView={setView} 
        setTargetProfileId={setTargetProfileId} 
      />

      <div className="main-layout">
        {/* Left Sidebar (Desktop/Tablet) */}
        <LeftSidebar 
          currentView={view} 
          setCurrentView={setView} 
          setTargetProfileId={setTargetProfileId} 
        />

        {/* Center Main Workspace */}
        <div style={styles.centerFeed}>
          {view === 'home' && (
            <>
              <StoriesPanel />
              <Feed />
            </>
          )}
          {view === 'messenger' && <Messenger />}
          {view === 'profile' && (
            <Profile 
              targetUserId={targetProfileId || user.id} 
              setCurrentView={setView}
              setTargetProfileId={setTargetProfileId}
            />
          )}
          {view === 'groups' && <GroupsView />}
          {view === 'events' && <EventsView />}
          {view === 'saved' && <SavedPostsView />}
          {view === 'memories' && <MemoriesView />}
        </div>

        {/* Right Sidebar Widget (Desktop Only) */}
        <RightSidebar 
          setCurrentView={setView} 
          setTargetProfileId={setTargetProfileId} 
        />
      </div>

      {/* Mobile Bottom Navigation Bar */}
      <div style={styles.mobileBottomNav} className="mobile-bottom-nav">
        <div 
          style={view === 'home' ? styles.mobileNavActive : styles.mobileNav} 
          onClick={() => setView('home')}
        >
          <Home size={20} />
          <span>Home</span>
        </div>
        <div 
          style={view === 'profile' ? styles.mobileNavActive : styles.mobileNav} 
          onClick={() => {
            setTargetProfileId(user.id);
            setView('profile');
          }}
        >
          <User size={20} />
          <span>Profile</span>
        </div>
        <div 
          style={view === 'messenger' ? styles.mobileNavActive : styles.mobileNav} 
          onClick={() => setView('messenger')}
        >
          <MessageCircle size={20} />
          <span>Chat</span>
        </div>
        <div 
          style={view === 'groups' ? styles.mobileNavActive : styles.mobileNav} 
          onClick={() => setView('groups')}
        >
          <Users size={20} />
          <span>Groups</span>
        </div>
        <div 
          style={view === 'events' ? styles.mobileNavActive : styles.mobileNav} 
          onClick={() => setView('events')}
        >
          <Calendar size={20} />
          <span>Events</span>
        </div>
      </div>
    </div>
  );
};

const styles = {
  centerFeed: {
    display: 'flex',
    flexDirection: 'column',
    width: '100%',
    minWidth: 0 // Prevents grid layout blowout
  },
  mobileBottomNav: {
    position: 'fixed',
    bottom: 0,
    left: 0,
    width: '100%',
    height: '60px',
    backgroundColor: 'rgba(22, 32, 50, 0.95)',
    borderTop: '1px solid #273b5c',
    display: 'none', // Managed by responsive CSS below
    gridTemplateColumns: 'repeat(5, 1fr)',
    zIndex: 100,
    backdropFilter: 'blur(12px)'
  },
  mobileNav: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#9ca3af',
    fontSize: '10px',
    gap: '2px',
    cursor: 'pointer'
  },
  mobileNavActive: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#3b82f6',
    fontSize: '10px',
    gap: '2px',
    cursor: 'pointer',
    fontWeight: 'bold'
  }
};

// Inject responsive CSS stylesheet for Bottom Nav trigger
if (typeof document !== 'undefined') {
  const style = document.createElement('style');
  style.innerHTML = `
    @media (max-width: 768px) {
      .mobile-bottom-nav {
        display: grid !important;
      }
    }
  `;
  document.head.appendChild(style);
}

export default App;
