import React, { useState, useEffect } from 'react';
import {
  BedDouble,
  CalendarCheck,
  Receipt,
  ConciergeBell,
  User,
  CheckCircle2,
  AlertCircle,
  Clock,
  DollarSign,
  Download,
  CreditCard,
  Building,
  Sparkles,
  ShieldAlert,
  ArrowRight,
  Plus,
  Coffee,
  Car,
  Utensils,
  LogOut,
  Bell,
  BellRing,
  Check,
  CheckCheck,
  X,
  ChevronRight,
  Compass,
  Image as ImageIcon
} from 'lucide-react';
import { downloadFolioReceiptImage } from '../utils/receiptImageGenerator';
import {
  AuthUser,
  RoomItem,
  ReservationItem,
  InvoiceItem,
  ServiceItem,
  GuestServiceCharge,
  HotelSettingsConfig,
  GuestNotification,
  GuestNotificationType
} from '../types/pms';
import { BookingModal, BookingData } from './BookingModal';
import { playLuxuryChime } from '../utils/audioChime';

interface CustomerPortalProps {
  currentUser: AuthUser;
  rooms: RoomItem[];
  setRooms: React.Dispatch<React.SetStateAction<RoomItem[]>>;
  reservations: ReservationItem[];
  setReservations: React.Dispatch<React.SetStateAction<ReservationItem[]>>;
  invoices: InvoiceItem[];
  setInvoices: React.Dispatch<React.SetStateAction<InvoiceItem[]>>;
  services: ServiceItem[];
  guestServices: GuestServiceCharge[];
  setGuestServices: React.Dispatch<React.SetStateAction<GuestServiceCharge[]>>;
  notifications?: GuestNotification[];
  setNotifications?: React.Dispatch<React.SetStateAction<GuestNotification[]>>;
  settings: HotelSettingsConfig;
  onLogout: () => void;
  onRequestAdmin: () => void;
}

export const CustomerPortal: React.FC<CustomerPortalProps> = ({
  currentUser,
  rooms,
  setRooms,
  reservations,
  setReservations,
  invoices,
  setInvoices,
  services,
  guestServices,
  setGuestServices,
  notifications: propNotifications,
  setNotifications: propSetNotifications,
  settings,
  onLogout,
  onRequestAdmin
}) => {
  const [activeTab, setActiveTab] = useState<'book' | 'bookings' | 'services' | 'profile'>('book');
  const [bookingRoom, setBookingRoom] = useState<RoomItem | null>(null);
  const [selectedInvoice, setSelectedInvoice] = useState<InvoiceItem | null>(null);
  const [payingInvoice, setPayingInvoice] = useState<InvoiceItem | null>(null);
  const [isDownloadingImage, setIsDownloadingImage] = useState<boolean>(false);
  const [notification, setNotification] = useState<{ message: string; type: 'success' | 'info' | 'error' } | null>(null);

  // Notification Drawer & Live Toast state
  const [showNotifDrawer, setShowNotifDrawer] = useState<boolean>(false);
  const [notifFilter, setNotifFilter] = useState<'all' | 'room_service' | 'checkout'>('all');
  const [liveAlertToast, setLiveAlertToast] = useState<GuestNotification | null>(null);
  const [dismissedUpcomingBanner, setDismissedUpcomingBanner] = useState<boolean>(false);
  const [localNotifications, setLocalNotifications] = useState<GuestNotification[]>([]);

  const notifications = propNotifications || localNotifications;
  const setNotifications = propSetNotifications || setLocalNotifications;

  const showNotification = (message: string, type: 'success' | 'info' | 'error' = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 3500);
  };

  const formatCurrency = (val: number, minimumFractionDigits = 2) => {
    return `${settings.currencySymbol}${val.toLocaleString('en-IN', {
      minimumFractionDigits,
      maximumFractionDigits: minimumFractionDigits
    })}`;
  };

  // Filter reservations and invoices for this customer
  // Match either by customer name or sample customer
  const customerReservations = reservations.filter(
    r => r.guestName.toLowerCase().includes(currentUser.name.toLowerCase()) ||
         currentUser.name.toLowerCase().includes(r.guestName.toLowerCase()) ||
         currentUser.email === 'alexander@example.com' && r.guestName.includes('Alexander')
  );

  const customerInvoices = invoices.filter(
    inv => inv.guestName.toLowerCase().includes(currentUser.name.toLowerCase()) ||
           currentUser.name.toLowerCase().includes(inv.guestName.toLowerCase()) ||
           currentUser.email === 'alexander@example.com' && inv.guestName.includes('Alexander')
  );

  // Filter notifications for this customer
  const userNotifications = notifications.filter(
    n => !n.guestEmail ||
         n.guestEmail === currentUser.email ||
         n.guestName.toLowerCase().includes(currentUser.name.toLowerCase()) ||
         (currentUser.email === 'alexander@example.com' && n.guestName.includes('Alexander'))
  );

  const unreadCount = userNotifications.filter(n => !n.read).length;

  const filteredNotifications = userNotifications.filter(n => {
    if (notifFilter === 'room_service') return n.type === 'room_service';
    if (notifFilter === 'checkout') return n.type === 'checkout_reminder' || n.type === 'late_checkout';
    return true;
  });

  // Current active reservation of the guest
  const activeStay = customerReservations.find(r => r.status === 'Checked_In') ||
                     customerReservations.find(r => r.status === 'Confirmed');

  // Room service orders belonging to this guest
  const customerOrders = guestServices.filter(
    gs => gs.guestName.toLowerCase().includes(currentUser.name.toLowerCase()) ||
          (activeStay && gs.roomNumber === activeStay.roomNumber) ||
          (currentUser.email === 'alexander@example.com' && gs.guestName.includes('Alexander'))
  );

  // 1. Automated Stay Check-Out Alert System
  useEffect(() => {
    if (!activeStay) return;
    const existingCheckOutAlert = userNotifications.find(
      n => n.type === 'checkout_reminder' && (n.roomNumber === activeStay.roomNumber || n.id.includes(String(activeStay.id)))
    );
    if (!existingCheckOutAlert) {
      const newAlert: GuestNotification = {
        id: `checkout-alert-${activeStay.id}`,
        guestName: currentUser.name,
        guestEmail: currentUser.email,
        roomNumber: activeStay.roomNumber,
        type: 'checkout_reminder',
        title: 'Upcoming Check-Out Notice',
        message: `Your stay in Room ${activeStay.roomNumber} (${activeStay.roomType} Suite) concludes on ${activeStay.checkOutDate} at 11:00 AM. Please ensure personal luggage is gathered. You may review your folio balance or request a late check-out privilege (until 4 PM).`,
        timestamp: 'Stay Schedule Advisory',
        read: false,
        actionLabel: 'Review Folio & Departure',
        actionTab: 'bookings'
      };
      setNotifications(prev => [newAlert, ...prev]);
    }
  }, [activeStay, currentUser.name, currentUser.email]);

  // 2. Automated Room Service Progression & Completion Alerts
  useEffect(() => {
    // 2a. Advance Preparing -> In_Transit after 4.5 seconds
    const preparingOrder = guestServices.find(
      gs => (gs.guestName.toLowerCase().includes(currentUser.name.toLowerCase()) || (activeStay && gs.roomNumber === activeStay.roomNumber)) &&
            gs.status === 'Preparing'
    );
    if (preparingOrder) {
      const prepTimer = setTimeout(() => {
        setGuestServices(prev =>
          prev.map(item => item.id === preparingOrder.id ? { ...item, status: 'In_Transit' } : item)
        );
      }, 4500);
      return () => clearTimeout(prepTimer);
    }

    // 2b. Advance In_Transit -> Completed after 5.5 seconds
    const transitOrder = guestServices.find(
      gs => (gs.guestName.toLowerCase().includes(currentUser.name.toLowerCase()) || (activeStay && gs.roomNumber === activeStay.roomNumber)) &&
            gs.status === 'In_Transit'
    );
    if (transitOrder) {
      const transitTimer = setTimeout(() => {
        const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        setGuestServices(prev =>
          prev.map(item => item.id === transitOrder.id ? { ...item, status: 'Completed', completedAt: timeStr } : item)
        );

        // Play gentle luxury audio chime
        playLuxuryChime();

        // Dispatch automated completion notification
        const alertNotif: GuestNotification = {
          id: `rs-auto-${transitOrder.id}-${Date.now()}`,
          guestName: currentUser.name,
          guestEmail: currentUser.email,
          roomNumber: transitOrder.roomNumber,
          type: 'room_service',
          title: 'Room Service Completed & Delivered',
          message: `Your culinary request for "${transitOrder.serviceName}" has been completed and delivered to Room ${transitOrder.roomNumber}. Prepared fresh with compliments of our culinary staff!`,
          timestamp: 'Just now',
          read: false,
          actionLabel: 'View Concierge Services',
          actionTab: 'services',
          orderId: transitOrder.id
        };

        setNotifications(prev => [alertNotif, ...prev]);
        setLiveAlertToast(alertNotif);
      }, 5500);
      return () => clearTimeout(transitTimer);
    }
  }, [guestServices, currentUser.name, currentUser.email, activeStay]);

  // Expedite room service delivery immediately for quick preview / demo
  const handleExpediteOrder = (orderId: number) => {
    const target = guestServices.find(gs => gs.id === orderId);
    if (!target) return;
    const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    setGuestServices(prev =>
      prev.map(item => item.id === orderId ? { ...item, status: 'Completed', completedAt: timeStr } : item)
    );
    playLuxuryChime();
    const alertNotif: GuestNotification = {
      id: `rs-expedite-${orderId}-${Date.now()}`,
      guestName: currentUser.name,
      guestEmail: currentUser.email,
      roomNumber: target.roomNumber,
      type: 'room_service',
      title: 'Room Service Completed & Delivered',
      message: `Your culinary order for "${target.serviceName}" has been completed and delivered to Room ${target.roomNumber}. Freshly plated with compliments of our culinary staff!`,
      timestamp: 'Just now',
      read: false,
      actionLabel: 'View Concierge Services',
      actionTab: 'services',
      orderId: target.id
    };
    setNotifications(prev => [alertNotif, ...prev]);
    setLiveAlertToast(alertNotif);
    showNotification(`Delivery for "${target.serviceName}" marked completed. Automated alert received!`);
  };

  // Request Late Check-Out Privilege (Until 4 PM)
  const handleRequestLateCheckout = (res: ReservationItem) => {
    const existingLateCheckout = guestServices.find(
      gs => gs.reservationId === res.id && gs.serviceName.toLowerCase().includes('late check-out')
    );
    if (existingLateCheckout) {
      showNotification('Late check-out privilege is already active for this room.', 'info');
      return;
    }

    const lateService = services.find(s => s.name.toLowerCase().includes('late check-out')) || {
      id: 6,
      name: 'Late Check-out Privilege (Until 4 PM)',
      price: 2000,
      category: 'Housekeeping',
      description: 'Guaranteed extended room access on departure day',
      status: 'Active' as const
    };

    const newCharge: GuestServiceCharge = {
      id: guestServices.length + 1,
      reservationId: res.id,
      guestName: currentUser.name,
      roomNumber: res.roomNumber,
      serviceId: lateService.id,
      serviceName: lateService.name,
      quantity: 1,
      unitPrice: lateService.price,
      totalPrice: lateService.price,
      serviceDate: new Date().toISOString().replace('T', ' ').slice(0, 19),
      status: 'Completed',
      completedAt: 'Approved',
      notes: 'Late check-out extension until 4:00 PM confirmed'
    };

    setGuestServices([newCharge, ...guestServices]);

    // Update associated invoice
    setInvoices(invoices.map(inv => {
      if (inv.reservationId === res.id) {
        const newServiceCharges = inv.serviceCharges + lateService.price;
        const newSubtotal = inv.roomCharges + newServiceCharges;
        const newTax = (newSubtotal * inv.taxPercentage) / 100;
        const newGrand = newSubtotal + newTax - inv.discount;
        const newBalance = newGrand - inv.paidAmount;
        return {
          ...inv,
          serviceCharges: newServiceCharges,
          subtotal: newSubtotal,
          taxAmount: newTax,
          grandTotal: newGrand,
          balanceAmount: newBalance,
          paymentStatus: newBalance <= 0 ? 'Paid' : 'Partially_Paid'
        };
      }
      return inv;
    }));

    // Update reservation special requests
    setReservations(reservations.map(r =>
      r.id === res.id
        ? { ...r, specialRequests: (r.specialRequests ? `${r.specialRequests} • ` : '') + 'Guaranteed Late Check-Out until 4:00 PM' }
        : r
    ));

    playLuxuryChime();

    const lateNotif: GuestNotification = {
      id: `late-checkout-confirm-${res.id}-${Date.now()}`,
      guestName: currentUser.name,
      guestEmail: currentUser.email,
      roomNumber: res.roomNumber,
      type: 'late_checkout',
      title: 'Late Check-Out Privilege Confirmed',
      message: `Your departure from Room ${res.roomNumber} has been extended to 4:00 PM. Enjoy your extra leisurely afternoon at KR LUXE HOTEL!`,
      timestamp: 'Just now',
      read: false,
      actionLabel: 'Review Updated Folio',
      actionTab: 'bookings'
    };

    setNotifications(prev => [lateNotif, ...prev]);
    setLiveAlertToast(lateNotif);
    showNotification(`Late check-out privilege granted until 4:00 PM for Room ${res.roomNumber}!`);
  };

  const handleMarkAsRead = (id: string) => {
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
  };

  const handleMarkAllAsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
    showNotification('All guest alerts marked as read.');
  };

  const handleClearNotifications = () => {
    setNotifications(prev => prev.filter(n => n.guestEmail && n.guestEmail !== currentUser.email));
    showNotification('Cleared guest alerts.');
  };

  const handleNotificationAction = (n: GuestNotification) => {
    handleMarkAsRead(n.id);
    setShowNotifDrawer(false);
    if (n.actionTab === 'services') {
      setActiveTab('services');
    } else if (n.actionTab === 'bookings') {
      setActiveTab('bookings');
    } else if (n.actionTab === 'billing') {
      setActiveTab('bookings');
      if (customerInvoices.length > 0) {
        setSelectedInvoice(customerInvoices[0]);
      }
    }
  };

  // Calculate nights between dates
  const calculateNights = (inDate: string, outDate: string) => {
    try {
      const d1 = new Date(inDate);
      const d2 = new Date(outDate);
      const diffTime = Math.abs(d2.getTime() - d1.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays > 0 ? diffDays : 1;
    } catch {
      return 1;
    }
  };

  // Handle Room Booking with itemized dates, costs, promo discount, and payment preference
  const handleConfirmBooking = (bookingData: BookingData) => {
    if (!bookingRoom) return;

    const {
      checkInDate,
      checkOutDate,
      numberOfGuests,
      specialRequests,
      discountAmount,
      promoCode,
      paymentPreference,
      guestName,
      guestPhone,
      guestEmail,
      calculatedGrandTotal,
      calculatedTax,
      calculatedRoomTotal,
      numberOfNights
    } = bookingData;

    const isPrepaid = paymentPreference === 'prepay_now';

    // Create reservation
    const newReservation: ReservationItem = {
      id: reservations.length + 1,
      guestId: 100 + reservations.length,
      guestName: guestName || currentUser.name,
      guestPhone: guestPhone || currentUser.phone || '+91 98765 43210',
      roomId: bookingRoom.id,
      roomNumber: bookingRoom.number,
      roomType: bookingRoom.type,
      checkInDate,
      checkOutDate,
      numberOfGuests,
      status: 'Confirmed',
      specialRequests: promoCode ? `${specialRequests} [Voucher: ${promoCode}]`.trim() : specialRequests,
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19)
    };

    // Create initial invoice with full itemization
    const newInvoice: InvoiceItem = {
      id: invoices.length + 1,
      invoiceNumber: `INV-2026-${1000 + invoices.length + 1}`,
      reservationId: newReservation.id,
      guestName: guestName || currentUser.name,
      roomNumber: bookingRoom.number,
      roomType: bookingRoom.type,
      checkInDate,
      checkOutDate,
      numberOfNights,
      roomCharges: bookingRoom.pricePerNight * numberOfNights,
      serviceCharges: 0,
      additionalCharges: 0,
      subtotal: calculatedRoomTotal,
      discount: discountAmount,
      taxPercentage: settings.taxPercentage,
      taxAmount: calculatedTax,
      grandTotal: calculatedGrandTotal,
      paidAmount: isPrepaid ? calculatedGrandTotal : 0,
      balanceAmount: isPrepaid ? 0 : calculatedGrandTotal,
      paymentStatus: isPrepaid ? 'Paid' : 'Pending',
      invoiceDate: new Date().toISOString().slice(0, 10),
      payments: isPrepaid ? [{
        id: 1,
        amount: calculatedGrandTotal,
        method: 'UPI',
        transactionRef: `TXN-PRE-${Date.now().toString().slice(-6)}`,
        date: new Date().toISOString().slice(0, 10),
        notes: 'Prepaid online reservation via UPI / Card'
      }] : []
    };

    // Update room status to Reserved
    setRooms(rooms.map(r => r.id === bookingRoom.id ? { ...r, status: 'Reserved' } : r));
    setReservations([newReservation, ...reservations]);
    setInvoices([newInvoice, ...invoices]);

    showNotification(
      isPrepaid
        ? `Reservation confirmed & prepaid for Room ${bookingRoom.number}! Folio #${newInvoice.invoiceNumber} marked Settled.`
        : `Reservation confirmed for Room ${bookingRoom.number}! Folio #${newInvoice.invoiceNumber} generated.`
    );
    setBookingRoom(null);
    setActiveTab('bookings');
  };

  // Handle Customer Service Order
  const handleOrderService = (service: ServiceItem) => {
    // Find customer's active reservation
    const activeRes = customerReservations.find(r => r.status === 'Checked_In' || r.status === 'Confirmed') || customerReservations[0];

    if (!activeRes) {
      showNotification('Please book a room first before ordering room service.', 'error');
      return;
    }

    const newCharge: GuestServiceCharge = {
      id: guestServices.length + 1,
      reservationId: activeRes.id,
      guestName: currentUser.name,
      roomNumber: activeRes.roomNumber,
      serviceId: service.id,
      serviceName: service.name,
      quantity: 1,
      unitPrice: service.price,
      totalPrice: service.price,
      serviceDate: new Date().toISOString().replace('T', ' ').slice(0, 19),
      status: 'Preparing',
      notes: 'Guest suite culinary order'
    };

    setGuestServices([newCharge, ...guestServices]);

    // Update associated invoice
    setInvoices(invoices.map(inv => {
      if (inv.reservationId === activeRes.id) {
        const newServiceCharges = inv.serviceCharges + service.price;
        const newSubtotal = inv.roomCharges + newServiceCharges;
        const newTax = (newSubtotal * inv.taxPercentage) / 100;
        const newGrand = newSubtotal + newTax - inv.discount;
        const newBalance = newGrand - inv.paidAmount;
        return {
          ...inv,
          serviceCharges: newServiceCharges,
          subtotal: newSubtotal,
          taxAmount: newTax,
          grandTotal: newGrand,
          balanceAmount: newBalance,
          paymentStatus: newBalance <= 0 ? 'Paid' : 'Partially_Paid'
        };
      }
      return inv;
    }));

    showNotification(
      `Order placed for "${service.name}" (${formatCurrency(service.price)})! Kitchen preparation has begun for Suite ${activeRes.roomNumber}. Automated delivery alert scheduled.`
    );
  };

  // Handle Online Payment of Invoice
  const handleSettleBalance = (invoiceId: number) => {
    setInvoices(invoices.map(inv => {
      if (inv.id === invoiceId) {
        return {
          ...inv,
          paidAmount: inv.grandTotal,
          balanceAmount: 0,
          paymentStatus: 'Paid',
          payments: [
            ...inv.payments,
            {
              id: Date.now(),
              amount: inv.balanceAmount,
              method: 'UPI',
              transactionRef: `TXN-${Math.floor(100000 + Math.random() * 900000)}`,
              date: new Date().toISOString().slice(0, 10),
              notes: 'Customer portal online instant payment'
            }
          ]
        };
      }
      return inv;
    }));

    showNotification('Payment processed successfully! Your folio balance is now ₹0.00.');
    setPayingInvoice(null);
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-slate-950 text-slate-100 flex flex-col">
      {/* Toast Notification */}
      {notification && (
        <div className="fixed bottom-6 right-6 z-50 animate-bounce">
          <div className={`px-4 py-3 rounded-xl shadow-2xl border flex items-center space-x-2 text-sm font-medium backdrop-blur-md ${
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

      {/* Guest Welcome Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-900/95 to-slate-950 border-b border-slate-800 px-6 py-6 sm:px-8">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div className="space-y-1">
            <div className="flex items-center space-x-3">
              <span className="text-xs uppercase tracking-widest text-amber-400 font-bold">
                Guest Experience Portal
              </span>
              <span className="px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 text-[11px] font-bold border border-amber-500/30">
                {currentUser.memberTier || 'Platinum Member'}
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-bold text-white font-serif tracking-tight">
              Welcome to KR LUXE HOTEL, {currentUser.name}
            </h1>
            <p className="text-xs text-slate-400">
              Account: <span className="text-slate-300 font-medium">{currentUser.email}</span> • Book luxury suites, order private dining & services, and manage your billing.
            </p>
          </div>

          {/* Quick Switch / Action Buttons */}
          <div className="flex items-center space-x-2.5">
            {/* Automated Alerts Notification Bell */}
            <button
              onClick={() => setShowNotifDrawer(prev => !prev)}
              className={`relative px-3.5 py-2 rounded-xl border text-xs font-semibold transition-all flex items-center space-x-2 shadow-sm ${
                showNotifDrawer
                  ? 'bg-amber-500 text-slate-950 border-amber-400 font-bold shadow-amber-500/20'
                  : unreadCount > 0
                  ? 'bg-slate-900 border-amber-500/60 text-amber-300 hover:bg-slate-850'
                  : 'bg-slate-800/80 hover:bg-slate-700/80 border-slate-700 text-slate-300 hover:text-white'
              }`}
              title="Guest Stay & Room Service Alerts"
            >
              <div className="relative">
                <Bell className={`w-4 h-4 ${unreadCount > 0 ? 'text-amber-400' : 'text-slate-300'}`} />
                {unreadCount > 0 && (
                  <span className="absolute -top-1.5 -right-2 flex h-4 min-w-4 px-1 items-center justify-center rounded-full bg-red-500 text-white text-[9px] font-black shadow-md animate-pulse">
                    {unreadCount}
                  </span>
                )}
              </div>
              <span className="hidden sm:inline">Guest Alerts</span>
              {unreadCount > 0 && (
                <span className="px-1.5 py-0.2 rounded-full bg-amber-500/20 text-amber-300 text-[10px] font-bold border border-amber-500/30">
                  {unreadCount} new
                </span>
              )}
            </button>

            <button
              onClick={onRequestAdmin}
              className="px-3.5 py-2 rounded-xl bg-slate-800/80 hover:bg-slate-700/80 border border-slate-700 text-xs font-semibold text-slate-300 hover:text-amber-300 transition-all flex items-center space-x-2 shadow-sm"
              title="Switch to Admin login"
            >
              <ShieldAlert className="w-3.5 h-3.5 text-amber-400" />
              <span>Admin Access</span>
            </button>

            <button
              onClick={onLogout}
              className="px-3.5 py-2 rounded-xl bg-red-950/50 hover:bg-red-900/60 border border-red-800/60 text-xs font-semibold text-red-300 transition-all flex items-center space-x-1.5 shadow-sm"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>Sign Out</span>
            </button>
          </div>
        </div>

        {/* ================= UPCOMING CHECK-OUT NOTICE BANNER ================= */}
        {activeStay && !dismissedUpcomingBanner && (
          <div className="max-w-7xl mx-auto mt-4 p-4 rounded-2xl bg-gradient-to-r from-amber-950/60 via-slate-900 to-amber-950/40 border border-amber-500/40 shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4 animate-fadeIn relative">
            <div className="flex items-start sm:items-center space-x-3.5 relative z-10">
              <div className="w-10 h-10 rounded-xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400 flex-shrink-0">
                <Clock className="w-5 h-5 animate-pulse" />
              </div>
              <div>
                <div className="flex items-center space-x-2">
                  <span className="text-xs font-bold uppercase tracking-wider text-amber-400">Upcoming Check-Out Notice</span>
                  <span className="px-2 py-0.2 rounded-md bg-amber-500/10 text-amber-300 text-[10px] font-bold border border-amber-500/30">
                    Suite {activeStay.roomNumber}
                  </span>
                </div>
                <p className="text-xs text-slate-200 mt-0.5 leading-relaxed">
                  Your scheduled departure is on <strong className="text-amber-300">{activeStay.checkOutDate} at 11:00 AM</strong>. Review your folio balance or extend your stay with late check-out privileges.
                </p>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-2 relative z-10">
              <button
                onClick={() => {
                  setActiveTab('bookings');
                  if (customerInvoices.length > 0) {
                    setSelectedInvoice(customerInvoices[0]);
                  }
                }}
                className="px-3.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-bold transition-all flex items-center space-x-1.5 shadow-sm cursor-pointer"
              >
                <Receipt className="w-3.5 h-3.5 text-amber-400" />
                <span>Review Folio Statement</span>
              </button>

              <button
                onClick={() => handleRequestLateCheckout(activeStay)}
                className="px-3.5 py-1.5 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 text-xs font-bold transition-all flex items-center space-x-1.5 shadow-md shadow-amber-500/20 cursor-pointer"
              >
                <Sparkles className="w-3.5 h-3.5" />
                <span>Request Late Check-Out (4 PM)</span>
              </button>

              <button
                onClick={() => setDismissedUpcomingBanner(true)}
                className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-all text-xs cursor-pointer"
                title="Dismiss reminder"
              >
                ✕
              </button>
            </div>
          </div>
        )}

        {/* Portal Navigation Tabs */}
        <div className="max-w-7xl mx-auto mt-6 flex flex-wrap gap-2 border-t border-slate-800/80 pt-4">
          <button
            onClick={() => setActiveTab('book')}
            className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'book'
                ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 shadow-md shadow-amber-500/20'
                : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
            }`}
          >
            <BedDouble className="w-4 h-4" />
            <span>Book a Luxury Room</span>
          </button>

          <button
            onClick={() => setActiveTab('bookings')}
            className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'bookings'
                ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 shadow-md shadow-amber-500/20'
                : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
            }`}
          >
            <CalendarCheck className="w-4 h-4" />
            <span>My Bookings & Invoices ({customerReservations.length})</span>
          </button>

          <button
            onClick={() => setActiveTab('services')}
            className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'services'
                ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 shadow-md shadow-amber-500/20'
                : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
            }`}
          >
            <ConciergeBell className="w-4 h-4" />
            <span>Order Services & Dining</span>
          </button>

          <button
            onClick={() => setActiveTab('profile')}
            className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'profile'
                ? 'bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 shadow-md shadow-amber-500/20'
                : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
            }`}
          >
            <User className="w-4 h-4" />
            <span>My Profile</span>
          </button>
        </div>
      </div>

      {/* Portal Content Area */}
      <div className="flex-1 max-w-7xl w-full mx-auto p-6 sm:p-8 space-y-6">
        {/* ==================== TAB 1: BOOK A ROOM ==================== */}
        {activeTab === 'book' && (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold text-white font-serif">Available Rooms & Suites</h2>
                <p className="text-xs text-slate-400">Select your preferred accommodation at KR LUXE HOTEL. All prices shown in Indian Rupees (₹).</p>
              </div>
              <div className="text-xs font-medium text-amber-400 flex items-center gap-1.5">
                <Sparkles className="w-4 h-4" />
                <span>Complimentary Breakfast & Wi-Fi included</span>
              </div>
            </div>

            {/* Room Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {rooms.map((room) => {
                const isAvailable = room.status === 'Available';
                return (
                  <div
                    key={room.id}
                    className={`rounded-2xl border transition-all overflow-hidden flex flex-col ${
                      isAvailable
                        ? 'bg-slate-900/90 border-slate-800 hover:border-amber-500/60 shadow-xl'
                        : 'bg-slate-900/40 border-slate-800/50 opacity-75'
                    }`}
                  >
                    <div className="p-5 flex-1 space-y-3">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-amber-400">Room {room.number}</span>
                        <span className={`text-[10px] font-bold px-2.5 py-0.5 rounded-full ${
                          isAvailable
                            ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                            : 'bg-slate-800 text-slate-400 border border-slate-700'
                        }`}>
                          {room.status}
                        </span>
                      </div>

                      <div>
                        <h3 className="text-base font-bold text-slate-100">{room.type} Suite</h3>
                        <p className="text-xs text-slate-400 mt-1 leading-relaxed">{room.description}</p>
                      </div>

                      <div className="pt-2 flex items-center space-x-4 text-xs text-slate-400">
                        <span>Floor: <strong className="text-slate-200">{room.floor}</strong></span>
                        <span>Capacity: <strong className="text-slate-200">{room.capacity} Guests</strong></span>
                      </div>
                    </div>

                    <div className="p-5 bg-slate-950/60 border-t border-slate-800/80 flex items-center justify-between">
                      <div>
                        <span className="text-xl font-bold text-amber-400">{formatCurrency(room.pricePerNight, 0)}</span>
                        <span className="text-xs text-slate-400"> / night</span>
                      </div>

                      <button
                        disabled={!isAvailable}
                        onClick={() => setBookingRoom(room)}
                        className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center space-x-1.5 ${
                          isAvailable
                            ? 'bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 shadow-md shadow-amber-500/20 cursor-pointer'
                            : 'bg-slate-800 text-slate-500 cursor-not-allowed'
                        }`}
                      >
                        <span>{isAvailable ? 'Book This Room' : 'Unavailable'}</span>
                        {isAvailable && <ArrowRight className="w-3.5 h-3.5" />}
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* ==================== TAB 2: MY BOOKINGS & INVOICES ==================== */}
        {activeTab === 'bookings' && (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold text-white font-serif">My Reservations & Invoices</h2>
                <p className="text-xs text-slate-400">Track your check-in dates, view itemized charges in ₹, and download guest folio statements.</p>
              </div>
              <button
                onClick={() => setActiveTab('book')}
                className="px-3 py-1.5 bg-amber-500 text-slate-950 rounded-xl text-xs font-bold hover:bg-amber-400 transition-all flex items-center space-x-1.5"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>New Booking</span>
              </button>
            </div>

            {customerReservations.length === 0 ? (
              <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-12 text-center space-y-4">
                <CalendarCheck className="w-12 h-12 text-slate-600 mx-auto" />
                <div>
                  <h3 className="text-base font-bold text-slate-200">No Reservations Found</h3>
                  <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
                    You haven't made any bookings yet under this account. Explore our luxury suites and reserve your stay.
                  </p>
                </div>
                <button
                  onClick={() => setActiveTab('book')}
                  className="px-4 py-2 bg-amber-500 text-slate-950 rounded-xl text-xs font-bold hover:bg-amber-400 transition-all"
                >
                  Explore Available Rooms
                </button>
              </div>
            ) : (
              <div className="space-y-4">
                {customerReservations.map((res) => {
                  const inv = customerInvoices.find(i => i.reservationId === res.id) || customerInvoices[0];
                  return (
                    <div key={res.id} className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
                      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-800/80 pb-4">
                        <div>
                          <div className="flex items-center space-x-2">
                            <span className="text-sm font-bold text-amber-400">Room {res.roomNumber}</span>
                            <span className="text-slate-400">•</span>
                            <span className="text-sm font-bold text-white">{res.roomType} Suite</span>
                          </div>
                          <div className="text-xs text-slate-400 mt-0.5">
                            Reservation ID: <span className="font-semibold text-slate-200">RES-00{res.id}</span>
                          </div>
                        </div>

                        <div className="flex items-center space-x-2">
                          <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                            res.status === 'Confirmed'
                              ? 'bg-blue-500/20 text-blue-300 border border-blue-500/40'
                              : res.status === 'Checked_In'
                              ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                              : 'bg-slate-800 text-slate-400'
                          }`}>
                            {res.status.replace('_', ' ')}
                          </span>

                          {inv && (
                            <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                              inv.paymentStatus === 'Paid'
                                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                                : 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                            }`}>
                              {inv.paymentStatus === 'Paid' ? 'Folio Settled (₹0.00)' : `Due: ${formatCurrency(inv.balanceAmount)}`}
                            </span>
                          )}
                        </div>
                      </div>

                      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
                        <div>
                          <span className="text-slate-500 block">Check-In</span>
                          <span className="font-semibold text-slate-200">{res.checkInDate}</span>
                        </div>
                        <div>
                          <span className="text-slate-500 block">Check-Out</span>
                          <span className="font-semibold text-slate-200">{res.checkOutDate}</span>
                        </div>
                        <div>
                          <span className="text-slate-500 block">Guests</span>
                          <span className="font-semibold text-slate-200">{res.numberOfGuests} Person(s)</span>
                        </div>
                        <div>
                          <span className="text-slate-500 block">Grand Total</span>
                          <span className="font-bold text-amber-400">{inv ? formatCurrency(inv.grandTotal) : 'N/A'}</span>
                        </div>
                      </div>

                      {inv && (
                        <div className="pt-2 flex flex-wrap items-center justify-end gap-2 border-t border-slate-800/80">
                          <button
                            onClick={() => setSelectedInvoice(inv)}
                            className="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl text-xs font-semibold transition-all flex items-center space-x-1.5 border border-slate-700 cursor-pointer"
                          >
                            <Receipt className="w-3.5 h-3.5 text-amber-400" />
                            <span>View Folio Statement</span>
                          </button>

                          <button
                            onClick={async () => {
                              try {
                                const filename = await downloadFolioReceiptImage(inv, settings);
                                showNotification(`Receipt downloaded as image: ${filename}`);
                              } catch {
                                showNotification('Failed to generate receipt image', 'error');
                              }
                            }}
                            className="px-3.5 py-1.5 bg-amber-500/10 hover:bg-amber-500/20 text-amber-300 border border-amber-500/30 rounded-xl text-xs font-semibold transition-all flex items-center space-x-1.5 cursor-pointer"
                            title="Download official hotel folio as PNG image"
                          >
                            <ImageIcon className="w-3.5 h-3.5 text-amber-400" />
                            <span>Download Receipt (Image)</span>
                          </button>

                          {inv.balanceAmount > 0 && (
                            <button
                              onClick={() => setPayingInvoice(inv)}
                              className="px-3.5 py-1.5 bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-400 hover:to-emerald-500 text-white rounded-xl text-xs font-bold transition-all flex items-center space-x-1.5 shadow-md shadow-emerald-500/20"
                            >
                              <CreditCard className="w-3.5 h-3.5" />
                              <span>Settle Balance ({formatCurrency(inv.balanceAmount)})</span>
                            </button>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* ==================== TAB 3: SERVICES & DINING ==================== */}
        {activeTab === 'services' && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <h2 className="text-xl font-bold text-white font-serif">In-Room Dining & Hotel Services</h2>
                <p className="text-xs text-slate-400">Order directly to your suite. Automated alerts notify you the moment your order is prepared and delivered.</p>
              </div>
              <div className="flex items-center space-x-2 text-xs text-amber-400 bg-amber-500/10 px-3 py-1.5 rounded-xl border border-amber-500/20 w-fit">
                <Bell className="w-3.5 h-3.5" />
                <span>Automated Delivery Alerts Active</span>
              </div>
            </div>

            {/* LIVE ROOM SERVICE DELIVERY TRACKER */}
            {customerOrders.length > 0 && (
              <div className="bg-slate-900/95 border border-slate-800 rounded-2xl p-5 shadow-xl space-y-4 backdrop-blur-sm">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-800 pb-3">
                  <div className="flex items-center space-x-2">
                    <ConciergeBell className="w-4 h-4 text-amber-400" />
                    <h3 className="font-bold text-sm text-white font-serif">Suite Orders & Delivery Tracker</h3>
                  </div>
                  <span className="text-[11px] text-slate-400">
                    {customerOrders.some(o => o.status !== 'Completed')
                      ? 'Live preparation in progress'
                      : 'All recent orders completed & delivered'}
                  </span>
                </div>

                <div className="space-y-3">
                  {customerOrders.map((order) => {
                    const isCompleted = order.status === 'Completed';
                    const isTransit = order.status === 'In_Transit';
                    const isPrep = order.status === 'Preparing';

                    return (
                      <div key={order.id} className="p-4 bg-slate-950/70 rounded-xl border border-slate-800 space-y-3">
                        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                          <div>
                            <div className="flex items-center space-x-2">
                              <span className="font-bold text-white text-sm">{order.serviceName}</span>
                              <span className="text-xs text-amber-400 font-semibold">{formatCurrency(order.totalPrice)}</span>
                            </div>
                            <div className="text-[11px] text-slate-400 mt-0.5">
                              Suite {order.roomNumber} • Ordered on {order.serviceDate}
                              {order.completedAt && <span className="text-emerald-400 ml-1.5 font-medium">• Delivered at {order.completedAt}</span>}
                            </div>
                          </div>

                          <div className="flex items-center space-x-2">
                            <span className={`px-2.5 py-1 rounded-full text-[11px] font-bold flex items-center space-x-1.5 ${
                              isCompleted
                                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                                : isTransit
                                ? 'bg-blue-500/20 text-blue-300 border border-blue-500/40 animate-pulse'
                                : 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                            }`}>
                              <span className={`w-2 h-2 rounded-full ${isCompleted ? 'bg-emerald-400' : isTransit ? 'bg-blue-400' : 'bg-amber-400 animate-ping'}`} />
                              <span>{isCompleted ? 'Completed & Delivered' : isTransit ? 'En Route to Suite' : 'In Preparation'}</span>
                            </span>

                            {!isCompleted && (
                              <button
                                onClick={() => handleExpediteOrder(order.id)}
                                className="px-2.5 py-1 bg-amber-500 hover:bg-amber-400 text-slate-950 text-[10px] font-bold rounded-lg transition-all shadow-sm cursor-pointer whitespace-nowrap"
                                title="Simulate immediate delivery alert"
                              >
                                Expedite Delivery
                              </button>
                            )}
                          </div>
                        </div>

                        {/* Stepper Progress Bar */}
                        <div className="grid grid-cols-3 gap-2 text-[10px] sm:text-[11px] pt-1">
                          <div className={`p-2 rounded-lg border text-center transition-all ${
                            isPrep || isTransit || isCompleted
                              ? 'bg-amber-500/15 border-amber-500/40 text-amber-300 font-bold'
                              : 'bg-slate-900 border-slate-800 text-slate-500'
                          }`}>
                            1. Kitchen Prep
                          </div>
                          <div className={`p-2 rounded-lg border text-center transition-all ${
                            isTransit || isCompleted
                              ? 'bg-blue-500/15 border-blue-500/40 text-blue-300 font-bold'
                              : 'bg-slate-900 border-slate-800 text-slate-500'
                          }`}>
                            2. En Route to Suite
                          </div>
                          <div className={`p-2 rounded-lg border text-center transition-all ${
                            isCompleted
                              ? 'bg-emerald-500/20 border-emerald-500/40 text-emerald-300 font-bold'
                              : 'bg-slate-900 border-slate-800 text-slate-500'
                          }`}>
                            3. Delivered to Room
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {services.map((service) => (
                <div key={service.id} className="bg-slate-900/90 border border-slate-800 rounded-2xl p-5 shadow-xl flex flex-col justify-between space-y-4">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded bg-slate-800 text-amber-400 border border-slate-700">
                        {service.category}
                      </span>
                      <span className="text-base font-bold text-amber-400">{formatCurrency(service.price)}</span>
                    </div>

                    <h3 className="text-base font-bold text-slate-100">{service.name}</h3>
                    <p className="text-xs text-slate-400 leading-relaxed">{service.description}</p>
                  </div>

                  <button
                    onClick={() => handleOrderService(service)}
                    className="w-full py-2.5 px-4 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold text-xs shadow-md shadow-amber-500/20 transition-all flex items-center justify-center space-x-1.5 cursor-pointer"
                  >
                    <ConciergeBell className="w-3.5 h-3.5" />
                    <span>Order to My Room</span>
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ==================== TAB 4: MY PROFILE ==================== */}
        {activeTab === 'profile' && (
          <div className="max-w-2xl bg-slate-900/90 border border-slate-800 rounded-2xl p-6 sm:p-8 shadow-xl space-y-6">
            <div className="flex items-center space-x-4 border-b border-slate-800 pb-6">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-amber-400 to-amber-600 flex items-center justify-center font-bold text-2xl text-slate-950 font-serif">
                {currentUser.name.charAt(0)}
              </div>
              <div>
                <h2 className="text-xl font-bold text-white">{currentUser.name}</h2>
                <p className="text-xs text-slate-400">{currentUser.email}</p>
                <div className="mt-1.5 inline-flex items-center space-x-1.5 text-xs text-amber-400 font-semibold bg-amber-950/40 px-2.5 py-0.5 rounded-full border border-amber-800/40">
                  <Sparkles className="w-3 h-3" />
                  <span>{currentUser.memberTier || 'Platinum Member'} Status</span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
              <div className="p-3.5 bg-slate-950 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Registered Phone</span>
                <span className="text-slate-200 font-semibold">{currentUser.phone || '+91 98421 00000'}</span>
              </div>

              <div className="p-3.5 bg-slate-950 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Account Role</span>
                <span className="text-emerald-400 font-semibold">Verified Hotel Guest (Customer)</span>
              </div>

              <div className="p-3.5 bg-slate-950 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Registration Timestamp</span>
                <span className="text-slate-200 font-semibold">{currentUser.registeredAt || 'Active Session'}</span>
              </div>

              <div className="p-3.5 bg-slate-950 rounded-xl border border-slate-800">
                <span className="text-slate-500 block mb-1">Preferred Currency</span>
                <span className="text-amber-400 font-bold">Indian Rupee (₹ INR)</span>
              </div>
            </div>

            <div className="pt-4 border-t border-slate-800 flex justify-end">
              <button
                onClick={onLogout}
                className="px-4 py-2 bg-red-950/80 hover:bg-red-900 border border-red-800 text-red-200 rounded-xl text-xs font-bold transition-all flex items-center space-x-1.5"
              >
                <LogOut className="w-4 h-4" />
                <span>Sign Out of Account</span>
              </button>
            </div>
          </div>
        )}
      </div>

      {/* ==================== INTERACTIVE BOOKING MODAL ==================== */}
      {bookingRoom && (
        <BookingModal
          room={bookingRoom}
          currentUser={currentUser}
          settings={settings}
          onClose={() => setBookingRoom(null)}
          onConfirmBooking={handleConfirmBooking}
        />
      )}

      {/* ==================== MODAL: FOLIO STATEMENT ==================== */}
      {selectedInvoice && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full p-6 shadow-2xl space-y-4 animate-fadeIn">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <h3 className="text-base font-bold text-white font-serif">KR LUXE HOTEL GUEST FOLIO</h3>
                <p className="text-xs text-amber-400 font-medium">Folio #{selectedInvoice.invoiceNumber}</p>
              </div>
              <button
                onClick={() => setSelectedInvoice(null)}
                className="text-slate-400 hover:text-white text-xs font-bold p-1"
              >
                ✕
              </button>
            </div>

            <div className="bg-slate-950 p-5 rounded-xl border border-slate-800 text-xs text-slate-300 space-y-3">
              <div className="flex items-center justify-between border-b border-slate-800/80 pb-2">
                <span className="text-amber-400 font-bold font-serif text-sm">KR LUXE HOTEL & RESORT</span>
                <span className="text-[11px] text-slate-400">Official Guest Folio</span>
              </div>
              <div className="grid grid-cols-2 gap-2 text-xs">
                <div><span className="text-slate-500">Guest Name:</span> <strong className="text-white block">{selectedInvoice.guestName}</strong></div>
                <div><span className="text-slate-500">Suite Assigned:</span> <strong className="text-white block">Room {selectedInvoice.roomNumber} ({selectedInvoice.roomType})</strong></div>
                <div className="col-span-2"><span className="text-slate-500">Stay Duration:</span> <span className="text-slate-200 block">{selectedInvoice.checkInDate} to {selectedInvoice.checkOutDate} ({selectedInvoice.numberOfNights} Nights)</span></div>
              </div>
              <div className="border-t border-slate-800/80 pt-2 space-y-1.5">
                <div className="flex justify-between text-slate-300"><span>Room Tariff Charges:</span><span className="font-semibold text-white">{formatCurrency(selectedInvoice.roomCharges)}</span></div>
                <div className="flex justify-between text-slate-300"><span>In-Room Dining & Concierge:</span><span className="font-semibold text-white">{formatCurrency(selectedInvoice.serviceCharges)}</span></div>
                <div className="flex justify-between text-slate-300"><span>Applicable Tax ({settings.taxPercentage}%):</span><span className="font-semibold text-white">{formatCurrency(selectedInvoice.taxAmount)}</span></div>
              </div>
              <div className="border-t border-slate-800 pt-2 space-y-1">
                <div className="flex justify-between text-white font-bold text-sm"><span>Grand Total:</span><span>{formatCurrency(selectedInvoice.grandTotal)}</span></div>
                <div className="flex justify-between text-emerald-400 font-semibold"><span>Amount Paid:</span><span>{formatCurrency(selectedInvoice.paidAmount)}</span></div>
                <div className="flex justify-between text-amber-400 font-semibold"><span>Balance Due:</span><span>{formatCurrency(selectedInvoice.balanceAmount)}</span></div>
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-end gap-2 pt-3 border-t border-slate-800">
              <button
                disabled={isDownloadingImage}
                onClick={async () => {
                  setIsDownloadingImage(true);
                  try {
                    const filename = await downloadFolioReceiptImage(selectedInvoice, settings);
                    showNotification(`Folio receipt downloaded as image: ${filename}`);
                  } catch {
                    showNotification('Failed to generate receipt image. Please try again.', 'error');
                  } finally {
                    setIsDownloadingImage(false);
                  }
                }}
                className="px-3.5 py-2 bg-gradient-to-r from-amber-500 via-amber-400 to-amber-500 hover:from-amber-400 hover:to-amber-300 text-slate-950 rounded-xl text-xs font-bold flex items-center space-x-1.5 shadow-lg shadow-amber-500/20 cursor-pointer transition-all disabled:opacity-50"
                title="Download high-resolution official folio receipt as PNG image"
              >
                <ImageIcon className="w-3.5 h-3.5 text-slate-950" />
                <span>{isDownloadingImage ? 'Generating Image...' : 'Download Folio Receipt (Image)'}</span>
              </button>

              <button
                onClick={() => {
                  const blob = new Blob([`KR LUXE HOTEL - GUEST FOLIO\nInvoice: ${selectedInvoice.invoiceNumber}\nGuest: ${selectedInvoice.guestName}\nRoom: ${selectedInvoice.roomNumber}\nGrand Total: ${formatCurrency(selectedInvoice.grandTotal)}\nPaid: ${formatCurrency(selectedInvoice.paidAmount)}\nBalance Due: ${formatCurrency(selectedInvoice.balanceAmount)}\nStatus: ${selectedInvoice.paymentStatus}\nThank you for staying at KR LUXE HOTEL!`], { type: 'text/plain' });
                  const url = URL.createObjectURL(blob);
                  const a = document.createElement('a');
                  a.href = url;
                  a.download = `KR_LUXE_${selectedInvoice.invoiceNumber}.txt`;
                  a.click();
                  showNotification('Guest folio receipt downloaded as text.');
                }}
                className="px-3 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 rounded-xl text-xs font-medium flex items-center space-x-1 cursor-pointer"
                title="Download plain text summary"
              >
                <Download className="w-3.5 h-3.5 text-slate-400" />
                <span>Text Copy</span>
              </button>

              <button
                onClick={() => setSelectedInvoice(null)}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 rounded-xl text-xs font-bold cursor-pointer"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ==================== MODAL: SETTLE BALANCE ==================== */}
      {payingInvoice && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4 animate-fadeIn">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-base font-bold text-white font-serif">Settle Folio Balance</h3>
              <button
                onClick={() => setPayingInvoice(null)}
                className="text-slate-400 hover:text-white text-xs font-bold p-1"
              >
                ✕
              </button>
            </div>

            <div className="p-4 bg-slate-950 rounded-xl border border-slate-800 space-y-2 text-xs">
              <div className="flex justify-between text-slate-400">
                <span>Invoice Number:</span>
                <span className="text-slate-200 font-semibold">{payingInvoice.invoiceNumber}</span>
              </div>
              <div className="flex justify-between text-slate-400">
                <span>Room Assigned:</span>
                <span className="text-slate-200">Room {payingInvoice.roomNumber}</span>
              </div>
              <div className="flex justify-between text-slate-400 font-bold text-sm pt-2 border-t border-slate-800">
                <span className="text-white">Total Amount to Pay:</span>
                <span className="text-emerald-400">{formatCurrency(payingInvoice.balanceAmount)}</span>
              </div>
            </div>

            <div className="space-y-2 text-xs">
              <label className="font-semibold text-slate-300 block">Select Payment Method</label>
              <div className="grid grid-cols-3 gap-2">
                <div className="p-2.5 rounded-xl bg-slate-950 border border-emerald-500/80 text-emerald-400 font-bold text-center">
                  UPI / QR
                </div>
                <div className="p-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-400 text-center">
                  Credit Card
                </div>
                <div className="p-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-400 text-center">
                  Net Banking
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-2 pt-3 border-t border-slate-800">
              <button
                type="button"
                onClick={() => setPayingInvoice(null)}
                className="px-4 py-2 bg-slate-800 text-slate-300 rounded-xl text-xs font-medium"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => handleSettleBalance(payingInvoice.id)}
                className="px-4 py-2 bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-400 hover:to-emerald-500 text-white font-bold rounded-xl text-xs shadow-md shadow-emerald-500/20"
              >
                Pay {formatCurrency(payingInvoice.balanceAmount)} Now
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ==================== AUTOMATED GUEST ALERTS DRAWER / POPOVER ==================== */}
      {showNotifDrawer && (
        <div className="fixed inset-0 bg-slate-950/60 backdrop-blur-sm z-50 flex items-start justify-end p-3 sm:p-6 animate-fadeIn">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-md w-full shadow-2xl overflow-hidden flex flex-col max-h-[88vh] mt-12 sm:mt-16 animate-fadeIn">
            {/* Drawer Header */}
            <div className="p-5 bg-slate-950/95 border-b border-slate-800 flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <div className="w-9 h-9 rounded-xl bg-amber-500/20 text-amber-400 flex items-center justify-center border border-amber-500/30">
                  <Bell className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="font-bold text-sm text-white font-serif">Guest Stay & Service Alerts</h3>
                  <p className="text-[11px] text-slate-400">Automated updates for your suite</p>
                </div>
              </div>
              <button
                onClick={() => setShowNotifDrawer(false)}
                className="w-7 h-7 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-white flex items-center justify-center text-xs cursor-pointer"
              >
                ✕
              </button>
            </div>

            {/* Filter Tabs */}
            <div className="px-5 py-2.5 bg-slate-950/50 border-b border-slate-800 flex items-center space-x-2 text-xs overflow-x-auto">
              <button
                onClick={() => setNotifFilter('all')}
                className={`px-3 py-1 rounded-lg text-[11px] font-bold transition-all cursor-pointer whitespace-nowrap ${
                  notifFilter === 'all'
                    ? 'bg-amber-500 text-slate-950 shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                All ({userNotifications.length})
              </button>
              <button
                onClick={() => setNotifFilter('room_service')}
                className={`px-3 py-1 rounded-lg text-[11px] font-bold transition-all cursor-pointer whitespace-nowrap ${
                  notifFilter === 'room_service'
                    ? 'bg-amber-500 text-slate-950 shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                Room Service ({userNotifications.filter(n => n.type === 'room_service').length})
              </button>
              <button
                onClick={() => setNotifFilter('checkout')}
                className={`px-3 py-1 rounded-lg text-[11px] font-bold transition-all cursor-pointer whitespace-nowrap ${
                  notifFilter === 'checkout'
                    ? 'bg-amber-500 text-slate-950 shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                Check-Out & Stay ({userNotifications.filter(n => n.type === 'checkout_reminder' || n.type === 'late_checkout').length})
              </button>
            </div>

            {/* Notifications List */}
            <div className="p-4 overflow-y-auto space-y-3 flex-1 text-xs">
              {filteredNotifications.length === 0 ? (
                <div className="text-center py-12 text-slate-500 space-y-2">
                  <Bell className="w-8 h-8 mx-auto text-slate-600" />
                  <p className="text-xs">No alerts in this category.</p>
                </div>
              ) : (
                filteredNotifications.map((n) => {
                  const isRoomService = n.type === 'room_service';
                  const isLateCheckOut = n.type === 'late_checkout';

                  return (
                    <div
                      key={n.id}
                      onClick={() => handleMarkAsRead(n.id)}
                      className={`p-3.5 rounded-2xl border transition-all space-y-2 cursor-pointer ${
                        !n.read
                          ? 'bg-amber-500/10 border-amber-500/40 shadow-sm'
                          : 'bg-slate-950/60 border-slate-800/80 opacity-85 hover:opacity-100'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="flex items-center space-x-2">
                          <div className={`w-6 h-6 rounded-lg flex items-center justify-center flex-shrink-0 ${
                            isRoomService
                              ? 'bg-amber-500/20 text-amber-400'
                              : isLateCheckOut
                              ? 'bg-emerald-500/20 text-emerald-300'
                              : 'bg-blue-500/20 text-blue-300'
                          }`}>
                            {isRoomService ? (
                              <Utensils className="w-3.5 h-3.5" />
                            ) : isLateCheckOut ? (
                              <Sparkles className="w-3.5 h-3.5" />
                            ) : (
                              <Clock className="w-3.5 h-3.5" />
                            )}
                          </div>
                          <div>
                            <span className="font-bold text-slate-100 text-xs block">{n.title}</span>
                            {n.roomNumber && (
                              <span className="text-[10px] text-amber-400 font-semibold">Suite {n.roomNumber}</span>
                            )}
                          </div>
                        </div>
                        <div className="flex items-center space-x-1.5 flex-shrink-0">
                          <span className="text-[10px] text-slate-400">{n.timestamp}</span>
                          {!n.read && (
                            <span className="w-2 h-2 rounded-full bg-amber-400 animate-pulse" />
                          )}
                        </div>
                      </div>

                      <p className="text-xs text-slate-300 leading-relaxed pl-8">{n.message}</p>

                      {n.actionLabel && (
                        <div className="pl-8 pt-1 flex items-center justify-between">
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleNotificationAction(n);
                            }}
                            className="text-xs text-amber-400 hover:text-amber-300 font-bold flex items-center space-x-1 cursor-pointer"
                          >
                            <span>{n.actionLabel}</span>
                            <ArrowRight className="w-3 h-3" />
                          </button>
                          {!n.read && (
                            <span className="text-[10px] text-amber-400/80 font-medium">Click to mark read</span>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })
              )}
            </div>

            {/* Drawer Footer */}
            <div className="p-3 bg-slate-950/90 border-t border-slate-800 flex items-center justify-between text-xs">
              <button
                onClick={handleMarkAllAsRead}
                className="text-slate-400 hover:text-amber-300 font-semibold cursor-pointer"
              >
                Mark all as read
              </button>
              <button
                onClick={handleClearNotifications}
                className="text-slate-500 hover:text-red-400 cursor-pointer"
              >
                Clear alerts
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ==================== LIVE FLOATING ALERT TOAST ==================== */}
      {liveAlertToast && (
        <div className="fixed top-20 right-4 sm:right-8 z-50 max-w-sm w-full bg-slate-900/95 border border-amber-500/60 rounded-2xl p-4 shadow-2xl backdrop-blur-xl animate-fadeIn">
          <div className="flex items-start justify-between gap-3">
            <div className="w-9 h-9 rounded-xl bg-amber-500/20 text-amber-400 flex items-center justify-center flex-shrink-0 border border-amber-500/30">
              {liveAlertToast.type === 'room_service' ? (
                <Utensils className="w-5 h-5" />
              ) : liveAlertToast.type === 'late_checkout' ? (
                <Sparkles className="w-5 h-5" />
              ) : (
                <Clock className="w-5 h-5" />
              )}
            </div>
            <div className="flex-1 space-y-1">
              <div className="flex items-center justify-between">
                <h4 className="font-bold text-xs text-white">{liveAlertToast.title}</h4>
                <span className="text-[10px] text-amber-400 font-bold bg-amber-500/10 px-1.5 py-0.2 rounded border border-amber-500/20">
                  New Alert
                </span>
              </div>
              <p className="text-[11px] text-slate-300 leading-snug">{liveAlertToast.message}</p>
              {liveAlertToast.actionLabel && (
                <button
                  onClick={() => {
                    handleNotificationAction(liveAlertToast);
                    setLiveAlertToast(null);
                  }}
                  className="mt-1.5 text-[11px] font-bold text-amber-400 hover:text-amber-300 flex items-center space-x-1 cursor-pointer"
                >
                  <span>{liveAlertToast.actionLabel}</span>
                  <ArrowRight className="w-3 h-3" />
                </button>
              )}
            </div>
            <button
              onClick={() => setLiveAlertToast(null)}
              className="text-slate-400 hover:text-white text-xs p-1 cursor-pointer"
              title="Close alert"
            >
              ✕
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
