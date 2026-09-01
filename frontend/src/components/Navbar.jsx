import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { useWebSocket } from '../context/WebSocketContext';
import { Search, Bell, MessageCircle, LogOut } from 'lucide-react';
import { getAvatarUrl } from '../utils/media';

const Navbar = ({ currentView, setCurrentView, setTargetProfileId }) => {
  const { user, logout, apiFetch } = useAuth();
  const { liveNotifications, setLiveNotifications } = useWebSocket();
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [showSearchDropdown, setShowSearchDropdown] = useState(false);
  
  const [notifications, setNotifications] = useState([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  const searchRef = useRef(null);
  const notifRef = useRef(null);

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleOutsideClick = (e) => {
      if (searchRef.current && !searchRef.current.contains(e.target)) {
        setShowSearchDropdown(false);
      }
      if (notifRef.current && !notifRef.current.contains(e.target)) {
        setShowNotifications(false);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, []);

  // Fetch initial notifications and unread counts
  useEffect(() => {
    if (!user) return;
    loadNotifications();
  }, [user]);

  // Handle incoming live notifications via WebSockets
  useEffect(() => {
    if (liveNotifications.length > 0) {
      setNotifications(prev => [liveNotifications[0], ...prev]);
      setUnreadCount(prev => prev + 1);
      // Clear processed websocket notice
      setLiveNotifications([]);
    }
  }, [liveNotifications]);

  const loadNotifications = async () => {
    try {
      const list = await apiFetch('/api/notifications/list');
      setNotifications(list);
      const countData = await apiFetch('/api/notifications/unread-count');
      setUnreadCount(countData.unreadCount);
    } catch (e) {
      console.error(e);
    }
  };

  const handleSearch = async (val) => {
    setSearchQuery(val);
    if (!val.trim()) {
      setSearchResults([]);
      setShowSearchDropdown(false);
      return;
    }

    try {
      // Fuzzy search users
      const users = await apiFetch(`/api/users/search?query=${val}`);
      setSearchResults(users);
      setShowSearchDropdown(true);
    } catch (e) {
      console.error(e);
    }
  };

  const clickSearchResult = (targetUserId) => {
    setSearchQuery('');
    setSearchResults([]);
    setShowSearchDropdown(false);
    setTargetProfileId(targetUserId);
    setCurrentView('profile');
  };

  const markAllRead = async () => {
    try {
      await apiFetch('/api/notifications/read-all', { method: 'POST' });
      setUnreadCount(0);
      setNotifications(prev => prev.map(n => ({ ...n, read: true })));
    } catch (e) {
      console.error(e);
    }
  };

  const handleNotificationClick = async (notif) => {
    try {
      if (!notif.read) {
        await apiFetch(`/api/notifications/${notif.id}/read`, { method: 'POST' });
        setNotifications(prev => prev.map(n => n.id === notif.id ? { ...n, read: true } : n));
        setUnreadCount(prev => Math.max(0, prev - 1));
      }

      // Route click events based on notification types
      if (notif.type === 'FRIEND_REQUEST' || notif.type === 'FRIEND_REQUEST_ACCEPT') {
        setTargetProfileId(notif.senderId || user.id);
        setCurrentView('profile');
      } else if (notif.type.startsWith('POST_') || notif.type.startsWith('COMMENT_')) {
        // Switch to feed or open detail modal if needed
        setCurrentView('home');
      }
      setShowNotifications(false);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div style={styles.navbar}>
      <div style={styles.navLeft}>
        <div style={styles.logo} onClick={() => setCurrentView('home')}>LinkUp</div>
        
        {/* Global Search Bar */}
        <div ref={searchRef} style={styles.searchContainer}>
          <Search size={18} style={styles.searchIcon} />
          <input
            type="text"
            placeholder="Search LinkUp..."
            style={styles.searchInput}
            value={searchQuery}
            onChange={(e) => handleSearch(e.target.value)}
            onFocus={() => searchQuery && setShowSearchDropdown(true)}
          />

          {showSearchDropdown && searchResults.length > 0 && (
            <div style={styles.searchDropdown} className="glass-card">
              {searchResults.map(u => (
                <div 
                  key={u.id} 
                  style={styles.searchItem} 
                  onClick={() => clickSearchResult(u.id)}
                >
                  <img 
                    src={getAvatarUrl(u.avatarUrl)} 
                    alt="" 
                    style={styles.searchAvatar} 
                  />
                  <div>
                    <div style={styles.searchName}>{u.firstName} {u.lastName}</div>
                    <div style={styles.searchUsername}>@{u.username}</div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <div style={styles.navRight}>
        {/* Messenger Shortcut */}
        <div 
          style={currentView === 'messenger' ? styles.iconBtnActive : styles.iconBtn} 
          onClick={() => setCurrentView('messenger')}
          title="Messenger"
        >
          <MessageCircle size={20} />
        </div>

        {/* Notifications Icon with Badge */}
        <div ref={notifRef} style={styles.notifWrapper}>
          <div 
            style={showNotifications ? styles.iconBtnActive : styles.iconBtn} 
            onClick={() => setShowNotifications(!showNotifications)}
            title="Notifications"
          >
            <Bell size={20} />
            {unreadCount > 0 && <span style={styles.notifBadge}>{unreadCount}</span>}
          </div>

          {showNotifications && (
            <div style={styles.notifDropdown} className="glass-card">
              <div style={styles.notifHeader}>
                <h3>Notifications</h3>
                {unreadCount > 0 && (
                  <span style={styles.markReadText} onClick={markAllRead}>Mark all read</span>
                )}
              </div>
              <div style={styles.notifList}>
                {notifications.length === 0 ? (
                  <div style={styles.emptyNotif}>No notifications yet.</div>
                ) : (
                  notifications.map(n => (
                    <div 
                      key={n.id} 
                      style={n.read ? styles.notifItemRead : styles.notifItemUnread}
                      onClick={() => handleNotificationClick(n)}
                    >
                      <img 
                        src={getAvatarUrl(n.senderAvatar)} 
                        alt="" 
                        style={styles.notifAvatar}
                      />
                      <div style={styles.notifContent}>
                        <p style={styles.notifText}>{n.content}</p>
                        <span style={styles.notifTime}>
                          {new Date(n.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* User Profile Button */}
        <div 
          style={styles.profileBtn}
          onClick={() => {
            setTargetProfileId(user.id);
            setCurrentView('profile');
          }}
          title="My Profile"
        >
          <img 
            src={getAvatarUrl(user.avatarUrl)} 
            className="avatar" 
            style={styles.profileAvatar} 
            alt="" 
          />
          <span style={styles.profileName}>{user.firstName}</span>
        </div>

        {/* Log Out */}
        <div style={styles.iconBtn} onClick={logout} title="Log Out">
          <LogOut size={20} />
        </div>
      </div>
    </div>
  );
};

const styles = {
  navbar: {
    height: '60px',
    backgroundColor: 'rgba(22, 32, 50, 0.95)',
    borderBottom: '1px solid #273b5c',
    position: 'fixed',
    top: 0,
    left: 0,
    width: '100%',
    zIndex: 100,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 24px',
    backdropFilter: 'blur(12px)'
  },
  navLeft: {
    display: 'flex',
    alignItems: 'center',
    gap: '24px',
    flex: 1
  },
  logo: {
    fontSize: '24px',
    fontWeight: '800',
    background: 'linear-gradient(to right, #3b82f6, #ec4899)',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    letterSpacing: '-1px',
    cursor: 'pointer'
  },
  searchContainer: {
    position: 'relative',
    maxWidth: '280px',
    width: '100%',
    display: 'flex',
    alignItems: 'center'
  },
  searchIcon: {
    position: 'absolute',
    left: '12px',
    color: '#6b7280',
    pointerEvents: 'none'
  },
  searchInput: {
    backgroundColor: '#0b0f19',
    border: '1px solid #273b5c',
    color: '#f3f4f6',
    borderRadius: '20px',
    padding: '8px 12px 8px 38px',
    outline: 'none',
    width: '100%',
    fontSize: '13px',
    transition: '0.2s'
  },
  searchDropdown: {
    position: 'absolute',
    top: '46px',
    left: 0,
    width: '100%',
    maxHeight: '300px',
    overflowY: 'auto',
    padding: '8px',
    display: 'flex',
    flexDirection: 'column',
    gap: '4px'
  },
  searchItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    padding: '8px',
    borderRadius: '8px',
    cursor: 'pointer',
    transition: '0.2s',
    '&:hover': {
      backgroundColor: '#1f2d47'
    }
  },
  searchAvatar: {
    width: '32px',
    height: '32px',
    borderRadius: '50%',
    objectFit: 'cover'
  },
  searchName: {
    fontSize: '13px',
    fontWeight: '600',
    color: '#f3f4f6'
  },
  searchUsername: {
    fontSize: '11px',
    color: '#9ca3af'
  },
  navRight: {
    display: 'flex',
    alignItems: 'center',
    gap: '16px'
  },
  iconBtn: {
    width: '38px',
    height: '38px',
    borderRadius: '50%',
    backgroundColor: '#1f2d47',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    cursor: 'pointer',
    color: '#f3f4f6',
    transition: '0.2s',
    position: 'relative'
  },
  iconBtnActive: {
    width: '38px',
    height: '38px',
    borderRadius: '50%',
    backgroundColor: '#3b82f6',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    cursor: 'pointer',
    color: 'white',
    transition: '0.2s',
    position: 'relative'
  },
  profileBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    padding: '4px 12px 4px 4px',
    borderRadius: '20px',
    backgroundColor: '#1f2d47',
    cursor: 'pointer',
    transition: '0.2s'
  },
  profileAvatar: {
    width: '30px',
    height: '30px',
    border: 'none'
  },
  profileName: {
    fontSize: '13px',
    fontWeight: '600',
    color: '#f3f4f6'
  },
  notifWrapper: {
    position: 'relative'
  },
  notifBadge: {
    position: 'absolute',
    top: '-4px',
    right: '-4px',
    backgroundColor: '#ec4899',
    color: 'white',
    borderRadius: '50%',
    padding: '1px 5px',
    fontSize: '10px',
    fontWeight: 'bold'
  },
  notifDropdown: {
    position: 'absolute',
    top: '46px',
    right: 0,
    width: '360px',
    maxHeight: '400px',
    overflowY: 'auto',
    padding: '16px',
    display: 'flex',
    flexDirection: 'column',
    gap: '12px'
  },
  notifHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottom: '1px solid #273b5c',
    paddingBottom: '8px'
  },
  markReadText: {
    fontSize: '12px',
    color: '#3b82f6',
    cursor: 'pointer'
  },
  notifList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px'
  },
  emptyNotif: {
    textAlign: 'center',
    color: '#9ca3af',
    padding: '20px 0',
    fontSize: '13px'
  },
  notifItemUnread: {
    display: 'flex',
    gap: '12px',
    padding: '10px',
    borderRadius: '8px',
    backgroundColor: 'rgba(59, 130, 246, 0.08)',
    borderLeft: '3px solid #3b82f6',
    cursor: 'pointer',
    transition: '0.2s'
  },
  notifItemRead: {
    display: 'flex',
    gap: '12px',
    padding: '10px',
    borderRadius: '8px',
    cursor: 'pointer',
    transition: '0.2s'
  },
  notifAvatar: {
    width: '36px',
    height: '36px',
    borderRadius: '50%',
    objectFit: 'cover'
  },
  notifContent: {
    display: 'flex',
    flexDirection: 'column',
    gap: '2px'
  },
  notifText: {
    fontSize: '12px',
    color: '#f3f4f6',
    lineHeight: '1.3'
  },
  notifTime: {
    fontSize: '10px',
    color: '#9ca3af'
  }
};

export default Navbar;
