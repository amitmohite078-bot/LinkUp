import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Users, Globe, Lock, ShieldCheck, ShieldAlert, UserPlus, LogOut, Check, X, Shield, Settings, Plus, ArrowLeft } from 'lucide-react';
import { getAvatarUrl } from '../utils/media';
import Feed from './Feed';

const GroupsView = () => {
  const { user, apiFetch } = useAuth();
  const [joinedGroups, setJoinedGroups] = useState([]);
  const [recommendedGroups, setRecommendedGroups] = useState([]);
  const [selectedGroup, setSelectedGroup] = useState(null); // Group
  
  // Tab within group
  const [activeSubTab, setActiveSubTab] = useState('discussion'); // discussion, members, rules
  
  // Creation States
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [privacy, setPrivacy] = useState('PUBLIC');
  const [rules, setRules] = useState('');

  // Group Details lists
  const [groupMembers, setGroupMembers] = useState([]);
  const [groupRules, setGroupRules] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [isMember, setIsMember] = useState(false);
  const [isAdminOrMod, setIsAdminOrMod] = useState(false);

  useEffect(() => {
    loadGroupsSummary();
  }, []);

  useEffect(() => {
    if (!selectedGroup) return;
    loadGroupDetails(selectedGroup.id);
  }, [selectedGroup]);

  const loadGroupsSummary = async () => {
    try {
      // 1. Get joined groups (API status ACTIVE)
      // Since backend has groupMemberRepository, let's fetch user's memberships
      const memberships = await apiFetch(`/api/groups/recommended`); // suggestions fallback
      setRecommendedGroups(memberships);

      // Simple local search fallback: let's query all groups and filter
      const allGroups = await apiFetch(`/api/groups/search?query=`);
      
      // Separate joined vs suggestions based on membership counts
      // Actually we can hit: /api/groups/recommended
      const recommendations = await apiFetch(`/api/groups/recommended`);
      setRecommendedGroups(recommendations);

      // Joined groups are those where user is an active member
      // For simplicity: filter allGroups where user is creator or check membership
      const joined = allGroups.filter(g => g.creator.id === user.id);
      setJoinedGroups(joined);
    } catch (e) {
      console.error(e);
    }
  };

  const loadGroupDetails = async (groupId) => {
    try {
      const members = await apiFetch(`/api/groups/${groupId}/members`);
      setGroupMembers(members);
      
      const rulesList = await apiFetch(`/api/groups/${groupId}/rules`);
      setGroupRules(rulesList);

      const activeMember = members.find(m => m.user.id === user.id && m.status === 'ACTIVE');
      setIsMember(!!activeMember);

      const privileged = members.find(m => m.user.id === user.id && (m.role === 'ADMIN' || m.role === 'MODERATOR'));
      setIsAdminOrMod(!!privileged);

      // Load pending requests if privileged
      if (privileged) {
        const pending = members.filter(m => m.status === 'PENDING');
        setPendingRequests(pending);
      } else {
        setPendingRequests([]);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleCreateGroup = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;

    try {
      const rulesArray = rules.split('\n').filter(r => r.trim().length > 0);
      const params = new URLSearchParams();
      params.append('name', name);
      params.append('description', description);
      params.append('privacy', privacy);
      if (rulesArray.length > 0) {
        rulesArray.forEach(r => params.append('rules', r));
      }

      const group = await apiFetch(`/api/groups/create?${params.toString()}`, {
        method: 'POST'
      });

      setShowCreateModal(false);
      setName('');
      setDescription('');
      setRules('');
      loadGroupsSummary();
      setSelectedGroup(group);
    } catch (e) {
      console.error(e);
    }
  };

  const handleJoinGroup = async (groupId) => {
    try {
      await apiFetch(`/api/groups/${groupId}/join`, { method: 'POST' });
      loadGroupsSummary();
      if (selectedGroup && selectedGroup.id === groupId) {
        // Refresh details
        const refreshed = await apiFetch(`/api/groups/${groupId}`);
        setSelectedGroup(refreshed);
      } else {
        const g = await apiFetch(`/api/groups/${groupId}`);
        setSelectedGroup(g);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleLeaveGroup = async (groupId) => {
    if (!confirm('Leave this group?')) return;
    try {
      await apiFetch(`/api/groups/${groupId}/leave`, { method: 'POST' });
      loadGroupsSummary();
      setSelectedGroup(null);
    } catch (e) {
      console.error(e);
    }
  };

  const handleApprove = async (targetUserId) => {
    try {
      await apiFetch(`/api/groups/${selectedGroup.id}/approve/${targetUserId}`, { method: 'POST' });
      loadGroupDetails(selectedGroup.id);
    } catch (e) {
      console.error(e);
    }
  };

  const handleReject = async (targetUserId) => {
    try {
      await apiFetch(`/api/groups/${selectedGroup.id}/reject/${targetUserId}`, { method: 'POST' });
      loadGroupDetails(selectedGroup.id);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div style={styles.container} className="glass-card">
      {/* Left Sidebar Groups lists */}
      <div style={styles.sidebar}>
        <div style={styles.sidebarHeader}>
          <h3>Groups</h3>
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)} style={{ padding: '6px 12px', fontSize: '12px' }}>
            Create Group
          </button>
        </div>

        <div style={styles.groupsListContainer}>
          <div style={styles.sectionHeader}>Joined Groups</div>
          {joinedGroups.length === 0 ? (
            <div style={styles.emptyText}>No joined groups yet.</div>
          ) : (
            joinedGroups.map(g => (
              <div 
                key={g.id} 
                style={selectedGroup && selectedGroup.id === g.id ? styles.groupItemActive : styles.groupItem}
                onClick={() => setSelectedGroup(g)}
              >
                <span>👥</span>
                <div style={styles.groupInfo}>
                  <div style={styles.groupName}>{g.name}</div>
                  <div style={styles.groupMeta}>{g.privacy}</div>
                </div>
              </div>
            ))
          )}

          <div style={{ ...styles.sectionHeader, marginTop: '16px' }}>Suggestions</div>
          {recommendedGroups.length === 0 ? (
            <div style={styles.emptyText}>No suggestions.</div>
          ) : (
            recommendedGroups.map(g => (
              <div 
                key={g.id} 
                style={styles.groupItem}
                onClick={() => setSelectedGroup(g)}
              >
                <span>🌐</span>
                <div style={styles.groupInfo}>
                  <div style={styles.groupName}>{g.name}</div>
                  <button 
                    onClick={(e) => { e.stopPropagation(); handleJoinGroup(g.id); }} 
                    className="btn btn-primary" 
                    style={styles.joinBtn}
                  >
                    Join
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Right Dashboard Workspace */}
      <div style={styles.dashboard}>
        {selectedGroup ? (
          <>
            {/* Header banner */}
            <div style={styles.groupHeader}>
              <div style={styles.groupTitleRow}>
                <h2>{selectedGroup.name}</h2>
                <div style={styles.headerMetaRow}>
                  {selectedGroup.privacy === 'PUBLIC' ? (
                    <><Globe size={14} /> Public Group</>
                  ) : (
                    <><Lock size={14} /> Private Group</>
                  )}
                  <span>•</span>
                  <span>{groupMembers.length} Members</span>
                </div>
              </div>

              <div style={styles.groupActions}>
                {isMember ? (
                  <button className="btn btn-secondary" onClick={() => handleLeaveGroup(selectedGroup.id)}>
                    <LogOut size={16} /> Leave Group
                  </button>
                ) : (
                  <button className="btn btn-primary" onClick={() => handleJoinGroup(selectedGroup.id)}>
                    <UserPlus size={16} /> Join Group
                  </button>
                )}
              </div>
            </div>

            {/* Sub-tabs row */}
            <div style={styles.tabsRow}>
              <div 
                style={activeSubTab === 'discussion' ? styles.tabItemActive : styles.tabItem}
                onClick={() => setActiveSubTab('discussion')}
                className="pointer"
              >
                Discussion
              </div>
              <div 
                style={activeSubTab === 'members' ? styles.tabItemActive : styles.tabItem}
                onClick={() => setActiveSubTab('members')}
                className="pointer"
              >
                Members
              </div>
              <div 
                style={activeSubTab === 'rules' ? styles.tabItemActive : styles.tabItem}
                onClick={() => setActiveSubTab('rules')}
                className="pointer"
              >
                Group Rules
              </div>
            </div>

            {/* Tab Workspace content */}
            <div style={styles.dashboardContent}>
              {activeSubTab === 'discussion' && (
                isMember || selectedGroup.privacy === 'PUBLIC' ? (
                  <Feed groupId={selectedGroup.id} />
                ) : (
                  <div style={styles.restrictedFeed} className="flex-center">
                    <Lock size={32} color="#6b7280" style={{ marginBottom: '8px' }} />
                    <h4>This Group is Private</h4>
                    <p>Join this group to view discussion boards and participate in community posts.</p>
                  </div>
                )
              )}

              {activeSubTab === 'members' && (
                <div style={styles.membersPane}>
                  {/* Admin review section if current user is admin/moderator */}
                  {isAdminOrMod && pendingRequests.length > 0 && (
                    <div style={styles.adminReviewBox} className="glass-card">
                      <h4 style={{ color: '#ec4899', display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
                        <ShieldAlert size={18} /> Member Requests ({pendingRequests.length})
                      </h4>
                      <div style={styles.requestsList}>
                        {pendingRequests.map(r => (
                          <div key={r.id} style={styles.requestItem}>
                            <span>{r.user.firstName} {r.user.lastName} (@{r.user.username})</span>
                            <div style={styles.requestItemActions}>
                              <button onClick={() => handleApprove(r.user.id)} className="btn btn-primary" style={{ padding: '4px 10px', fontSize: '11px' }}>
                                <Check size={14} /> Approve
                              </button>
                              <button onClick={() => handleReject(r.user.id)} className="btn btn-danger" style={{ padding: '4px 10px', fontSize: '11px' }}>
                                <X size={14} /> Reject
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  <h3>Group Members ({groupMembers.length})</h3>
                  <div style={styles.membersList}>
                    {groupMembers.map(m => (
                      <div key={m.id} style={styles.memberItemRow}>
                        <img src={getAvatarUrl(m.user.avatarUrl)} className="avatar" style={{ width: '32px', height: '32px' }} alt="" />
                        <div style={styles.memberInfo}>
                          <div style={styles.memberName}>{m.user.firstName} {m.user.lastName}</div>
                          <div style={styles.memberRoleBadge}>
                            {m.role === 'ADMIN' && <><Shield size={10} color="#ef4444" fill="#ef4444" /> Admin</>}
                            {m.role === 'MODERATOR' && <><ShieldCheck size={10} color="#3b82f6" /> Moderator</>}
                            {m.role === 'MEMBER' && 'Member'}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {activeSubTab === 'rules' && (
                <div style={styles.rulesPane}>
                  <h3>Group Guidelines & Rules</h3>
                  <div style={styles.rulesList}>
                    {groupRules.length === 0 ? (
                      <div style={{ color: '#9ca3af' }}>No rules created yet for this group.</div>
                    ) : (
                      groupRules.map((r, i) => (
                        <div key={r.id} style={styles.ruleItemRow}>
                          <span style={styles.ruleIndex}>{i + 1}.</span>
                          <p style={styles.ruleText}>{r.ruleText}</p>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>
          </>
        ) : (
          <div style={styles.placeholder} className="flex-center">
            <Users size={48} color="#273b5c" style={{ marginBottom: '12px' }} />
            <h3>LinkUp Communities</h3>
            <p>Select a group from the sidebar, create your own, or join recommended communities to start discussions.</p>
          </div>
        )}
      </div>

      {/* Group Creation Dialog Modal */}
      {showCreateModal && (
        <div style={styles.modalOverlay} className="flex-center">
          <div style={styles.createModal} className="glass-card">
            <div style={styles.modalHeader}>
              <h3>Create a Group</h3>
              <X size={20} className="pointer" onClick={() => setShowCreateModal(false)} />
            </div>
            <form onSubmit={handleCreateGroup} style={styles.form}>
              <input
                type="text"
                placeholder="Group Name"
                className="input-field"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
              <textarea
                placeholder="Group Description..."
                className="input-field"
                style={{ height: '80px', resize: 'none' }}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
              <textarea
                placeholder="Group Rules (one rule per line)..."
                className="input-field"
                style={{ height: '80px', resize: 'none' }}
                value={rules}
                onChange={(e) => setRules(e.target.value)}
              />
              <select className="input-field" value={privacy} onChange={(e) => setPrivacy(e.target.value)}>
                <option value="PUBLIC">🌍 Public (Anyone can join instantly)</option>
                <option value="PRIVATE">🔒 Private (Admins must approve requests)</option>
              </select>
              <button type="submit" className="btn btn-primary">Create Group</button>
            </form>
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
  groupsListContainer: {
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
  groupItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    padding: '10px 8px',
    borderRadius: '10px',
    cursor: 'pointer',
    transition: '0.2s',
    '&:hover': {
      backgroundColor: 'rgba(255,255,255,0.02)'
    }
  },
  groupItemActive: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    padding: '10px 8px',
    borderRadius: '10px',
    cursor: 'pointer',
    backgroundColor: 'rgba(59, 130, 246, 0.08)',
    transition: '0.2s'
  },
  groupInfo: {
    display: 'flex',
    flexDirection: 'column',
    flex: 1,
    overflow: 'hidden'
  },
  groupName: {
    fontSize: '13px',
    fontWeight: '600',
    color: '#f3f4f6',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis'
  },
  groupMeta: {
    fontSize: '10px',
    color: '#9ca3af'
  },
  joinBtn: {
    padding: '2px 8px',
    fontSize: '10px',
    borderRadius: '4px',
    alignSelf: 'flex-start',
    marginTop: '4px'
  },
  dashboard: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%'
  },
  groupHeader: {
    padding: '20px 24px',
    borderBottom: '1px solid #273b5c',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: 'rgba(22,32,50,0.4)'
  },
  groupTitleRow: {
    display: 'flex',
    flexDirection: 'column',
    gap: '4px'
  },
  headerMetaRow: {
    display: 'flex',
    gap: '8px',
    fontSize: '12px',
    color: '#9ca3af',
    alignItems: 'center'
  },
  groupActions: {
    display: 'flex',
    gap: '8px'
  },
  tabsRow: {
    display: 'flex',
    padding: '0 24px',
    gap: '24px',
    borderBottom: '1px solid #273b5c',
    backgroundColor: 'rgba(22,32,50,0.2)'
  },
  tabItem: {
    padding: '12px 0',
    fontSize: '12.5px',
    fontWeight: '600',
    color: '#9ca3af',
    borderBottom: '3px solid transparent'
  },
  tabItemActive: {
    padding: '12px 0',
    fontSize: '12.5px',
    fontWeight: '700',
    color: '#3b82f6',
    borderBottom: '3px solid #3b82f6'
  },
  dashboardContent: {
    flex: 1,
    overflowY: 'auto',
    padding: '20px'
  },
  restrictedFeed: {
    flexDirection: 'column',
    padding: '60px 20px',
    color: '#9ca3af',
    textAlign: 'center'
  },
  membersPane: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px'
  },
  adminReviewBox: {
    padding: '16px',
    border: '1px solid rgba(236, 72, 153, 0.3)',
    backgroundColor: 'rgba(236, 72, 153, 0.04)'
  },
  requestsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px'
  },
  requestItem: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    fontSize: '13px',
    backgroundColor: '#0b0f19',
    padding: '8px 12px',
    borderRadius: '8px'
  },
  requestItemActions: {
    display: 'flex',
    gap: '6px'
  },
  membersList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px'
  },
  memberItemRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    padding: '6px 0'
  },
  memberInfo: {
    display: 'flex',
    flexDirection: 'column'
  },
  memberName: {
    fontSize: '13.5px',
    fontWeight: '600',
    color: '#f3f4f6'
  },
  memberRoleBadge: {
    fontSize: '11px',
    color: '#9ca3af',
    display: 'flex',
    alignItems: 'center',
    gap: '4px'
  },
  rulesPane: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  rulesList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px'
  },
  ruleItemRow: {
    display: 'flex',
    gap: '10px',
    alignItems: 'flex-start'
  },
  ruleIndex: {
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#3b82f6'
  },
  ruleText: {
    fontSize: '13.5px',
    color: '#d1d5db',
    lineHeight: '1.4'
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
    maxWidth: '440px',
    padding: '24px'
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
    gap: '14px'
  }
};

export default GroupsView;
