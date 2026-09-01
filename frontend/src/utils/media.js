export const getMediaUrl = (url, fallback = '') => {
  if (!url) return fallback;
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('blob:')) return url;
  return `http://localhost:8080${url.startsWith('/') ? '' : '/'}${url}`;
};

export const getAvatarUrl = (url) => {
  return getMediaUrl(url, 'https://cdn-icons-png.flaticon.com/512/149/149071.png');
};

export const getCoverUrl = (url) => {
  return getMediaUrl(url, 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80');
};
