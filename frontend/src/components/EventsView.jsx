import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Plus, X, CalendarDays } from 'lucide-react';
import { getAvatarUrl } from '../utils/media';

const EventsView = () => {
  const { user, apiFetch } = useAuth();
  const [upcomingEvents, setUpcomingEvents] = useState([]);
  const [userEvents, setUserEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null); // Event

  // Creation States
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [type, setType] = useState('PHYSICAL'); // PHYSICAL, ONLINE
  const [privacy, setPrivacy] = useState('PUBLIC');
  const [dateTime, setDateTime] = useState('');
  const [coverUrl, setCoverUrl] = useState('');

  // Event Details lists
  const [attendees, setAttendees] = useState([]);
  const [userRsvp, setUserRsvp] = useState('NONE'); // GOING, INTERESTED, NOT_GOING, NONE
  const [friendsList, setFriendsList] = useState([]);
  const [showInviteModal, setShowInviteModal] = useState(false);

  useEffect(() => {
    loadEventsSummary();
    loadFriends();
  }, []);

  useEffect(() => {
    if (!selectedEvent) return;
    loadEventDetails(selectedEvent.id);
  }, [selectedEvent]);

  const loadEventsSummary = async () => {
    try {
      const upcoming = await apiFetch('/api/events/upcoming');
      setUpcomingEvents(upcoming);

      const userAttending = await apiFetch('/api/events/user-upcoming');
      setUserEvents(userAttending);
    } catch (e) {
      console.error(e);
    }
  };

  const loadEventDetails = async (eventId) => {
    try {
      const attendeesList = await apiFetch(`/api/events/${eventId}/attendees`);
      setAttendees(attendeesList);

      const currentAttendee = attendeesList.find(a => a.user.id === user.id);
      setUserRsvp(currentAttendee ? currentAttendee.status : 'NONE');
    } catch (e) {
      console.error(e);
    }
  };

  const loadFriends = async () => {
    try {
      const list = await apiFetch(`/api/friends/list/${user.id}`);
      setFriendsList(list);
    } catch (e) {
      console.error(e);
    }
  };

  const handleCreateEvent = async (e) => {
    e.preventDefault();
    if (!name.trim() || !dateTime) return;

    try {
      const params = new URLSearchParams();
      params.append('name', name);
      params.append('description', description);
      params.append('location', location);
      params.append('type', type);
      params.append('privacy', privacy);
      params.append('dateTime', dateTime);
      if (coverUrl) params.append('coverUrl', coverUrl);

      const event = await apiFetch(`/api/events/create?${params.toString()}`, {
        method: 'POST'
      });

      setShowCreateModal(false);
      setName('');
      setDescription('');
      setLocation('');
      setDateTime('');
      setCoverUrl('');
      loadEventsSummary();
      setSelectedEvent(event);
    } catch (e) {
      console.error(e);
    }
  };

  const handleRsvp = async (eventId, status) => {
    try {
      await apiFetch(`/api/events/${eventId}/rsvp?status=${status}`, { method: 'POST' });
      loadEventsSummary();
      if (selectedEvent && selectedEvent.id === eventId) {
        loadEventDetails(eventId);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleInviteFriend = async (friendId) => {
    try {
      await apiFetch(`/api/events/${selectedEvent.id}/invite/${friendId}`, { method: 'POST' });
      alert("Invitation sent successfully!");
      setShowInviteModal(false);
    } catch (e) {
      alert("Friend already invited or attending!");
    }
  };

  return (
    <div style={styles.container} className="glass-card">
      {/* Left Sidebar events list */}
      <div style={styles.sidebar}>
        <div style={styles.sidebarHeader}>
          <h3>Events</h3>
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)} style={{ padding: '6px 12px', fontSize: '12px' }}>
            <Plus size={14} /> New Event
          </button>
        </div>

        <div style={styles.eventsListContainer}>
          <div style={styles.sectionHeader}>Upcoming Events</div>
          {upcomingEvents.length === 0 ? (
            <div style={styles.emptyText}>No upcoming events.</div>
          ) : (
            upcomingEvents.map(e => (
              <div 
                key={e.id} 
                style={selectedEvent && selectedEvent.id === e.id ? styles.eventItemActive : styles.eventItem}
                onClick={() => setSelectedEvent(e)}
              >
                <div style={styles.dateThumb}>
                  <span style={styles.month}>{new Date(e.dateTime).toLocaleString('en-US', { month: 'short' }).toUpperCase()}</span>
                  <span style={styles.day}>{new Date(e.dateTime).getDate()}</span>
                </div>
                <div style={styles.eventInfo}>
                  <div style={styles.eventName}>{e.name}</div>
                  <div style={styles.eventMeta}>{e.location}</div>
                </div>
              </div>
            ))
          )}

          <div style={{ ...styles.sectionHeader, marginTop: '16px' }}>Your Schedule</div>
          {userEvents.length === 0 ? (
            <div style={styles.emptyText}>You aren't attending any upcoming events.</div>
          ) : (
            userEvents.map(e => (
              <div 
                key={e.id} 
                style={selectedEvent && selectedEvent.id === e.id ? styles.eventItemActive : styles.eventItem}
                onClick={() => setSelectedEvent(e)}
              >
                <div style={styles.dateThumb}>
                  <span style={styles.month}>{new Date(e.dateTime).toLocaleString('en-US', { month: 'short' }).toUpperCase()}</span>
                  <span style={styles.day}>{new Date(e.dateTime).getDate()}</span>
                </div>
                <div style={styles.eventInfo}>
                  <div style={styles.eventName}>{e.name}</div>
                  <div style={styles.eventMeta}>{e.location}</div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Right Dashboard Workspace */}
      <div style={styles.dashboard}>
        {selectedEvent ? (
          <>
            {/* Header info */}
            <div style={styles.eventHeader}>
              <div style={styles.eventCoverWrapper}>
                <img 
                  src={selectedEvent.coverUrl || 'https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?w=800&auto=format&fit=crop&q=80'} 
                  alt="" 
                  style={styles.eventCover} 
                />
              </div>

              <div style={styles.eventMetaRow}>
                <div style={styles.eventTitleDetails}>
                  <h2>{selectedEvent.name}</h2>
                  <div style={styles.dateTimeLoc}>
                    <span>📅 {new Date(selectedEvent.dateTime).toLocaleString()}</span>
                    <span>📍 {selectedEvent.location} ({selectedEvent.type})</span>
                  </div>
                </div>

                <div style={styles.eventRsvpActions}>
                  <select 
                    value={userRsvp} 
                    onChange={(e) => handleRsvp(selectedEvent.id, e.target.value)}
                    style={styles.rsvpSelect}
                    className="pointer"
                  >
                    <option value="NONE">Respond RSVP</option>
                    <option value="GOING">Going</option>
                    <option value="INTERESTED">Interested</option>
                    <option value="NOT_GOING">Not Going</option>
                  </select>

                  <button className="btn btn-primary" onClick={() => setShowInviteModal(true)}>
                    Invite Friends
                  </button>
                </div>
              </div>
            </div>

            {/* Description and attendees panels */}
            <div style={styles.eventBodyGrid}>
              <div className="glass-card" style={styles.descCard}>
                <h3>Details</h3>
                <p style={styles.descText}>{selectedEvent.description || 'No description provided.'}</p>
              </div>

              <div className="glass-card" style={styles.attendeesCard}>
                <h3>Attendees ({attendees.length})</h3>
                <div style={styles.attendeesList}>
                  {attendees.length === 0 ? (
                    <div style={{ color: '#9ca3af', fontSize: '12px' }}>No attendees yet.</div>
                  ) : (
                    attendees.map(a => (
                      <div key={a.id} style={styles.attendeeRow}>
                        <img src={getAvatarUrl(a.user.avatarUrl)} className="avatar" style={{ width: '28px', height: '28px' }} alt="" />
                        <div>
                          <div style={styles.attendeeName}>{a.user.firstName} {a.user.lastName}</div>
                          <span style={styles.attendeeStatus}>{a.status}</span>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>
          </>
        ) : (
          <div style={styles.placeholder} className="flex-center">
            <CalendarDays size={48} color="#273b5c" style={{ marginBottom: '12px' }} />
            <h3>LinkUp Events</h3>
            <p>Schedule meetups, organize online webinars, invite your friends, and track guest RSVPs.</p>
          </div>
        )}
      </div>

      {/* Event Creation Modal */}
      {showCreateModal && (
        <div style={styles.modalOverlay} className="flex-center">
          <div style={styles.createModal} className="glass-card">
            <div style={styles.modalHeader}>
              <h3>Create Event</h3>
              <X size={20} className="pointer" onClick={() => setShowCreateModal(false)} />
            </div>
            <form onSubmit={handleCreateEvent} style={styles.form}>
              <input
                type="text"
                placeholder="Event Name"
                className="input-field"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
              <textarea
                placeholder="Event Description..."
                className="input-field"
                style={{ height: '70px', resize: 'none' }}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
              <input
                type="text"
                placeholder="Location / Web Link"
                className="input-field"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                required
              />
              <div style={styles.row}>
                <select className="input-field" value={type} onChange={(e) => setType(e.target.value)}>
                  <option value="PHYSICAL">Physical Location</option>
                  <option value="ONLINE">Online Stream</option>
                </select>
                <select className="input-field" value={privacy} onChange={(e) => setPrivacy(e.target.value)}>
                  <option value="PUBLIC">Public</option>
                  <option value="PRIVATE">Private (Invite only)</option>
                </select>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <label style={{ fontSize: '11px', color: '#9ca3af' }}>Date & Time</label>
                <input
                  type="datetime-local"
                  className="input-field"
                  value={dateTime}
                  onChange={(e) => setDateTime(e.target.value)}
                  required
                />
              </div>
              <input
                type="text"
                placeholder="Cover Image URL (Optional)"
                className="input-field"
                value={coverUrl}
                onChange={(e) => setCoverUrl(e.target.value)}
              />
              <button type="submit" className="btn btn-primary">Schedule Event</button>
            </form>
          </div>
        </div>
      )}

      {/* Invite Friends Modal */}
      {showInviteModal && (
        <div style={styles.modalOverlay} className="flex-center">
          <div style={styles.inviteModal} className="glass-card">
            <div style={styles.modalHeader}>
              <h3>Invite Friends</h3>
              <X size={20} className="pointer" onClick={() => setShowInviteModal(false)} />
            </div>
            <div style={styles.friendsListContainer}>
              {friendsList.length === 0 ? (
                <div style={styles.emptyText}>No friends found to invite.</div>
              ) : (
                friendsList.map(f => (
                  <div key={f.id} style={styles.inviteRow}>
                    <span>{f.firstName} {f.lastName}</span>
                    <button className="btn btn-primary" style={{ padding: '4px 10px', fontSize: '11px' }} onClick={() => handleInviteFriend(f.id)}>
                      Invite
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const styles = {
  container: {
    display: 'grid',
    gridTemplateColumns: '260px 1fr',
    height: 'calc(100vh - 100px)',
    width: '100%',
    padding: 0,
    overflow: 'hidden',
    border: '1px solid #273b5c'
  },
  sidebar: {
    borderRight: '1px solid #273b5c',
    display: 'flex',
    flexDirection: 'column',
    height: '100%'
  },
  sidebarHeader: {
    padding: '16px',
    borderBottom: '1px solid #273b5c',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center'
  },
  eventsListContainer: {
    flex: 1,
    overflowY: 'auto',
    display: 'flex',
    flexDirection: 'column',
    padding: '8px'
  },
  sectionHeader: {
    fontSize: '11px',
    color: '#6b7280',
    fontWeight: 'bold',
    textTransform: 'uppercase',
    padding: '8px 6px',
    letterSpacing: '1px'
  },
  emptyText: {
    fontSize: '11.5px',
    color: '#6b7280',
    padding: '4px 6px'
  },
  eventItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    padding: '10px 8px',
    borderRadius: '10px',
    cursor: 'pointer',
    transition: '0.2s',
    '&:hover': {
      backgroundColor: 'rgba(255,255,255,0.02)'
    }
  },
  eventItemActive: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    padding: '10px 8px',
    borderRadius: '10px',
    cursor: 'pointer',
    backgroundColor: 'rgba(59, 130, 246, 0.08)',
    transition: '0.2s'
  },
  dateThumb: {
    width: '36px',
    height: '36px',
    borderRadius: '6px',
    backgroundColor: '#0b0f19',
    border: '1px solid #273b5c',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0
  },
  month: {
    fontSize: '8px',
    fontWeight: 'bold',
    color: '#ef4444'
  },
  day: {
    fontSize: '13px',
    fontWeight: 'bold',
    color: '#f3f4f6',
    lineHeight: '1'
  },
  eventInfo: {
    display: 'flex',
    flexDirection: 'column',
    flex: 1,
    overflow: 'hidden'
  },
  eventName: {
    fontSize: '13px',
    fontWeight: '600',
    color: '#f3f4f6',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis'
  },
  eventMeta: {
    fontSize: '10px',
    color: '#9ca3af',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis'
  },
  dashboard: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    overflowY: 'auto'
  },
  eventHeader: {
    display: 'flex',
    flexDirection: 'column'
  },
  eventCoverWrapper: {
    height: '200px',
    width: '100%',
    backgroundColor: '#1f2d47'
  },
  eventCover: {
    width: '100%',
    height: '100%',
    objectFit: 'cover'
  },
  eventMetaRow: {
    padding: '20px 24px',
    borderBottom: '1px solid #273b5c',
    backgroundColor: 'rgba(22,32,50,0.4)',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: '16px'
  },
  eventTitleDetails: {
    display: 'flex',
    flexDirection: 'column',
    gap: '4px'
  },
  dateTimeLoc: {
    display: 'flex',
    gap: '12px',
    fontSize: '12px',
    color: '#9ca3af'
  },
  eventRsvpActions: {
    display: 'flex',
    gap: '8px',
    alignItems: 'center'
  },
  rsvpSelect: {
    backgroundColor: '#0b0f19',
    color: '#f3f4f6',
    border: '1px solid #273b5c',
    borderRadius: '8px',
    padding: '8px 12px',
    outline: 'none',
    fontSize: '13px'
  },
  eventBodyGrid: {
    display: 'grid',
    gridTemplateColumns: '1fr 280px',
    gap: '20px',
    padding: '24px',
    alignItems: 'flex-start'
  },
  descCard: {
    padding: '20px',
    display: 'flex',
    flexDirection: 'column',
    gap: '12px'
  },
  descText: {
    fontSize: '13.5px',
    color: '#d1d5db',
    lineHeight: '1.4'
  },
  attendeesCard: {
    padding: '16px',
    display: 'flex',
    flexDirection: 'column',
    gap: '14px'
  },
  attendeesList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px'
  },
  attendeeRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px'
  },
  attendeeName: {
    fontSize: '12.5px',
    fontWeight: '600',
    color: '#f3f4f6'
  },
  attendeeStatus: {
    fontSize: '10px',
    color: '#10b981'
  },
  placeholder: {
    flex: 1,
    flexDirection: 'column',
    color: '#9ca3af',
    textAlign: 'center'
  },
  modalOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    width: '100vw',
    height: '100vh',
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    zIndex: 200,
    backdropFilter: 'blur(4px)'
  },
  createModal: {
    width: '90%',
    maxWidth: '420px',
    padding: '24px'
  },
  inviteModal: {
    width: '90%',
    maxWidth: '340px',
    padding: '20px'
  },
  modalHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottom: '1px solid #273b5c',
    paddingBottom: '10px',
    marginBottom: '20px'
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px'
  },
  row: {
    display: 'flex',
    gap: '8px'
  },
  friendsListContainer: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    maxHeight: '200px',
    overflowY: 'auto'
  },
  inviteRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    fontSize: '13px',
    padding: '4px 0'
  }
};

export default EventsView;
