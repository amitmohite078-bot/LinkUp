import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { 
  Image, Smile, MapPin, Globe, Users, Lock, 
  MessageCircle, Share2, Bookmark, Heart, Trash, 
  Send, CornerDownRight 
} from 'lucide-react';
import { getAvatarUrl, getMediaUrl } from '../utils/media';

const Feed = ({ groupId }) => {
  const { user, apiFetch } = useAuth();
  const [posts, setPosts] = useState([]);
  const [rankingStrategy, setRankingStrategy] = useState('personalized');
  
  // Post Composer States
  const [content, setContent] = useState('');
  const [feeling, setFeeling] = useState('');
  const [location, setLocation] = useState('');
  const [privacy, setPrivacy] = useState('PUBLIC');
  const [files, setFiles] = useState([]);
  const [showFeelingsMenu, setShowFeelingsMenu] = useState(false);

  // Modal Dialog States
  const [activeReactionPostId, setActiveReactionPostId] = useState(null);
  const [detailedReactions, setDetailedReactions] = useState([]);
  const [showShareModal, setShowShareModal] = useState(null); // original post object
  const [shareMessage, setShareMessage] = useState('');
  const [sharePrivacy, setSharePrivacy] = useState('PUBLIC');

  // Comments / Replies Toggle
  const [activeCommentsPostId, setActiveCommentsPostId] = useState(null);
  const [commentsMap, setCommentsMap] = useState({}); // postId -> comments list
  const [commentInputs, setCommentInputs] = useState({}); // postId -> text
  const [replyInputs, setReplyInputs] = useState({}); // commentId -> text
  const [activeReplyBoxId, setActiveReplyBoxId] = useState(null); // commentId

  useEffect(() => {
    loadFeed();
  }, [groupId, rankingStrategy]);

  const loadFeed = async () => {
    try {
      let data;
      if (groupId) {
        // Group Feed
        data = await apiFetch(`/api/groups/${groupId}/posts`);
      } else {
        // News Feed
        data = await apiFetch(`/api/posts/feed?strategy=${rankingStrategy}`);
      }
      setPosts(data);
    } catch (e) {
      console.error(e);
    }
  };

  const handleCompose = async (e) => {
    e.preventDefault();
    if (!content.trim() && files.length === 0) return;

    try {
      const formData = new FormData();
      formData.append('content', content);
      if (feeling) formData.append('feelingActivity', feeling);
      if (location) formData.append('location', location);
      if (privacy) formData.append('privacy', privacy);
      if (groupId) formData.append('groupId', groupId.toString());
      
      for (let i = 0; i < files.length; i++) {
        formData.append('files', files[i]);
      }

      await apiFetch('/api/posts/create', {
        method: 'POST',
        body: formData
      });

      setContent('');
      setFeeling('');
      setLocation('');
      setFiles([]);
      loadFeed();
    } catch (e) {
      console.error(e);
    }
  };

  // Reactions Engine
  const handleReact = async (postId, type) => {
    try {
      const updatedPost = await apiFetch(`/api/posts/${postId}/react?reactionType=${type}`, {
        method: 'POST'
      });
      setPosts(prev => prev.map(p => p.id === postId ? updatedPost : p));
    } catch (e) {
      console.error(e);
    }
  };

  const openReactionDetails = async (postId) => {
    try {
      const detail = await apiFetch(`/api/posts/${postId}`);
      // Parse reactions list (usually mapped via controller logic)
      // For fallback if api doesn't return full reactive lists, we mock list based on reaction counts
      const res = [];
      if (detail.reactionCounts) {
        Object.entries(detail.reactionCounts).forEach(([type, count]) => {
          for (let i = 0; i < count; i++) {
            res.push({ username: `User_${i+1}`, type });
          }
        });
      }
      setDetailedReactions(res);
      setActiveReactionPostId(postId);
    } catch (e) {
      console.error(e);
    }
  };

  // Comments System
  const toggleComments = (postId) => {
    if (activeCommentsPostId === postId) {
      setActiveCommentsPostId(null);
    } else {
      setActiveCommentsPostId(postId);
      loadComments(postId);
    }
  };

  const loadComments = async (postId) => {
    try {
      const comments = await apiFetch(`/api/posts/${postId}/comments`);
      setCommentsMap(prev => ({ ...prev, [postId]: Array.isArray(comments) ? comments : [] }));
    } catch (e) {
      console.error(e);
    }
  };

  const submitComment = async (postId, parentId = null) => {
    const text = parentId ? replyInputs[parentId] : commentInputs[postId];
    if (!text || !text.trim()) return;

    try {
      let url = `/api/posts/${postId}/comment?content=${encodeURIComponent(text)}`;
      if (parentId) {
        url += `&parentCommentId=${parentId}`;
      }

      await apiFetch(url, { method: 'POST' });
      
      if (parentId) {
        setReplyInputs(prev => ({ ...prev, [parentId]: '' }));
        setActiveReplyBoxId(null);
      } else {
        setCommentInputs(prev => ({ ...prev, [postId]: '' }));
      }
      
      loadComments(postId);
      
      // Update comment count on post response locally
      setPosts(prev => prev.map(p => p.id === postId ? { ...p, commentCount: p.commentCount + 1 } : p));
    } catch (e) {
      console.error(e);
    }
  };

  // Sharing System
  const openShareModal = (post) => {
    setShowShareModal(post);
    setShareMessage('');
    setSharePrivacy('PUBLIC');
  };

  const handleShare = async () => {
    if (!showShareModal) return;
    try {
      await apiFetch(`/api/posts/${showShareModal.id}/share?content=${encodeURIComponent(shareMessage)}&privacy=${sharePrivacy}`, {
        method: 'POST'
      });
      setShowShareModal(null);
      loadFeed();
    } catch (e) {
      console.error(e);
    }
  };

  // Saved System
  const toggleSave = async (post) => {
    try {
      // Basic category folders: Saved Posts
      await apiFetch(`/api/posts/${post.id}/save?categoryName=Saved Posts`, { method: 'POST' });
      alert("Post saved successfully!");
    } catch (e) {
      console.error(e);
    }
  };

  // Delete Post
  const handleDeletePost = async (postId) => {
    if (!confirm('Are you sure you want to delete this post?')) return;
    try {
      await apiFetch(`/api/posts/${postId}`, { method: 'DELETE' });
      setPosts(prev => prev.filter(p => p.id !== postId));
    } catch (e) {
      console.error(e);
    }
  };

  const feelings = ['happy', 'excited', 'sad', 'motivated', 'blessed', 'tired'];

  return (
    <div style={styles.container}>
      {/* 1. Feed Strategy Toggle (Hide in group view) */}
      {!groupId && (
        <div style={styles.strategyRow} className="glass-card">
          <span style={styles.strategyText}>Feed Strategy:</span>
          <select 
            value={rankingStrategy} 
            onChange={(e) => setRankingStrategy(e.target.value)}
            style={styles.strategySelect}
            className="pointer"
          >
            <option value="personalized">⭐ Personalized (Hybrid)</option>
            <option value="chronological">📅 Chronological (Newest)</option>
            <option value="engagement">🔥 Engagement (Popular)</option>
            <option value="relationship">🤝 Relationship (Friends First)</option>
          </select>
        </div>
      )}

      {/* 2. Post Composer */}
      <div className="glass-card" style={styles.composer}>
        <div style={styles.composerHeader}>
          <img 
            src={getAvatarUrl(user.avatarUrl)} 
            className="avatar" 
            alt="" 
          />
          <textarea
            placeholder={groupId ? "Share something with the group..." : `What's on your mind, ${user.firstName}?`}
            style={styles.composerInput}
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />
        </div>

        {/* Dynamic add-ons preview */}
        {(feeling || location || files.length > 0) && (
          <div style={styles.previewContainer}>
            {feeling && <span style={styles.feelingBadge}>😊 Feeling {feeling}</span>}
            {location && <span style={styles.locationBadge}>📍 at {location}</span>}
            {files.length > 0 && <span style={styles.fileBadge}>📎 {files.length} file(s) attached</span>}
          </div>
        )}

        <div style={styles.composerFooter}>
          <div style={styles.composerOptions}>
            {/* Image upload icon */}
            <label style={styles.optionBtn} className="pointer">
              <Image size={18} color="#10b981" />
              <span>Photo/Video</span>
              <input
                type="file"
                multiple
                accept="image/*,video/*"
                onChange={(e) => setFiles(Array.from(e.target.files))}
                style={{ display: 'none' }}
              />
            </label>

            {/* Feelings trigger */}
            <div style={styles.optionBtnRelative}>
              <div style={styles.optionBtn} className="pointer" onClick={() => setShowFeelingsMenu(!showFeelingsMenu)}>
                <Smile size={18} color="#eab308" />
                <span>Feeling</span>
              </div>
              {showFeelingsMenu && (
                <div style={styles.feelingsDropdown} className="glass-card">
                  {feelings.map(f => (
                    <div 
                      key={f} 
                      style={styles.feelingItem} 
                      className="pointer"
                      onClick={() => {
                        setFeeling(f);
                        setShowFeelingsMenu(false);
                      }}
                    >
                      {f}
                    </div>
                  ))}
                  <div style={styles.feelingItem} className="pointer" onClick={() => { setFeeling(''); setShowFeelingsMenu(false); }}>Clear</div>
                </div>
              )}
            </div>

            {/* Location input dialog */}
            <div style={styles.optionBtn} className="pointer" onClick={() => {
              const loc = prompt("Enter location name:");
              if (loc !== null) setLocation(loc);
            }}>
              <MapPin size={18} color="#ef4444" />
              <span>Location</span>
            </div>
          </div>

          <div style={styles.composerSubmitRow}>
            {/* Privacy Select */}
            <select 
              value={privacy} 
              onChange={(e) => setPrivacy(e.target.value)} 
              style={styles.privacySelect}
              className="pointer"
            >
              <option value="PUBLIC">🌎 Public</option>
              <option value="FRIENDS">👥 Friends</option>
              <option value="ONLY_ME">🔒 Only me</option>
            </select>

            <button onClick={handleCompose} className="btn btn-primary" style={styles.postBtn}>
              Post
            </button>
          </div>
        </div>
      </div>

      {/* 3. Feed Posts List */}
      <div style={styles.postsList}>
        {posts.length === 0 ? (
          <div style={styles.emptyFeed} className="glass-card">
            <h3>No Posts to Show</h3>
            <p>Add some friends, join a group, or change your strategy to discover posts.</p>
          </div>
        ) : (
          posts.map(post => {
            const isAuthor = post.userId === user.id;
            const hasReactions = post.reactionCounts && Object.keys(post.reactionCounts).length > 0;
            const totalReactionsCount = post.reactionCounts 
                ? Object.values(post.reactionCounts).reduce((acc, curr) => acc + curr, 0)
                : 0;

            return (
              <div key={post.id} className="glass-card" style={styles.postCard}>
                {/* Header info */}
                <div style={styles.postHeader}>
                  <img 
                    src={getAvatarUrl(post.avatarUrl)} 
                    className="avatar" 
                    alt="" 
                  />
                  <div style={styles.postAuthorInfo}>
                    <div style={styles.postAuthorName}>
                      {post.firstName} {post.lastName}
                      {post.feelingActivity && (
                        <span style={styles.authorFeeling}> is feeling {post.feelingActivity}</span>
                      )}
                      {post.location && (
                        <span style={styles.authorLoc}> at {post.location}</span>
                      )}
                    </div>
                    <div style={styles.postMeta}>
                      <span>{new Date(post.createdAt).toLocaleDateString()}</span>
                      <span style={{ margin: '0 4px' }}>•</span>
                      {post.privacy === 'PUBLIC' && <Globe size={12} />}
                      {post.privacy === 'FRIENDS' && <Users size={12} />}
                      {post.privacy === 'ONLY_ME' && <Lock size={12} />}
                    </div>
                  </div>

                  {isAuthor && (
                    <Trash 
                      size={16} 
                      style={styles.deleteIcon} 
                      className="pointer" 
                      onClick={() => handleDeletePost(post.id)} 
                    />
                  )}
                </div>

                {/* Content body */}
                <p style={styles.postContent}>{post.content}</p>

                {/* Media grid attachments */}
                {post.mediaUrls && post.mediaUrls.length > 0 && (
                  <div style={styles.mediaContainer}>
                    {post.mediaUrls.map((url, i) => (
                      <div key={i} style={styles.mediaItem}>
                        {post.type === 'VIDEO' ? (
                          <video src={getMediaUrl(url)} controls style={styles.postMedia} />
                        ) : (
                          <img src={getMediaUrl(url)} alt="" style={styles.postMedia} />
                        )}
                      </div>
                    ))}
                  </div>
                )}

                {/* Render shared post card if type = SHARE */}
                {post.type === 'SHARE' && post.originalPost && (
                  <div style={styles.sharedPostCard}>
                    <div style={styles.postHeader}>
                      <img 
                        src={getAvatarUrl(post.originalPost.avatarUrl)} 
                        className="avatar" 
                        style={{ width: '32px', height: '32px' }} 
                        alt="" 
                      />
                      <div>
                        <div style={styles.sharedAuthorName}>{post.originalPost.firstName} {post.originalPost.lastName}</div>
                        <div style={styles.postMeta}>{new Date(post.originalPost.createdAt).toLocaleDateString()}</div>
                      </div>
                    </div>
                    <p style={styles.sharedContent}>{post.originalPost.content}</p>
                    {post.originalPost.mediaUrls && post.originalPost.mediaUrls.length > 0 && (
                      <img src={getMediaUrl(post.originalPost.mediaUrls[0])} alt="" style={styles.sharedMedia} />
                    )}
                  </div>
                )}

                {/* Interaction summary bar */}
                <div style={styles.interactionSummary}>
                  <div 
                    style={styles.summaryLeft} 
                    className="pointer"
                    onClick={() => openReactionDetails(post.id)}
                  >
                    {hasReactions ? (
                      <>
                        <span>👍❤️😂 {totalReactionsCount} reactions</span>
                      </>
                    ) : (
                      <span>No reactions</span>
                    )}
                  </div>
                  <div style={styles.summaryRight}>
                    <span className="pointer" onClick={() => toggleComments(post.id)}>
                      {post.commentCount} comments
                    </span>
                  </div>
                </div>

                {/* Action buttons */}
                <div style={styles.actionRow}>
                  {/* Reactions Hover overlay */}
                  <div style={styles.reactionBtnContainer}>
                    <div style={styles.actionBtn} className="pointer">
                      <Heart size={18} color={post.userReaction ? '#ef4444' : '#6b7280'} />
                      <span style={post.userReaction ? { color: '#ef4444', fontWeight: 'bold' } : {}}>
                        {post.userReaction || 'React'}
                      </span>
                    </div>
                    <div style={styles.reactionsHoverBox} className="glass-card">
                      <span className="pointer" style={styles.reactionEmoji} onClick={() => handleReact(post.id, 'LIKE')}>👍</span>
                      <span className="pointer" style={styles.reactionEmoji} onClick={() => handleReact(post.id, 'LOVE')}>❤️</span>
                      <span className="pointer" style={styles.reactionEmoji} onClick={() => handleReact(post.id, 'HAHA')}>😂</span>
                      <span className="pointer" style={styles.reactionEmoji} onClick={() => handleReact(post.id, 'WOW')}>😮</span>
                      <span className="pointer" style={styles.reactionEmoji} onClick={() => handleReact(post.id, 'SAD')}>😢</span>
                      <span className="pointer" style={styles.reactionEmoji} onClick={() => handleReact(post.id, 'ANGRY')}>😡</span>
                    </div>
                  </div>

                  <div style={styles.actionBtn} className="pointer" onClick={() => toggleComments(post.id)}>
                    <MessageCircle size={18} />
                    <span>Comment</span>
                  </div>

                  <div style={styles.actionBtn} className="pointer" onClick={() => openShareModal(post)}>
                    <Share2 size={18} />
                    <span>Share</span>
                  </div>

                  <div style={styles.actionBtn} className="pointer" onClick={() => toggleSave(post)}>
                    <Bookmark size={18} />
                    <span>Save</span>
                  </div>
                </div>

                {/* 4. Comments Drawer */}
                {activeCommentsPostId === post.id && (
                  <div style={styles.commentsDrawer}>
                    {/* Add Root Comment */}
                    <div style={styles.commentInputRow}>
                      <img 
                        src={getAvatarUrl(user.avatarUrl)} 
                        className="avatar" 
                        style={{ width: '32px', height: '32px' }} 
                        alt="" 
                      />
                      <input
                        type="text"
                        placeholder="Write a comment..."
                        className="input-field"
                        style={{ borderRadius: '20px' }}
                        value={commentInputs[post.id] || ''}
                        onChange={(e) => setCommentInputs({ ...commentInputs, [post.id]: e.target.value })}
                        onKeyDown={(e) => e.key === 'Enter' && submitComment(post.id)}
                      />
                      <Send size={18} className="pointer" color="#3b82f6" onClick={() => submitComment(post.id)} />
                    </div>

                    {/* Comments List */}
                    <div style={styles.commentsList}>
                      {(!commentsMap[post.id] || commentsMap[post.id].length === 0) ? (
                        <div style={styles.emptyComments}>No comments yet. Be the first to comment!</div>
                      ) : (
                        commentsMap[post.id].map(comment => (
                          <div key={comment.id} style={styles.commentItem}>
                            <img 
                              src={getAvatarUrl(comment.user.avatarUrl)} 
                              className="avatar" 
                              style={{ width: '28px', height: '28px', marginTop: '4px' }} 
                              alt="" 
                            />
                            <div style={{ flex: 1 }}>
                              <div style={styles.commentBubble}>
                                <div style={styles.commentAuthorName}>{comment.user.firstName} {comment.user.lastName}</div>
                                <div style={styles.commentContent}>{comment.content}</div>
                              </div>
                              
                              {/* Comment Actions (React/Reply) */}
                              <div style={styles.commentActionsRow}>
                                <span className="pointer" style={styles.commentActionBtn} onClick={() => setActiveReplyBoxId(comment.id)}>
                                  Reply
                                </span>
                                <span style={styles.commentTime}>
                                  {new Date(comment.createdAt).toLocaleDateString()}
                                </span>
                              </div>

                              {/* Nested replies trigger */}
                              {activeReplyBoxId === comment.id && (
                                <div style={styles.replyInputRow}>
                                  <CornerDownRight size={16} color="#6b7280" />
                                  <input
                                    type="text"
                                    placeholder="Write a reply..."
                                    className="input-field"
                                    style={{ borderRadius: '20px', padding: '6px 12px' }}
                                    value={replyInputs[comment.id] || ''}
                                    onChange={(e) => setReplyInputs({ ...replyInputs, [comment.id]: e.target.value })}
                                    onKeyDown={(e) => e.key === 'Enter' && submitComment(post.id, comment.id)}
                                    autoFocus
                                  />
                                  <Send size={16} className="pointer" color="#3b82f6" onClick={() => submitComment(post.id, comment.id)} />
                                </div>
                              )}
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      {/* Share Modal popup */}
      {showShareModal && (
        <div style={styles.modalOverlay} className="flex-center">
          <div style={styles.shareModal} className="glass-card">
            <div style={styles.modalHeader}>
              <h3>Share Post</h3>
              <Trash size={20} className="pointer" onClick={() => setShowShareModal(null)} />
            </div>
            <div style={styles.shareBody}>
              <textarea
                placeholder="Say something about this shared post..."
                className="input-field"
                style={{ height: '80px', marginBottom: '14px', resize: 'none' }}
                value={shareMessage}
                onChange={(e) => setShareMessage(e.target.value)}
              />
              <div style={styles.sharePrivacyRow}>
                <Globe size={18} color="#9ca3af" />
                <select 
                  value={sharePrivacy} 
                  onChange={(e) => setSharePrivacy(e.target.value)}
                  style={styles.strategySelect}
                >
                  <option value="PUBLIC">Public</option>
                  <option value="FRIENDS">Friends</option>
                  <option value="ONLY_ME">Only me</option>
                </select>
              </div>

              {/* Shared card preview */}
              <div style={styles.sharedPostCard}>
                <div style={styles.postHeader}>
                  <img src={getAvatarUrl(showShareModal.avatarUrl)} className="avatar" style={{ width: '28px', height: '28px' }} alt="" />
                  <div>
                    <div style={styles.sharedAuthorName}>{showShareModal.firstName} {showShareModal.lastName}</div>
                    <div style={styles.postMeta}>{new Date(showShareModal.createdAt).toLocaleDateString()}</div>
                  </div>
                </div>
                <p style={styles.sharedContent}>{showShareModal.content}</p>
              </div>

              <div style={styles.shareActions}>
                <button className="btn btn-secondary" onClick={() => setShowShareModal(null)}>Cancel</button>
                <button className="btn btn-primary" onClick={handleShare}>Share Now</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Reaction Details Modal */}
      {activeReactionPostId && (
        <div style={styles.modalOverlay} className="flex-center">
          <div style={styles.reactionModal} className="glass-card">
            <div style={styles.modalHeader}>
              <h3>Post Reactions</h3>
              <Trash size={20} className="pointer" onClick={() => setActiveReactionPostId(null)} />
            </div>
            <div style={styles.reactionListContainer}>
              {detailedReactions.length === 0 ? (
                <div style={styles.emptyViewers}>No reactions loaded.</div>
              ) : (
                detailedReactions.map((r, i) => (
                  <div key={i} style={styles.reactionListItem}>
                    <span style={{ fontSize: '20px' }}>{
                      r.type === 'LOVE' ? '❤️' : 
                      r.type === 'HAHA' ? '😂' : 
                      r.type === 'WOW' ? '😮' : 
                      r.type === 'SAD' ? '😢' : 
                      r.type === 'ANGRY' ? '😡' : '👍'
                    }</span>
                    <span style={{ color: '#f3f4f6' }}>{r.username}</span>
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
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
    width: '100%'
  },
  strategyRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    padding: '12px 20px',
    justifyContent: 'flex-start'
  },
  strategyText: {
    fontSize: '13px',
    color: '#9ca3af',
    fontWeight: '600'
  },
  strategySelect: {
    backgroundColor: '#0b0f19',
    color: '#f3f4f6',
    border: '1px solid #273b5c',
    borderRadius: '8px',
    padding: '6px 12px',
    outline: 'none',
    fontSize: '13px'
  },
  composer: {
    padding: '20px',
    display: 'flex',
    flexDirection: 'column',
    gap: '14px'
  },
  composerHeader: {
    display: 'flex',
    gap: '12px',
    alignItems: 'flex-start'
  },
  composerInput: {
    flex: 1,
    height: '60px',
    backgroundColor: 'transparent',
    border: 'none',
    outline: 'none',
    color: '#f3f4f6',
    fontFamily: 'inherit',
    fontSize: '15px',
    resize: 'none'
  },
  previewContainer: {
    display: 'flex',
    gap: '8px',
    flexWrap: 'wrap',
    padding: '4px 0'
  },
  feelingBadge: {
    backgroundColor: 'rgba(234, 179, 8, 0.12)',
    color: '#eab308',
    padding: '4px 10px',
    borderRadius: '16px',
    fontSize: '11px',
    fontWeight: '600'
  },
  locationBadge: {
    backgroundColor: 'rgba(239, 68, 68, 0.12)',
    color: '#ef4444',
    padding: '4px 10px',
    borderRadius: '16px',
    fontSize: '11px',
    fontWeight: '600'
  },
  fileBadge: {
    backgroundColor: 'rgba(16, 185, 129, 0.12)',
    color: '#10b981',
    padding: '4px 10px',
    borderRadius: '16px',
    fontSize: '11px',
    fontWeight: '600'
  },
  composerFooter: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTop: '1px solid #273b5c',
    paddingTop: '12px',
    flexWrap: 'wrap',
    gap: '12px'
  },
  composerOptions: {
    display: 'flex',
    gap: '14px',
    alignItems: 'center'
  },
  optionBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    fontSize: '12.5px',
    color: '#9ca3af',
    padding: '6px 10px',
    borderRadius: '8px',
    transition: '0.2s',
    '&:hover': {
      backgroundColor: '#1f2d47'
    }
  },
  optionBtnRelative: {
    position: 'relative'
  },
  feelingsDropdown: {
    position: 'absolute',
    top: '36px',
    left: 0,
    width: '120px',
    maxHeight: '180px',
    overflowY: 'auto',
    padding: '6px',
    display: 'flex',
    flexDirection: 'column',
    gap: '4px',
    zIndex: 10
  },
  feelingItem: {
    fontSize: '12px',
    padding: '6px',
    borderRadius: '6px',
    '&:hover': {
      backgroundColor: '#1f2d47'
    }
  },
  composerSubmitRow: {
    display: 'flex',
    gap: '10px',
    alignItems: 'center'
  },
  privacySelect: {
    backgroundColor: '#0b0f19',
    color: '#9ca3af',
    border: '1px solid #273b5c',
    borderRadius: '8px',
    padding: '6px',
    outline: 'none',
    fontSize: '12px'
  },
  postBtn: {
    padding: '6px 20px',
    borderRadius: '20px'
  },
  postsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  emptyFeed: {
    textAlign: 'center',
    padding: '40px 20px',
    color: '#9ca3af'
  },
  postCard: {
    display: 'flex',
    flexDirection: 'column',
    gap: '14px'
  },
  postHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    position: 'relative'
  },
  postAuthorInfo: {
    display: 'flex',
    flexDirection: 'column'
  },
  postAuthorName: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#f3f4f6'
  },
  authorFeeling: {
    color: '#9ca3af',
    fontWeight: 'normal',
    fontSize: '13px'
  },
  authorLoc: {
    color: '#9ca3af',
    fontWeight: 'normal',
    fontSize: '13px'
  },
  postMeta: {
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
    fontSize: '11px',
    color: '#6b7280'
  },
  deleteIcon: {
    position: 'absolute',
    top: '4px',
    right: '4px',
    color: '#6b7280',
    '&:hover': {
      color: '#ef4444'
    }
  },
  postContent: {
    fontSize: '14.5px',
    color: '#f3f4f6',
    whiteSpace: 'pre-wrap',
    lineHeight: '1.4'
  },
  mediaContainer: {
    width: '100%',
    borderRadius: '12px',
    overflow: 'hidden',
    border: '1px solid #273b5c',
    backgroundColor: '#0b0f19'
  },
  mediaItem: {
    width: '100%',
    maxHeight: '400px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center'
  },
  postMedia: {
    maxWidth: '100%',
    maxHeight: '400px',
    objectFit: 'contain'
  },
  sharedPostCard: {
    border: '1px solid #273b5c',
    borderRadius: '12px',
    padding: '14px',
    backgroundColor: 'rgba(11, 15, 25, 0.4)',
    display: 'flex',
    flexDirection: 'column',
    gap: '10px'
  },
  sharedAuthorName: {
    fontSize: '13px',
    fontWeight: '600',
    color: '#f3f4f6'
  },
  sharedContent: {
    fontSize: '13.5px',
    color: '#d1d5db'
  },
  sharedMedia: {
    width: '100%',
    maxHeight: '260px',
    objectFit: 'cover',
    borderRadius: '8px'
  },
  interactionSummary: {
    display: 'flex',
    justifyContent: 'space-between',
    fontSize: '12px',
    color: '#9ca3af',
    borderBottom: '1px solid #273b5c',
    paddingBottom: '10px',
    marginTop: '6px'
  },
  actionRow: {
    display: 'flex',
    justifyContent: 'space-between',
    padding: '2px 0'
  },
  reactionBtnContainer: {
    position: 'relative',
    '&:hover .reactions-hover-box': {
      display: 'flex'
    }
  },
  actionBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    color: '#9ca3af',
    fontSize: '13px',
    padding: '6px 12px',
    borderRadius: '8px',
    transition: '0.2s',
    '&:hover': {
      backgroundColor: '#1f2d47',
      color: '#f3f4f6'
    }
  },
  reactionsHoverBox: {
    display: 'none', // Controlled by hover container
    position: 'absolute',
    bottom: '36px',
    left: '0',
    padding: '8px 12px',
    borderRadius: '30px',
    gap: '10px',
    zIndex: 15,
    boxShadow: '0 10px 25px rgba(0,0,0,0.5)'
  },
  reactionEmoji: {
    fontSize: '22px',
    transition: '0.15s',
    display: 'inline-block',
    '&:hover': {
      transform: 'scale(1.3) translateY(-4px)'
    }
  },
  commentsDrawer: {
    borderTop: '1px solid #273b5c',
    paddingTop: '14px',
    display: 'flex',
    flexDirection: 'column',
    gap: '14px'
  },
  commentInputRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px'
  },
  commentsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
    maxHeight: '350px',
    overflowY: 'auto'
  },
  emptyComments: {
    fontSize: '12px',
    color: '#9ca3af',
    textAlign: 'center',
    padding: '10px 0'
  },
  commentItem: {
    display: 'flex',
    gap: '10px',
    alignItems: 'flex-start'
  },
  commentBubble: {
    backgroundColor: '#1f2d47',
    padding: '8px 12px',
    borderRadius: '16px',
    display: 'inline-flex',
    flexDirection: 'column',
    gap: '2px',
    maxWidth: '85%'
  },
  commentAuthorName: {
    fontSize: '12px',
    fontWeight: '700',
    color: '#f3f4f6'
  },
  commentContent: {
    fontSize: '13px',
    color: '#d1d5db',
    lineHeight: '1.3'
  },
  commentActionsRow: {
    display: 'flex',
    gap: '12px',
    fontSize: '11px',
    color: '#9ca3af',
    paddingLeft: '10px',
    marginTop: '2px'
  },
  commentActionBtn: {
    '&:hover': {
      color: '#3b82f6'
    }
  },
  commentTime: {
    color: '#6b7280'
  },
  replyInputRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    marginTop: '6px',
    paddingLeft: '10px'
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
  shareModal: {
    width: '90%',
    maxWidth: '480px',
    padding: '24px'
  },
  modalHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottom: '1px solid #273b5c',
    paddingBottom: '10px',
    marginBottom: '16px'
  },
  shareBody: {
    display: 'flex',
    flexDirection: 'column'
  },
  sharePrivacyRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    marginBottom: '16px'
  },
  shareActions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '12px',
    marginTop: '18px'
  },
  reactionModal: {
    width: '90%',
    maxWidth: '360px',
    padding: '20px'
  },
  reactionListContainer: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
    maxHeight: '260px',
    overflowY: 'auto'
  },
  reactionListItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    padding: '6px 0'
  }
};

// Global styles injector for CSS reaction boxes
if (typeof document !== 'undefined') {
  const style = document.createElement('style');
  style.innerHTML = `
    .reactions-hover-box {
      display: none;
    }
  `;
  document.head.appendChild(style);
}

export default Feed;
