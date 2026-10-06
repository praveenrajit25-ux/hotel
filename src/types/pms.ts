export type UserRole = 'Admin' | 'Manager' | 'Receptionist' | 'Housekeeping' | 'Chef' | 'Staff';

export type AuthRole = 'Admin' | 'Manager' | 'Customer';

export interface AuthUser {
  id: string;
  email: string;
  name: string;
  role: AuthRole;
  phone?: string;
  memberTier?: 'Silver' | 'Gold' | 'Platinum' | 'Diamond';
  registeredAt?: string;
  avatar?: string;
}

export type RoomStatus = 'Available' | 'Reserved' | 'Occupied' | 'Cleaning' | 'Maintenance';
export type RoomType = 'Single' | 'Double' | 'Deluxe' | 'Suite' | 'Executive';

export type ReservationStatus = 'Confirmed' | 'Checked_In' | 'Checked_Out' | 'Cancelled';
export type PaymentStatus = 'Pending' | 'Partially_Paid' | 'Paid';
export type PaymentMethod = 'Cash' | 'Credit_Card' | 'Debit_Card' | 'Bank_Transfer' | 'UPI' | 'Mobile_Payment';

export interface RoomItem {
  id: number;
  number: string;
  type: RoomType;
  floor: number;
  pricePerNight: number;
  capacity: number;
  status: RoomStatus;
  description: string;
}

export interface GuestItem {
  id: number;
  fullName: string;
  gender: string;
  dob: string;
  phone: string;
  email: string;
  address: string;
  idProofType: string;
  idProofNumber: string;
  nationality: string;
  registeredDate: string;
}

export interface ReservationItem {
  id: number;
  guestId: number;
  guestName: string;
  guestPhone: string;
  roomId: number;
  roomNumber: string;
  roomType: RoomType;
  checkInDate: string;
  checkOutDate: string;
  numberOfGuests: number;
  status: ReservationStatus;
  specialRequests?: string;
  createdAt: string;
}

export interface CheckInLog {
  id: number;
  reservationId: number;
  guestName: string;
  roomNumber: string;
  roomKeyIssued: string;
  verifiedIdProof: boolean;
  checkInTime: string;
  processedBy: string;
  notes?: string;
}

export interface CheckOutLog {
  id: number;
  reservationId: number;
  guestName: string;
  roomNumber: string;
  roomInspected: boolean;
  keyReturned: boolean;
  checkOutTime: string;
  processedBy: string;
  notes?: string;
}

export interface ServiceItem {
  id: number;
  name: string;
  category: string;
  price: number;
  description: string;
  status: 'Active' | 'Inactive';
}

export type ServiceOrderStatus = 'Preparing' | 'In_Transit' | 'Completed';

export interface GuestServiceCharge {
  id: number;
  reservationId: number;
  guestName: string;
  roomNumber: string;
  serviceId: number;
  serviceName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  serviceDate: string;
  notes?: string;
  status?: ServiceOrderStatus;
  completedAt?: string;
}

export type GuestNotificationType = 'room_service' | 'checkout_reminder' | 'welcome' | 'late_checkout';

export interface GuestNotification {
  id: string;
  guestName: string;
  guestEmail?: string;
  roomNumber?: string;
  type: GuestNotificationType;
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
  actionLabel?: string;
  actionTab?: 'overview' | 'suites' | 'services' | 'bookings' | 'billing' | 'profile';
  orderId?: number;
}

export interface InvoiceItem {
  id: number;
  invoiceNumber: string;
  reservationId: number;
  guestName: string;
  roomNumber: string;
  roomType: string;
  checkInDate: string;
  checkOutDate: string;
  numberOfNights: number;
  roomCharges: number;
  serviceCharges: number;
  additionalCharges: number;
  subtotal: number;
  discount: number;
  taxPercentage: number;
  taxAmount: number;
  grandTotal: number;
  paidAmount: number;
  balanceAmount: number;
  paymentStatus: PaymentStatus;
  invoiceDate: string;
  payments: PaymentRecord[];
}

export interface PaymentRecord {
  id: number;
  amount: number;
  method: PaymentMethod;
  transactionRef: string;
  date: string;
  notes?: string;
}

export interface StaffItem {
  id: number;
  employeeCode: string;
  fullName: string;
  username: string;
  role: UserRole;
  phone: string;
  email: string;
  salary: number;
  joiningDate: string;
  status: 'Active' | 'Inactive' | 'On Leave' | 'Terminated';
}

export interface HotelSettingsConfig {
  hotelName: string;
  tagline: string;
  address: string;
  phone: string;
  email: string;
  website: string;
  taxPercentage: number;
  currencySymbol: string;
  checkInTime: string;
  checkOutTime: string;
  invoiceFooter: string;
}
