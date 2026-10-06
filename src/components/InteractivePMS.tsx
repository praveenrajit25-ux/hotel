import React, { useState } from 'react';
import {
  LayoutDashboard,
  BedDouble,
  Users,
  CalendarCheck,
  LogIn,
  LogOut,
  Receipt,
  ConciergeBell,
  UserCheck,
  BarChart3,
  Settings,
  Plus,
  Search,
  CheckCircle2,
  AlertCircle,
  Clock,
  DollarSign,
  FileText,
  Trash2,
  RefreshCw,
  Sparkles,
  Download,
  CreditCard,
  Building,
  KeyRound,
  ShieldAlert,
  Image as ImageIcon
} from 'lucide-react';
import { downloadFolioReceiptImage } from '../utils/receiptImageGenerator';
import {
  initialSettings,
  initialRooms,
  initialGuests,
  initialReservations,
  initialServices,
  initialGuestServices,
  initialInvoices,
  initialCheckIns,
  initialCheckOuts,
  initialStaff
} from '../data/mockPmsData';
import {
  RoomItem,
  GuestItem,
  ReservationItem,
  ServiceItem,
  GuestServiceCharge,
  InvoiceItem,
  CheckInLog,
  CheckOutLog,
  StaffItem,
  HotelSettingsConfig,
  RoomStatus,
  RoomType,
  PaymentMethod,
  AuthUser,
  GuestNotification
} from '../types/pms';

interface InteractivePMSProps {
  currentUser?: AuthUser | null;
  onLogout?: () => void;
  onRequestCustomerView?: () => void;
  rooms?: RoomItem[];
  setRooms?: React.Dispatch<React.SetStateAction<RoomItem[]>>;
  reservations?: ReservationItem[];
  setReservations?: React.Dispatch<React.SetStateAction<ReservationItem[]>>;
  invoices?: InvoiceItem[];
  setInvoices?: React.Dispatch<React.SetStateAction<InvoiceItem[]>>;
  services?: ServiceItem[];
  guestServices?: GuestServiceCharge[];
  setGuestServices?: React.Dispatch<React.SetStateAction<GuestServiceCharge[]>>;
  notifications?: GuestNotification[];
  setNotifications?: React.Dispatch<React.SetStateAction<GuestNotification[]>>;
  settings?: HotelSettingsConfig;
  setSettings?: React.Dispatch<React.SetStateAction<HotelSettingsConfig>>;
}

export const InteractivePMS: React.FC<InteractivePMSProps> = ({
  currentUser,
  onLogout,
  onRequestCustomerView,
  rooms: propRooms,
  setRooms: propSetRooms,
  reservations: propReservations,
  setReservations: propSetReservations,
  invoices: propInvoices,
  setInvoices: propSetInvoices,
  services: propServices,
  guestServices: propGuestServices,
  setGuestServices: propSetGuestServices,
  notifications: _propNotifications,
  setNotifications: propSetNotifications,
  settings: propSettings,
  setSettings: propSetSettings
}) => {
  const [activeModule, setActiveModule] = useState<string>('dashboard');
  const [currentUserRole, setCurrentUserRole] = useState<'Admin' | 'Manager' | 'Receptionist' | 'Housekeeping'>('Admin');

  // State entities (use props if provided or fallback to local state)
  const [localSettings, setLocalSettings] = useState<HotelSettingsConfig>(initialSettings);
  const settings = propSettings || localSettings;
  const setSettings = propSetSettings || setLocalSettings;

  const [localRooms, setLocalRooms] = useState<RoomItem[]>(initialRooms);
  const rooms = propRooms || localRooms;
  const setRooms = propSetRooms || setLocalRooms;

  const [guests, setGuests] = useState<GuestItem[]>(initialGuests);

  const [localReservations, setLocalReservations] = useState<ReservationItem[]>(initialReservations);
  const reservations = propReservations || localReservations;
  const setReservations = propSetReservations || setLocalReservations;

  const [localServices] = useState<ServiceItem[]>(initialServices);
  const services = propServices || localServices;

  const [localGuestServices, setLocalGuestServices] = useState<GuestServiceCharge[]>(initialGuestServices);
  const guestServices = propGuestServices || localGuestServices;
  const setGuestServices = propSetGuestServices || setLocalGuestServices;

  const [localInvoices, setLocalInvoices] = useState<InvoiceItem[]>(initialInvoices);
  const invoices = propInvoices || localInvoices;
  const setInvoices = propSetInvoices || setLocalInvoices;

  const [checkIns, setCheckIns] = useState<CheckInLog[]>(initialCheckIns);
  const [checkOuts, setCheckOuts] = useState<CheckOutLog[]>(initialCheckOuts);
  const [staffList, setStaffList] = useState<StaffItem[]>(initialStaff);

  // Modals & Dialogs state
  const [modalType, setModalType] = useState<string | null>(null);
  const [selectedEntity, setSelectedEntity] = useState<any>(null);
  const [notification, setNotification] = useState<{ message: string; type: 'success' | 'info' | 'error' } | null>(null);

  const showNotification = (message: string, type: 'success' | 'info' | 'error' = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 3500);
  };

  // Helper stats
  const totalRooms = rooms.length;
  const occupiedRooms = rooms.filter(r => r.status === 'Occupied').length;
  const availableRooms = rooms.filter(r => r.status === 'Available').length;
  const reservedRooms = rooms.filter(r => r.status === 'Reserved').length;
  const cleaningRooms = rooms.filter(r => r.status === 'Cleaning').length;
  const maintenanceRooms = rooms.filter(r => r.status === 'Maintenance').length;
  const occupancyRate = totalRooms > 0 ? ((occupiedRooms / totalRooms) * 100).toFixed(1) : '0';

  const totalRevenue = invoices.reduce((sum, inv) => sum + inv.paidAmount, 0);
  const pendingArrivals = reservations.filter(r => r.status === 'Confirmed').length;
  const inHouseGuests = reservations.filter(r => r.status === 'Checked_In').length;

  const formatCurrency = (val: number, minimumFractionDigits = 2) => {
    return `${settings.currencySymbol}${val.toLocaleString('en-IN', {
      minimumFractionDigits,
      maximumFractionDigits: minimumFractionDigits
    })}`;
  };

  if (currentUser && currentUser.role === 'Customer') {
    return (
      <div className="min-h-[calc(100vh-4rem)] bg-slate-950 flex items-center justify-center p-6">
        <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-8 text-center space-y-4 shadow-2xl">
          <div className="w-14 h-14 rounded-2xl bg-amber-500/10 text-amber-400 border border-amber-500/20 flex items-center justify-center mx-auto">
            <ShieldAlert className="w-7 h-7" />
          </div>
          <h2 className="text-xl font-bold text-white font-serif">Administrator Access Required</h2>
          <p className="text-xs text-slate-300 leading-relaxed">
            The Property Management Operations Desk can only be accessed with hotel administrator credentials:
          </p>
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 font-medium text-xs text-amber-300">
            suthaker09s@gmail.com
          </div>
          <p className="text-xs text-slate-400">
            As a registered guest, you have full access to room reservations, concierge services, and your personal billing folio in the Guest Portal.
          </p>
          <div className="pt-2 flex flex-col gap-2">
            {onRequestCustomerView && (
              <button
                onClick={onRequestCustomerView}
                className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold text-xs hover:from-amber-400 hover:to-amber-500 shadow-md shadow-amber-500/20"
              >
                Go to Guest Portal
              </button>
            )}
            {onLogout && (
              <button
                onClick={onLogout}
                className="w-full py-2.5 px-4 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white font-semibold text-xs border border-slate-700"
              >
                Sign In as Admin
              </button>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-slate-950 min-h-[calc(100vh-4rem)] flex flex-col text-slate-100">
      {/* Toast Notification */}
      {notification && (
        <div className="fixed bottom-6 right-6 z-50 animate-bounce">
          <div className={`px-4 py-3 rounded-lg shadow-2xl border flex items-center space-x-2 text-sm font-medium backdrop-blur-md ${
            notification.type === 'success'
              ? 'bg-emerald-950/90 text-emerald-200 border-emerald-700/80'
              : notification.type === 'error'
              ? 'bg-red-950/90 text-red-200 border-red-700/80'
              : 'bg-slate-900/90 text-slate-200 border-slate-700/80'
          }`}>
            {notification.type === 'success' ? <CheckCircle2 className="w-4 h-4 text-emerald-400" /> : <AlertCircle className="w-4 h-4 text-red-400" />}
            <span>{notification.message}</span>
          </div>
        </div>
      )}

      {/* Simulator Control Header */}
      <div className="bg-slate-950/90 text-white px-6 py-2.5 flex items-center justify-between border-b border-slate-800 text-xs backdrop-blur-md">
        <div className="flex items-center space-x-4">
          <span className="font-semibold text-slate-200 flex items-center gap-1.5">
            <Building className="w-3.5 h-3.5 text-amber-400" />
            {settings.hotelName}
          </span>
          <span className="text-slate-600">•</span>
          <span className="text-slate-400">System Time: {new Date().toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })}</span>
          <span className="hidden sm:inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-bold border border-amber-500/30 text-[10px]">
            <span>Admin: suthaker09s@gmail.com</span>
          </span>
        </div>

        {/* Role Switcher & Customer View Preview */}
        <div className="flex items-center space-x-3">
          {onRequestCustomerView && (
            <button
              onClick={onRequestCustomerView}
              className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-amber-300 text-xs font-semibold flex items-center space-x-1 border border-slate-700 transition-all"
            >
              <Sparkles className="w-3.5 h-3.5 text-amber-400" />
              <span>Guest Portal View</span>
            </button>
          )}

          <div className="flex items-center space-x-2">
            <span className="text-slate-400">Staff Mode:</span>
            <select
              value={currentUserRole}
              onChange={(e) => setCurrentUserRole(e.target.value as any)}
              className="bg-slate-900 text-amber-300 font-semibold border border-slate-700 rounded px-2 py-0.5 text-xs focus:outline-none focus:border-amber-400"
            >
              <option value="Admin">Admin (suthaker09s@gmail.com)</option>
              <option value="Manager">Manager (Robert Clark)</option>
              <option value="Receptionist">Receptionist (Sarah Jenkins)</option>
              <option value="Housekeeping">Housekeeping (Maria Rodriguez)</option>
            </select>
          </div>
        </div>
      </div>

      {/* Main Workspace Frame */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Sidebar (Swing Style) */}
        <aside className="w-64 bg-slate-950 text-slate-300 flex flex-col border-r border-slate-800/90 select-none">
          <div className="p-4 border-b border-slate-800/80">
            <div className="text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-1">Navigation Menu</div>
            <div className="text-xs text-amber-400 font-medium">Hotel Management Suite</div>
          </div>

          <nav className="flex-1 p-2 space-y-0.5 overflow-y-auto">
            <button
              onClick={() => setActiveModule('dashboard')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                activeModule === 'dashboard' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
              }`}
            >
              <LayoutDashboard className="w-4 h-4" />
              <span>Operations Dashboard</span>
            </button>

            <button
              onClick={() => setActiveModule('rooms')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                activeModule === 'rooms' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
              }`}
            >
              <BedDouble className="w-4 h-4" />
              <span>Room Inventory</span>
            </button>

            <button
              onClick={() => setActiveModule('guests')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                activeModule === 'guests' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
              }`}
            >
              <Users className="w-4 h-4" />
              <span>Guest Directory</span>
            </button>

            <button
              onClick={() => setActiveModule('reservations')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                activeModule === 'reservations' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
              }`}
            >
              <CalendarCheck className="w-4 h-4" />
              <span>Reservations</span>
            </button>

            <div className="pt-2 pb-1 px-3 text-[10px] uppercase font-bold tracking-wider text-slate-500">Front Desk Ops</div>

            <button
              onClick={() => setActiveModule('checkin')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                activeModule === 'checkin' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
              }`}
            >
              <LogIn className="w-4 h-4" />
              <span>Guest Check-In</span>
              {pendingArrivals > 0 && (
                <span className="ml-auto bg-blue-500/30 text-blue-300 text-[10px] px-1.5 py-0.5 rounded-full font-bold">
                  {pendingArrivals}
                </span>
              )}
            </button>

            <button
              onClick={() => setActiveModule('checkout')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                activeModule === 'checkout' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
              }`}
            >
              <LogOut className="w-4 h-4" />
              <span>Express Check-Out</span>
              {inHouseGuests > 0 && (
                <span className="ml-auto bg-amber-500/30 text-amber-300 text-[10px] px-1.5 py-0.5 rounded-full font-bold">
                  {inHouseGuests}
                </span>
              )}
            </button>

            <button
              onClick={() => setActiveModule('billing')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                activeModule === 'billing' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
              }`}
            >
              <Receipt className="w-4 h-4" />
              <span>Billing & Payments</span>
            </button>

            <button
              onClick={() => setActiveModule('services')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                activeModule === 'services' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
              }`}
            >
              <ConciergeBell className="w-4 h-4" />
              <span>Hotel Services</span>
            </button>

            <div className="pt-2 pb-1 px-3 text-[10px] uppercase font-bold tracking-wider text-slate-500">Administration</div>

            {currentUserRole === 'Admin' && (
              <button
                onClick={() => setActiveModule('staff')}
                className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                  activeModule === 'staff' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
                }`}
              >
                <UserCheck className="w-4 h-4" />
                <span>Staff & Roles</span>
              </button>
            )}

            <button
              onClick={() => setActiveModule('reports')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                activeModule === 'reports' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
              }`}
            >
              <BarChart3 className="w-4 h-4" />
              <span>Financial Reports</span>
            </button>

            <button
              onClick={() => setActiveModule('settings')}
              className={`w-full flex items-center space-x-3 px-3 py-2.5 rounded-md text-xs font-semibold transition-all ${
                activeModule === 'settings' ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-400 hover:bg-slate-850 hover:text-white hover:bg-slate-900'
              }`}
            >
              <Settings className="w-4 h-4" />
              <span>Property Settings</span>
            </button>
          </nav>

          <div className="p-3 border-t border-slate-800/80 text-[11px] text-slate-400 flex items-center justify-between">
            <span>System Status</span>
            <span className="text-emerald-400 font-semibold">● Operational</span>
          </div>
        </aside>

        {/* Center Panel View */}
        <main className="flex-1 p-6 overflow-y-auto bg-[#0b0f19]">
          {/* ======================= DASHBOARD MODULE ======================= */}
          {activeModule === 'dashboard' && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-xl font-bold text-slate-100">Operations Dashboard</h1>
                  <p className="text-xs text-slate-400">Real-time room occupancy, front desk metrics, and revenue analytics</p>
                </div>
                <button
                  onClick={() => showNotification('Analytics refreshed with latest operational records.', 'info')}
                  className="flex items-center space-x-1.5 px-3 py-1.5 rounded-md bg-slate-900 border border-slate-700/80 text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 shadow-sm transition-all"
                >
                  <RefreshCw className="w-3.5 h-3.5" />
                  <span>Refresh</span>
                </button>
              </div>

              {/* KPI Stat Cards */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
                <div className="bg-slate-900/90 p-4 rounded-xl border border-slate-800/90 shadow-xl backdrop-blur-sm">
                  <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Total Rooms</div>
                  <div className="text-2xl font-bold text-slate-100 mt-1">{totalRooms}</div>
                  <div className="text-xs text-slate-400 mt-1 font-medium">Occupancy: <span className="text-amber-400 font-bold">{occupancyRate}%</span></div>
                </div>

                <div className="bg-slate-900/90 p-4 rounded-xl border border-slate-800/90 shadow-xl backdrop-blur-sm">
                  <div className="text-[11px] font-bold text-emerald-400 uppercase tracking-wider">Available</div>
                  <div className="text-2xl font-bold text-emerald-400 mt-1">{availableRooms}</div>
                  <div className="text-xs text-slate-400 mt-1">Ready for check-in</div>
                </div>

                <div className="bg-slate-900/90 p-4 rounded-xl border border-slate-800/90 shadow-xl backdrop-blur-sm">
                  <div className="text-[11px] font-bold text-blue-400 uppercase tracking-wider">Arrivals Ready</div>
                  <div className="text-2xl font-bold text-blue-400 mt-1">{pendingArrivals}</div>
                  <div className="text-xs text-slate-400 mt-1">Confirmed bookings</div>
                </div>

                <div className="bg-slate-900/90 p-4 rounded-xl border border-slate-800/90 shadow-xl backdrop-blur-sm">
                  <div className="text-[11px] font-bold text-amber-400 uppercase tracking-wider">In-House Guests</div>
                  <div className="text-2xl font-bold text-amber-400 mt-1">{inHouseGuests}</div>
                  <div className="text-xs text-slate-400 mt-1">Occupied rooms</div>
                </div>

                <div className="bg-slate-900/90 p-4 rounded-xl border border-slate-800/90 shadow-xl backdrop-blur-sm">
                  <div className="text-[11px] font-bold text-purple-400 uppercase tracking-wider">Housekeeping</div>
                  <div className="text-2xl font-bold text-purple-400 mt-1">{cleaningRooms}</div>
                  <div className="text-xs text-slate-400 mt-1">{maintenanceRooms} in maintenance</div>
                </div>

                <div className="bg-slate-900/90 p-4 rounded-xl border border-slate-800/90 shadow-xl backdrop-blur-sm">
                  <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Total Revenue</div>
                  <div className="text-2xl font-bold text-amber-400 mt-1">{formatCurrency(totalRevenue)}</div>
                  <div className="text-xs text-emerald-400 mt-1 font-medium">All settled folios</div>
                </div>
              </div>

              {/* Quick Actions Panel */}
              <div className="bg-slate-900/90 p-4 rounded-xl border border-slate-800/90 shadow-xl backdrop-blur-sm">
                <div className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">Quick Front Desk Actions</div>
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
                  <button
                    onClick={() => setActiveModule('reservations')}
                    className="p-3 bg-slate-950 text-white rounded-lg text-xs font-semibold flex flex-col items-center justify-center space-y-1.5 border border-slate-800 hover:border-amber-500/50 hover:bg-slate-850 transition-all shadow-sm group"
                  >
                    <Plus className="w-4 h-4 text-amber-400 group-hover:scale-110 transition-transform" />
                    <span>New Booking</span>
                  </button>

                  <button
                    onClick={() => setActiveModule('checkin')}
                    className="p-3 bg-blue-600 text-white rounded-lg text-xs font-semibold flex flex-col items-center justify-center space-y-1.5 hover:bg-blue-500 transition-all shadow-sm"
                  >
                    <LogIn className="w-4 h-4" />
                    <span>Process Check-In</span>
                  </button>

                  <button
                    onClick={() => setActiveModule('checkout')}
                    className="p-3 bg-amber-600 text-white rounded-lg text-xs font-semibold flex flex-col items-center justify-center space-y-1.5 hover:bg-amber-500 transition-all shadow-sm"
                  >
                    <LogOut className="w-4 h-4" />
                    <span>Express Check-Out</span>
                  </button>

                  <button
                    onClick={() => setActiveModule('billing')}
                    className="p-3 bg-emerald-600 text-white rounded-lg text-xs font-semibold flex flex-col items-center justify-center space-y-1.5 hover:bg-emerald-500 transition-all shadow-sm"
                  >
                    <Receipt className="w-4 h-4" />
                    <span>Issue Invoice</span>
                  </button>

                  <button
                    onClick={() => setActiveModule('services')}
                    className="p-3 bg-indigo-600 text-white rounded-lg text-xs font-semibold flex flex-col items-center justify-center space-y-1.5 hover:bg-indigo-500 transition-all shadow-sm"
                  >
                    <ConciergeBell className="w-4 h-4" />
                    <span>Charge Room</span>
                  </button>

                  <button
                    onClick={() => setActiveModule('guests')}
                    className="p-3 bg-slate-800 text-white rounded-lg text-xs font-semibold flex flex-col items-center justify-center space-y-1.5 hover:bg-slate-700 transition-all shadow-sm border border-slate-700"
                  >
                    <Users className="w-4 h-4 text-amber-400" />
                    <span>Register Guest</span>
                  </button>
                </div>
              </div>

              {/* Live Room Status Breakdown Bar */}
              <div className="bg-slate-900/90 p-4 rounded-xl border border-slate-800/90 shadow-xl backdrop-blur-sm">
                <div className="flex items-center justify-between mb-2">
                  <div className="text-xs font-bold uppercase tracking-wider text-slate-400">Live Inventory Status Breakdown</div>
                  <div className="text-xs font-semibold text-slate-300">{totalRooms} Registered Rooms</div>
                </div>
                <div className="h-3 w-full rounded-full bg-slate-950 border border-slate-800 flex overflow-hidden">
                  <div style={{ width: `${(availableRooms / totalRooms) * 100}%` }} className="bg-emerald-500" title={`Available: ${availableRooms}`} />
                  <div style={{ width: `${(occupiedRooms / totalRooms) * 100}%` }} className="bg-blue-600" title={`Occupied: ${occupiedRooms}`} />
                  <div style={{ width: `${(reservedRooms / totalRooms) * 100}%` }} className="bg-amber-500" title={`Reserved: ${reservedRooms}`} />
                  <div style={{ width: `${(cleaningRooms / totalRooms) * 100}%` }} className="bg-purple-500" title={`Cleaning: ${cleaningRooms}`} />
                  <div style={{ width: `${(maintenanceRooms / totalRooms) * 100}%` }} className="bg-red-500" title={`Maintenance: ${maintenanceRooms}`} />
                </div>
                <div className="flex flex-wrap gap-4 text-xs mt-3 pt-2 border-t border-slate-800/80 text-slate-300 font-medium">
                  <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span> Available: <b className="text-slate-100">{availableRooms}</b></span>
                  <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-blue-600"></span> Occupied: <b className="text-slate-100">{occupiedRooms}</b></span>
                  <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-amber-500"></span> Reserved: <b className="text-slate-100">{reservedRooms}</b></span>
                  <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-purple-500"></span> Cleaning: <b className="text-slate-100">{cleaningRooms}</b></span>
                  <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-red-500"></span> Maintenance: <b className="text-slate-100">{maintenanceRooms}</b></span>
                </div>
              </div>

              {/* Recent Bookings Table */}
              <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 shadow-xl overflow-hidden backdrop-blur-sm">
                <div className="p-4 border-b border-slate-800 flex items-center justify-between">
                  <div className="text-xs font-bold uppercase tracking-wider text-slate-400">Recent Active Bookings</div>
                  <button onClick={() => setActiveModule('reservations')} className="text-xs text-amber-400 hover:text-amber-300 font-semibold transition-colors">View All Reservations →</button>
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full text-xs text-left">
                    <thead className="bg-slate-950 text-slate-300 font-bold uppercase tracking-wider text-[11px] border-b border-slate-800">
                      <tr>
                        <th className="px-4 py-3">Res ID</th>
                        <th className="px-4 py-3">Guest Name</th>
                        <th className="px-4 py-3">Room #</th>
                        <th className="px-4 py-3">Type</th>
                        <th className="px-4 py-3">Check-In</th>
                        <th className="px-4 py-3">Check-Out</th>
                        <th className="px-4 py-3">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-800/80">
                      {reservations.slice(0, 5).map((r) => (
                        <tr key={r.id} className="hover:bg-slate-800/40 transition-colors">
                          <td className="px-4 py-3 font-semibold text-amber-400">#{r.id}</td>
                          <td className="px-4 py-3 font-medium text-slate-200">{r.guestName}</td>
                          <td className="px-4 py-3 font-bold text-slate-100">Room {r.roomNumber}</td>
                          <td className="px-4 py-3 text-slate-400">{r.roomType}</td>
                          <td className="px-4 py-3 text-slate-400">{r.checkInDate}</td>
                          <td className="px-4 py-3 text-slate-400">{r.checkOutDate}</td>
                          <td className="px-4 py-3">
                            <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                              r.status === 'Checked_In'
                                ? 'bg-blue-500/20 text-blue-300 border border-blue-500/40'
                                : r.status === 'Confirmed'
                                ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                                : r.status === 'Checked_Out'
                                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                                : 'bg-red-500/20 text-red-300 border border-red-500/40'
                            }`}>
                              {r.status.replace('_', ' ')}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}

          {/* ======================= ROOM INVENTORY MODULE ======================= */}
          {activeModule === 'rooms' && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-xl font-bold text-slate-100">Room Inventory & Housekeeping</h1>
                  <p className="text-xs text-slate-400">Manage rooms, pricing, floor assignment, and live operational status</p>
                </div>
                <button
                  onClick={() => setModalType('add_room')}
                  className="flex items-center space-x-1.5 px-3.5 py-2 rounded-md bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 text-xs font-bold hover:from-amber-400 hover:to-amber-500 shadow-md shadow-amber-500/20 transition-all"
                >
                  <Plus className="w-4 h-4 text-slate-950" />
                  <span>Add New Room</span>
                </button>
              </div>

              {/* Room Grid Cards */}
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
                {rooms.map((room) => (
                  <div
                    key={room.id}
                    className="bg-slate-900/90 rounded-xl border border-slate-800/90 p-4 shadow-xl flex flex-col justify-between relative hover:border-slate-700 transition-all backdrop-blur-sm"
                  >
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-base font-bold text-slate-100">Room {room.number}</span>
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                          room.status === 'Available'
                            ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                            : room.status === 'Occupied'
                            ? 'bg-blue-500/20 text-blue-300 border border-blue-500/40'
                            : room.status === 'Reserved'
                            ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                            : room.status === 'Cleaning'
                            ? 'bg-purple-500/20 text-purple-300 border border-purple-500/40'
                            : 'bg-red-500/20 text-red-300 border border-red-500/40'
                        }`}>
                          {room.status}
                        </span>
                      </div>

                      <div className="text-xs text-slate-300 font-medium">{room.type} • Floor {room.floor}</div>
                      <div className="text-xs text-slate-400 mt-1 line-clamp-2">{room.description}</div>
                    </div>

                    <div className="mt-4 pt-3 border-t border-slate-800 flex items-center justify-between">
                      <div className="text-sm font-bold text-amber-400">{formatCurrency(room.pricePerNight, 0)}<span className="text-[10px] text-slate-400 font-normal">/nt</span></div>

                      <select
                        value={room.status}
                        onChange={(e) => {
                          const newStatus = e.target.value as RoomStatus;
                          setRooms(rooms.map(r => r.id === room.id ? { ...r, status: newStatus } : r));
                          showNotification(`Room ${room.number} updated to ${newStatus}`);
                        }}
                        className="text-[11px] bg-slate-950 border border-slate-700 rounded px-2 py-1 text-slate-200 font-medium focus:outline-none focus:border-amber-400"
                      >
                        <option value="Available">Available</option>
                        <option value="Occupied">Occupied</option>
                        <option value="Reserved">Reserved</option>
                        <option value="Cleaning">Cleaning</option>
                        <option value="Maintenance">Maintenance</option>
                      </select>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* ======================= GUEST DIRECTORY MODULE ======================= */}
          {activeModule === 'guests' && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-xl font-bold text-slate-100">Guest Directory & CRM</h1>
                  <p className="text-xs text-slate-400">Profiles, identification verification documents, and contact details</p>
                </div>
                <button
                  onClick={() => setModalType('add_guest')}
                  className="flex items-center space-x-1.5 px-3.5 py-2 rounded-md bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 text-xs font-bold hover:from-amber-400 hover:to-amber-500 shadow-md shadow-amber-500/20 transition-all"
                >
                  <Plus className="w-4 h-4 text-slate-950" />
                  <span>Register New Guest</span>
                </button>
              </div>

              <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 shadow-xl overflow-hidden backdrop-blur-sm">
                <table className="w-full text-xs text-left">
                  <thead className="bg-slate-950 text-slate-300 font-bold uppercase tracking-wider text-[11px] border-b border-slate-800">
                    <tr>
                      <th className="px-4 py-3">Guest ID</th>
                      <th className="px-4 py-3">Full Name</th>
                      <th className="px-4 py-3">Phone</th>
                      <th className="px-4 py-3">Email</th>
                      <th className="px-4 py-3">ID Proof Type</th>
                      <th className="px-4 py-3">ID Number</th>
                      <th className="px-4 py-3">Nationality</th>
                      <th className="px-4 py-3">Registered On</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/80">
                    {guests.map((g) => (
                      <tr key={g.id} className="hover:bg-slate-800/40 transition-colors">
                        <td className="px-4 py-3 font-semibold text-amber-400">#{g.id}</td>
                        <td className="px-4 py-3 font-bold text-slate-100">{g.fullName}</td>
                        <td className="px-4 py-3 text-slate-300">{g.phone}</td>
                        <td className="px-4 py-3 text-slate-400">{g.email}</td>
                        <td className="px-4 py-3 text-slate-300">{g.idProofType.replace('_', ' ')}</td>
                        <td className="px-4 py-3 font-medium text-slate-200">{g.idProofNumber}</td>
                        <td className="px-4 py-3 text-slate-400">{g.nationality}</td>
                        <td className="px-4 py-3 text-slate-500">{g.registeredDate.split(' ')[0]}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ======================= RESERVATIONS MODULE ======================= */}
          {activeModule === 'reservations' && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-xl font-bold text-slate-100">Reservation Management</h1>
                  <p className="text-xs text-slate-400">Bookings, date intervals, room allocations, and special guest requests</p>
                </div>
                <button
                  onClick={() => setModalType('add_reservation')}
                  className="flex items-center space-x-1.5 px-3.5 py-2 rounded-md bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 text-xs font-bold hover:from-amber-400 hover:to-amber-500 shadow-md shadow-amber-500/20 transition-all"
                >
                  <Plus className="w-4 h-4 text-slate-950" />
                  <span>Create New Booking</span>
                </button>
              </div>

              <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 shadow-xl overflow-hidden backdrop-blur-sm">
                <table className="w-full text-xs text-left">
                  <thead className="bg-slate-950 text-slate-300 font-bold uppercase tracking-wider text-[11px] border-b border-slate-800">
                    <tr>
                      <th className="px-4 py-3">ID</th>
                      <th className="px-4 py-3">Guest Name</th>
                      <th className="px-4 py-3">Room</th>
                      <th className="px-4 py-3">Check-In</th>
                      <th className="px-4 py-3">Check-Out</th>
                      <th className="px-4 py-3">Guests</th>
                      <th className="px-4 py-3">Special Requests</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/80">
                    {reservations.map((r) => (
                      <tr key={r.id} className="hover:bg-slate-800/40 transition-colors">
                        <td className="px-4 py-3 font-semibold text-amber-400">#{r.id}</td>
                        <td className="px-4 py-3 font-bold text-slate-100">{r.guestName}</td>
                        <td className="px-4 py-3 font-medium text-slate-200">Room {r.roomNumber} ({r.roomType})</td>
                        <td className="px-4 py-3 text-slate-400">{r.checkInDate}</td>
                        <td className="px-4 py-3 text-slate-400">{r.checkOutDate}</td>
                        <td className="px-4 py-3 text-slate-400">{r.numberOfGuests}</td>
                        <td className="px-4 py-3 text-slate-400 italic max-w-xs truncate">{r.specialRequests || '-'}</td>
                        <td className="px-4 py-3">
                          <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                            r.status === 'Checked_In'
                              ? 'bg-blue-500/20 text-blue-300 border border-blue-500/40'
                              : r.status === 'Confirmed'
                              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                              : r.status === 'Checked_Out'
                              ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                              : 'bg-red-500/20 text-red-300 border border-red-500/40'
                          }`}>
                            {r.status.replace('_', ' ')}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right space-x-2">
                          {r.status === 'Confirmed' && (
                            <button
                              onClick={() => {
                                setReservations(reservations.map(x => x.id === r.id ? { ...x, status: 'Checked_In' } : x));
                                setRooms(rooms.map(rm => rm.number === r.roomNumber ? { ...rm, status: 'Occupied' } : rm));
                                showNotification(`Guest ${r.guestName} checked in to Room ${r.roomNumber}!`);
                              }}
                              className="px-2.5 py-1 bg-emerald-600 text-white rounded text-[11px] font-bold hover:bg-emerald-500 shadow-sm"
                            >
                              Check-In
                            </button>
                          )}
                          {r.status === 'Confirmed' && (
                            <button
                              onClick={() => {
                                setReservations(reservations.map(x => x.id === r.id ? { ...x, status: 'Cancelled' } : x));
                                setRooms(rooms.map(rm => rm.number === r.roomNumber ? { ...rm, status: 'Available' } : rm));
                                showNotification(`Reservation #${r.id} cancelled & room released.`);
                              }}
                              className="px-2.5 py-1 bg-red-500/20 text-red-300 border border-red-500/40 rounded text-[11px] font-semibold hover:bg-red-500/30"
                            >
                              Cancel
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ======================= CHECK-IN MODULE ======================= */}
          {activeModule === 'checkin' && (
            <div className="space-y-6">
              <div>
                <h1 className="text-xl font-bold text-slate-100">Front Desk Guest Check-In</h1>
                <p className="text-xs text-slate-400">Verify guest identity, assign electronic keycards, and activate room occupancy</p>
              </div>

              <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 p-5 shadow-xl space-y-4 backdrop-blur-sm">
                <div className="text-xs font-bold uppercase tracking-wider text-slate-400">Pending Arrivals (Confirmed Bookings)</div>

                {reservations.filter(r => r.status === 'Confirmed').length === 0 ? (
                  <div className="p-8 text-center text-slate-400 text-xs">No pending arrivals scheduled for check-in right now.</div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {reservations.filter(r => r.status === 'Confirmed').map((res) => (
                      <div key={res.id} className="border border-slate-800 rounded-lg p-4 bg-slate-950/60 flex flex-col justify-between space-y-3">
                        <div>
                          <div className="flex items-center justify-between">
                            <span className="font-bold text-slate-100 text-sm">Res #{res.id}</span>
                            <span className="text-[10px] font-bold px-2 py-0.5 bg-amber-500/20 text-amber-300 border border-amber-500/40 rounded">Arrival Ready</span>
                          </div>
                          <div className="text-xs font-semibold text-slate-200 mt-1">{res.guestName}</div>
                          <div className="text-xs text-slate-400">Assigned: <b className="text-slate-100">Room {res.roomNumber}</b> ({res.roomType})</div>
                          <div className="text-xs text-slate-400 mt-1">Dates: {res.checkInDate} → {res.checkOutDate}</div>
                        </div>

                        <button
                          onClick={() => {
                            setReservations(reservations.map(r => r.id === res.id ? { ...r, status: 'Checked_In' } : r));
                            setRooms(rooms.map(rm => rm.number === res.roomNumber ? { ...rm, status: 'Occupied' } : rm));
                            const newLog: CheckInLog = {
                              id: checkIns.length + 1,
                              reservationId: res.id,
                              guestName: res.guestName,
                              roomNumber: res.roomNumber,
                              roomKeyIssued: `KeyCard #${res.roomNumber}`,
                              verifiedIdProof: true,
                              checkInTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
                              processedBy: `${currentUserRole} Staff`,
                              notes: 'Standard front desk check-in'
                            };
                            setCheckIns([newLog, ...checkIns]);
                            showNotification(`Guest ${res.guestName} successfully checked in to Room ${res.roomNumber}!`);
                          }}
                          className="w-full py-2 bg-emerald-600 text-white rounded text-xs font-bold hover:bg-emerald-500 flex items-center justify-center space-x-1.5 shadow-sm transition-all"
                        >
                          <KeyRound className="w-3.5 h-3.5" />
                          <span>Complete Check-In & Issue Key</span>
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Check-In History Log */}
              <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 shadow-xl overflow-hidden backdrop-blur-sm">
                <div className="p-4 border-b border-slate-800 text-xs font-bold uppercase tracking-wider text-slate-400">
                  Recent Check-In Activity Records
                </div>
                <table className="w-full text-xs text-left">
                  <thead className="bg-slate-950 text-slate-300 font-bold uppercase tracking-wider text-[11px] border-b border-slate-800">
                    <tr>
                      <th className="px-4 py-3">Arrival #</th>
                      <th className="px-4 py-3">Res ID</th>
                      <th className="px-4 py-3">Guest Name</th>
                      <th className="px-4 py-3">Room #</th>
                      <th className="px-4 py-3">Key Card Issued</th>
                      <th className="px-4 py-3">ID Verified</th>
                      <th className="px-4 py-3">Check-In Time</th>
                      <th className="px-4 py-3">Processed By</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/80">
                    {checkIns.map((ci) => (
                      <tr key={ci.id} className="hover:bg-slate-800/40">
                        <td className="px-4 py-3 font-semibold text-amber-400">CI-{ci.id}</td>
                        <td className="px-4 py-3 text-slate-400">#{ci.reservationId}</td>
                        <td className="px-4 py-3 font-bold text-slate-100">{ci.guestName}</td>
                        <td className="px-4 py-3 font-medium text-slate-200">Room {ci.roomNumber}</td>
                        <td className="px-4 py-3 text-slate-300">{ci.roomKeyIssued}</td>
                        <td className="px-4 py-3 text-emerald-400 font-semibold">Yes</td>
                        <td className="px-4 py-3 text-slate-400">{ci.checkInTime}</td>
                        <td className="px-4 py-3 text-slate-300">{ci.processedBy}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ======================= CHECK-OUT MODULE ======================= */}
          {activeModule === 'checkout' && (
            <div className="space-y-6">
              <div>
                <h1 className="text-xl font-bold text-slate-100">Front Desk Express Check-Out</h1>
                <p className="text-xs text-slate-400">Settle billing folios, inspect room condition, collect key cards, and trigger housekeeping</p>
              </div>

              <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 p-5 shadow-xl space-y-4 backdrop-blur-sm">
                <div className="text-xs font-bold uppercase tracking-wider text-slate-400">In-House Guests (Currently Checked-In)</div>

                {reservations.filter(r => r.status === 'Checked_In').length === 0 ? (
                  <div className="p-8 text-center text-slate-400 text-xs">No in-house guests currently checked-in.</div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {reservations.filter(r => r.status === 'Checked_In').map((res) => {
                      const inv = invoices.find(i => i.reservationId === res.id);
                      const balance = inv ? inv.balanceAmount : 0;
                      const isSettled = balance <= 0;

                      return (
                        <div key={res.id} className="border border-slate-800 rounded-lg p-4 bg-slate-950/60 flex flex-col justify-between space-y-3">
                          <div>
                            <div className="flex items-center justify-between">
                              <span className="font-bold text-slate-100 text-sm">Room {res.roomNumber}</span>
                              <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                                isSettled ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40' : 'bg-red-500/20 text-red-300 border border-red-500/40'
                              }`}>
                                {isSettled ? `Folio Settled (${formatCurrency(0)})` : `Due: ${formatCurrency(balance)}`}
                              </span>
                            </div>
                            <div className="text-xs font-semibold text-slate-200 mt-1">{res.guestName}</div>
                            <div className="text-xs text-slate-400">Stay: {res.checkInDate} → {res.checkOutDate}</div>
                          </div>

                          <button
                            onClick={() => {
                              setReservations(reservations.map(r => r.id === res.id ? { ...r, status: 'Checked_Out' } : r));
                              setRooms(rooms.map(rm => rm.number === res.roomNumber ? { ...rm, status: 'Cleaning' } : rm));
                              const newOutLog: CheckOutLog = {
                                id: checkOuts.length + 1,
                                reservationId: res.id,
                                guestName: res.guestName,
                                roomNumber: res.roomNumber,
                                roomInspected: true,
                                keyReturned: true,
                                checkOutTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
                                processedBy: `${currentUserRole} Staff`,
                                notes: 'Room scheduled for cleaning'
                              };
                              setCheckOuts([newOutLog, ...checkOuts]);
                              showNotification(`Check-out complete! Room ${res.roomNumber} is now marked 'Cleaning' for housekeeping.`);
                            }}
                            className="w-full py-2 bg-amber-600 text-white rounded text-xs font-bold hover:bg-amber-500 flex items-center justify-center space-x-1.5 shadow-sm transition-all"
                          >
                            <LogOut className="w-3.5 h-3.5" />
                            <span>Complete Check-Out & Release</span>
                          </button>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>

              {/* Check-Out History Log */}
              <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 shadow-xl overflow-hidden backdrop-blur-sm">
                <div className="p-4 border-b border-slate-800 text-xs font-bold uppercase tracking-wider text-slate-400">
                  Recent Check-Out & Room Release Records
                </div>
                <table className="w-full text-xs text-left">
                  <thead className="bg-slate-950 text-slate-300 font-bold uppercase tracking-wider text-[11px] border-b border-slate-800">
                    <tr>
                      <th className="px-4 py-3">Departure #</th>
                      <th className="px-4 py-3">Res ID</th>
                      <th className="px-4 py-3">Guest Name</th>
                      <th className="px-4 py-3">Room #</th>
                      <th className="px-4 py-3">Inspected</th>
                      <th className="px-4 py-3">Key Returned</th>
                      <th className="px-4 py-3">Check-Out Time</th>
                      <th className="px-4 py-3">Processed By</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/80">
                    {checkOuts.map((co) => (
                      <tr key={co.id} className="hover:bg-slate-800/40">
                        <td className="px-4 py-3 font-semibold text-amber-400">CO-{co.id}</td>
                        <td className="px-4 py-3 text-slate-400">#{co.reservationId}</td>
                        <td className="px-4 py-3 font-bold text-slate-100">{co.guestName}</td>
                        <td className="px-4 py-3 font-medium text-slate-200">Room {co.roomNumber}</td>
                        <td className="px-4 py-3 text-emerald-400 font-semibold">Yes</td>
                        <td className="px-4 py-3 text-emerald-400 font-semibold">Yes</td>
                        <td className="px-4 py-3 text-slate-400">{co.checkOutTime}</td>
                        <td className="px-4 py-3 text-slate-300">{co.processedBy}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ======================= BILLING & PAYMENTS MODULE ======================= */}
          {activeModule === 'billing' && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-xl font-bold text-slate-100">Billing, Invoices & Folio Settlements</h1>
                  <p className="text-xs text-slate-400">Multi-night calculations, service add-on rollups, tax rules, and receipt exports</p>
                </div>
              </div>

              <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 shadow-xl overflow-hidden backdrop-blur-sm">
                <table className="w-full text-xs text-left">
                  <thead className="bg-slate-950 text-slate-300 font-bold uppercase tracking-wider text-[11px] border-b border-slate-800">
                    <tr>
                      <th className="px-4 py-3">Invoice #</th>
                      <th className="px-4 py-3">Guest Name</th>
                      <th className="px-4 py-3">Room #</th>
                      <th className="px-4 py-3">Room Chg</th>
                      <th className="px-4 py-3">Services</th>
                      <th className="px-4 py-3">Tax ({settings.taxPercentage}%)</th>
                      <th className="px-4 py-3">Grand Total</th>
                      <th className="px-4 py-3">Paid</th>
                      <th className="px-4 py-3">Balance</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/80">
                    {invoices.map((inv) => (
                      <tr key={inv.id} className="hover:bg-slate-800/40 transition-colors">
                        <td className="px-4 py-3 font-semibold text-amber-400">{inv.invoiceNumber}</td>
                        <td className="px-4 py-3 font-bold text-slate-100">{inv.guestName}</td>
                        <td className="px-4 py-3 font-medium text-slate-200">Room {inv.roomNumber}</td>
                        <td className="px-4 py-3 text-slate-400">{formatCurrency(inv.roomCharges)}</td>
                        <td className="px-4 py-3 text-slate-400">{formatCurrency(inv.serviceCharges)}</td>
                        <td className="px-4 py-3 text-slate-400">{formatCurrency(inv.taxAmount)}</td>
                        <td className="px-4 py-3 font-bold text-slate-100">{formatCurrency(inv.grandTotal)}</td>
                        <td className="px-4 py-3 text-emerald-400 font-semibold">{formatCurrency(inv.paidAmount)}</td>
                        <td className={`px-4 py-3 font-bold ${inv.balanceAmount > 0 ? 'text-red-400' : 'text-slate-400'}`}>
                          {formatCurrency(inv.balanceAmount)}
                        </td>
                        <td className="px-4 py-3">
                          <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                            inv.paymentStatus === 'Paid'
                              ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                              : inv.paymentStatus === 'Partially_Paid'
                              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                              : 'bg-red-500/20 text-red-300 border border-red-500/40'
                          }`}>
                            {inv.paymentStatus.replace('_', ' ')}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right space-x-2">
                          {inv.balanceAmount > 0 && (
                            <button
                              onClick={() => {
                                setInvoices(invoices.map(i => i.id === inv.id ? { ...i, paidAmount: i.grandTotal, balanceAmount: 0, paymentStatus: 'Paid' } : i));
                                showNotification(`Full payment of ${formatCurrency(inv.balanceAmount)} recorded for ${inv.invoiceNumber}!`);
                              }}
                              className="px-2.5 py-1 bg-emerald-600 text-white rounded text-[11px] font-bold hover:bg-emerald-500 shadow-sm"
                            >
                              Settle Balance
                            </button>
                          )}
                          <button
                            onClick={() => {
                              setSelectedEntity(inv);
                              setModalType('view_folio');
                            }}
                            className="px-2.5 py-1 bg-slate-800 text-slate-200 border border-slate-700 rounded text-[11px] font-semibold hover:bg-slate-700"
                          >
                            View Folio
                          </button>
                          <button
                            onClick={async () => {
                              try {
                                const filename = await downloadFolioReceiptImage(inv, settings);
                                showNotification(`Receipt downloaded in image format: ${filename}`);
                              } catch {
                                showNotification('Failed to generate receipt image', 'error');
                              }
                            }}
                            className="px-2.5 py-1 bg-amber-500/10 hover:bg-amber-500/20 text-amber-300 border border-amber-500/30 rounded text-[11px] font-semibold transition-all inline-flex items-center space-x-1"
                            title="Download Folio Receipt in Image Format (PNG)"
                          >
                            <ImageIcon className="w-3 h-3 text-amber-400" />
                            <span>Image</span>
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ======================= HOTEL SERVICES MODULE ======================= */}
          {activeModule === 'services' && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-xl font-bold text-slate-100">Hotel Services & Guest Charges</h1>
                  <p className="text-xs text-slate-400">Dining, spa treatments, transportation, laundry, and room folios</p>
                </div>
              </div>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Catalog */}
                <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 p-5 shadow-xl space-y-3 backdrop-blur-sm">
                  <div className="text-xs font-bold uppercase tracking-wider text-slate-400">Services Catalog</div>
                  <div className="space-y-2">
                    {services.map((s) => (
                      <div key={s.id} className="p-3 border border-slate-800 rounded-lg flex items-center justify-between bg-slate-950/60">
                        <div>
                          <div className="font-bold text-slate-100 text-xs">{s.name}</div>
                          <div className="text-[11px] text-slate-400">{s.category} • {s.description}</div>
                        </div>
                        <div className="flex items-center space-x-3">
                          <span className="font-bold text-amber-400 text-xs">{formatCurrency(s.price)}</span>
                          <button
                            onClick={() => {
                              const activeRes = reservations.find(r => r.status === 'Checked_In');
                              if (!activeRes) {
                                showNotification('No checked-in guests available to charge.', 'error');
                                return;
                              }
                              const newCharge: GuestServiceCharge = {
                                id: guestServices.length + 1,
                                reservationId: activeRes.id,
                                guestName: activeRes.guestName,
                                roomNumber: activeRes.roomNumber,
                                serviceId: s.id,
                                serviceName: s.name,
                                quantity: 1,
                                unitPrice: s.price,
                                totalPrice: s.price,
                                serviceDate: new Date().toISOString().replace('T', ' ').slice(0, 19),
                                notes: 'Charged at front desk'
                              };
                              setGuestServices([newCharge, ...guestServices]);

                              // Update invoice
                              setInvoices(invoices.map(inv => {
                                if (inv.reservationId === activeRes.id) {
                                  const newServTotal = inv.serviceCharges + s.price;
                                  const newSubtotal = inv.roomCharges + newServTotal + inv.additionalCharges - inv.discount;
                                  const newTax = (newSubtotal * inv.taxPercentage) / 100;
                                  const newGrand = newSubtotal + newTax;
                                  return {
                                    ...inv,
                                    serviceCharges: newServTotal,
                                    subtotal: newSubtotal,
                                    taxAmount: newTax,
                                    grandTotal: newGrand,
                                    balanceAmount: newGrand - inv.paidAmount
                                  };
                                }
                                return inv;
                              }));

                              showNotification(`Charged ${formatCurrency(s.price)} (${s.name}) to Room ${activeRes.roomNumber}!`);
                            }}
                            className="px-2.5 py-1 bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 rounded text-[11px] font-bold hover:from-amber-400 hover:to-amber-500 shadow-sm"
                          >
                            + Charge Room
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Charged Services Log */}
                <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 p-5 shadow-xl space-y-3 backdrop-blur-sm">
                  <div className="flex items-center justify-between">
                    <div className="text-xs font-bold uppercase tracking-wider text-slate-400">Recent Guest Folio Postings & Service Dispatch</div>
                    <span className="text-[10px] text-amber-400">Automated Guest Alerts Active</span>
                  </div>
                  <div className="space-y-2 max-h-[400px] overflow-y-auto">
                    {guestServices.map((gs) => (
                      <div key={gs.id} className="p-3 border border-slate-800 rounded-lg flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-xs bg-slate-950/60">
                        <div>
                          <div className="flex items-center space-x-2">
                            <span className="font-bold text-slate-100">{gs.serviceName}</span>
                            <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                              gs.status === 'Completed'
                                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                                : gs.status === 'In_Transit'
                                ? 'bg-blue-500/20 text-blue-300 border border-blue-500/30'
                                : 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                            }`}>
                              {gs.status === 'Completed' ? 'Delivered' : gs.status === 'In_Transit' ? 'En Route' : 'Preparing'}
                            </span>
                          </div>
                          <div className="text-[11px] text-slate-400 mt-0.5">
                            {gs.guestName} • Room {gs.roomNumber} ({gs.quantity}x @ {formatCurrency(gs.unitPrice)})
                            {gs.completedAt && <span className="text-emerald-400 ml-1">• Delivered at {gs.completedAt}</span>}
                          </div>
                        </div>

                        <div className="flex items-center space-x-3">
                          <div className="font-bold text-amber-400">{formatCurrency(gs.totalPrice)}</div>
                          {gs.status !== 'Completed' && (
                            <button
                              onClick={() => {
                                const updated = guestServices.map(item =>
                                  item.id === gs.id ? { ...item, status: 'Completed' as const, completedAt: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) } : item
                                );
                                setGuestServices(updated);
                                if (propSetNotifications) {
                                  const alertNotif: GuestNotification = {
                                    id: `rs-staff-${gs.id}-${Date.now()}`,
                                    guestName: gs.guestName,
                                    roomNumber: gs.roomNumber,
                                    type: 'room_service',
                                    title: 'Room Service Completed & Delivered',
                                    message: `Your room service order for "${gs.serviceName}" has been completed and delivered to Room ${gs.roomNumber}. Enjoy your meal!`,
                                    timestamp: 'Just now',
                                    read: false,
                                    actionLabel: 'View Services',
                                    actionTab: 'services',
                                    orderId: gs.id
                                  };
                                  propSetNotifications(prev => [alertNotif, ...prev]);
                                }
                                showNotification(`Marked "${gs.serviceName}" delivered. Automated alert sent to guest!`);
                              }}
                              className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-[10px] font-bold shadow-sm transition-all whitespace-nowrap cursor-pointer"
                            >
                              Dispatch & Alert Guest
                            </button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* ======================= STAFF & ACCESS ROLES ======================= */}
          {activeModule === 'staff' && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-xl font-bold text-slate-100">Staff Directory & Access Roles</h1>
                  <p className="text-xs text-slate-400">Employee records, credentials, salary bands, and role access privileges</p>
                </div>
              </div>

              <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 shadow-xl overflow-hidden backdrop-blur-sm">
                <table className="w-full text-xs text-left">
                  <thead className="bg-slate-950 text-slate-300 font-bold uppercase tracking-wider text-[11px] border-b border-slate-800">
                    <tr>
                      <th className="px-4 py-3">Staff ID</th>
                      <th className="px-4 py-3">Full Name</th>
                      <th className="px-4 py-3">Username</th>
                      <th className="px-4 py-3">Role</th>
                      <th className="px-4 py-3">Phone</th>
                      <th className="px-4 py-3">Email</th>
                      <th className="px-4 py-3">Salary</th>
                      <th className="px-4 py-3">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-800/80">
                    {staffList.map((s) => (
                      <tr key={s.id} className="hover:bg-slate-800/40">
                        <td className="px-4 py-3 font-semibold text-amber-400">{s.employeeCode}</td>
                        <td className="px-4 py-3 font-bold text-slate-100">{s.fullName}</td>
                        <td className="px-4 py-3 font-medium text-slate-300">@{s.username}</td>
                        <td className="px-4 py-3">
                          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-800 text-slate-200 border border-slate-700">
                            {s.role}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-slate-400">{s.phone}</td>
                        <td className="px-4 py-3 text-slate-400">{s.email}</td>
                        <td className="px-4 py-3 font-medium text-slate-200">{formatCurrency(s.salary)}</td>
                        <td className="px-4 py-3 text-emerald-400 font-semibold">{s.status}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ======================= REPORTS MODULE ======================= */}
          {activeModule === 'reports' && (
            <div className="space-y-6">
              <div>
                <h1 className="text-xl font-bold text-slate-100">Financial & Operations Reports</h1>
                <p className="text-xs text-slate-400">Revenue breakdowns, room yield rates, and top hotel services analytics</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-slate-900/90 p-5 rounded-xl border border-slate-800/90 shadow-xl space-y-2 backdrop-blur-sm">
                  <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">Gross Billed Revenue</div>
                  <div className="text-2xl font-bold text-slate-100">{formatCurrency(invoices.reduce((a, b) => a + b.grandTotal, 0))}</div>
                  <div className="text-xs text-slate-400">Total itemized room + service charges</div>
                </div>

                <div className="bg-slate-900/90 p-5 rounded-xl border border-slate-800/90 shadow-xl space-y-2 backdrop-blur-sm">
                  <div className="text-xs font-bold text-emerald-400 uppercase tracking-wider">Collected Cash & Payments</div>
                  <div className="text-2xl font-bold text-emerald-400">{formatCurrency(totalRevenue)}</div>
                  <div className="text-xs text-slate-400">Directly settled at front desk (INR ₹)</div>
                </div>

                <div className="bg-slate-900/90 p-5 rounded-xl border border-slate-800/90 shadow-xl space-y-2 backdrop-blur-sm">
                  <div className="text-xs font-bold text-amber-400 uppercase tracking-wider">Outstanding Accounts Receivable</div>
                  <div className="text-2xl font-bold text-amber-400">{formatCurrency(invoices.reduce((a, b) => a + b.balanceAmount, 0))}</div>
                  <div className="text-xs text-slate-400">Pending departure settlements</div>
                </div>
              </div>
            </div>
          )}

          {/* ======================= SETTINGS MODULE ======================= */}
          {activeModule === 'settings' && (
            <div className="space-y-6">
              <div>
                <h1 className="text-xl font-bold text-slate-100">Property Configuration & Policies</h1>
                <p className="text-xs text-slate-400">Hotel legal profile, default tax rates, check-in / check-out hours, and folio branding</p>
              </div>

              <div className="bg-slate-900/90 rounded-xl border border-slate-800/90 p-6 shadow-xl max-w-3xl space-y-4 backdrop-blur-sm">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-xs font-bold text-slate-300">Hotel Legal Name</label>
                    <input
                      type="text"
                      value={settings.hotelName}
                      onChange={(e) => setSettings({ ...settings, hotelName: e.target.value })}
                      className="w-full mt-1 px-3 py-2 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 focus:outline-none focus:border-amber-500"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-bold text-slate-300">Tagline / Slogan</label>
                    <input
                      type="text"
                      value={settings.tagline}
                      onChange={(e) => setSettings({ ...settings, tagline: e.target.value })}
                      className="w-full mt-1 px-3 py-2 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 focus:outline-none focus:border-amber-500"
                    />
                  </div>

                  <div className="col-span-2">
                    <label className="text-xs font-bold text-slate-300">Physical Address</label>
                    <input
                      type="text"
                      value={settings.address}
                      onChange={(e) => setSettings({ ...settings, address: e.target.value })}
                      className="w-full mt-1 px-3 py-2 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 focus:outline-none focus:border-amber-500"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-bold text-slate-300">Telephone</label>
                    <input
                      type="text"
                      value={settings.phone}
                      onChange={(e) => setSettings({ ...settings, phone: e.target.value })}
                      className="w-full mt-1 px-3 py-2 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 focus:outline-none focus:border-amber-500"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-bold text-slate-300">Email Address</label>
                    <input
                      type="text"
                      value={settings.email}
                      onChange={(e) => setSettings({ ...settings, email: e.target.value })}
                      className="w-full mt-1 px-3 py-2 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 focus:outline-none focus:border-amber-500"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-bold text-slate-300">Standard Lodging Tax (%)</label>
                    <input
                      type="number"
                      value={settings.taxPercentage}
                      onChange={(e) => setSettings({ ...settings, taxPercentage: parseFloat(e.target.value) || 0 })}
                      className="w-full mt-1 px-3 py-2 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 focus:outline-none focus:border-amber-500"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-bold text-slate-300">Currency Symbol</label>
                    <input
                      type="text"
                      value={settings.currencySymbol}
                      onChange={(e) => setSettings({ ...settings, currencySymbol: e.target.value })}
                      className="w-full mt-1 px-3 py-2 bg-slate-950 border border-slate-800 rounded text-xs text-slate-100 focus:outline-none focus:border-amber-500"
                    />
                  </div>
                </div>

                <div className="pt-4 border-t border-slate-800 flex justify-end">
                  <button
                    onClick={() => showNotification('Hotel settings updated successfully!')}
                    className="px-4 py-2 bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 rounded text-xs font-bold hover:from-amber-400 hover:to-amber-500 shadow-md shadow-amber-500/20"
                  >
                    Save Property Settings
                  </button>
                </div>
              </div>
            </div>
          )}
        </main>
      </div>

      {/* ======================= MODALS ======================= */}
      {/* 1. Add Room Modal */}
      {modalType === 'add_room' && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 rounded-xl max-w-md w-full p-6 shadow-2xl border border-slate-800 space-y-4">
            <h3 className="text-base font-bold text-slate-100">Add New Room</h3>
            <div className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-300">Room Number</label>
                <input id="rm-num" placeholder="e.g. 305" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500" />
              </div>
              <div>
                <label className="font-semibold text-slate-300">Room Type</label>
                <select id="rm-type" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500">
                  <option value="Single">Single</option>
                  <option value="Double">Double</option>
                  <option value="Deluxe">Deluxe</option>
                  <option value="Suite">Suite</option>
                  <option value="Executive">Executive</option>
                </select>
              </div>
              <div>
                <label className="font-semibold text-slate-300">Price / Night ({settings.currencySymbol})</label>
                <input id="rm-price" defaultValue="3500.00" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500" />
              </div>
            </div>
            <div className="flex justify-end space-x-2 pt-3 border-t border-slate-800">
              <button onClick={() => setModalType(null)} className="px-3 py-1.5 border border-slate-700 rounded text-xs text-slate-300 hover:bg-slate-800">Cancel</button>
              <button
                onClick={() => {
                  const num = (document.getElementById('rm-num') as HTMLInputElement).value || '305';
                  const type = (document.getElementById('rm-type') as HTMLSelectElement).value as RoomType;
                  const price = parseFloat((document.getElementById('rm-price') as HTMLInputElement).value) || 150;
                  setRooms([...rooms, {
                    id: rooms.length + 1,
                    number: num,
                    type,
                    floor: parseInt(num[0]) || 3,
                    pricePerNight: price,
                    capacity: 2,
                    status: 'Available',
                    description: `${type} room with modern amenities`
                  }]);
                  setModalType(null);
                  showNotification(`Room ${num} added to inventory!`);
                }}
                className="px-3 py-1.5 bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 rounded text-xs font-bold hover:from-amber-400 hover:to-amber-500 shadow-md shadow-amber-500/20"
              >
                Save Room
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 2. Add Guest Modal */}
      {modalType === 'add_guest' && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 rounded-xl max-w-md w-full p-6 shadow-2xl border border-slate-800 space-y-4">
            <h3 className="text-base font-bold text-slate-100">Register New Guest Profile</h3>
            <div className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-300">Full Name</label>
                <input id="gst-name" placeholder="e.g. Jessica Pearson" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500" />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-semibold text-slate-300">Phone</label>
                  <input id="gst-phone" placeholder="+1-555-0199" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500" />
                </div>
                <div>
                  <label className="font-semibold text-slate-300">Email</label>
                  <input id="gst-email" placeholder="jessica@example.com" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-semibold text-slate-300">ID Proof Type</label>
                  <select id="gst-proof" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500">
                    <option value="Passport">Passport</option>
                    <option value="National_ID">National ID</option>
                    <option value="Driving_License">Driving License</option>
                  </select>
                </div>
                <div>
                  <label className="font-semibold text-slate-300">ID Number</label>
                  <input id="gst-idnum" placeholder="P984210" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500" />
                </div>
              </div>
            </div>
            <div className="flex justify-end space-x-2 pt-3 border-t border-slate-800">
              <button onClick={() => setModalType(null)} className="px-3 py-1.5 border border-slate-700 rounded text-xs text-slate-300 hover:bg-slate-800">Cancel</button>
              <button
                onClick={() => {
                  const name = (document.getElementById('gst-name') as HTMLInputElement).value || 'New Guest';
                  const phone = (document.getElementById('gst-phone') as HTMLInputElement).value || '+1-555-0100';
                  const email = (document.getElementById('gst-email') as HTMLInputElement).value || 'guest@example.com';
                  const proof = (document.getElementById('gst-proof') as HTMLSelectElement).value;
                  const idnum = (document.getElementById('gst-idnum') as HTMLInputElement).value || 'ID-999';
                  setGuests([...guests, {
                    id: guests.length + 1,
                    fullName: name,
                    gender: 'Male',
                    dob: '1990-01-01',
                    phone,
                    email,
                    idProofType: proof,
                    idProofNumber: idnum,
                    address: '742 Evergreen Terrace',
                    nationality: 'United States',
                    registeredDate: new Date().toISOString().replace('T', ' ').slice(0, 19)
                  }]);
                  setModalType(null);
                  showNotification(`Guest profile created for ${name}!`);
                }}
                className="px-3 py-1.5 bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 rounded text-xs font-bold hover:from-amber-400 hover:to-amber-500 shadow-md shadow-amber-500/20"
              >
                Register Guest
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 3. Add Reservation Modal */}
      {modalType === 'add_reservation' && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 rounded-xl max-w-md w-full p-6 shadow-2xl border border-slate-800 space-y-4">
            <h3 className="text-base font-bold text-slate-100">Create New Reservation</h3>
            <div className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-300">Select Guest</label>
                <select id="res-guest" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500">
                  {guests.map(g => (
                    <option key={g.id} value={`${g.id}|${g.fullName}`}>{g.fullName} (#{g.id})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="font-semibold text-slate-300">Select Available Room</label>
                <select id="res-room" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500">
                  {rooms.filter(r => r.status === 'Available').map(r => (
                    <option key={r.id} value={`${r.id}|${r.number}|${r.type}|${r.pricePerNight}`}>Room {r.number} ({r.type} - {formatCurrency(r.pricePerNight, 0)}/nt)</option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-semibold text-slate-300">Check-In Date</label>
                  <input id="res-cin" type="date" defaultValue="2026-08-30" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500" />
                </div>
                <div>
                  <label className="font-semibold text-slate-300">Check-Out Date</label>
                  <input id="res-cout" type="date" defaultValue="2026-09-02" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500" />
                </div>
              </div>
              <div>
                <label className="font-semibold text-slate-300">Special Requests</label>
                <input id="res-req" placeholder="e.g. Quiet room, high floor" className="w-full mt-1 p-2 bg-slate-950 border border-slate-800 text-slate-100 rounded focus:outline-none focus:border-amber-500" />
              </div>
            </div>
            <div className="flex justify-end space-x-2 pt-3 border-t border-slate-800">
              <button onClick={() => setModalType(null)} className="px-3 py-1.5 border border-slate-700 rounded text-xs text-slate-300 hover:bg-slate-800">Cancel</button>
              <button
                onClick={() => {
                  const guestVal = (document.getElementById('res-guest') as HTMLSelectElement).value.split('|');
                  const roomVal = (document.getElementById('res-room') as HTMLSelectElement).value.split('|');
                  const cin = (document.getElementById('res-cin') as HTMLInputElement).value || '2026-08-30';
                  const cout = (document.getElementById('res-cout') as HTMLInputElement).value || '2026-09-02';
                  const req = (document.getElementById('res-req') as HTMLInputElement).value;

                  const newRes: ReservationItem = {
                    id: reservations.length + 1,
                    guestId: parseInt(guestVal[0]),
                    guestName: guestVal[1],
                    guestPhone: '+1-555-0199',
                    roomId: parseInt(roomVal[0]),
                    roomNumber: roomVal[1],
                    roomType: roomVal[2] as RoomType,
                    checkInDate: cin,
                    checkOutDate: cout,
                    numberOfGuests: 2,
                    status: 'Confirmed',
                    specialRequests: req,
                    createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19)
                  };

                  setReservations([...reservations, newRes]);
                  setRooms(rooms.map(rm => rm.number === roomVal[1] ? { ...rm, status: 'Reserved' } : rm));

                  // Auto create initial invoice
                  const roomPrice = parseFloat(roomVal[3]) || 3500;
                  const nights = 3;
                  const roomTotal = roomPrice * nights;
                  const tax = (roomTotal * settings.taxPercentage) / 100;
                  const grand = roomTotal + tax;
                  const newInv: InvoiceItem = {
                    id: invoices.length + 1,
                    invoiceNumber: `INV-2026-00${invoices.length + 1}`,
                    reservationId: newRes.id,
                    guestName: newRes.guestName,
                    roomNumber: newRes.roomNumber,
                    roomType: newRes.roomType,
                    checkInDate: cin,
                    checkOutDate: cout,
                    numberOfNights: nights,
                    roomCharges: roomTotal,
                    serviceCharges: 0,
                    additionalCharges: 0,
                    discount: 0,
                    subtotal: roomTotal,
                    taxPercentage: 10,
                    taxAmount: tax,
                    grandTotal: grand,
                    paidAmount: 0,
                    balanceAmount: grand,
                    paymentStatus: 'Pending',
                    invoiceDate: new Date().toISOString().replace('T', ' ').slice(0, 19),
                    payments: []
                  };
                  setInvoices([...invoices, newInv]);

                  setModalType(null);
                  showNotification(`Reservation #${newRes.id} confirmed for ${newRes.guestName}!`);
                }}
                className="px-3 py-1.5 bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 rounded text-xs font-bold hover:from-amber-400 hover:to-amber-500 shadow-md shadow-amber-500/20"
              >
                Confirm Booking
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 4. View Folio Modal */}
      {modalType === 'view_folio' && selectedEntity && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 rounded-xl max-w-xl w-full p-6 shadow-2xl border border-slate-800 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <h3 className="text-base font-bold text-slate-100">Guest Folio: {selectedEntity.invoiceNumber}</h3>
                <p className="text-xs text-slate-400">{settings.hotelName}</p>
              </div>
              <button onClick={() => setModalType(null)} className="text-slate-400 hover:text-slate-200 text-lg">✕</button>
            </div>

            <div className="bg-slate-950 p-5 rounded-xl border border-slate-800 text-xs text-slate-300 space-y-3 max-h-80 overflow-y-auto">
              <div className="flex items-center justify-between border-b border-slate-800/80 pb-2">
                <span className="text-amber-400 font-bold font-serif text-sm">KR LUXE HOTEL & RESORT</span>
                <span className="text-[11px] text-slate-400">Official Guest Folio</span>
              </div>
              <div className="grid grid-cols-2 gap-2 text-xs">
                <div><span className="text-slate-500">Guest Name:</span> <strong className="text-white block">{selectedEntity.guestName}</strong></div>
                <div><span className="text-slate-500">Room Assigned:</span> <strong className="text-white block">Room {selectedEntity.roomNumber} ({selectedEntity.roomType})</strong></div>
                <div className="col-span-2"><span className="text-slate-500">Dates of Stay:</span> <span className="text-slate-200 block">{selectedEntity.checkInDate} to {selectedEntity.checkOutDate} ({selectedEntity.numberOfNights} Nights)</span></div>
              </div>
              <div className="border-t border-slate-800/80 pt-2 space-y-1.5">
                <div className="flex justify-between text-slate-300"><span>Room Tariff Charges:</span><span className="font-semibold text-white">{formatCurrency(selectedEntity.roomCharges)}</span></div>
                <div className="flex justify-between text-slate-300"><span>Concierge & Room Services:</span><span className="font-semibold text-white">{formatCurrency(selectedEntity.serviceCharges)}</span></div>
                <div className="flex justify-between text-emerald-400"><span>Privilege Discount:</span><span className="font-semibold">-{formatCurrency(selectedEntity.discount)}</span></div>
                <div className="flex justify-between text-slate-300"><span>Applicable Tax ({settings.taxPercentage}%):</span><span className="font-semibold text-white">{formatCurrency(selectedEntity.taxAmount)}</span></div>
              </div>
              <div className="border-t border-slate-800 pt-2 space-y-1">
                <div className="flex justify-between text-white font-bold text-sm"><span>Grand Total:</span><span>{formatCurrency(selectedEntity.grandTotal)}</span></div>
                <div className="flex justify-between text-emerald-400 font-semibold"><span>Paid Amount:</span><span>{formatCurrency(selectedEntity.paidAmount)}</span></div>
                <div className="flex justify-between text-amber-400 font-semibold"><span>Balance Due:</span><span>{formatCurrency(selectedEntity.balanceAmount)}</span></div>
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-end gap-2 pt-2 border-t border-slate-800">
              <button
                onClick={async () => {
                  try {
                    const filename = await downloadFolioReceiptImage(selectedEntity, settings);
                    showNotification(`Folio receipt downloaded as image: ${filename}`);
                  } catch {
                    showNotification('Failed to generate receipt image', 'error');
                  }
                }}
                className="px-3.5 py-1.5 bg-gradient-to-r from-amber-500 via-amber-400 to-amber-500 hover:from-amber-400 hover:to-amber-300 text-slate-950 rounded-lg text-xs font-bold flex items-center space-x-1.5 shadow-md shadow-amber-500/20 cursor-pointer transition-all"
                title="Download official hotel folio as high-resolution PNG image"
              >
                <ImageIcon className="w-3.5 h-3.5 text-slate-950" />
                <span>Download Folio Receipt (Image)</span>
              </button>
              <button
                onClick={() => {
                  const blob = new Blob([`GUEST FOLIO - ${selectedEntity.invoiceNumber}\nGuest: ${selectedEntity.guestName}\nTotal: ${formatCurrency(selectedEntity.grandTotal)}\nStatus: ${selectedEntity.paymentStatus}`], { type: 'text/plain' });
                  const url = URL.createObjectURL(blob);
                  const a = document.createElement('a');
                  a.href = url;
                  a.download = `${selectedEntity.invoiceNumber}_Receipt.txt`;
                  a.click();
                  showNotification('Receipt downloaded as text copy.');
                }}
                className="px-2.5 py-1.5 bg-slate-800/80 hover:bg-slate-700 text-slate-300 border border-slate-700 rounded-lg text-xs font-medium flex items-center space-x-1 cursor-pointer"
                title="Download plain text copy"
              >
                <Download className="w-3 h-3 text-slate-400" />
                <span>Text</span>
              </button>
              <button onClick={() => setModalType(null)} className="px-3 py-1.5 bg-slate-800 text-slate-300 border border-slate-700 rounded-lg text-xs font-semibold hover:bg-slate-700 cursor-pointer">Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
