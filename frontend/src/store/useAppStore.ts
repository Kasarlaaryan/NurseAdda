import { create } from 'zustand';

interface AppState {
  sidebarCollapsed: boolean;
  mobileSidebarOpen: boolean;
  globalSearchQuery: string;
  sessionTimeoutModalOpen: boolean;
  toggleSidebar: () => void;
  toggleMobileSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setMobileSidebarOpen: (open: boolean) => void;
  setGlobalSearchQuery: (query: string) => void;
  triggerSessionTimeout: () => void;
  closeSessionTimeoutModal: () => void;
}

export const useAppStore = create<AppState>((set) => ({
  sidebarCollapsed: false,
  mobileSidebarOpen: false,
  globalSearchQuery: '',
  sessionTimeoutModalOpen: false,

  toggleSidebar: () => set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),
  toggleMobileSidebar: () => set((state) => ({ mobileSidebarOpen: !state.mobileSidebarOpen })),
  setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),
  setMobileSidebarOpen: (open) => set({ mobileSidebarOpen: open }),
  setGlobalSearchQuery: (query) => set({ globalSearchQuery: query }),
  triggerSessionTimeout: () => set({ sessionTimeoutModalOpen: true }),
  closeSessionTimeoutModal: () => set({ sessionTimeoutModalOpen: false })
}));
