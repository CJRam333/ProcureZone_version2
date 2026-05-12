import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';

export interface AppSettings {
  // Appearance
  theme: 'light' | 'dark' | 'system';
  sidebarCollapsed: boolean;
  compactMode: boolean;
  // Regional
  language: string;
  timezone: string;
  dateFormat: string;
  currency: string;
  // Security
  sessionTimeout: number;
  twoFactorEnabled: boolean;
  // Notifications (stored as JSON)
  notifications: NotificationSetting[];
}

export interface NotificationSetting {
  id: string;
  title: string;
  description: string;
  email: boolean;
  push: boolean;
  inApp: boolean;
}

const STORAGE_KEY = 'procurezone_settings';

const defaultNotifications: NotificationSetting[] = [
  { id: 'indent-approval', title: 'Indent Approvals', description: 'When an indent is submitted for your approval', email: true, push: true, inApp: true },
  { id: 'po-created', title: 'PO Created', description: 'When a purchase order is generated from your indent', email: true, push: false, inApp: true },
  { id: 'grn-received', title: 'GRN Received', description: 'When goods are received against your PO', email: true, push: true, inApp: true },
  { id: 'low-stock', title: 'Low Stock Alerts', description: 'When inventory falls below reorder level', email: true, push: true, inApp: true },
  { id: 'system-updates', title: 'System Updates', description: 'Important system announcements and updates', email: false, push: false, inApp: true },
];

const defaultSettings: AppSettings = {
  theme: 'light',
  sidebarCollapsed: false,
  compactMode: false,
  language: 'en',
  timezone: 'Asia/Kolkata',
  dateFormat: 'DD/MM/YYYY',
  currency: 'INR',
  sessionTimeout: 30,
  twoFactorEnabled: false,
  notifications: defaultNotifications,
};

function loadSettings(): AppSettings {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      return { ...defaultSettings, ...JSON.parse(raw) };
    }
  } catch {
    // ignore parse errors
  }
  return defaultSettings;
}

function saveSettings(settings: AppSettings) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(settings));
  } catch {
    // ignore storage errors
  }
}

/** Resolve the effective theme (handles 'system') */
function resolveTheme(theme: 'light' | 'dark' | 'system'): 'light' | 'dark' {
  if (theme === 'system') {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  return theme;
}

/** Apply theme, compact mode to the DOM immediately */
function applySettings(settings: AppSettings) {
  // Theme: set data-bs-theme on <html> for Bootstrap 5+ dark mode support
  const resolved = resolveTheme(settings.theme);
  document.documentElement.setAttribute('data-bs-theme', resolved);

  // Compact mode: toggle class on body
  if (settings.compactMode) {
    document.body.classList.add('compact-mode');
  } else {
    document.body.classList.remove('compact-mode');
  }
}

// ─── Context ────────────────────────────────────────────────────────────────

interface SettingsContextValue {
  settings: AppSettings;
  updateSettings: (partial: Partial<AppSettings>) => void;
  saveAllSettings: (s: AppSettings) => Promise<void>;
}

const SettingsContext = createContext<SettingsContextValue | null>(null);

export const SettingsProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [settings, setSettings] = useState<AppSettings>(() => {
    const loaded = loadSettings();
    applySettings(loaded); // apply on first render
    return loaded;
  });

  // Re-apply whenever settings change
  useEffect(() => {
    applySettings(settings);
  }, [settings]);

  // Listen for system theme changes when 'system' mode is selected
  useEffect(() => {
    if (settings.theme !== 'system') return;
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    const handler = () => applySettings(settings);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, [settings.theme]); // eslint-disable-line

  const updateSettings = useCallback((partial: Partial<AppSettings>) => {
    setSettings(prev => {
      const next = { ...prev, ...partial };
      saveSettings(next);
      return next;
    });
  }, []);

  const saveAllSettings = useCallback(async (s: AppSettings) => {
    saveSettings(s);
    setSettings(s);
    // Simulate brief async pause (could be replaced with real API call)
    await new Promise(resolve => setTimeout(resolve, 600));
  }, []);

  return (
    <SettingsContext.Provider value={{ settings, updateSettings, saveAllSettings }}>
      {children}
    </SettingsContext.Provider>
  );
};

export function useSettings(): SettingsContextValue {
  const ctx = useContext(SettingsContext);
  if (!ctx) throw new Error('useSettings must be used within a SettingsProvider');
  return ctx;
}
