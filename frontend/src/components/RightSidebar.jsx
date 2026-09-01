import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useWebSocket } from '../context/WebSocketContext';
import { Gift, Calendar, UserPlus, Circle } from 'lucide-react';
import { getAvatarUrl } from '../utils/media';

const RightSidebar = ({ setCurrentView, setTargetProfileId }) => {
  const { user, apiFetch } = useAuth();
  const { subscribeChannel, unsubscribeChannel } = useWebSocket();
  const [suggestions, setSuggestions] = useState([]);
  const [upcomingEvents, setUpcomingEvents] = useState([]);
  const [friends, setFriends] = useState([]);
  const [onlineUserIds, setOnlineUserIds] = useState(new Set());

  useEffect(() => {
    if (!user) return;
    loadData();

    // Subscribe to online status indicators from WebSockets
    subscribeChannel('/topic/online', (data) => {
      setOnlineUserIds(prev => {
        const next = new Set(prev);
        if (data.isOnline) {
          next.add(data.userId);
        } else {
          next.delete(data.userId);
        }
        return next;
      });
    });

    return () => {
      unsubscribeChannel('/topic/online');
    };
  }, [user]);

  const loadData = async () => {
    try {
      // 1. Suggestions
      const sugList = await apiFetch('/api/users/suggestions');
      setSuggestions(sugList.slice(0, 4));

      // 2. Upcoming Events
      const evList = await apiFetch('/api/events/upcoming');
      setUpcomingEvents(evList.slice(0, 2));

      // 3. Friends List
      const frList = await apiFetch(`/api/friends/list/${user.id}`);
      setFriends(frList);

      // Simple mock: make a few friends online at start to make UI look alive
      if (frList.length > 0) {
        setOnlineUserIds(new Set([frList[0].id]));
      }
    } catch (e) {
      console.error(e);
    }
  };

  const sendRequest = async (targetId) => {
    try {
      await apiFetch(`/api/friends/request/send/${targetId}`, { method: 'POST' });
      setSuggestions(prev => prev.filter(s => s.id !== targetId));
    } catch (e) {
      console.error(e);
    }
  };

  const clickUser = (userId) => {
    setTargetProfileId(userId);
    setCurrentView('profile');
  };

  return (
    <div className="right-sidebar-panel" style={styles.sidebar}>
      {/* Birthdays Widget */}
      <div className="glass-card" style={styles.card}>
        <div style={styles.header}>
          <Gift size={20} style={{ color: '#ec4899' }} />
          <h4 style={styles.cardTitle}>Birthdays</h4>
        </div>
        <p style={styles.cardText}>
          <strong>Sneha Sharma</strong> and 2 other friends have birthdays today.
        </p>
      </div>

      {/* Friend Suggestions */}
      {suggestions.length > 0 && (
        <div className="glass-card" style={styles.card}>
          <div style={styles.header}>
            <UserPlus size={20} style={{ color: '#3b82f6' }} />
            <h4 style={styles.cardTitle}>People You May Know</h4>
          </div>
          <div style={styles.suggestionsList}>
            {suggestions.map(s => (
              <div key={s.id} style={styles.suggestionItem}>
                <img 
                  src={getAvatarUrl(s.avatarUrl)} 
                  alt="" 
                  style={styles.suggestionAvatar}
                  onClick={() => clickUser(s.id)}
                />
                <div style={styles.suggestionInfo}>
                  <div style={styles.suggestionName} onClick={() => clickUser(s.id)}>
                    {s.firstName} {s.lastName}
                  </div>
                  <button 
                    onClick={() => sendRequest(s.id)} 
                    className="btn btn-primary" 
                    style={styles.addBtn}
                  >
                    Add Friend
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Upcoming Events */}
      {upcomingEvents.length > 0 && (
        <div className="glass-card" style={styles.card}>
          <div style={styles.header}>
            <Calendar size={20} style={{ color: '#10b981' }} />
            <h4 style={styles.cardTitle}>Upcoming Events</h4>
          </div>
          <div style={styles.eventsList}>
            {upcomingEvents.map(e => (
              <div key={e.id} style={styles.eventItem} onClick={() => setCurrentView('events')}>
                <div style={styles.eventDate}>
                  <span style={styles.eventMonth}>
                    {new Date(e.dateTime).toLocaleString('en-US', { month: 'short' }).toUpperCase()}
                  </span>
                  <span style={styles.eventDay}>
                    {new Date(e.dateTime).getDate()}
                  </span>
                </div>
                <div style={styles.eventInfo}>
                  <div style={styles.eventName}>{e.name}</div>
                  <div style={styles.eventLoc}>{e.location}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Contacts Widget (Online Friends) */}
      <div className="glass-card" style={styles.card}>
        <div style={styles.header}>
          <Circle size={10} fill="#10b981" color="#10b981" />
          <h4 style={styles.cardTitle}>Contacts</h4>
        </div>
        <div style={styles.contactsList}>
          {friends.length === 0 ? (
            <div style={styles.emptyContacts}>Find some friends to chat!</div>
          ) : (
            friends.map(f => {
              const isOnline = onlineUserIds.has(f.id);
              return (
                <div key={f.id} style={styles.contactItem} onClick={() => clickUser(f.id)}>
                  <div style={styles.contactAvatarWrapper}>
                    <img 
                      src={getAvatarUrl(f.avatarUrl)} 
                      alt="" 
                      className="avatar" 
                      style={styles.contactAvatar}
                    />
                    {isOnline && <span style={styles.onlineDot}></span>}
                  </div>
                  <span style={styles.contactName}>{f.firstName} {f.lastName}</span>
                </div>
              );
            })
          )}
        </div>
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
  card: {
    padding: '16px'
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    borderBottom: '1px solid #273b5c',
    paddingBottom: '8px',
    marginBottom: '12px'
  },
  cardTitle: {
    fontSize: '13px',
    fontWeight: '700',
    color: '#f3f4f6',
    textTransform: 'uppercase',
    letterSpacing: '1px'
  },
  cardText: {
    fontSize: '12.5px',
    color: '#9ca3af'
  },
  suggestionsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px'
  },
  suggestionItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px'
  },
  suggestionAvatar: {
    width: '38px',
    height: '38px',
    borderRadius: '50%',
    objectFit: 'cover',
    cursor: 'pointer'
  },
  suggestionInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: '2px',
    flex: 1
  },
  suggestionName: {
    fontSize: '12.5px',
    fontWeight: '600',
    color: '#f3f4f6',
    cursor: 'pointer'
  },
  addBtn: {
    padding: '3px 8px',
    fontSize: '11px',
    borderRadius: '6px',
    alignSelf: 'flex-start'
  },
  eventsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px'
  },
  eventItem: {
    display: 'flex',
    gap: '12px',
    cursor: 'pointer',
    padding: '6px',
    borderRadius: '8px',
    transition: '0.2s',
    '&:hover': {
      backgroundColor: '#1f2d47'
    }
  },
  eventDate: {
    backgroundColor: '#0b0f19',
    border: '1px solid #273b5c',
    borderRadius: '8px',
    width: '42px',
    height: '42px',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center'
  },
  eventMonth: {
    fontSize: '9px',
    color: '#ef4444',
    fontWeight: 'bold'
  },
  eventDay: {
    fontSize: '15px',
    fontWeight: 'bold',
    color: '#f3f4f6',
    lineHeight: '1'
  },
  eventInfo: {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center'
  },
  eventName: {
    fontSize: '12.5px',
    fontWeight: '600',
    color: '#f3f4f6'
  },
  eventLoc: {
    fontSize: '10px',
    color: '#9ca3af'
  },
  contactsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
    maxHeight: '220px',
    overflowY: 'auto'
  },
  emptyContacts: {
    fontSize: '12px',
    color: '#9ca3af',
    textAlign: 'center',
    padding: '10px 0'
  },
  contactItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    cursor: 'pointer',
    padding: '4px',
    borderRadius: '8px',
    transition: '0.2s',
    '&:hover': {
      backgroundColor: '#1f2d47'
    }
  },
  contactAvatarWrapper: {
    position: 'relative'
  },
  contactAvatar: {
    width: '32px',
    height: '32px',
    border: 'none'
  },
  onlineDot: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: '10px',
    height: '10px',
    borderRadius: '50%',
    backgroundColor: '#10b981',
    border: '2px solid #162032'
  },
  contactName: {
    fontSize: '13px',
    color: '#f3f4f6'
  }
};

export default RightSidebar;
