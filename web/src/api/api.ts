import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const safeUUID = (): string => {
  if (typeof window !== 'undefined' && window.crypto && window.crypto.randomUUID) {
    return window.crypto.randomUUID();
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
};

// ─── Types ────────────────────────────────────────────────────────────────────

export interface Machine {
  id: string;
  name: string;
  manufacturer: string;
  model: string;
  thumbnailPath?: string;
  manualPdfPath?: string;
  userGuidePdfPath?: string;
  createdAt: string;
  updatedAt: string;
  referenceImages?: ReferenceImage[];
}

export interface ReferenceImage {
  id: string;
  machineId: string;
  filename: string;
  partName: string;
  circleX?: number;
  circleY?: number;
  circleRadius?: number;
  filePath: string;
}

export interface DiagnosisReport {
  id: string;
  machineId: string;
  machineName: string;
  problemDescription: string;
  uploadedImagePath: string;
  diagnosisProblem: string;
  diagnosisSolution: string;
  highlightX?: number;
  highlightY?: number;
  highlightRadius?: number;
  timestamp: string;
  chatHistory?: ChatMessage[];
}

export interface ChatMessage {
  id: string;
  sender: 'USER' | 'AI';
  messageText: string;
  timestamp: string;
  reportId?: string;
}

export interface LearnMessage {
  id: string;
  machineId: string;
  sessionId: string;
  sender: 'USER' | 'AI';
  messageText: string;
  timestamp: string;
}

export interface KnowledgeBaseLayer1 {
  id: string;
  machineId: string;
  contentJson: string;
}

export interface DashboardStats {
  totalMachines: number;
  totalReports: number;
  totalLayer1Datastores: number;
  totalLayer2Vectors: number;
}

// ─── Health ────────────────────────────────────────────────────────────────────

export const getHealth = () =>
  api.get<{ status: string; service: string; timestamp: number }>('/health');

// ─── Machines ──────────────────────────────────────────────────────────────────

export const getMachines = () =>
  api.get<Machine[]>('/machines');

export const getMachineById = (id: string) =>
  api.get<Machine>(`/machines/${id}`);

export const createMachine = (formData: FormData) =>
  api.post<Machine>('/machines', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export const addReferenceImage = (machineId: string, formData: FormData) =>
  api.post<ReferenceImage>(`/machines/${machineId}/ref-image`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export const getReferenceImages = (machineId: string) =>
  api.get<ReferenceImage[]>(`/machines/${machineId}/ref-images`);

export const updateReferenceImage = (refImageId: string, formData: FormData) =>
  api.put<ReferenceImage>(`/machines/ref-images/${refImageId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export const deleteReferenceImage = (refImageId: string) =>
  api.delete(`/machines/ref-images/${refImageId}`);

// Backend stores full relative paths like "/thumbnails/uuid.jpg" or "/uploads/uuid.jpg".
export const getFileUrl = (path?: string | null) => {
  if (!path) return '';
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  return `/api/machines/files${cleanPath}`;
};

// Fetches the raw text content of a stored document (e.g. a .md manual/guide).
export const fetchFileContent = async (path: string): Promise<string> => {
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  const res = await api.get<string>(`/machines/files${cleanPath}`, { responseType: 'text' });
  return res.data;
};

export const isMarkdownPath = (path?: string | null): boolean =>
  !!path && /\.(md|markdown|txt)$/i.test(path);

// ─── Analysis ──────────────────────────────────────────────────────────────────

export const getDiagnosisModels = () =>
  api.get<{ default: string; models: string[] }>('/analysis/models');

export const diagnose = (machineId: string, problemDescription: string, image: File, model?: string) => {
  const formData = new FormData();
  formData.append('machineId', machineId);
  formData.append('problemDescription', problemDescription);
  formData.append('image', image);
  if (model) formData.append('model', model);
  return api.post<DiagnosisReport>('/analysis/diagnose', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000, // 2 min for AI processing
  });
};

// ─── Knowledge ─────────────────────────────────────────────────────────────────

export const generateKnowledgeBase = (machineId: string) =>
  api.post<{ success: boolean; message: string }>(`/knowledge/generate/${machineId}`);

// ─── Chat ──────────────────────────────────────────────────────────────────────

export const sendChatMessage = (reportId: string, message: string) =>
  api.post<ChatMessage>(`/chat/${reportId}`, { message });

export const getChatHistory = (reportId: string) =>
  api.get<ChatMessage[]>(`/chat/${reportId}/history`);

// ─── Learn ─────────────────────────────────────────────────────────────────────

export const sendLearnMessage = (machineId: string, message: string, sessionId: string, model?: string) =>
  api.post<LearnMessage>(`/learn/${machineId}`, { message, model }, { params: { sessionId } });

export const getLearnHistory = (machineId: string, sessionId: string) =>
  api.get<LearnMessage[]>(`/learn/${machineId}/history`, { params: { sessionId } });

export const clearLearnHistory = (machineId: string, sessionId: string) =>
  api.delete(`/learn/${machineId}`, { params: { sessionId } });


// ─── Admin ─────────────────────────────────────────────────────────────────────

export const getDashboardStats = () =>
  api.get<DashboardStats>('/admin/dashboard');

export const getAllReports = () =>
  api.get<DiagnosisReport[]>('/admin/reports');

export const getReportDetails = (id: string) =>
  api.get<DiagnosisReport>(`/admin/reports/${id}`);

export default api;
