import React, { useState, useEffect } from 'react';
import { HeaderNav } from './components/HeaderNav';
import { InteractivePMS } from './components/InteractivePMS';
import { CustomerPortal } from './components/CustomerPortal';
import { LoginPage } from './components/LoginPage';
import { AuthUser, RoomItem, ReservationItem, InvoiceItem, ServiceItem, GuestServiceCharge, HotelSettingsConfig, GuestNotification } from './types/pms';
import { getCurrentUser, setCurrentUser as persistCurrentUser, logoutUser } from './data/authService';
import {
  initialSettings,
  initialRooms,
  initialReservations,
  initialServices,
  initialGuestServices,
  initialInvoices,
  initialNotifications
} from './data/mockPmsData';
import { Sparkles, ArrowLeft } from 'lucide-react';

export default function App() {
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(() => getCurrentUser());
  const [customerViewMode, setCustomerViewMode] = useState<boolean>(false);

  // Shared application state across Admin Operations Desk & Customer Portal
  const [settings, setSettings] = useState<HotelSettingsConfig>(initialSettings);
  const [rooms, setRooms] = useState<RoomItem[]>(initialRooms);
  const [reservations, setReservations] = useState<ReservationItem[]>(initialReservations);
  const [invoices, setInvoices] = useState<InvoiceItem[]>(initialInvoices);
  const [services] = useState<ServiceItem[]>(initialServices);
  const [guestServices, setGuestServices] = useState<GuestServiceCharge[]>(initialGuestServices);
  const [notifications, setNotifications] = useState<GuestNotification[]>(() => {
    try {
      const saved = localStorage.getItem('kr_luxe_guest_notifications_v1');
      if (saved) return JSON.parse(saved);
    } catch {}
    return initialNotifications;
  });

  useEffect(() => {
    try {
      localStorage.setItem('kr_luxe_guest_notifications_v1', JSON.stringify(notifications));
    } catch {}
  }, [notifications]);

  const handleLoginSuccess = (user: AuthUser) => {
    setCurrentUser(user);
    persistCurrentUser(user);
    setCustomerViewMode(false);
  };

  const handleLogout = () => {
    logoutUser();
    setCurrentUser(null);
    setCustomerViewMode(false);
  };

  // If no user is logged in, show the login & registration page
  if (!currentUser) {
    return <LoginPage onLoginSuccess={handleLoginSuccess} />;
  }

  const isAdmin = currentUser.role === 'Admin' || currentUser.role === 'Manager';

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col font-sans antialiased text-slate-100">
      {/* Admin previewing customer view notification banner */}
      {isAdmin && customerViewMode && (
        <div className="bg-amber-500/90 text-slate-950 px-4 py-2 text-xs font-bold flex items-center justify-between shadow-md">
          <div className="flex items-center space-x-2">
            <Sparkles className="w-4 h-4" />
            <span>{currentUser.role === 'Manager' ? 'Manager Preview' : 'Admin Preview'}: Currently previewing the Guest Experience Portal as seen by guests.</span>
          </div>
          <button
            onClick={() => setCustomerViewMode(false)}
            className="px-3 py-1 bg-slate-950 text-amber-300 rounded-lg text-xs font-bold hover:bg-slate-900 transition-all flex items-center space-x-1"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Return to Operations Desk</span>
          </button>
        </div>
      )}

      {/* Main Navigation Header */}
      <HeaderNav
        isAdmin={isAdmin}
        customerViewMode={customerViewMode}
        onSelectOperations={() => setCustomerViewMode(false)}
        onSelectGuestPortal={() => setCustomerViewMode(true)}
        currentUser={currentUser}
        onLogout={handleLogout}
      />

      {/* Main Content Area */}
      <main className="flex-1">
        {(!isAdmin || customerViewMode) ? (
          <CustomerPortal
            currentUser={currentUser}
            rooms={rooms}
            setRooms={setRooms}
            reservations={reservations}
            setReservations={setReservations}
            invoices={invoices}
            setInvoices={setInvoices}
            services={services}
            guestServices={guestServices}
            setGuestServices={setGuestServices}
            notifications={notifications}
            setNotifications={setNotifications}
            settings={settings}
            onLogout={handleLogout}
            onRequestAdmin={() => {
              if (isAdmin) {
                setCustomerViewMode(false);
              } else {
                handleLogout();
              }
            }}
          />
        ) : (
          <InteractivePMS
            currentUser={currentUser}
            onLogout={handleLogout}
            onRequestCustomerView={() => setCustomerViewMode(true)}
            rooms={rooms}
            setRooms={setRooms}
            reservations={reservations}
            setReservations={setReservations}
            invoices={invoices}
            setInvoices={setInvoices}
            services={services}
            guestServices={guestServices}
            setGuestServices={setGuestServices}
            notifications={notifications}
            setNotifications={setNotifications}
            settings={settings}
            setSettings={setSettings}
          />
        )}
      </main>
    </div>
  );
}
