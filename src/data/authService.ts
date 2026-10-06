import { AuthUser } from '../types/pms';

export interface StoredUserAccount {
  id: string;
  email: string;
  passwordHash: string; // stored credential
  name: string;
  role: 'Admin' | 'Manager' | 'Customer';
  phone?: string;
  memberTier?: 'Silver' | 'Gold' | 'Platinum' | 'Diamond';
  registeredAt: string;
}

// Fixed Admin Credential specified by user
export const ADMIN_CREDENTIALS = {
  email: 'suthaker09s@gmail.com',
  password: 'J2551',
  name: 'Administrator (Suthaker)',
  role: 'Admin' as const,
  phone: '+91 98765 43210'
};

// Fixed Manager Credential specified by user
export const MANAGER_CREDENTIALS = {
  email: 'yugan0302@gmail.com',
  password: '123123123',
  name: 'Yugan G',
  role: 'Manager' as const,
  phone: '+91 98421 00101'
};

const DEFAULT_USERS: StoredUserAccount[] = [
  {
    id: 'usr-admin-01',
    email: ADMIN_CREDENTIALS.email,
    passwordHash: ADMIN_CREDENTIALS.password,
    name: ADMIN_CREDENTIALS.name,
    role: 'Admin',
    phone: ADMIN_CREDENTIALS.phone,
    memberTier: 'Diamond',
    registeredAt: '2024-01-01 00:00:00'
  },
  {
    id: 'usr-manager-01',
    email: MANAGER_CREDENTIALS.email,
    passwordHash: MANAGER_CREDENTIALS.password,
    name: MANAGER_CREDENTIALS.name,
    role: 'Manager',
    phone: MANAGER_CREDENTIALS.phone,
    memberTier: 'Diamond',
    registeredAt: '2024-03-01 00:00:00'
  },
  {
    id: 'usr-guest-01',
    email: 'alexander@example.com',
    passwordHash: 'guest123',
    name: 'Alexander Vance',
    role: 'Customer',
    phone: '+91 98421 00142',
    memberTier: 'Platinum',
    registeredAt: '2026-08-01 10:15:00'
  },
  {
    id: 'usr-guest-02',
    email: 'elena@example.com',
    passwordHash: 'guest123',
    name: 'Elena Rostova',
    role: 'Customer',
    phone: '+91 98421 00188',
    memberTier: 'Gold',
    registeredAt: '2026-08-05 14:20:00'
  }
];

const STORAGE_USERS_KEY = 'kr_luxe_hotel_users_v2';
const STORAGE_CURRENT_USER_KEY = 'kr_luxe_hotel_current_user_v2';

export function getStoredUsers(): StoredUserAccount[] {
  try {
    const raw = localStorage.getItem(STORAGE_USERS_KEY);
    if (!raw) {
      localStorage.setItem(STORAGE_USERS_KEY, JSON.stringify(DEFAULT_USERS));
      return DEFAULT_USERS;
    }
    const parsed = JSON.parse(raw);
    // Ensure the required admin is always present and updated
    const adminExists = parsed.some((u: StoredUserAccount) => u.email.toLowerCase() === ADMIN_CREDENTIALS.email.toLowerCase());
    if (!adminExists) {
      parsed.unshift(DEFAULT_USERS[0]);
    }

    // Ensure the required manager is always present and updated
    const managerIdx = parsed.findIndex((u: StoredUserAccount) => u.email.toLowerCase() === MANAGER_CREDENTIALS.email.toLowerCase());
    if (managerIdx === -1) {
      parsed.splice(1, 0, DEFAULT_USERS[1]);
    } else {
      parsed[managerIdx].passwordHash = MANAGER_CREDENTIALS.password;
      parsed[managerIdx].name = MANAGER_CREDENTIALS.name;
      parsed[managerIdx].role = 'Manager';
    }
    localStorage.setItem(STORAGE_USERS_KEY, JSON.stringify(parsed));
    return parsed;
  } catch {
    return DEFAULT_USERS;
  }
}

export function saveStoredUsers(users: StoredUserAccount[]) {
  try {
    localStorage.setItem(STORAGE_USERS_KEY, JSON.stringify(users));
  } catch {
    // silent catch
  }
}

export function getCurrentUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem(STORAGE_CURRENT_USER_KEY);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function setCurrentUser(user: AuthUser | null) {
  try {
    if (!user) {
      localStorage.removeItem(STORAGE_CURRENT_USER_KEY);
    } else {
      localStorage.setItem(STORAGE_CURRENT_USER_KEY, JSON.stringify(user));
    }
  } catch {
    // silent catch
  }
}

export function loginUser(emailInput: string, passwordInput: string): { success: boolean; user?: AuthUser; error?: string } {
  const email = emailInput.trim().toLowerCase();
  const password = passwordInput.trim();

  if (!email || !password) {
    return { success: false, error: 'Please provide both email address and password.' };
  }

  // Check specific admin credentials
  if (email === ADMIN_CREDENTIALS.email.toLowerCase()) {
    if (password === ADMIN_CREDENTIALS.password) {
      const adminUser: AuthUser = {
        id: 'usr-admin-01',
        email: ADMIN_CREDENTIALS.email,
        name: ADMIN_CREDENTIALS.name,
        role: 'Admin',
        phone: ADMIN_CREDENTIALS.phone,
        memberTier: 'Diamond',
        registeredAt: '2024-01-01 00:00:00'
      };
      setCurrentUser(adminUser);
      return { success: true, user: adminUser };
    } else {
      return { success: false, error: 'Incorrect administrator password. Please verify credentials.' };
    }
  }

  // Check specific manager credentials
  if (email === MANAGER_CREDENTIALS.email.toLowerCase()) {
    if (password === MANAGER_CREDENTIALS.password) {
      const managerUser: AuthUser = {
        id: 'usr-manager-01',
        email: MANAGER_CREDENTIALS.email,
        name: MANAGER_CREDENTIALS.name,
        role: 'Manager',
        phone: MANAGER_CREDENTIALS.phone,
        memberTier: 'Diamond',
        registeredAt: '2024-03-01 00:00:00'
      };
      setCurrentUser(managerUser);
      return { success: true, user: managerUser };
    } else {
      return { success: false, error: 'Incorrect manager password. Please verify credentials.' };
    }
  }

  // Check stored accounts
  const users = getStoredUsers();
  const found = users.find(u => u.email.toLowerCase() === email);

  if (!found) {
    return {
      success: false,
      error: 'No account found with this email address. Please check your spelling or create a customer account.'
    };
  }

  if (found.passwordHash !== password) {
    return { success: false, error: 'Incorrect password for this account. Please try again.' };
  }

  const authUser: AuthUser = {
    id: found.id,
    email: found.email,
    name: found.name,
    role: found.role,
    phone: found.phone,
    memberTier: found.memberTier || 'Silver',
    registeredAt: found.registeredAt
  };

  setCurrentUser(authUser);
  return { success: true, user: authUser };
}

export function registerCustomer(params: {
  name: string;
  email: string;
  password: string;
  phone?: string;
}): { success: boolean; user?: AuthUser; error?: string } {
  const name = params.name.trim();
  const email = params.email.trim().toLowerCase();
  const password = params.password.trim();
  const phone = params.phone?.trim() || '+91 98421 00000';

  if (!name || name.length < 2) {
    return { success: false, error: 'Please enter a valid full name.' };
  }

  if (!email || !email.includes('@') || !email.includes('.')) {
    return { success: false, error: 'Please enter a valid email address.' };
  }

  if (!password || password.length < 4) {
    return { success: false, error: 'Password must be at least 4 characters.' };
  }

  // Prevent customer registering with the Admin email
  if (email === ADMIN_CREDENTIALS.email.toLowerCase()) {
    return { success: false, error: 'This email is reserved for system administration.' };
  }

  const users = getStoredUsers();
  const exists = users.some(u => u.email.toLowerCase() === email);

  if (exists) {
    return { success: false, error: 'An account with this email already exists. Please log in instead.' };
  }

  const newUser: StoredUserAccount = {
    id: `usr-cust-${Date.now()}`,
    email,
    passwordHash: password,
    name,
    role: 'Customer',
    phone,
    memberTier: 'Silver',
    registeredAt: new Date().toISOString().replace('T', ' ').slice(0, 19)
  };

  users.push(newUser);
  saveStoredUsers(users);

  const authUser: AuthUser = {
    id: newUser.id,
    email: newUser.email,
    name: newUser.name,
    role: 'Customer',
    phone: newUser.phone,
    memberTier: newUser.memberTier,
    registeredAt: newUser.registeredAt
  };

  setCurrentUser(authUser);
  return { success: true, user: authUser };
}

export function logoutUser() {
  setCurrentUser(null);
}
