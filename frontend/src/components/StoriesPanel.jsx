import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { Plus, X, Music, Smile, Eye } from 'lucide-react';
import { getAvatarUrl, getMediaUrl } from '../utils/media';

const StoriesPanel = () => {
  const { user, apiFetch } = useAuth();
  const [stories, setStories] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedStory, setSelectedStory] = useState(null);
  const [viewers, setViewers] = useState([]);
  
  // Creation States
  const [textContent, setTextContent] = useState('');
  const [emoji, setEmoji] = useState('');
  const [musicTitle, setMusicTitle] = useState('');
  const [privacy] = useState('PUBLIC');
  const [selectedFile, setSelectedFile] = useState(null);

  const timerRef = useRef(null);

  useEffect(() => {
    loadStories();
  }, []);

  const loadStories = async () => {
    try {
      const list = await apiFetch('/api/stories/active');
      setStories(list);
    } catch (e) {
      console.error(e);
    }
  };

  const handleCreateStory = async (e) => {
    e.preventDefault();
    try {
      const formData = new FormData();
      if (textContent) formData.append('textContent', textContent);
      if (emoji) formData.append('emoji', emoji);
      if (musicTitle) formData.append('musicTitle', musicTitle);
      if (privacy) formData.append('privacy', privacy);
      if (selectedFile) {
        formData.append('file', selectedFile);
      }

      await apiFetch('/api/stories/create', {
        method: 'POST',
        body: formData
      });

      setShowCreateModal(false);
      setTextContent('');
      setEmoji('');
      setMusicTitle('');
      setSelectedFile(null);
      loadStories();
    } catch (e) {
      console.error(e);
    }
  };

  // Viewer Controls
  const openStory = (story, idx) => {
    setSelectedStory(story);
    setStoryIndex(idx);
    registerView(story.id);
    
    // If the viewing user is the story owner, load the viewers list
    if (story.user.id === user.id) {
      loadViewers(story.id);
    } else {
      setViewers([]);
    }
    
    startProgressTimer(story, idx);
  };

  const registerView = async (storyId) => {
    try {
      await apiFetch(`/api/stories/${storyId}/view`, { method: 'POST' });
    } catch (e) {
      console.error(e);
    }
  };

  const loadViewers = async (storyId) => {
    try {
      const viewerList = await apiFetch(`/api/stories/${storyId}/viewers`);
      setViewers(viewerList);
    } catch (e) {
      console.error(e);
    }
  };

  const startProgressTimer = (story, idx) => {
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      // Swipe to next story, or close if last
      if (idx + 1 < stories.length) {
        openStory(stories[idx + 1], idx + 1);
      } else {
        closeStoryViewer();
      }
    }, 5000); // 5 seconds display
  };

  const closeStoryViewer = () => {
    if (timerRef.current) clearTimeout(timerRef.current);
    setSelectedStory(null);
    setViewers([]);
  };

  return (
    <div style={styles.container}>
      {/* Scrollable Tray */}
      <div style={styles.tray}>
        {/* User Story Card (Creator) */}
        <div style={styles.userCard} onClick={() => setShowCreateModal(true)}>
          <img 
            src={getAvatarUrl(user.avatarUrl)} 
            alt="" 
            style={styles.userCardAvatar} 
          />
          <div style={styles.plusIconWrapper}>
            <Plus size={20} color="white" />
          </div>
          <span style={styles.userCardText}>Create Story</span>
        </div>

        {/* Other active stories */}
        {stories.map((s, idx) => (
          <div 
            key={s.id} 
            style={styles.storyCard} 
            onClick={() => openStory(s, idx)}
          >
            {s.mediaUrl ? (
              <img src={getMediaUrl(s.mediaUrl)} alt="" style={styles.storyCardMedia} />
            ) : (
              <div style={styles.storyTextCardBg}>
                <span style={styles.storyTextPreview}>{s.textContent}</span>
              </div>
            )}
            <img 
              src={getAvatarUrl(s.user.avatarUrl)} 
              alt="" 
              style={styles.storyCardUserAvatar}
            />
            <span style={styles.storyCardUser}>{s.user.firstName}</span>
          </div>
        ))}
      </div>

      {/* Creation Modal */}
      {showCreateModal && (
        <div style={styles.modalOverlay} className="flex-center">
          <div style={styles.createModal} className="glass-card">
            <div style={styles.modalHeader}>
              <h3>Create a Story</h3>
              <X size={20} className="pointer" onClick={() => setShowCreateModal(false)} />
            </div>
            <form onSubmit={handleCreateStory} style={styles.form}>
              <textarea
                placeholder="What's on your mind? Add text..."
                className="input-field"
                style={{ height: '100px', resize: 'none' }}
                value={textContent}
                onChange={(e) => setTextContent(e.target.value)}
              />
              <div style={styles.row}>
                <div style={{ flex: 1, position: 'relative' }}>
                  <Smile size={18} style={styles.inputIcon} />
                  <input
                    type="text"
                    placeholder="Add Emoji"
                    className="input-field"
                    style={{ paddingLeft: '36px' }}
                    value={emoji}
                    onChange={(e) => setEmoji(e.target.value)}
                  />
                </div>
                <div style={{ flex: 1, position: 'relative' }}>
                  <Music size={18} style={styles.inputIcon} />
                  <input
                    type="text"
                    placeholder="Add Music Track Name"
                    className="input-field"
                    style={{ paddingLeft: '36px' }}
                    value={musicTitle}
                    onChange={(e) => setMusicTitle(e.target.value)}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <label style={{ fontSize: '12px', color: '#9ca3af' }}>Select Image File (Optional)</label>
                <input
                  type="file"
                  accept="image/*"
                  onChange={(e) => setSelectedFile(e.target.files[0])}
                  style={{ color: '#9ca3af' }}
                />
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
                Publish Story
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Full-Screen Story Viewer */}
      {selectedStory && (
        <div style={styles.viewerOverlay} className="flex-center">
          <X size={30} color="white" style={styles.viewerCloseBtn} className="pointer" onClick={closeStoryViewer} />
          
          <div style={styles.viewerContent}>
            {/* Story Card */}
            <div style={styles.viewerCard}>
              {/* Progress bar */}
              <div style={styles.progressBarBg}>
                <div style={styles.progressBarFill}></div>
              </div>

              {/* Author Info */}
              <div style={styles.viewerAuthor}>
                <img 
                  src={getAvatarUrl(selectedStory.user.avatarUrl)} 
                  alt="" 
                  style={styles.viewerAvatar} 
                />
                <div>
                  <span style={styles.viewerName}>{selectedStory.user.firstName} {selectedStory.user.lastName}</span>
                  {selectedStory.musicTitle && (
                    <div style={styles.viewerMusic}>
                      <Music size={12} /> {selectedStory.musicTitle}
                    </div>
                  )}
                </div>
              </div>

              {/* Story Asset */}
              <div style={styles.viewerAssetContainer}>
                {selectedStory.mediaUrl ? (
                  <img src={getMediaUrl(selectedStory.mediaUrl)} alt="" style={styles.viewerImage} />
                ) : (
                  <div style={styles.viewerTextBg}>
                    <h2>{selectedStory.textContent}</h2>
                  </div>
                )}
                {selectedStory.emoji && (
                  <span style={styles.viewerEmojiOverlay}>{selectedStory.emoji}</span>
                )}
              </div>
            </div>

            {/* Viewers panel (only show if viewing user is the story author) */}
            {selectedStory.user.id === user.id && (
              <div style={styles.viewersPanel} className="glass-card">
                <div style={styles.viewersHeader}>
                  <Eye size={18} />
                  <span>Story Viewers ({viewers.length})</span>
                </div>
                <div style={styles.viewersList}>
                  {viewers.length === 0 ? (
                    <div style={styles.emptyViewers}>No views yet.</div>
                  ) : (
                    viewers.map(v => (
                      <div key={v.id} style={styles.viewerItem}>
                        <img 
                          src={getAvatarUrl(v.avatarUrl)} 
                          alt="" 
                          style={styles.viewerItemAvatar}
                        />
                        <span>{v.firstName} {v.lastName}</span>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

const styles = {
  container: {
    width: '100%',
    marginBottom: '16px'
  },
  tray: {
    display: 'flex',
    gap: '12px',
    overflowX: 'auto',
    padding: '4px 0 12px 0',
    scrollbarWidth: 'none' // Hide scrollbar for standard Firefox
  },
  userCard: {
    minWidth: '110px',
    width: '110px',
    height: '180px',
    borderRadius: '12px',
    backgroundColor: '#162032',
    border: '1px solid #273b5c',
    position: 'relative',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
    cursor: 'pointer',
    flexShrink: 0
  },
  userCardAvatar: {
    width: '100%',
    height: '120px',
    objectFit: 'cover'
  },
  plusIconWrapper: {
    width: '32px',
    height: '32px',
    borderRadius: '50%',
    backgroundColor: '#3b82f6',
    border: '4px solid #162032',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    position: 'absolute',
    top: '104px',
    left: '50%',
    transform: 'translateX(-50%)',
    zIndex: 2
  },
  userCardText: {
    fontSize: '11px',
    fontWeight: '600',
    color: '#f3f4f6',
    textAlign: 'center',
    marginTop: '22px',
    width: '100%'
  },
  storyCard: {
    minWidth: '110px',
    width: '110px',
    height: '180px',
    borderRadius: '12px',
    position: 'relative',
    overflow: 'hidden',
    cursor: 'pointer',
    flexShrink: 0,
    border: '1px solid #273b5c'
  },
  storyCardMedia: {
    width: '100%',
    height: '100%',
    objectFit: 'cover'
  },
  storyTextCardBg: {
    width: '100%',
    height: '100%',
    background: 'linear-gradient(to bottom, #1f2d47, #0b0f19)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '8px'
  },
  storyTextPreview: {
    fontSize: '11px',
    color: '#f3f4f6',
    textAlign: 'center',
    display: '-webkit-box',
    WebkitLineClamp: 5,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden'
  },
  storyCardUserAvatar: {
    width: '28px',
    height: '28px',
    borderRadius: '50%',
    position: 'absolute',
    top: '8px',
    left: '8px',
    border: '2px solid #3b82f6',
    objectFit: 'cover',
    zIndex: 2
  },
  storyCardUser: {
    fontSize: '11px',
    color: 'white',
    position: 'absolute',
    bottom: '8px',
    left: '8px',
    fontWeight: '600',
    textShadow: '0 1px 4px rgba(0,0,0,0.8)',
    zIndex: 2
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
    width: '100%',
    maxWidth: '440px',
    padding: '24px'
  },
  modalHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
    borderBottom: '1px solid #273b5c',
    paddingBottom: '10px'
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  row: {
    display: 'flex',
    gap: '12px'
  },
  inputIcon: {
    position: 'absolute',
    left: '10px',
    top: '50%',
    transform: 'translateY(-50%)',
    color: '#6b7280'
  },
  viewerOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    width: '100vw',
    height: '100vh',
    backgroundColor: 'rgba(0, 0, 0, 0.95)',
    zIndex: 210
  },
  viewerCloseBtn: {
    position: 'absolute',
    top: '20px',
    right: '20px',
    zIndex: 220
  },
  viewerContent: {
    display: 'flex',
    gap: '30px',
    maxWidth: '800px',
    width: '90%',
    height: '80vh',
    alignItems: 'stretch'
  },
  viewerCard: {
    flex: 2,
    backgroundColor: '#0b0f19',
    borderRadius: '16px',
    border: '1px solid #273b5c',
    position: 'relative',
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden'
  },
  progressBarBg: {
    height: '4px',
    backgroundColor: 'rgba(255,255,255,0.2)',
    width: 'calc(100% - 32px)',
    position: 'absolute',
    top: '12px',
    left: '16px',
    borderRadius: '2px',
    zIndex: 10
  },
  progressBarFill: {
    height: '100%',
    backgroundColor: '#3b82f6',
    borderRadius: '2px',
    width: '100%',
    animation: 'progressBarAnim 5s linear forwards'
  },
  viewerAuthor: {
    position: 'absolute',
    top: '28px',
    left: '16px',
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    zIndex: 10,
    textShadow: '0 1px 4px rgba(0,0,0,0.8)'
  },
  viewerAvatar: {
    width: '36px',
    height: '36px',
    borderRadius: '50%',
    objectFit: 'cover',
    border: '2px solid #3b82f6'
  },
  viewerName: {
    fontSize: '13px',
    fontWeight: '600',
    color: 'white'
  },
  viewerMusic: {
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
    fontSize: '11px',
    color: '#9ca3af'
  },
  viewerAssetContainer: {
    flex: 1,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative'
  },
  viewerImage: {
    width: '100%',
    height: '100%',
    objectFit: 'contain'
  },
  viewerTextBg: {
    width: '100%',
    height: '100%',
    background: 'linear-gradient(to bottom, #162032, #0b0f19)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '40px',
    textAlign: 'center'
  },
  viewerEmojiOverlay: {
    position: 'absolute',
    bottom: '40px',
    fontSize: '48px',
    zIndex: 10
  },
  viewersPanel: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    padding: '16px',
    height: '100%',
    overflowY: 'auto'
  },
  viewersHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    borderBottom: '1px solid #273b5c',
    paddingBottom: '10px',
    marginBottom: '12px',
    fontWeight: '600',
    fontSize: '14px'
  },
  viewersList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px'
  },
  emptyViewers: {
    fontSize: '12px',
    color: '#9ca3af',
    textAlign: 'center',
    padding: '20px 0'
  },
  viewerItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    fontSize: '13px'
  },
  viewerItemAvatar: {
    width: '28px',
    height: '28px',
    borderRadius: '50%',
    objectFit: 'cover'
  }
};

// Insert dynamic keyframe animation into document head
if (typeof document !== 'undefined') {
  const style = document.createElement('style');
  style.innerHTML = `
    @keyframes progressBarAnim {
      0% { width: 0%; }
      100% { width: 100%; }
    }
  `;
  document.head.appendChild(style);
}

export default StoriesPanel;
