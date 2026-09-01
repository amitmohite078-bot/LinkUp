import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { 
  Camera, Briefcase, GraduationCap, MapPin, Heart, 
  Calendar, UserPlus, MessageCircle, Ban, Check, Plus
} from 'lucide-react';
import { getAvatarUrl, getCoverUrl, getMediaUrl } from '../utils/media';

const Profile = ({ targetUserId, setCurrentView, setTargetProfileId }) => {
  const { user, apiFetch, login } = useAuth();
  const [profileUser, setProfileUser] = useState(null);
  
  // Tabs
  const [activeTab, setActiveTab] = useState('posts'); // posts, about, friends, photos, groups
  
  // Relationship status indicator
  const [friendshipStatus, setFriendshipStatus] = useState('NONE'); // NONE, FRIENDS, REQUEST_SENT, REQUEST_RECEIVED, SELF
  
  // Edit Profile States
  const [isEditingBio, setIsEditingBio] = useState(false);
  const [bio, setBio] = useState('');
  const [relationshipStatus, setRelationshipStatus] = useState('');
  const [work, setWork] = useState('');
  const [education, setEducation] = useState('');
  const [location, setLocation] = useState('');

  // Feed & Friends lists
  const [posts, setPosts] = useState([]);
  const [friends, setFriends] = useState([]);
  const [albums, setAlbums] = useState([]);
  const [newAlbumName, setNewAlbumName] = useState('');
  const [showCreateAlbum, setShowCreateAlbum] = useState(false);

  const isSelf = user.id === targetUserId;

  useEffect(() => {
    loadProfile();
  }, [targetUserId]);

  const loadProfile = async () => {
    try {
      // 1. Load Profile details
      const profile = await apiFetch(`/api/users/profile/${targetUserId}`);
      setProfileUser(profile);
      setBio(profile.bio || '');
      setRelationshipStatus(profile.relationshipStatus || 'Single');
      setWork(profile.work || '');
      setEducation(profile.education || '');
      setLocation(profile.location || '');

      // 2. Load Friendship status
      if (!isSelf) {
        const statusData = await apiFetch(`/api/friends/status/${targetUserId}`);
        setFriendshipStatus(statusData.status);
      } else {
        setFriendshipStatus('SELF');
      }

      // 3. Load Tab lists
      loadTabContent();
    } catch (e) {
      console.error(e);
    }
  };

  const loadTabContent = async () => {
    try {
      // Posts
      const postsData = await apiFetch(`/api/posts/user/${targetUserId}`);
      setPosts(postsData);

      // Friends
      const friendsData = await apiFetch(`/api/friends/list/${targetUserId}`);
      setFriends(friendsData);

      // Albums
      const albumsData = await apiFetch(`/api/albums/user/${targetUserId}`);
      setAlbums(albumsData);
    } catch (e) {
      console.error(e);
    }
  };

  // Profile File Uploader
  const handlePhotoUpload = async (e, type) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      const formData = new FormData();
      formData.append('files', file);
      formData.append('content', `${type.toUpperCase()} Upload`);
      
      const uploadResult = await apiFetch('/api/posts/create', {
        method: 'POST',
        body: formData
      });
      const mediaUrl = uploadResult.mediaUrls && uploadResult.mediaUrls[0];

      if (mediaUrl) {
        // Update user profile record
        const params = new URLSearchParams();
        params.append(type === 'avatar' ? 'avatarUrl' : 'coverUrl', mediaUrl);

        const updated = await apiFetch(`/api/users/profile?${params.toString()}`, {
          method: 'PUT'
        });

        setProfileUser(updated);
        if (isSelf) {
          // Sync context
          login(updated);
        }
      }
    } catch (err) {
      console.error("Failed to upload avatar/cover:", err);
    }
  };

  // Edit Bio
  const saveBio = async () => {
    try {
      const updated = await apiFetch(`/api/users/profile?bio=${encodeURIComponent(bio)}`, {
        method: 'PUT'
      });
      setProfileUser(updated);
      setIsEditingBio(false);
      if (isSelf) login(updated);
    } catch (e) {
      console.error(e);
    }
  };

  // Edit Intro Details
  const saveIntro = async () => {
    try {
      const params = new URLSearchParams();
      params.append('relationshipStatus', relationshipStatus);
      params.append('work', work);
      params.append('education', education);
      params.append('location', location);

      const updated = await apiFetch(`/api/users/profile?${params.toString()}`, {
        method: 'PUT'
      });
      setProfileUser(updated);
      if (isSelf) login(updated);
      alert("Intro updated successfully!");
    } catch (e) {
      console.error(e);
    }
  };

  // Friendship Actions
  const handleFriendAction = async () => {
    try {
      if (friendshipStatus === 'NONE') {
        // Send request
        await apiFetch(`/api/friends/request/send/${targetUserId}`, { method: 'POST' });
        setFriendshipStatus('REQUEST_SENT');
      } else if (friendshipStatus === 'REQUEST_SENT') {
        // Cancel request (same as delete)
        await apiFetch(`/api/friends/request/delete/${targetUserId}`, { method: 'POST' });
        setFriendshipStatus('NONE');
      } else if (friendshipStatus === 'REQUEST_RECEIVED') {
        // Accept request
        await apiFetch(`/api/friends/request/accept/${targetUserId}`, { method: 'POST' });
        setFriendshipStatus('FRIENDS');
      } else if (friendshipStatus === 'FRIENDS') {
        if (confirm("Remove this user from your friends?")) {
          await apiFetch(`/api/friends/remove/${targetUserId}`, { method: 'POST' });
          setFriendshipStatus('NONE');
        }
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleMessageUser = async () => {
    try {
      // Create chat room and redirect to Messenger
      await apiFetch(`/api/chat/room/direct?friendId=${targetUserId}`, { method: 'POST' });
      setCurrentView('messenger');
    } catch (e) {
      console.error(e);
    }
  };

  const handleBlockUser = async () => {
    if (!confirm("Are you sure you want to block this user? This will remove friendship and block communications.")) return;
    try {
      await apiFetch(`/api/users/${targetUserId}/block`, { method: 'POST' });
      alert("User blocked successfully");
      setCurrentView('home');
    } catch (e) {
      console.error(e);
    }
  };

  const handleCreateAlbum = async () => {
    if (!newAlbumName.trim()) return;
    try {
      await apiFetch(`/api/albums/create?name=${encodeURIComponent(newAlbumName)}`, { method: 'POST' });
      setNewAlbumName('');
      setShowCreateAlbum(false);
      loadTabContent();
    } catch (e) {
      console.error(e);
    }
  };

  if (!profileUser) {
    return <div className="flex-center" style={{ minHeight: '400px' }}>Loading profile...</div>;
  }

  return (
    <div style={styles.container}>
      {/* Cover & Avatar Header Banner */}
      <div className="glass-card" style={styles.headerCard}>
        <div style={styles.coverWrapper}>
          <img 
            src={getCoverUrl(profileUser.coverUrl)} 
            alt="Cover" 
            style={styles.coverImage} 
          />
          {isSelf && (
            <label style={styles.editCoverBtn} className="pointer">
              <Camera size={16} /> Edit Cover
              <input type="file" onChange={(e) => handlePhotoUpload(e, 'cover')} style={{ display: 'none' }} />
            </label>
          )}
        </div>

        <div style={styles.headerProfileRow}>
          <div style={styles.avatarWrapper}>
            <img 
              src={getAvatarUrl(profileUser.avatarUrl)} 
              alt="Avatar" 
              style={styles.avatarImage} 
            />
            {isSelf && (
              <label style={styles.editAvatarBtn} className="pointer">
                <Camera size={16} color="white" />
                <input type="file" onChange={(e) => handlePhotoUpload(e, 'avatar')} style={{ display: 'none' }} />
              </label>
            )}
          </div>

          <div style={styles.authorHeaderInfo}>
            <h2 style={styles.profileName}>{profileUser.firstName} {profileUser.lastName}</h2>
            <p style={styles.profileUsername}>@{profileUser.username}</p>
            <div style={styles.friendsSummaryText}>
              {friends.length} Friend(s)
            </div>
          </div>

          {/* Action Row */}
          <div style={styles.headerActionRow}>
            {isSelf ? (
              <button className="btn btn-secondary" onClick={() => setActiveTab('about')}>Edit Profile</button>
            ) : (
              <>
                <button 
                  onClick={handleFriendAction} 
                  className={friendshipStatus === 'FRIENDS' ? "btn btn-secondary" : "btn btn-primary"}
                  style={styles.actionBtn}
                >
                  {friendshipStatus === 'NONE' && <><UserPlus size={16} /> Add Friend</>}
                  {friendshipStatus === 'REQUEST_SENT' && 'Cancel Request'}
                  {friendshipStatus === 'REQUEST_RECEIVED' && 'Accept Request'}
                  {friendshipStatus === 'FRIENDS' && <><Check size={16} /> Friends</>}
                </button>
                <button onClick={handleMessageUser} className="btn btn-primary" style={styles.actionBtn}>
                  <MessageCircle size={16} /> Message
                </button>
                <button onClick={handleBlockUser} className="btn btn-danger" style={styles.actionBtn}>
                  <Ban size={16} /> Block
                </button>
              </>
            )}
          </div>
        </div>

        {/* Tab switch row */}
        <div style={styles.tabsRow}>
          {['posts', 'about', 'friends', 'photos'].map(tab => (
            <div 
              key={tab} 
              style={activeTab === tab ? styles.tabItemActive : styles.tabItem}
              onClick={() => setActiveTab(tab)}
              className="pointer"
            >
              {tab.toUpperCase()}
            </div>
          ))}
        </div>
      </div>

      {/* Nested Tab workspace grid */}
      <div style={styles.profileContentGrid}>
        
        {/* LEFT COLUMN: Intro Bio card */}
        <div style={styles.leftColumn}>
          <div className="glass-card" style={styles.introCard}>
            <h3 style={styles.cardTitle}>Intro</h3>
            
            {/* Bio section */}
            <div style={styles.bioContainer}>
              {isEditingBio ? (
                <div style={styles.bioEdit}>
                  <textarea
                    value={bio}
                    onChange={(e) => setBio(e.target.value)}
                    className="input-field"
                    style={{ height: '70px', resize: 'none' }}
                  />
                  <div style={styles.bioEditActions}>
                    <button className="btn btn-secondary" onClick={() => setIsEditingBio(false)}>Cancel</button>
                    <button className="btn btn-primary" onClick={saveBio}>Save</button>
                  </div>
                </div>
              ) : (
                <>
                  <p style={styles.bioText}>{profileUser.bio || 'No bio description yet.'}</p>
                  {isSelf && (
                    <button className="btn btn-secondary" style={{ width: '100%', padding: '6px' }} onClick={() => setIsEditingBio(true)}>
                      Edit Bio
                    </button>
                  )}
                </>
              )}
            </div>

            {/* Intro Details list */}
            <div style={styles.introDetails}>
              {profileUser.work && (
                <div style={styles.introDetailItem}>
                  <Briefcase size={18} color="#9ca3af" />
                  <span>Works at <strong>{profileUser.work}</strong></span>
                </div>
              )}
              {profileUser.education && (
                <div style={styles.introDetailItem}>
                  <GraduationCap size={18} color="#9ca3af" />
                  <span>Studied at <strong>{profileUser.education}</strong></span>
                </div>
              )}
              {profileUser.location && (
                <div style={styles.introDetailItem}>
                  <MapPin size={18} color="#9ca3af" />
                  <span>Lives in <strong>{profileUser.location}</strong></span>
                </div>
              )}
              <div style={styles.introDetailItem}>
                <Heart size={18} color="#9ca3af" />
                <span>Relationship Status: <strong>{profileUser.relationshipStatus || 'Single'}</strong></span>
              </div>
              <div style={styles.introDetailItem}>
                <Calendar size={18} color="#9ca3af" />
                <span>Joined {new Date(profileUser.joinedAt).toLocaleDateString()}</span>
              </div>
            </div>
          </div>
        </div>

        {/* RIGHT COLUMN: Tab specific content panels */}
        <div style={styles.rightColumn}>
          
          {/* Active Tab: Posts */}
          {activeTab === 'posts' && (
            <div style={styles.postsList}>
              {posts.length === 0 ? (
                <div className="glass-card" style={{ padding: '30px', textAlign: 'center', color: '#9ca3af' }}>
                  No posts uploaded yet.
                </div>
              ) : (
                posts.map(p => (
                  <div key={p.id} className="glass-card" style={styles.profilePostCard}>
                    <div style={styles.postCardHeader}>
                      <img src={getAvatarUrl(profileUser.avatarUrl)} className="avatar" alt="" />
                      <div>
                        <h4>{profileUser.firstName} {profileUser.lastName}</h4>
                        <span style={styles.postMeta}>{new Date(p.createdAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                    <p style={styles.postContent}>{p.content}</p>
                    {p.mediaUrls && p.mediaUrls.length > 0 && (
                      <img src={getMediaUrl(p.mediaUrls[0])} alt="" style={styles.postMedia} />
                    )}
                  </div>
                ))
              )}
            </div>
          )}

          {/* Active Tab: About */}
          {activeTab === 'about' && (
            <div className="glass-card" style={styles.aboutPanel}>
              <h3>Edit Profile Intro Details</h3>
              <div style={styles.aboutForm}>
                <div style={styles.formGroup}>
                  <label>Workplace</label>
                  <input type="text" className="input-field" value={work} onChange={(e) => setWork(e.target.value)} />
                </div>
                <div style={styles.formGroup}>
                  <label>Education</label>
                  <input type="text" className="input-field" value={education} onChange={(e) => setEducation(e.target.value)} />
                </div>
                <div style={styles.formGroup}>
                  <label>Current Location</label>
                  <input type="text" className="input-field" value={location} onChange={(e) => setLocation(e.target.value)} />
                </div>
                <div style={styles.formGroup}>
                  <label>Relationship Status</label>
                  <select className="input-field" value={relationshipStatus} onChange={(e) => setRelationshipStatus(e.target.value)}>
                    <option value="Single">Single</option>
                    <option value="In a relationship">In a relationship</option>
                    <option value="Engaged">Engaged</option>
                    <option value="Married">Married</option>
                    <option value="It's complicated">It's complicated</option>
                  </select>
                </div>
                {isSelf && (
                  <button className="btn btn-primary" onClick={saveIntro}>Save Changes</button>
                )}
              </div>
            </div>
          )}

          {/* Active Tab: Friends */}
          {activeTab === 'friends' && (
            <div className="glass-card" style={styles.friendsPanel}>
              <h3>Friends</h3>
              <div style={styles.friendsGrid}>
                {friends.length === 0 ? (
                  <div style={{ color: '#9ca3af' }}>No friends added yet.</div>
                ) : (
                  friends.map(f => (
                    <div 
                      key={f.id} 
                      style={styles.friendGridItem} 
                      className="pointer"
                      onClick={() => setTargetProfileId(f.id)}
                    >
                      <img src={getAvatarUrl(f.avatarUrl)} alt="" style={styles.friendGridAvatar} />
                      <div style={styles.friendGridName}>{f.firstName} {f.lastName}</div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {/* Active Tab: Photos/Albums */}
          {activeTab === 'photos' && (
            <div className="glass-card" style={styles.friendsPanel}>
              <div style={styles.albumHeader}>
                <h3>Albums</h3>
                {isSelf && (
                  <button className="btn btn-primary" onClick={() => setShowCreateAlbum(!showCreateAlbum)}>
                    <Plus size={16} /> New Album
                  </button>
                )}
              </div>

              {showCreateAlbum && (
                <div style={styles.createAlbumForm}>
                  <input
                    type="text"
                    placeholder="Enter album name..."
                    className="input-field"
                    value={newAlbumName}
                    onChange={(e) => setNewAlbumName(e.target.value)}
                  />
                  <button className="btn btn-primary" onClick={handleCreateAlbum}>Create</button>
                </div>
              )}

              <div style={styles.albumsGrid}>
                {albums.length === 0 ? (
                  <div style={{ color: '#9ca3af', padding: '10px 0' }}>No albums created yet.</div>
                ) : (
                  albums.map(a => (
                    <div key={a.id} style={styles.albumItemCard}>
                      <div style={styles.albumThumbnailBg}>
                        <span>📁</span>
                      </div>
                      <div style={styles.albumTitle}>{a.name}</div>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

        </div>
      </div>
    </div>
  );
};

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    gap: '24px',
    width: '100%'
  },
  headerCard: {
    padding: 0,
    overflow: 'hidden'
  },
  coverWrapper: {
    height: '280px',
    width: '100%',
    position: 'relative',
    backgroundColor: '#1f2d47'
  },
  coverImage: {
    width: '100%',
    height: '100%',
    objectFit: 'cover'
  },
  editCoverBtn: {
    position: 'absolute',
    bottom: '16px',
    right: '16px',
    backgroundColor: 'rgba(11, 15, 25, 0.8)',
    border: '1px solid #273b5c',
    color: 'white',
    borderRadius: '8px',
    padding: '6px 12px',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    fontSize: '12px'
  },
  headerProfileRow: {
    display: 'flex',
    padding: '24px',
    position: 'relative',
    alignItems: 'flex-end',
    borderBottom: '1px solid #273b5c',
    flexWrap: 'wrap',
    gap: '20px'
  },
  avatarWrapper: {
    width: '150px',
    height: '150px',
    borderRadius: '50%',
    border: '5px solid #162032',
    position: 'relative',
    marginTop: '-80px',
    zIndex: 5,
    backgroundColor: '#0b0f19'
  },
  avatarImage: {
    width: '100%',
    height: '100%',
    borderRadius: '50%',
    objectFit: 'cover'
  },
  editAvatarBtn: {
    position: 'absolute',
    bottom: '6px',
    right: '6px',
    backgroundColor: '#3b82f6',
    borderRadius: '50%',
    width: '32px',
    height: '32px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    border: '3px solid #162032'
  },
  authorHeaderInfo: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column'
  },
  profileName: {
    fontSize: '24px',
    fontWeight: '700',
    color: '#f3f4f6'
  },
  profileUsername: {
    fontSize: '13px',
    color: '#9ca3af'
  },
  friendsSummaryText: {
    fontSize: '12px',
    color: '#6b7280',
    marginTop: '4px'
  },
  headerActionRow: {
    display: 'flex',
    gap: '10px',
    flexWrap: 'wrap'
  },
  actionBtn: {
    padding: '8px 14px',
    fontSize: '13px'
  },
  tabsRow: {
    display: 'flex',
    padding: '0 24px',
    gap: '24px'
  },
  tabItem: {
    padding: '16px 0',
    fontSize: '13px',
    fontWeight: '600',
    color: '#9ca3af',
    borderBottom: '3px solid transparent'
  },
  tabItemActive: {
    padding: '16px 0',
    fontSize: '13px',
    fontWeight: '700',
    color: '#3b82f6',
    borderBottom: '3px solid #3b82f6'
  },
  profileContentGrid: {
    display: 'grid',
    gridTemplateColumns: '360px 1fr',
    gap: '24px',
    alignItems: 'flex-start'
  },
  leftColumn: {
    display: 'flex',
    flexDirection: 'column',
    gap: '24px'
  },
  introCard: {
    padding: '20px',
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  cardTitle: {
    fontSize: '16px',
    fontWeight: '700'
  },
  bioContainer: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px'
  },
  bioText: {
    fontSize: '13.5px',
    color: '#d1d5db',
    textAlign: 'center',
    lineHeight: '1.4',
    fontStyle: 'italic'
  },
  bioEdit: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px'
  },
  bioEditActions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '8px'
  },
  introDetails: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
    borderTop: '1px solid #273b5c',
    paddingTop: '14px'
  },
  introDetailItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    fontSize: '13.5px',
    color: '#d1d5db'
  },
  rightColumn: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  postsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  profilePostCard: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px'
  },
  postCardHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px'
  },
  postMeta: {
    fontSize: '11px',
    color: '#6b7280'
  },
  postContent: {
    fontSize: '14px',
    color: '#f3f4f6'
  },
  postMedia: {
    width: '100%',
    maxHeight: '350px',
    objectFit: 'cover',
    borderRadius: '12px',
    border: '1px solid #273b5c'
  },
  aboutPanel: {
    padding: '24px'
  },
  aboutForm: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
    marginTop: '16px'
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px'
  },
  friendsPanel: {
    padding: '20px'
  },
  friendsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(100px, 1fr))',
    gap: '16px',
    marginTop: '16px'
  },
  friendGridItem: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    textAlign: 'center',
    gap: '8px'
  },
  friendGridAvatar: {
    width: '64px',
    height: '64px',
    borderRadius: '12px',
    objectFit: 'cover'
  },
  friendGridName: {
    fontSize: '12px',
    fontWeight: '600',
    color: '#f3f4f6'
  },
  albumHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center'
  },
  createAlbumForm: {
    display: 'flex',
    gap: '8px',
    margin: '12px 0'
  },
  albumsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))',
    gap: '16px',
    marginTop: '16px'
  },
  albumItemCard: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    cursor: 'pointer'
  },
  albumThumbnailBg: {
    height: '90px',
    backgroundColor: '#1f2d47',
    borderRadius: '12px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '28px',
    border: '1px solid #273b5c'
  },
  albumTitle: {
    fontSize: '12px',
    fontWeight: '600',
    color: '#f3f4f6',
    textAlign: 'center'
  }
};

export default Profile;
