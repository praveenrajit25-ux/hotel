import React, { useState } from 'react';
import {
  Calendar,
  CalendarDays,
  Clock,
  Users,
  ShieldCheck,
  Sparkles,
  Tag,
  CreditCard,
  Building2,
  Coffee,
  Wifi,
  Car,
  CheckCircle2,
  ArrowRight,
  ArrowLeft,
  X,
  FileText
} from 'lucide-react';
import { RoomItem, HotelSettingsConfig, AuthUser } from '../types/pms';

export interface BookingData {
  checkInDate: string;
  checkOutDate: string;
  numberOfGuests: number;
  specialRequests: string;
  discountAmount: number;
  promoCode?: string;
  paymentPreference: 'pay_at_checkin' | 'prepay_now';
  guestName: string;
  guestPhone: string;
  guestEmail: string;
  calculatedGrandTotal: number;
  calculatedTax: number;
  calculatedRoomTotal: number;
  numberOfNights: number;
}

interface BookingModalProps {
  room: RoomItem;
  currentUser: AuthUser;
  settings: HotelSettingsConfig;
  onClose: () => void;
  onConfirmBooking: (data: BookingData) => void;
}

export const BookingModal: React.FC<BookingModalProps> = ({
  room,
  currentUser,
  settings,
  onClose,
  onConfirmBooking
}) => {
  // Navigation between Step 1 (Configure) and Step 2 (Summary & Finalize)
  const [step, setStep] = useState<'configure' | 'summary'>('configure');

  // Stay configuration state
  const [checkInDate, setCheckInDate] = useState<string>('2026-08-30');
  const [checkOutDate, setCheckOutDate] = useState<string>('2026-09-02');
  const [guestCount, setGuestCount] = useState<number>(Math.min(2, room.capacity));
  const [specialRequests, setSpecialRequests] = useState<string>('Quiet room on high floor, late arrival.');

  // Guest Contact Information (editable for this booking)
  const [guestName, setGuestName] = useState<string>(currentUser.name || '');
  const [guestPhone, setGuestPhone] = useState<string>(currentUser.phone || '+91 98765 43210');
  const [guestEmail, setGuestEmail] = useState<string>(currentUser.email || '');

  // Promo code & discount
  const [promoCodeInput, setPromoCodeInput] = useState<string>('');
  const [appliedPromo, setAppliedPromo] = useState<{ code: string; discount: number; label: string } | null>(null);
  const [promoError, setPromoError] = useState<string | null>(null);

  // Payment preference
  const [paymentPreference, setPaymentPreference] = useState<'pay_at_checkin' | 'prepay_now'>('pay_at_checkin');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  // Quick stay length presets
  const applyStayPreset = (days: number) => {
    try {
      const base = new Date(checkInDate);
      if (isNaN(base.getTime())) return;
      const target = new Date(base);
      target.setDate(target.getDate() + days);
      setCheckOutDate(target.toISOString().split('T')[0]);
    } catch {
      // ignore
    }
  };

  // Quick special request chips
  const quickRequestChips = [
    'Airport Shuttle Pick-up',
    'Quiet Room / High Floor',
    'Late Check-in (after 8 PM)',
    'Extra Pillows & Towels',
    'Honeymoon / Anniversary Setup',
    'Vegetarian Dining Preference'
  ];

  const toggleSpecialRequestChip = (chip: string) => {
    if (specialRequests.includes(chip)) {
      const cleaned = specialRequests
        .replace(new RegExp(`,?\\s*${chip}`, 'g'), '')
        .replace(/^,\s*/, '')
        .trim();
      setSpecialRequests(cleaned);
    } else {
      const updated = specialRequests.trim()
        ? `${specialRequests.trim()}, ${chip}`
        : chip;
      setSpecialRequests(updated);
    }
  };

  // Calculate nights
  const calculateNights = (inDate: string, outDate: string) => {
    try {
      const d1 = new Date(inDate);
      const d2 = new Date(outDate);
      const diffTime = d2.getTime() - d1.getTime();
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays > 0 ? diffDays : 1;
    } catch {
      return 1;
    }
  };

  const nights = calculateNights(checkInDate, checkOutDate);
  const roomCost = room.pricePerNight * nights;

  // Calculate discount
  let discountAmount = 0;
  if (appliedPromo) {
    discountAmount = appliedPromo.discount;
  }
  const discountedRoomCost = Math.max(0, roomCost - discountAmount);
  const taxAmount = (discountedRoomCost * settings.taxPercentage) / 100;
  const grandTotal = discountedRoomCost + taxAmount;

  const formatCurrency = (val: number, minimumFractionDigits = 2) => {
    return `${settings.currencySymbol}${val.toLocaleString('en-IN', {
      minimumFractionDigits,
      maximumFractionDigits: minimumFractionDigits
    })}`;
  };

  const formatDateDisplay = (dateStr: string) => {
    try {
      const d = new Date(dateStr + 'T00:00:00');
      return d.toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });
    } catch {
      return dateStr;
    }
  };

  // Privilege voucher validation
  const handleApplyPromo = (e: React.FormEvent) => {
    e.preventDefault();
    setPromoError(null);
    const code = promoCodeInput.trim().toUpperCase();

    if (!code) {
      setPromoError('Please enter a valid privilege voucher.');
      return;
    }

    if (code === 'LUXE10') {
      const discount = Math.round(roomCost * 0.1);
      setAppliedPromo({ code, discount, label: '10% Luxury Member Privilege' });
      setPromoError(null);
    } else if (code === 'STAYMORE') {
      if (nights >= 2) {
        setAppliedPromo({ code, discount: 1500, label: 'Extended Stay ₹1,500 Privilege' });
        setPromoError(null);
      } else {
        setPromoError('Privilege voucher STAYMORE requires a minimum stay of 2 nights.');
      }
    } else if (code === 'WELCOME') {
      setAppliedPromo({ code, discount: 1000, label: 'Welcome Guest Credit ₹1,000' });
      setPromoError(null);
    } else {
      setPromoError('Invalid privilege voucher. Try LUXE10, STAYMORE, or WELCOME.');
    }
  };

  const handleRemovePromo = () => {
    setAppliedPromo(null);
    setPromoCodeInput('');
    setPromoError(null);
  };

  // Finalize reservation submission
  const handleFinalize = () => {
    setIsSubmitting(true);
    setTimeout(() => {
      onConfirmBooking({
        checkInDate,
        checkOutDate,
        numberOfGuests: guestCount,
        specialRequests,
        discountAmount,
        promoCode: appliedPromo?.code,
        paymentPreference,
        guestName: guestName.trim() || currentUser.name,
        guestPhone: guestPhone.trim() || '+91 98765 43210',
        guestEmail: guestEmail.trim() || currentUser.email,
        calculatedGrandTotal: grandTotal,
        calculatedTax: taxAmount,
        calculatedRoomTotal: discountedRoomCost,
        numberOfNights: nights
      });
      setIsSubmitting(false);
    }, 600);
  };

  return (
    <div className="fixed inset-0 bg-slate-950/85 backdrop-blur-md z-50 flex items-center justify-center p-3 sm:p-5 overflow-y-auto">
      <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-2xl w-full shadow-2xl overflow-hidden my-auto animate-fadeIn flex flex-col max-h-[92vh]">
        {/* Modal Header */}
        <div className="p-5 sm:p-6 bg-gradient-to-r from-slate-900 via-slate-850 to-slate-900 border-b border-slate-800 flex items-start justify-between">
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <span className="px-2.5 py-0.5 rounded-md bg-amber-500/20 text-amber-300 text-xs font-bold border border-amber-500/30">
                Room {room.number}
              </span>
              <span className="text-xs text-slate-400">• Floor {room.floor} • Max {room.capacity} Guests</span>
            </div>
            <h2 className="text-xl sm:text-2xl font-bold text-white font-serif tracking-tight">
              {room.type} Suite
            </h2>
            <p className="text-xs text-amber-400/90 font-medium">
              KR LUXE HOTEL • {formatCurrency(room.pricePerNight, 0)} per night + {settings.taxPercentage}% GST
            </p>
          </div>

          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-800/80 hover:bg-slate-700 text-slate-400 hover:text-white flex items-center justify-center transition-colors text-sm"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Step Indicator Bar */}
        <div className="px-6 py-3 bg-slate-950/80 border-b border-slate-800/80 flex items-center justify-between text-xs">
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setStep('configure')}
              className={`flex items-center space-x-1.5 font-bold transition-colors ${
                step === 'configure' ? 'text-amber-400' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <span className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] ${
                step === 'configure' ? 'bg-amber-500 text-slate-950 font-bold' : 'bg-slate-800 text-slate-300'
              }`}>
                1
              </span>
              <span>Stay Details & Preferences</span>
            </button>

            <span className="text-slate-600">/</span>

            <button
              onClick={() => setStep('summary')}
              className={`flex items-center space-x-1.5 font-bold transition-colors ${
                step === 'summary' ? 'text-amber-400' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <span className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] ${
                step === 'summary' ? 'bg-amber-500 text-slate-950 font-bold' : 'bg-slate-800 text-slate-300'
              }`}>
                2
              </span>
              <span>Selected Dates & Costs Summary</span>
            </button>
          </div>

          <span className="hidden sm:inline-block text-[11px] text-slate-400 font-medium">
            {nights} Night{nights > 1 ? 's' : ''} • {formatCurrency(grandTotal)}
          </span>
        </div>

        {/* Modal Scrollable Content Area */}
        <div className="p-6 overflow-y-auto space-y-6 flex-1 text-xs">
          {step === 'configure' ? (
            /* ================= STEP 1: CONFIGURE STAY ================= */
            <div className="space-y-6">
              {/* Date Selection Section */}
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <label className="font-bold text-sm text-slate-100 flex items-center space-x-2">
                    <CalendarDays className="w-4 h-4 text-amber-400" />
                    <span>Select Stay Dates</span>
                  </label>
                  <span className="text-xs text-amber-400 font-semibold">
                    {nights} Night{nights > 1 ? 's' : ''} Selected
                  </span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
                  <div className="p-3.5 bg-slate-950/80 rounded-2xl border border-slate-800 focus-within:border-amber-500/80 transition-colors">
                    <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider mb-1 flex items-center justify-between">
                      <span>Check-In</span>
                      <span className="text-slate-500 font-normal">From 2:00 PM</span>
                    </div>
                    <input
                      type="date"
                      required
                      value={checkInDate}
                      onChange={(e) => {
                        const newIn = e.target.value;
                        setCheckInDate(newIn);
                        // If check-out is before or equal to new in-date, automatically push out by 1 night
                        if (newIn >= checkOutDate) {
                          const d = new Date(newIn);
                          d.setDate(d.getDate() + 1);
                          setCheckOutDate(d.toISOString().split('T')[0]);
                        }
                      }}
                      className="w-full bg-transparent text-slate-100 font-semibold text-sm focus:outline-none cursor-pointer"
                    />
                    <div className="text-[11px] text-slate-400 mt-1">
                      {formatDateDisplay(checkInDate)}
                    </div>
                  </div>

                  <div className="p-3.5 bg-slate-950/80 rounded-2xl border border-slate-800 focus-within:border-amber-500/80 transition-colors">
                    <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider mb-1 flex items-center justify-between">
                      <span>Check-Out</span>
                      <span className="text-slate-500 font-normal">Until 11:00 AM</span>
                    </div>
                    <input
                      type="date"
                      required
                      min={checkInDate}
                      value={checkOutDate}
                      onChange={(e) => setCheckOutDate(e.target.value)}
                      className="w-full bg-transparent text-slate-100 font-semibold text-sm focus:outline-none cursor-pointer"
                    />
                    <div className="text-[11px] text-slate-400 mt-1">
                      {formatDateDisplay(checkOutDate)}
                    </div>
                  </div>
                </div>

                {/* Quick Stay Presets */}
                <div className="flex items-center flex-wrap gap-2 pt-1">
                  <span className="text-[11px] text-slate-400 mr-1">Quick stay duration:</span>
                  {[
                    { label: '1 Night', days: 1 },
                    { label: '2 Nights (Weekend)', days: 2 },
                    { label: '3 Nights (Recommended)', days: 3 },
                    { label: '5 Nights', days: 5 },
                    { label: '1 Week', days: 7 }
                  ].map((preset) => (
                    <button
                      key={preset.label}
                      type="button"
                      onClick={() => applyStayPreset(preset.days)}
                      className={`px-2.5 py-1 rounded-lg text-[11px] font-medium transition-all ${
                        nights === preset.days
                          ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40 font-bold'
                          : 'bg-slate-800/80 hover:bg-slate-750 text-slate-300 border border-slate-700/60'
                      }`}
                    >
                      {preset.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Guest Count Selection */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <label className="font-bold text-sm text-slate-100 flex items-center space-x-2">
                    <Users className="w-4 h-4 text-amber-400" />
                    <span>Occupancy & Guests</span>
                  </label>
                  <span className="text-slate-400 text-xs">Maximum Room Capacity: {room.capacity}</span>
                </div>

                <div className="grid grid-cols-3 sm:grid-cols-4 gap-2.5">
                  {Array.from({ length: room.capacity }, (_, i) => i + 1).map((num) => (
                    <button
                      key={num}
                      type="button"
                      onClick={() => setGuestCount(num)}
                      className={`p-3 rounded-xl border text-center transition-all ${
                        guestCount === num
                          ? 'bg-amber-500/20 border-amber-500/60 text-amber-300 font-bold shadow-md shadow-amber-500/10'
                          : 'bg-slate-950/60 border-slate-800 text-slate-300 hover:border-slate-700'
                      }`}
                    >
                      <div className="text-sm font-bold">{num}</div>
                      <div className="text-[10px] text-slate-400">{num === 1 ? 'Guest' : 'Guests'}</div>
                    </button>
                  ))}
                </div>
              </div>

              {/* Special Requests & Preferences */}
              <div className="space-y-2.5">
                <label className="font-bold text-sm text-slate-100 flex items-center space-x-2">
                  <Sparkles className="w-4 h-4 text-amber-400" />
                  <span>Special Requests & Luxury Touches</span>
                </label>
                <p className="text-[11px] text-slate-400">
                  Select common requests or specify customized arrangements for our hospitality concierge:
                </p>

                <div className="flex flex-wrap gap-2">
                  {quickRequestChips.map((chip) => {
                    const isSelected = specialRequests.includes(chip);
                    return (
                      <button
                        key={chip}
                        type="button"
                        onClick={() => toggleSpecialRequestChip(chip)}
                        className={`px-3 py-1 rounded-full text-[11px] transition-all flex items-center space-x-1.5 ${
                          isSelected
                            ? 'bg-amber-500/20 border border-amber-500/40 text-amber-300 font-semibold'
                            : 'bg-slate-950 border border-slate-800 text-slate-400 hover:text-slate-200'
                        }`}
                      >
                        {isSelected && <CheckCircle2 className="w-3 h-3 text-amber-400" />}
                        <span>{chip}</span>
                      </button>
                    );
                  })}
                </div>

                <textarea
                  rows={2}
                  value={specialRequests}
                  onChange={(e) => setSpecialRequests(e.target.value)}
                  placeholder="e.g., Early check-in preference, extra room key, high floor suite..."
                  className="w-full p-3 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-amber-500/80 text-xs resize-none"
                />
              </div>

              {/* Coupon / Promo Code Input */}
              <div className="p-4 bg-slate-950/70 rounded-2xl border border-slate-800/80 space-y-2.5">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-xs text-slate-200 flex items-center space-x-1.5">
                    <Tag className="w-3.5 h-3.5 text-amber-400" />
                    <span>Have a Promotional Voucher or Privilege Pass?</span>
                  </span>
                  <span className="text-[10px] text-slate-500">Try: LUXE10, STAYMORE, WELCOME</span>
                </div>

                {appliedPromo ? (
                  <div className="flex items-center justify-between p-2.5 bg-emerald-500/10 border border-emerald-500/30 rounded-xl">
                    <div className="flex items-center space-x-2 text-emerald-300 font-medium">
                      <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                      <span>Voucher <strong>{appliedPromo.code}</strong> applied ({appliedPromo.label}: -{formatCurrency(appliedPromo.discount)})</span>
                    </div>
                    <button
                      type="button"
                      onClick={handleRemovePromo}
                      className="text-xs font-bold text-slate-400 hover:text-red-400 underline cursor-pointer"
                    >
                      Remove
                    </button>
                  </div>
                ) : (
                  <form onSubmit={handleApplyPromo} className="flex gap-2">
                    <input
                      type="text"
                      value={promoCodeInput}
                      onChange={(e) => setPromoCodeInput(e.target.value)}
                      placeholder="Enter privilege voucher (e.g. LUXE10)"
                      className="flex-1 p-2.5 bg-slate-900 border border-slate-800 rounded-xl text-slate-200 uppercase font-semibold text-xs focus:outline-none focus:border-amber-500"
                    />
                    <button
                      type="submit"
                      className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-amber-400 font-bold rounded-xl transition-colors border border-slate-700"
                    >
                      Apply Voucher
                    </button>
                  </form>
                )}

                {promoError && (
                  <p className="text-red-400 text-[11px]">{promoError}</p>
                )}
              </div>

              {/* Real-time Estimated Cost Callout */}
              <div className="p-4 bg-gradient-to-br from-slate-950 to-slate-900 rounded-2xl border border-amber-500/30 space-y-2">
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <span className="text-[11px] uppercase font-bold text-slate-400 tracking-wider">Live Cost Estimate</span>
                    <div className="text-sm font-semibold text-slate-200">
                      {nights} Night{nights > 1 ? 's' : ''} • {formatDateDisplay(checkInDate)} to {formatDateDisplay(checkOutDate)}
                    </div>
                  </div>
                  <div className="text-right">
                    <span className="text-xl font-bold text-amber-400">{formatCurrency(grandTotal)}</span>
                    <div className="text-[10px] text-slate-400">Total payable (incl. GST)</div>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            /* ================= STEP 2: SUMMARY OF SELECTED DATES AND COSTS ================= */
            <div className="space-y-6 animate-fadeIn">
              {/* Review Banner */}
              <div className="p-4 bg-amber-500/10 border border-amber-500/20 rounded-2xl flex items-start space-x-3">
                <div className="p-2 rounded-xl bg-amber-500/20 text-amber-400 mt-0.5">
                  <FileText className="w-5 h-5" />
                </div>
                <div className="space-y-1">
                  <h4 className="font-bold text-white text-sm">Review Your Reservation Summary</h4>
                  <p className="text-slate-300 text-xs leading-relaxed">
                    Please review your selected stay dates, guest contact details, and itemized cost calculations before finalizing your booking.
                  </p>
                </div>
              </div>

              {/* Selected Dates Summary Card */}
              <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-5 space-y-4 shadow-inner">
                <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                  <div className="flex items-center space-x-2">
                    <Calendar className="w-4 h-4 text-amber-400" />
                    <span className="font-bold text-slate-200 text-sm">Selected Dates & Duration</span>
                  </div>
                  <span className="px-3 py-1 rounded-full bg-amber-500/20 text-amber-300 font-bold text-xs border border-amber-500/30">
                    {nights} Night{nights > 1 ? 's' : ''} Stay
                  </span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="p-3 bg-slate-900 rounded-xl border border-slate-800/80">
                    <span className="text-slate-400 text-[10px] uppercase font-bold tracking-wider block mb-0.5">Check-In Arrival</span>
                    <div className="font-bold text-slate-100 text-sm">{formatDateDisplay(checkInDate)}</div>
                    <div className="text-slate-400 text-xs flex items-center space-x-1 mt-1">
                      <Clock className="w-3.5 h-3.5 text-amber-400" />
                      <span>Standard Check-in: 2:00 PM IST</span>
                    </div>
                  </div>

                  <div className="p-3 bg-slate-900 rounded-xl border border-slate-800/80">
                    <span className="text-slate-400 text-[10px] uppercase font-bold tracking-wider block mb-0.5">Check-Out Departure</span>
                    <div className="font-bold text-slate-100 text-sm">{formatDateDisplay(checkOutDate)}</div>
                    <div className="text-slate-400 text-xs flex items-center space-x-1 mt-1">
                      <Clock className="w-3.5 h-3.5 text-amber-400" />
                      <span>Standard Check-out: 11:00 AM IST</span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-between text-xs pt-1 text-slate-300">
                  <span className="text-slate-400">Reserved Accommodation:</span>
                  <span className="font-semibold text-white">Room {room.number} • {room.type} Suite ({guestCount} Guest{guestCount > 1 ? 's' : ''})</span>
                </div>
              </div>

              {/* Guest Details Confirmation */}
              <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-5 space-y-3 shadow-inner">
                <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                  <div className="flex items-center space-x-2">
                    <Users className="w-4 h-4 text-amber-400" />
                    <span className="font-bold text-slate-200 text-sm">Guest Contact Details</span>
                  </div>
                  <span className="text-[11px] text-slate-400">Registered to Account</span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  <div>
                    <label className="text-slate-400 text-[10px] uppercase font-bold block mb-1">Primary Guest</label>
                    <input
                      type="text"
                      value={guestName}
                      onChange={(e) => setGuestName(e.target.value)}
                      className="w-full p-2 bg-slate-900 border border-slate-800 rounded-lg text-slate-100 text-xs focus:outline-none focus:border-amber-500"
                    />
                  </div>
                  <div>
                    <label className="text-slate-400 text-[10px] uppercase font-bold block mb-1">Phone Number</label>
                    <input
                      type="text"
                      value={guestPhone}
                      onChange={(e) => setGuestPhone(e.target.value)}
                      className="w-full p-2 bg-slate-900 border border-slate-800 rounded-lg text-slate-100 text-xs focus:outline-none focus:border-amber-500"
                    />
                  </div>
                  <div>
                    <label className="text-slate-400 text-[10px] uppercase font-bold block mb-1">Confirmation Email</label>
                    <input
                      type="email"
                      value={guestEmail}
                      onChange={(e) => setGuestEmail(e.target.value)}
                      className="w-full p-2 bg-slate-900 border border-slate-800 rounded-lg text-slate-100 text-xs focus:outline-none focus:border-amber-500"
                    />
                  </div>
                </div>

                {specialRequests && (
                  <div className="pt-2 border-t border-slate-800/80 text-xs">
                    <span className="text-slate-400 block mb-0.5">Noted Special Requests:</span>
                    <p className="text-slate-300 italic">{specialRequests}</p>
                  </div>
                )}
              </div>

              {/* Itemized Cost Summary Breakdown */}
              <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-5 space-y-3.5 shadow-inner">
                <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                  <div className="flex items-center space-x-2">
                    <CreditCard className="w-4 h-4 text-amber-400" />
                    <span className="font-bold text-slate-200 text-sm">Itemized Cost & Billing Summary</span>
                  </div>
                  <span className="text-xs text-amber-400 font-medium">Currency: INR (₹)</span>
                </div>

                <div className="space-y-2 text-xs">
                  <div className="flex justify-between text-slate-300">
                    <span>
                      Room Tariff ({nights} Night{nights > 1 ? 's' : ''} × {formatCurrency(room.pricePerNight, 0)}):
                    </span>
                    <span className="font-medium text-slate-100">{formatCurrency(roomCost)}</span>
                  </div>

                  {appliedPromo && (
                    <div className="flex justify-between text-emerald-400 font-semibold">
                      <span>Privilege Voucher ({appliedPromo.code} - {appliedPromo.label}):</span>
                      <span>-{formatCurrency(discountAmount)}</span>
                    </div>
                  )}

                  <div className="flex justify-between text-slate-300">
                    <span>Applicable GST Tax ({settings.taxPercentage}% on accommodation):</span>
                    <span className="font-medium text-slate-100">{formatCurrency(taxAmount)}</span>
                  </div>

                  {/* Complimentary Luxury Amenities Included */}
                  <div className="p-3 bg-slate-900/90 rounded-xl border border-slate-800/80 space-y-2 my-2">
                    <div className="text-[11px] font-bold text-amber-300 flex items-center space-x-1.5">
                      <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                      <span>Complimentary Luxury Inclusions:</span>
                    </div>
                    <div className="grid grid-cols-2 gap-1.5 text-[11px] text-slate-300">
                      <div className="flex items-center space-x-1.5">
                        <Coffee className="w-3 h-3 text-amber-400" />
                        <span>Daily Artisanal Breakfast Buffet</span>
                      </div>
                      <div className="flex items-center space-x-1.5">
                        <Wifi className="w-3 h-3 text-amber-400" />
                        <span>High-Speed Optical Wi-Fi</span>
                      </div>
                      <div className="flex items-center space-x-1.5">
                        <Car className="w-3 h-3 text-amber-400" />
                        <span>Complimentary Valet Parking</span>
                      </div>
                      <div className="flex items-center space-x-1.5">
                        <Building2 className="w-3 h-3 text-amber-400" />
                        <span>Access to Infinity Pool & Spa</span>
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-between text-slate-400 text-[11px]">
                    <span>Refundable Incidentals Deposit:</span>
                    <span className="text-emerald-400 font-semibold">₹0.00 (No hold required)</span>
                  </div>

                  <div className="pt-3 border-t border-slate-800 flex justify-between items-baseline">
                    <div>
                      <span className="text-base font-bold text-white block">Grand Total Payable</span>
                      <span className="text-[10px] text-slate-400">All government hospitality taxes included</span>
                    </div>
                    <div className="text-right">
                      <span className="text-2xl font-bold text-amber-400 font-serif">
                        {formatCurrency(grandTotal)}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Payment Preference Selector */}
              <div className="space-y-3">
                <label className="font-bold text-sm text-slate-100 flex items-center space-x-2">
                  <CreditCard className="w-4 h-4 text-amber-400" />
                  <span>Choose Payment Preference</span>
                </label>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <button
                    type="button"
                    onClick={() => setPaymentPreference('pay_at_checkin')}
                    className={`p-3.5 rounded-2xl border text-left transition-all ${
                      paymentPreference === 'pay_at_checkin'
                        ? 'bg-amber-500/15 border-amber-500/70 text-slate-100 shadow-md shadow-amber-500/10'
                        : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-bold text-white text-xs">Pay at Front Desk</span>
                      {paymentPreference === 'pay_at_checkin' && (
                        <CheckCircle2 className="w-4 h-4 text-amber-400" />
                      )}
                    </div>
                    <p className="text-[11px] text-slate-400">
                      Reserve now with instant confirmation. Settle your folio during check-in or upon departure.
                    </p>
                  </button>

                  <button
                    type="button"
                    onClick={() => setPaymentPreference('prepay_now')}
                    className={`p-3.5 rounded-2xl border text-left transition-all ${
                      paymentPreference === 'prepay_now'
                        ? 'bg-amber-500/15 border-amber-500/70 text-slate-100 shadow-md shadow-amber-500/10'
                        : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-bold text-white text-xs">Prepay Online (UPI / Card)</span>
                      {paymentPreference === 'prepay_now' && (
                        <CheckCircle2 className="w-4 h-4 text-amber-400" />
                      )}
                    </div>
                    <p className="text-[11px] text-slate-400">
                      Instantly pay {formatCurrency(grandTotal)} to guarantee priority express suite check-in.
                    </p>
                  </button>
                </div>
              </div>

              {/* Cancellation & Guarantee Policy */}
              <div className="p-3 bg-slate-950/60 border border-slate-800/80 rounded-xl flex items-center space-x-2 text-slate-400 text-[11px]">
                <ShieldCheck className="w-4 h-4 text-emerald-400 flex-shrink-0" />
                <span>
                  <strong>Flexible Hospitality Guarantee:</strong> Free cancellation up to 24 hours prior to check-in. Instant digital invoice generated upon confirmation.
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer Controls */}
        <div className="p-4 sm:p-5 bg-slate-950/90 border-t border-slate-800/90 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="text-xs text-slate-400 flex items-center space-x-2">
            <span>Room {room.number}</span>
            <span>•</span>
            <span className="text-slate-200 font-medium">{nights} Night{nights > 1 ? 's' : ''}</span>
            <span>•</span>
            <span className="font-bold text-amber-400">{formatCurrency(grandTotal)}</span>
          </div>

          <div className="flex items-center space-x-2.5 w-full sm:w-auto justify-end">
            {step === 'summary' ? (
              <>
                <button
                  type="button"
                  onClick={() => setStep('configure')}
                  className="px-4 py-2.5 bg-slate-850 hover:bg-slate-800 text-slate-300 rounded-xl text-xs font-semibold transition-colors flex items-center space-x-1.5 border border-slate-700"
                >
                  <ArrowLeft className="w-3.5 h-3.5" />
                  <span>Modify Stay Details</span>
                </button>

                <button
                  type="button"
                  disabled={isSubmitting}
                  onClick={handleFinalize}
                  className="flex-1 sm:flex-initial px-6 py-2.5 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold rounded-xl text-xs shadow-lg shadow-amber-500/25 transition-all flex items-center justify-center space-x-2 cursor-pointer disabled:opacity-50"
                >
                  {isSubmitting ? (
                    <span>Confirming Reservation...</span>
                  ) : (
                    <>
                      <CheckCircle2 className="w-4 h-4" />
                      <span>Finalize & Confirm Reservation</span>
                    </>
                  )}
                </button>
              </>
            ) : (
              <>
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2.5 bg-slate-850 hover:bg-slate-800 text-slate-300 rounded-xl text-xs font-semibold transition-colors border border-slate-700"
                >
                  Cancel
                </button>

                <button
                  type="button"
                  onClick={() => setStep('summary')}
                  className="flex-1 sm:flex-initial px-6 py-2.5 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold rounded-xl text-xs shadow-lg shadow-amber-500/25 transition-all flex items-center justify-center space-x-2 cursor-pointer"
                >
                  <span>Review Dates & Cost Summary</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
