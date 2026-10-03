import { api } from './client';

export const authApi = {
  register: (username, email, password, displayName) =>
    api.post('/auth/register', { username, email, password, displayName }, { auth: false }),
  login: (usernameOrEmail, password) =>
    api.post('/auth/login', { usernameOrEmail, password }, { auth: false }),
};

export const domainsApi = {
  list: () => api.get('/domains', { auth: false }),
};

export const lessonsApi = {
  byDomain: (domainId) => api.get(`/lessons/by-domain/${domainId}`),
  detail: (lessonId) => api.get(`/lessons/${lessonId}`, { auth: false }),
};

export const questionsApi = {
  answer: (questionId, selectedOptionId, freeResponse) =>
    api.post('/questions/answer', { questionId, selectedOptionId, freeResponse }),
};

export const progressApi = {
  dashboard: () => api.get('/progress/dashboard'),
  completeLesson: (lessonId, scorePercent) =>
    api.post('/progress/complete-lesson', { lessonId, scorePercent }),
};

export const leaderboardApi = {
  top20: () => api.get('/leaderboard', { auth: false }),
};
