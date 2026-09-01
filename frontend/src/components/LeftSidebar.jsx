import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Home, MessageCircle, Users, Calendar, Bookmark, History } from 'lucide-react';
import { getAvatarUrl } from '../utils/media';

const LeftSidebar = ({ currentView, setCurrentView, setTargetProfileId }) => {
  const { user } = useAuth();

  const navItems = [
    { id: 'home', label: 'News Feed', icon: <Home size={20} /> },
    { id: 'messenger', label: 'Messenger', icon: <MessageCircle size={20} /> },
    { id: 'groups', label: 'Groups', icon: <Users size={20} /> },
    { id: 'events', label: 'Events', icon: <Calendar size={20} /> },
    { id: 'saved', label: 'Saved Posts', icon: <Bookmark size={20} /> },
    { id: 'memories', label: 'Memories', icon: <History size={20} /> }
  ];

  return (
    <div className="left-sidebar-panel" style={styles.sidebar}>
      {/* User profile card */}
      <div 
        className="glass-card" 
        style={styles.profileCard}
        onClick={() => {
          setTargetProfileId(user.id);
          setCurrentView('profile');
        }}
      >
        <img 
          src={getAvatarUrl(user.avatarUrl)} 
          alt="" 
          style={styles.profileAvatar}
        />
        <div style={styles.profileInfo}>
          <div style={styles.profileName}>{user.firstName} {user.lastName}</div>
          <div style={styles.profileUsername}>@{user.username}</div>
        </div>
      </div>

      {/* Nav List */}
      <div style={styles.navList}>
        {navItems.map(item => {
          const isActive = currentView === item.id;
          return (
            <div 
              key={item.id} 
              style={isActive ? styles.navItemActive : styles.navItem}
              onClick={() => setCurrentView(item.id)}
            >
              <div style={isActive ? styles.iconActive : styles.icon}>{item.icon}</div>
              <span style={styles.label}>{item.label}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

const styles = {
  sidebar: {
    display: 'flex',
    flexDirection: 'column',
    gap: '24px',
    position: 'sticky',
    top: '84px',
    height: 'calc(100vh - 100px)'
  },
  profileCard: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    cursor: 'pointer',
    padding: '16px'
  },
  profileAvatar: {
    width: '46px',
    height: '46px',
    borderRadius: '50%',
    objectFit: 'cover',
    border: '2px solid #273b5c'
  },
  profileInfo: {
    display: 'flex',
    flexDirection: 'column'
  },
  profileName: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#f3f4f6'
  },
  profileUsername: {
    fontSize: '12px',
    color: '#9ca3af'
  },
  navList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px'
  },
  navItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '14px',
    padding: '12px 16px',
    borderRadius: '12px',
    cursor: 'pointer',
    color: '#9ca3af',
    transition: '0.2s',
    backgroundColor: 'transparent'
  },
  navItemActive: {
    display: 'flex',
    alignItems: 'center',
    gap: '14px',
    padding: '12px 16px',
    borderRadius: '12px',
    cursor: 'pointer',
    color: '#3b82f6',
    backgroundColor: 'rgba(59, 130, 246, 0.08)',
    fontWeight: '600',
    transition: '0.2s'
  },
  icon: {
    color: '#6b7280'
  },
  iconActive: {
    color: '#3b82f6'
  },
  label: {
    fontSize: '14px'
  }
};

export default LeftSidebar;
