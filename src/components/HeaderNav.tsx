import React from 'react';
import {
  Building2,
  Sparkles,
  ShieldCheck,
  User,
  LogOut
} from 'lucide-react';
import { AuthUser } from '../types/pms';

interface HeaderNavProps {
  isAdmin: boolean;
  customerViewMode: boolean;
  onSelectOperations: () => void;
  onSelectGuestPortal: () => void;
  currentUser: AuthUser | null;
  onLogout: () => void;
}

export const HeaderNav: React.FC<HeaderNavProps> = ({
  isAdmin,
  customerViewMode,
  onSelectOperations,
  onSelectGuestPortal,
  currentUser,
  onLogout
}) => {
  return (
    <header className="bg-slate-950/95 backdrop-blur-md border-b border-slate-800 text-slate-100 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Brand Logo & Title */}
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-amber-400 via-amber-500 to-amber-600 flex items-center justify-center shadow-lg shadow-amber-500/20 text-slate-950 font-bold text-lg font-serif">
              KR
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-bold text-base tracking-wide text-slate-100 font-serif">KR LUXE HOTEL</span>
              </div>
              <p className="text-xs text-slate-400">
                {isAdmin ? 'Enterprise Property Management' : 'Luxury Hospitality & Guest Portal'}
              </p>
            </div>
          </div>

          {/* Navigation Controls */}
          <nav className="flex items-center space-x-1.5 bg-slate-900/90 p-1 rounded-xl border border-slate-800 backdrop-blur-sm">
            {isAdmin ? (
              <>
                <button
                  onClick={onSelectOperations}
                  className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                    !customerViewMode
                      ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 shadow-md shadow-amber-500/20 font-bold'
                      : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
                  }`}
                >
                  <Building2 className="w-4 h-4" />
                  <span>Operations Desk</span>
                </button>

                <button
                  onClick={onSelectGuestPortal}
                  className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                    customerViewMode
                      ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 shadow-md shadow-amber-500/20 font-bold'
                      : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
                  }`}
                >
                  <Sparkles className="w-4 h-4" />
                  <span>Guest Portal Preview</span>
                </button>
              </>
            ) : (
              <div className="flex items-center space-x-2 px-3.5 py-1.5 rounded-lg text-xs font-bold text-amber-400 bg-amber-500/10 border border-amber-500/20">
                <Sparkles className="w-4 h-4" />
                <span>Guest Experience Portal</span>
              </div>
            )}
          </nav>

          {/* User Profile & Account Controls */}
          <div className="flex items-center space-x-3 text-xs">
            {currentUser && (
              <div className="flex items-center space-x-3">
                <div className="flex items-center space-x-2 bg-slate-900 border border-slate-800 px-3 py-1.5 rounded-xl">
                  {isAdmin ? (
                    <div className="flex items-center space-x-1.5">
                      <ShieldCheck className="w-4 h-4 text-amber-400" />
                      <div className="text-left">
                        <div className="text-[11px] font-bold text-amber-300 leading-tight">
                          {currentUser.role === 'Manager' ? 'Manager (Yugan G)' : 'Admin (Suthaker)'}
                        </div>
                        <div className="text-[10px] text-slate-400 leading-tight">{currentUser.email}</div>
                      </div>
                    </div>
                  ) : (
                    <div className="flex items-center space-x-1.5">
                      <User className="w-4 h-4 text-emerald-400" />
                      <div className="text-left">
                        <div className="text-[11px] font-bold text-emerald-300 leading-tight">{currentUser.name}</div>
                        <div className="text-[10px] text-slate-400 leading-tight">{currentUser.memberTier || 'Silver'} Member</div>
                      </div>
                    </div>
                  )}
                </div>

                <button
                  onClick={onLogout}
                  className="px-3 py-1.5 rounded-xl bg-slate-900 hover:bg-red-950/60 text-slate-300 hover:text-red-300 border border-slate-800 hover:border-red-800/60 transition-all flex items-center space-x-1.5 font-semibold"
                  title="Sign out of account"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span className="hidden sm:inline">Sign Out</span>
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
