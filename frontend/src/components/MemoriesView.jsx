import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { History, Share2, Bookmark, Trash } from 'lucide-react';
import { getMediaUrl } from '../utils/media';

const MemoriesView = () => {
  const { apiFetch } = useAuth();
  const [memories, setMemories] = useState([]);

  useEffect(() => {
    loadMemories();
  }, []);

  const loadMemories = async () => {
    try {
      const list = await apiFetch('/api/posts/memories');
      setMemories(list);
    } catch (e) {
      console.error(e);
    }
  };

  const handleShareMemory = async (memoryId) => {
    try {
      await apiFetch(`/api/posts/${memoryId}/share?content=Sharing a memory!&privacy=PUBLIC`, {
        method: 'POST'
      });
      alert("Memory shared to your feed!");
    } catch (e) {
      console.error(e);
    }
  };

  const handleSaveMemory = async (memoryId) => {
    try {
      await apiFetch(`/api/posts/${memoryId}/save?categoryName=Saved Memories`, { method: 'POST' });
      alert("Memory saved successfully!");
    } catch (e) {
      console.error(e);
    }
  };

  const handleDeleteMemory = async (memoryId) => {
    if (!confirm('Are you sure you want to delete this post?')) return;
    try {
      await apiFetch(`/api/posts/${memoryId}`, { method: 'DELETE' });
      setMemories(prev => prev.filter(m => m.id !== memoryId));
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div style={styles.container} className="glass-card">
      <div style={styles.header}>
        <History size={24} style={{ color: '#ec4899' }} />
        <h3>On This Day — Memories</h3>
      </div>
      <p style={styles.subtitle}>We hope you enjoy looking back at your memories from August 30 in previous years.</p>

      <div style={styles.list}>
        {memories.length === 0 ? (
          <div style={styles.emptyText} className="glass-card">
            <h3>No Memories Today</h3>
            <p>We couldn't find any posts created on this date in previous years. Keep posting to build your memories!</p>
          </div>
        ) : (
          memories.map(m => {
            const yearsAgo = new Date().getFullYear() - new Date(m.createdAt).getFullYear();
            return (
              <div key={m.id} style={styles.memoryCard}>
                <div style={styles.memoryCardHeader}>
                  <div style={styles.yearsBadge}>
                    <span>📅</span>
                    <span>{yearsAgo} Year(s) Ago Today</span>
                  </div>
                  <div style={styles.metaRow}>
                    <span>Created on {new Date(m.createdAt).toLocaleDateString()}</span>
                  </div>
                </div>

                <div style={styles.postBody}>
                  <p style={styles.postContent}>{m.content}</p>
                  {m.mediaUrls && m.mediaUrls.length > 0 && (
                    <img src={getMediaUrl(m.mediaUrls[0])} alt="" style={styles.postMedia} />
                  )}
                </div>

                {/* Actions */}
                <div style={styles.actionRow}>
                  <button className="btn btn-secondary" style={styles.actionBtn} onClick={() => handleShareMemory(m.id)}>
                    <Share2 size={14} /> Share Memory
                  </button>
                  <button className="btn btn-secondary" style={styles.actionBtn} onClick={() => handleSaveMemory(m.id)}>
                    <Bookmark size={14} /> Save
                  </button>
                  <button className="btn btn-danger" style={styles.actionBtn} onClick={() => handleDeleteMemory(m.id)}>
                    <Trash size={14} /> Delete
                  </button>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

const styles = {
  container: {
    padding: '24px',
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
    width: '100%',
    minHeight: '400px'
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    borderBottom: '1px solid #273b5c',
    paddingBottom: '10px'
  },
  subtitle: {
    fontSize: '13.5px',
    color: '#9ca3af',
    lineHeight: '1.4'
  },
  list: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px'
  },
  emptyText: {
    textAlign: 'center',
    padding: '40px 20px',
    color: '#9ca3af'
  },
  memoryCard: {
    border: '1px solid #273b5c',
    borderRadius: '12px',
    padding: '20px',
    backgroundColor: 'rgba(22, 32, 50, 0.4)',
    display: 'flex',
    flexDirection: 'column',
    gap: '14px'
  },
  memoryCardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottom: '1px solid #273b5c',
    paddingBottom: '10px'
  },
  yearsBadge: {
    fontSize: '13px',
    fontWeight: 'bold',
    color: '#ec4899',
    display: 'flex',
    alignItems: 'center',
    gap: '6px'
  },
  metaRow: {
    fontSize: '11px',
    color: '#6b7280'
  },
  postBody: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px'
  },
  postContent: {
    fontSize: '14.5px',
    color: '#f3f4f6',
    lineHeight: '1.4'
  },
  postMedia: {
    width: '100%',
    maxHeight: '300px',
    objectFit: 'cover',
    borderRadius: '8px',
    border: '1px solid #273b5c'
  },
  actionRow: {
    display: 'flex',
    gap: '12px',
    marginTop: '6px'
  },
  actionBtn: {
    padding: '6px 12px',
    fontSize: '12px',
    borderRadius: '8px'
  }
};

export default MemoriesView;
