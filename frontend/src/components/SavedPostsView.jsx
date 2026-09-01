import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Bookmark, Folder, Trash } from 'lucide-react';

const SavedPostsView = () => {
  const { apiFetch } = useAuth();
  const [savedItems, setSavedItems] = useState([]);
  const [activeCategory, setActiveCategory] = useState('All');
  const [categories, setCategories] = useState(['All']);

  useEffect(() => {
    loadSavedPosts();
  }, [activeCategory]);

  const loadSavedPosts = async () => {
    try {
      const url = activeCategory === 'All' ? '/api/posts/saved' : `/api/posts/saved?category=${encodeURIComponent(activeCategory)}`;
      const list = await apiFetch(url);
      setSavedItems(list);

      // Extract unique categories for filter list from all saved items
      if (activeCategory === 'All') {
        const cats = new Set(list.map(item => item.categoryName));
        setCategories(['All', ...Array.from(cats)]);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleUnsave = async (postId) => {
    try {
      await apiFetch(`/api/posts/${postId}/unsave`, { method: 'POST' });
      setSavedItems(prev => prev.filter(item => item.post.id !== postId));
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div style={styles.container} className="glass-card">
      <div style={styles.header}>
        <Bookmark size={24} style={{ color: '#3b82f6' }} />
        <h3>Saved Posts</h3>
      </div>

      {/* Category Folders Filter */}
      <div style={styles.foldersTray}>
        {categories.map(cat => (
          <div 
            key={cat} 
            style={activeCategory === cat ? styles.folderItemActive : styles.folderItem}
            onClick={() => setActiveCategory(cat)}
            className="pointer"
          >
            <Folder size={16} />
            <span>{cat}</span>
          </div>
        ))}
      </div>

      {/* Saved list items */}
      <div style={styles.list}>
        {savedItems.length === 0 ? (
          <div style={styles.emptyText}>No saved posts in this folder.</div>
        ) : (
          savedItems.map(item => {
            const p = item.post;
            return (
              <div key={item.id} style={styles.postCard}>
                <div style={styles.postCardHeader}>
                  <div style={styles.authorRow}>
                    <span>👥</span>
                    <div>
                      <div style={styles.authorName}>Saved from Post #{p.id}</div>
                      <span style={styles.timeText}>Saved on {new Date(item.savedAt).toLocaleDateString()}</span>
                    </div>
                  </div>
                  <button 
                    onClick={() => handleUnsave(p.id)} 
                    className="btn btn-secondary" 
                    style={styles.unsaveBtn}
                  >
                    <Trash size={14} /> Unsave
                  </button>
                </div>
                <div style={styles.postFolderBadge}>
                  📁 Folder: {item.categoryName}
                </div>
                <p style={styles.postContent}>{p.content}</p>
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
  foldersTray: {
    display: 'flex',
    gap: '12px',
    overflowX: 'auto',
    paddingBottom: '6px'
  },
  folderItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    padding: '6px 12px',
    borderRadius: '16px',
    border: '1px solid #273b5c',
    fontSize: '12.5px',
    color: '#9ca3af',
    transition: '0.2s'
  },
  folderItemActive: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    padding: '6px 12px',
    borderRadius: '16px',
    border: '1px solid #3b82f6',
    backgroundColor: 'rgba(59, 130, 246, 0.08)',
    fontSize: '12.5px',
    color: '#3b82f6',
    fontWeight: '600'
  },
  list: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  emptyText: {
    color: '#9ca3af',
    fontSize: '13px',
    textAlign: 'center',
    padding: '40px 0'
  },
  postCard: {
    border: '1px solid #273b5c',
    borderRadius: '12px',
    padding: '16px',
    backgroundColor: 'rgba(11, 15, 25, 0.4)',
    display: 'flex',
    flexDirection: 'column',
    gap: '10px'
  },
  postCardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center'
  },
  authorRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px'
  },
  authorName: {
    fontSize: '13px',
    fontWeight: '600',
    color: '#f3f4f6'
  },
  timeText: {
    fontSize: '11px',
    color: '#6b7280'
  },
  unsaveBtn: {
    padding: '4px 10px',
    fontSize: '11px',
    borderRadius: '6px'
  },
  postFolderBadge: {
    fontSize: '11px',
    color: '#3b82f6',
    backgroundColor: 'rgba(59, 130, 246, 0.04)',
    padding: '4px 8px',
    borderRadius: '4px',
    alignSelf: 'flex-start'
  },
  postContent: {
    fontSize: '13.5px',
    color: '#d1d5db',
    lineHeight: '1.4'
  }
};

export default SavedPostsView;
