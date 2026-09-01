import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { useWebSocket } from '../context/WebSocketContext';
import { 
  Send, Image, Search, Users, 
  CornerUpLeft, Eye, X, MessageSquare 
} from 'lucide-react';
import { getAvatarUrl, getMediaUrl } from '../utils/media';

const Messenger = () => {
  const { user, apiFetch } = useAuth();
  const { subscribeChannel, unsubscribeChannel, sendPayload } = useWebSocket();

  // Chat Rooms Lists
  const [rooms, setRooms] = useState([]);
  const [activeRoom, setActiveRoom] = useState(null); // ChatRoomResponse
  const [messages, setMessages] = useState([]);
  
  // Input/Compose States
  const [text, setText] = useState('');
  const [typingUser, setTypingUser] = useState(null);
  const [replyingTo, setReplyingTo] = useState(null); // MessageResponse
  
  // Search state
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);

  // Create Group Chat Dialog
  const [showCreateGroup, setShowCreateGroup] = useState(false);
  const [groupName, setGroupName] = useState('');
  const [friendsList, setFriendsList] = useState([]);
  const [selectedFriendIds, setSelectedFriendIds] = useState([]);

  const messagesEndRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  // Load Rooms list at start
  useEffect(() => {
    loadRooms();
    loadFriends();
  }, []);

  // Set up dynamic WebSocket topic listener when active room changes
  useEffect(() => {
    if (!activeRoom) return;

    // 1. Fetch history
    loadHistory(activeRoom.id);

    // 2. Mark as read
    markRoomAsRead(activeRoom.id);

    // 3. Subscribe to room messages
    subscribeChannel(`/topic/chat/${activeRoom.id}`, (msg) => {
      setMessages(prev => {
        // Prevent duplicate logs
        if (prev.some(m => m.id === msg.id)) return prev;
        return [...prev, msg];
      });
      // Scroll to bottom
      scrollToBottom();
      // Auto mark read if this room is still open
      markRoomAsRead(activeRoom.id);
      loadRooms(); // Refresh snippet
    });

    // 4. Subscribe to typing indicators
    subscribeChannel(`/topic/chat/${activeRoom.id}/typing`, (indicator) => {
      if (indicator.userId === user.id) return;
      if (indicator.isTyping) {
        setTypingUser(indicator.username);
      } else {
        setTypingUser(null);
      }
    });

    // 5. Subscribe to read receipts
    subscribeChannel(`/topic/chat/${activeRoom.id}/read`, (readData) => {
      if (readData.readerId === user.id) return;
      setMessages(prev => prev.map(m => m.senderId === user.id ? { ...m, isRead: true } : m));
    });

    // 6. Subscribe to reaction updates
    subscribeChannel(`/topic/chat/${activeRoom.id}/reactions`, (reactionData) => {
      setMessages(prev => prev.map(m => m.id === reactionData.messageId 
        ? { ...m, reactions: reactionData.reactions } 
        : m
      ));
    });

    return () => {
      // Unsubscribe all
      unsubscribeChannel(`/topic/chat/${activeRoom.id}`);
      unsubscribeChannel(`/topic/chat/${activeRoom.id}/typing`);
      unsubscribeChannel(`/topic/chat/${activeRoom.id}/read`);
      unsubscribeChannel(`/topic/chat/${activeRoom.id}/reactions`);
      setTypingUser(null);
    };
  }, [activeRoom]);

  useEffect(() => {
    scrollToBottom();
  }, [messages, typingUser]);

  const loadRooms = async () => {
    try {
      const list = await apiFetch('/api/chat/rooms');
      setRooms(list);
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

  const loadHistory = async (roomId) => {
    try {
      const history = await apiFetch(`/api/chat/history/${roomId}`);
      setMessages(history);
    } catch (e) {
      console.error(e);
    }
  };

  const markRoomAsRead = async (roomId) => {
    try {
      await apiFetch(`/api/chat/read/${roomId}`, { method: 'POST' });
    } catch (e) {
      console.error(e);
    }
  };

  const handleSendMessage = () => {
    if (!text.trim()) return;

    // Send via STOMP payload
    sendPayload('/app/chat.sendMessage', {
      roomId: activeRoom.id,
      senderId: user.id,
      content: text,
      type: 'TEXT',
      parentMessageId: replyingTo ? replyingTo.id : null
    });

    setText('');
    setReplyingTo(null);
    stopTyping();
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      // Re-use file uploader logic of PostComposer to get an asset URL
      const formData = new FormData();
      formData.append('files', file);
      formData.append('content', 'Messenger Upload');

      const postResponse = await apiFetch('/api/posts/create', {
        method: 'POST',
        body: formData
      });

      const fileUrl = postResponse.mediaUrls && postResponse.mediaUrls[0];
      if (fileUrl) {
        sendPayload('/app/chat.sendMessage', {
          roomId: activeRoom.id,
          senderId: user.id,
          content: fileUrl,
          type: file.type.startsWith('image') ? 'IMAGE' : 'FILE',
          parentMessageId: replyingTo ? replyingTo.id : null
        });
        setReplyingTo(null);
      }
    } catch (err) {
      console.error("Failed to upload messenger attachment:", err);
    }
  };

  // Typing state machine
  const onTextInputChange = (val) => {
    setText(val);
    
    // Publish typing event if not already typing
    sendPayload('/app/chat.typing', {
      roomId: activeRoom.id,
      userId: user.id,
      isTyping: true
    });

    if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
    
    typingTimeoutRef.current = setTimeout(() => {
      stopTyping();
    }, 2000);
  };

  const stopTyping = () => {
    sendPayload('/app/chat.typing', {
      roomId: activeRoom.id,
      userId: user.id,
      isTyping: false
    });
  };

  const handleReact = (messageId, reactionType) => {
    // Send message reaction via API rest route (triggers WebSocket broadcast)
    apiFetch(`/api/chat/history/${messageId}/react?reactionType=${reactionType}`, {
      method: 'POST'
    }).catch(console.error);
  };

  // Group creation logic
  const handleCreateGroup = async () => {
    if (!groupName.trim() || selectedFriendIds.length === 0) return;

    try {
      // Call create group room REST endpoint
      const memberList = [user.id, ...selectedFriendIds];
      const room = await apiFetch(`/api/chat/room/group?name=${encodeURIComponent(groupName)}&memberIds=${memberList.join(',')}`, {
        method: 'POST'
      });
      
      setShowCreateGroup(false);
      setGroupName('');
      setSelectedFriendIds([]);
      
      loadRooms();
      setActiveRoom({
        id: room.id,
        name: room.name,
        type: 'GROUP',
        unreadCount: 0
      });
    } catch (e) {
      console.error(e);
    }
  };

  const toggleSelectFriend = (friendId) => {
    setSelectedFriendIds(prev => 
      prev.includes(friendId) ? prev.filter(id => id !== friendId) : [...prev, friendId]
    );
  };

  const handleSearchMessages = async () => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }
    try {
      const list = await apiFetch(`/api/chat/search/${activeRoom.id}?query=${searchQuery}`);
      setSearchResults(list);
    } catch (e) {
      console.error(e);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <div style={styles.container} className="glass-card">
      {/* Left Conversations Sidebar */}
      <div style={styles.sidebar}>
        <div style={styles.sidebarHeader}>
          <h3>Chats</h3>
          <div 
            style={styles.createGroupBtn} 
            className="pointer" 
            onClick={() => setShowCreateGroup(true)}
            title="Create Group Chat"
          >
            <Users size={18} />
          </div>
        </div>

        <div style={styles.roomsList}>
          {rooms.length === 0 ? (
            <div style={styles.emptyChats}>No active chats. Search for people and click "Message" to start.</div>
          ) : (
            rooms.map(room => {
              const isActive = activeRoom && activeRoom.id === room.id;
              return (
                <div 
                  key={room.id} 
                  style={isActive ? styles.roomItemActive : styles.roomItem}
                  onClick={() => setActiveRoom(room)}
                >
                  <img 
                    src={room.avatarUrl || 'https://cdn-icons-png.flaticon.com/512/166/166258.png'} 
                    alt="" 
                    className="avatar" 
                    style={{ width: '38px', height: '38px', border: 'none' }}
                  />
                  <div style={styles.roomInfo}>
                    <div style={styles.roomName}>{room.name}</div>
                    <div style={styles.roomSnippet}>{room.lastMessage}</div>
                  </div>
                  {room.unreadCount > 0 && (
                    <span style={styles.unreadBadge}>{room.unreadCount}</span>
                  )}
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* Center Chat Box Area */}
      <div style={styles.chatArea}>
        {activeRoom ? (
          <>
            {/* Header info */}
            <div style={styles.chatHeader}>
              <div style={styles.chatHeaderLeft}>
                <img 
                  src={activeRoom.avatarUrl || 'https://cdn-icons-png.flaticon.com/512/166/166258.png'} 
                  alt="" 
                  className="avatar"
                />
                <div>
                  <h4 style={{ color: '#f3f4f6' }}>{activeRoom.name}</h4>
                  <span style={styles.headerStatus}>
                    {activeRoom.type === 'ONE_TO_ONE' ? 'Direct Message' : 'Group Discussion'}
                  </span>
                </div>
              </div>
              <div style={styles.chatHeaderRight}>
                <Search size={18} className="pointer" onClick={() => setSearchOpen(!searchOpen)} />
              </div>
            </div>

            {/* Message Search Sidebar Overlay */}
            {searchOpen && (
              <div style={styles.searchOverlay} className="glass-card">
                <div style={styles.searchHeader}>
                  <h4>Search Messages</h4>
                  <X size={18} className="pointer" onClick={() => { setSearchOpen(false); setSearchQuery(''); setSearchResults([]); }} />
                </div>
                <div style={styles.searchForm}>
                  <input
                    type="text"
                    placeholder="Search query..."
                    className="input-field"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                  <button className="btn btn-primary" onClick={handleSearchMessages}>Find</button>
                </div>
                <div style={styles.searchResultsList}>
                  {searchResults.map(msg => (
                    <div key={msg.id} style={styles.searchResultItem}>
                      <span style={styles.resultSender}>{msg.senderName}:</span>
                      <p style={styles.resultText}>{msg.content}</p>
                      <span style={styles.resultTime}>{new Date(msg.createdAt).toLocaleTimeString()}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Message history pane */}
            <div style={styles.messagesContainer}>
              {messages.map(msg => {
                const isMyMessage = msg.senderId === user.id;
                const hasReactions = msg.reactions && msg.reactions.length > 0;
                
                return (
                  <div 
                    key={msg.id} 
                    style={isMyMessage ? styles.myMessageRow : styles.friendMessageRow}
                  >
                    {!isMyMessage && (
                      <img 
                        src={msg.senderAvatar || 'https://cdn-icons-png.flaticon.com/512/149/149071.png'} 
                        alt="" 
                        className="avatar" 
                        style={{ width: '28px', height: '28px', alignSelf: 'flex-end', border: 'none' }}
                      />
                    )}
                    
                    <div style={styles.messageBubbleWrapper}>
                      {/* Replying indicator card above bubble */}
                      {msg.parentMessageContent && (
                        <div style={styles.msgReplyCard}>
                          <CornerUpLeft size={10} />
                          <span>{msg.parentMessageContent}</span>
                        </div>
                      )}

                      <div style={styles.bubbleContainer}>
                        {/* Emoji Reactions Box on hover */}
                        <div className="message-hover-reactions" style={styles.msgReactionsHover}>
                          <span className="pointer" onClick={() => handleReact(msg.id, 'LOVE')}>❤️</span>
                          <span className="pointer" onClick={() => handleReact(msg.id, 'HAHA')}>😂</span>
                          <span className="pointer" onClick={() => handleReact(msg.id, 'WOW')}>😮</span>
                        </div>

                        <div 
                          style={isMyMessage ? styles.myBubble : styles.friendBubble}
                          title={new Date(msg.createdAt).toLocaleTimeString()}
                        >
                          {msg.type === 'IMAGE' ? (
                            <img src={getMediaUrl(msg.content)} alt="" style={styles.chatImage} />
                          ) : (
                            <p>{msg.content}</p>
                          )}

                          {/* Reaction badge overlays */}
                          {hasReactions && (
                            <div style={styles.bubbleReactionsBadge}>
                              {msg.reactions.map((r, i) => (
                                <span key={i} title={r.username}>{r.reaction === 'LOVE' ? '❤️' : r.reaction === 'HAHA' ? '😂' : '👍'}</span>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>

                      {/* Msg actions (Reply) */}
                      <div style={isMyMessage ? styles.myMsgMeta : styles.friendMsgMeta}>
                        <span className="pointer" style={styles.replyAction} onClick={() => setReplyingTo(msg)}>Reply</span>
                        {isMyMessage && (
                          <span style={styles.readReceiptText}>
                            {msg.isRead ? <Eye size={12} color="#10b981" /> : 'Sent'}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}

              {/* Typing bubble */}
              {typingUser && (
                <div style={styles.friendMessageRow}>
                  <div style={styles.typingIndicatorBubble}>
                    <span style={styles.typingDot}></span>
                    <span style={styles.typingDot}></span>
                    <span style={styles.typingDot}></span>
                  </div>
                  <span style={{ fontSize: '11px', color: '#9ca3af', alignSelf: 'center' }}>
                    {typingUser} is typing...
                  </span>
                </div>
              )}

              <div ref={messagesEndRef} />
            </div>

            {/* Replying indicator panel above composer */}
            {replyingTo && (
              <div style={styles.replyPreview} className="glass-card">
                <div style={styles.replyPreviewLeft}>
                  <CornerUpLeft size={14} color="#3b82f6" />
                  <span>Replying to <strong>{replyingTo.senderName}</strong>: {replyingTo.content}</span>
                </div>
                <X size={16} className="pointer" onClick={() => setReplyingTo(null)} />
              </div>
            )}

            {/* Text Composer at bottom */}
            <div style={styles.chatComposer}>
              <label style={styles.composerIconBtn} className="pointer">
                <Image size={20} />
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleFileUpload}
                  style={{ display: 'none' }}
                />
              </label>

              <input
                type="text"
                placeholder="Type a message..."
                className="input-field"
                style={{ borderRadius: '20px' }}
                value={text}
                onChange={(e) => onTextInputChange(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()}
              />

              <div style={styles.composerIconBtn} className="pointer" onClick={handleSendMessage}>
                <Send size={20} color="#3b82f6" />
              </div>
            </div>
          </>
        ) : (
          <div style={styles.chatPlaceholder} className="flex-center">
            <MessageSquare size={48} color="#273b5c" style={{ marginBottom: '12px' }} />
            <h3>Select a Conversation</h3>
            <p>Pick a chat from the sidebar or find friends to start chatting in real time.</p>
          </div>
        )}
      </div>

      {/* Create Group Chat Dialog Modal */}
      {showCreateGroup && (
        <div style={styles.modalOverlay} className="flex-center">
          <div style={styles.groupModal} className="glass-card">
            <div style={styles.modalHeader}>
              <h3>New Group Chat</h3>
              <X size={20} className="pointer" onClick={() => setShowCreateGroup(false)} />
            </div>
            <div style={styles.groupForm}>
              <input
                type="text"
                placeholder="Group Conversation Name"
                className="input-field"
                value={groupName}
                onChange={(e) => setGroupName(e.target.value)}
              />
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '10px' }}>
                <label style={{ fontSize: '12px', color: '#9ca3af' }}>Select Friends to Add:</label>
                <div style={styles.friendsSelectList}>
                  {friendsList.length === 0 ? (
                    <div style={styles.emptyViewers}>No friends found.</div>
                  ) : (
                    friendsList.map(f => (
                      <div key={f.id} style={styles.friendCheckRow} className="pointer" onClick={() => toggleSelectFriend(f.id)}>
                        <input
                          type="checkbox"
                          checked={selectedFriendIds.includes(f.id)}
                          onChange={() => {}} // Controlled by row clicker
                        />
                        <span>{f.firstName} {f.lastName}</span>
                      </div>
                    ))
                  )}
                </div>
              </div>

              <div style={styles.groupActions}>
                <button className="btn btn-secondary" onClick={() => setShowCreateGroup(false)}>Cancel</button>
                <button className="btn btn-primary" onClick={handleCreateGroup}>Create Chat</button>
              </div>
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
    gridTemplateColumns: '300px 1fr',
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
  createGroupBtn: {
    width: '32px',
    height: '32px',
    borderRadius: '50%',
    backgroundColor: '#1f2d47',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    '&:hover': {
      backgroundColor: '#273b5c'
    }
  },
  roomsList: {
    flex: 1,
    overflowY: 'auto',
    display: 'flex',
    flexDirection: 'column',
    padding: '8px'
  },
  roomItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    padding: '12px 10px',
    borderRadius: '12px',
    cursor: 'pointer',
    transition: '0.2s',
    '&:hover': {
      backgroundColor: 'rgba(255,255,255,0.02)'
    }
  },
  roomItemActive: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    padding: '12px 10px',
    borderRadius: '12px',
    cursor: 'pointer',
    backgroundColor: 'rgba(59, 130, 246, 0.08)',
    transition: '0.2s'
  },
  roomInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: '2px',
    flex: 1,
    overflow: 'hidden'
  },
  roomName: {
    fontSize: '13.5px',
    fontWeight: '600',
    color: '#f3f4f6',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis'
  },
  roomSnippet: {
    fontSize: '11px',
    color: '#9ca3af',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis'
  },
  unreadBadge: {
    backgroundColor: '#ec4899',
    color: 'white',
    borderRadius: '50%',
    padding: '1px 5px',
    fontSize: '10px',
    fontWeight: 'bold'
  },
  chatArea: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    position: 'relative'
  },
  chatHeader: {
    padding: '14px 20px',
    borderBottom: '1px solid #273b5c',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: 'rgba(22,32,50,0.4)'
  },
  chatHeaderLeft: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px'
  },
  headerStatus: {
    fontSize: '11px',
    color: '#9ca3af'
  },
  messagesContainer: {
    flex: 1,
    overflowY: 'auto',
    padding: '20px',
    display: 'flex',
    flexDirection: 'column',
    gap: '14px'
  },
  myMessageRow: {
    display: 'flex',
    justifyContent: 'flex-end',
    alignSelf: 'flex-end',
    maxWidth: '75%'
  },
  friendMessageRow: {
    display: 'flex',
    gap: '10px',
    alignSelf: 'flex-start',
    maxWidth: '75%'
  },
  messageBubbleWrapper: {
    display: 'flex',
    flexDirection: 'column',
    gap: '2px'
  },
  msgReplyCard: {
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
    padding: '4px 8px',
    borderRadius: '6px',
    backgroundColor: 'rgba(39, 59, 92, 0.4)',
    color: '#9ca3af',
    fontSize: '10px',
    alignSelf: 'flex-start'
  },
  bubbleContainer: {
    display: 'flex',
    position: 'relative',
    '&:hover .message-hover-reactions': {
      display: 'flex'
    }
  },
  msgReactionsHover: {
    display: 'none',
    position: 'absolute',
    top: '-32px',
    right: '10px',
    backgroundColor: '#1f2d47',
    borderRadius: '20px',
    padding: '4px 8px',
    gap: '6px',
    zIndex: 10
  },
  myBubble: {
    backgroundColor: '#3b82f6',
    color: 'white',
    padding: '10px 14px',
    borderRadius: '18px 18px 2px 18px',
    fontSize: '13.5px',
    position: 'relative'
  },
  friendBubble: {
    backgroundColor: '#1f2d47',
    color: '#f3f4f6',
    padding: '10px 14px',
    borderRadius: '18px 18px 18px 2px',
    fontSize: '13.5px',
    position: 'relative'
  },
  chatImage: {
    maxWidth: '240px',
    borderRadius: '12px',
    objectFit: 'cover'
  },
  bubbleReactionsBadge: {
    position: 'absolute',
    bottom: '-12px',
    right: '8px',
    backgroundColor: '#162032',
    border: '1px solid #273b5c',
    borderRadius: '10px',
    padding: '1px 4px',
    display: 'flex',
    gap: '2px',
    fontSize: '10px'
  },
  myMsgMeta: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '8px',
    fontSize: '10px',
    color: '#6b7280',
    paddingRight: '6px'
  },
  friendMsgMeta: {
    display: 'flex',
    gap: '8px',
    fontSize: '10px',
    color: '#6b7280',
    paddingLeft: '6px'
  },
  replyAction: {
    '&:hover': {
      color: '#3b82f6'
    }
  },
  readReceiptText: {
    color: '#10b981'
  },
  typingIndicatorBubble: {
    backgroundColor: '#1f2d47',
    padding: '10px 14px',
    borderRadius: '18px',
    display: 'flex',
    gap: '4px',
    alignItems: 'center',
    alignSelf: 'center'
  },
  typingDot: {
    width: '6px',
    height: '6px',
    backgroundColor: '#9ca3af',
    borderRadius: '50%',
    animation: 'typingDotBounce 1.2s infinite ease-in-out'
  },
  replyPreview: {
    padding: '8px 16px',
    borderTop: '1px solid #273b5c',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: 'rgba(59, 130, 246, 0.04)'
  },
  replyPreviewLeft: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    fontSize: '11px',
    color: '#9ca3af'
  },
  chatComposer: {
    padding: '14px 20px',
    borderTop: '1px solid #273b5c',
    display: 'flex',
    alignItems: 'center',
    gap: '12px'
  },
  composerIconBtn: {
    color: '#9ca3af',
    '&:hover': {
      color: '#3b82f6'
    }
  },
  chatPlaceholder: {
    flex: 1,
    flexDirection: 'column',
    textAlign: 'center',
    color: '#9ca3af'
  },
  emptyChats: {
    padding: '24px',
    fontSize: '12px',
    color: '#6b7280',
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
  groupModal: {
    width: '90%',
    maxWidth: '400px',
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
  groupForm: {
    display: 'flex',
    flexDirection: 'column',
    gap: '14px'
  },
  friendsSelectList: {
    maxHeight: '150px',
    overflowY: 'auto',
    border: '1px solid #273b5c',
    borderRadius: '8px',
    padding: '8px',
    display: 'flex',
    flexDirection: 'column',
    gap: '6px'
  },
  friendCheckRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    fontSize: '13px'
  },
  groupActions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '10px',
    marginTop: '12px'
  },
  searchOverlay: {
    position: 'absolute',
    top: '60px',
    right: '20px',
    width: '320px',
    height: '80%',
    zIndex: 100,
    display: 'flex',
    flexDirection: 'column',
    padding: '16px'
  },
  searchHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottom: '1px solid #273b5c',
    paddingBottom: '8px',
    marginBottom: '12px'
  },
  searchForm: {
    display: 'flex',
    gap: '8px',
    marginBottom: '14px'
  },
  searchResultsList: {
    flex: 1,
    overflowY: 'auto',
    display: 'flex',
    flexDirection: 'column',
    gap: '8px'
  },
  searchResultItem: {
    backgroundColor: '#0b0f19',
    padding: '8px',
    borderRadius: '8px',
    border: '1px solid #273b5c'
  },
  resultSender: {
    fontSize: '11px',
    fontWeight: 'bold',
    color: '#3b82f6'
  },
  resultText: {
    fontSize: '12px',
    color: '#f3f4f6',
    margin: '2px 0'
  },
  resultTime: {
    fontSize: '9px',
    color: '#6b7280'
  }
};

// Inject typing indicator CSS
if (typeof document !== 'undefined') {
  const style = document.createElement('style');
  style.innerHTML = `
    @keyframes typingDotBounce {
      0%, 80%, 100% { transform: scale(0); }
      40% { transform: scale(1.0); }
    }
  `;
  document.head.appendChild(style);
}

export default Messenger;
