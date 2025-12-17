const API_BASE = "http://localhost:8080/api";

const ROLE_KEY = "authRole"; // USER | PROVIDER

export function getCurrentUserId() {
  if (localStorage.getItem(ROLE_KEY) !== "USER") return null;
  const v = localStorage.getItem("currentUserId");
  return v ? Number(v) : null;
}

export function setCurrentUserId(userId) {
  localStorage.setItem(ROLE_KEY, "USER");
  localStorage.setItem("currentUserId", String(userId));
  localStorage.removeItem("currentProviderId");
}

export function getCurrentProviderId() {
  if (localStorage.getItem(ROLE_KEY) !== "PROVIDER") return null;
  const v = localStorage.getItem("currentProviderId");
  return v ? Number(v) : null;
}

export function setCurrentProviderId(providerId) {
  localStorage.setItem(ROLE_KEY, "PROVIDER");
  localStorage.setItem("currentProviderId", String(providerId));
  localStorage.removeItem("currentUserId");
}

export function clearCurrentUser() {
  localStorage.removeItem("currentUserId");
  localStorage.removeItem("currentProviderId");
  localStorage.removeItem(ROLE_KEY);
}

async function request(path, { method = "GET", body } = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let json = null;
  try { json = text ? JSON.parse(text) : null; } catch { json = null; }
  if (!res.ok) {
    const msg = json?.message || `HTTP ${res.status}`;
    throw new Error(msg);
  }
  return json;
}

export const api = {
  account: {
    create: (payload) => request("/account/create", { method: "POST", body: payload }),
    get: (userId) => request(`/account/${userId}`),
    update: (payload) => request("/account/update", { method: "PUT", body: payload }),
    providers: () => request("/account/providers"),
    addEmail: (payload) => request("/account/email/add", { method: "POST", body: payload }),
    removeEmail: (payload) => request("/account/email/remove", { method: "POST", body: payload }),
    upsertPhone: (payload) => request("/account/phone/upsert", { method: "POST", body: payload }),
    removePhone: (payload) => request("/account/phone/remove", { method: "POST", body: payload }),
    linkProvider: (payload) => request("/account/provider/link", { method: "POST", body: payload }),
    unlinkProvider: (payload) => request("/account/provider/unlink", { method: "POST", body: payload }),
    setPrimary: (payload) => request("/account/provider/set-primary", { method: "POST", body: payload }),
  },
  appointment: {
    create: (payload) => request("/appointment/create", { method: "POST", body: payload }),
    cancel: (payload) => request("/appointment/cancel", { method: "POST", body: payload }),
    search: (payload) => request("/appointment/search", { method: "POST", body: payload }),
  },
  challenge: {
    create: (payload) => request("/challenge/create", { method: "POST", body: payload }),
    list: () => request("/challenge/list"),
    my: (userId) => request(`/challenge/my?userId=${userId}`),
    join: (challengeId, userId) => request(`/challenge/join?challengeId=${challengeId}&userId=${userId}`, { method: "POST" }),
    participants: (challengeId) => request(`/challenge/${challengeId}/participants`),
    progress: (payload) => request("/challenge/progress", { method: "POST", body: payload }),
    invite: (payload) => request("/challenge/invite", { method: "POST", body: payload }),
    invitations: (challengeId) => request(`/challenge/${challengeId}/invitations`),
    top: (limit = 5) => request(`/challenge/top?limit=${limit}`),
  },
  summary: {
    appointmentCount: (payload) => request("/summary/appointment-count", { method: "POST", body: payload }),
    metricStats: (payload) => request("/summary/metric-stats", { method: "POST", body: payload }),
    topActiveUsers: (limit = 5) => request(`/summary/top-active-users?limit=${limit}`),
  },
  search: {
    healthRecords: ({ healthId, metricCode, start, end }) => {
      const p = new URLSearchParams();
      if (healthId) p.set("healthId", healthId);
      if (metricCode) p.set("metricCode", metricCode);
      if (start) p.set("start", start);
      if (end) p.set("end", end);
      return request(`/search/health-records?${p.toString()}`);
    },
  },
  healthRecord: {
    metricTypes: () => request("/health-record/metric-types"),
    create: (payload) => request("/health-record/create", { method: "POST", body: payload }),
  },
  provider: {
    login: (payload) => request("/provider/login", { method: "POST", body: payload }),
    appointments: (providerId) => request(`/provider/${providerId}/appointments`),
    cancelAppointment: (payload) => request("/provider/appointment/cancel", { method: "POST", body: payload }),
    patients: (providerId) => request(`/provider/${providerId}/patients`),
    patientHealthRecords: (providerId, userId, limit = 50) => request(`/provider/${providerId}/patients/${userId}/health-records?limit=${limit}`),
  },
  family: {
    members: (userId) => request(`/family/${userId}/members`),
    memberHealthRecords: (payload, limit = 50) => request(`/family/member/health-records?limit=${limit}`, { method: "POST", body: payload }),
    memberChallenges: (payload) => request(`/family/member/challenges`, { method: "POST", body: payload }),
  },
};

export function fmtIsoLocal(dtValue) {
  // dtValue from <input type="datetime-local">
  // backend expects ISO_LOCAL_DATE_TIME
  return dtValue ? dtValue : null;
}


