import {
  RoomItem,
  GuestItem,
  ReservationItem,
  CheckInLog,
  CheckOutLog,
  ServiceItem,
  GuestServiceCharge,
  InvoiceItem,
  StaffItem,
  HotelSettingsConfig,
  GuestNotification
} from '../types/pms';

export const initialSettings: HotelSettingsConfig = {
  hotelName: 'KR LUXE HOTEL',
  tagline: 'Excellence in Luxury Hospitality & Comfort',
  address: '777 Ocean Blvd, Suite 100, Miami Beach, FL 33139',
  phone: '+91 98765 43210',
  email: 'concierge@krluxehotel.com',
  website: 'www.krluxehotel.com',
  taxPercentage: 12.0,
  currencySymbol: '₹',
  checkInTime: '14:00',
  checkOutTime: '11:00',
  invoiceFooter: 'Thank you for choosing KR LUXE HOTEL. We look forward to welcoming you again!'
};

export const initialRooms: RoomItem[] = [
  { id: 1, number: '101', type: 'Single', floor: 1, pricePerNight: 3500.00, capacity: 1, status: 'Available', description: 'Cozy single room with queen bed and garden view' },
  { id: 2, number: '102', type: 'Double', floor: 1, pricePerNight: 5500.00, capacity: 2, status: 'Occupied', description: 'Spacious double room with balcony (Guest: Alexander Vance)' },
  { id: 3, number: '103', type: 'Double', floor: 1, pricePerNight: 5500.00, capacity: 2, status: 'Available', description: 'Double room facing courtyard' },
  { id: 4, number: '201', type: 'Deluxe', floor: 2, pricePerNight: 8500.00, capacity: 3, status: 'Reserved', description: 'Deluxe room with panoramic ocean view (Reserved: Sophia Chen)' },
  { id: 5, number: '202', type: 'Deluxe', floor: 2, pricePerNight: 8500.00, capacity: 3, status: 'Occupied', description: 'Deluxe room with king bed & workspace (Guest: Elena Rostova)' },
  { id: 6, number: '301', type: 'Suite', floor: 3, pricePerNight: 15000.00, capacity: 4, status: 'Occupied', description: 'Luxury corner suite with private jacuzzi (Guest: Marcus Sterling)' },
  { id: 7, number: '302', type: 'Executive', floor: 3, pricePerNight: 25000.00, capacity: 5, status: 'Available', description: 'Presidential penthouse with private terrace & bar' },
  { id: 8, number: '104', type: 'Single', floor: 1, pricePerNight: 3500.00, capacity: 1, status: 'Available', description: 'Executive single bedroom with marble bath' },
  { id: 9, number: '203', type: 'Double', floor: 2, pricePerNight: 5800.00, capacity: 2, status: 'Available', description: 'Sunny double room with modern walk-in shower' },
  { id: 10, number: '204', type: 'Deluxe', floor: 2, pricePerNight: 8800.00, capacity: 3, status: 'Available', description: 'Deluxe family room with sofa bed' }
];

export const initialGuests: GuestItem[] = [
  { id: 1, fullName: 'Alexander Vance', gender: 'Male', dob: '1985-04-12', phone: '+91 98421 00142', email: 'a.vance@example.com', address: '124 Beacon St, Boston, MA', idProofType: 'Passport', idProofNumber: 'P9842104', nationality: 'United States', registeredDate: '2026-10-01 10:15:00' },
  { id: 2, fullName: 'Elena Rostova', gender: 'Female', dob: '1992-09-23', phone: '+91 98421 00188', email: 'elena.rostova@example.com', address: '450 Park Ave, New York, NY', idProofType: 'Driver_License', idProofNumber: 'DL-NY-84920', nationality: 'United States', registeredDate: '2026-10-05 14:20:00' },
  { id: 3, fullName: 'Marcus Sterling', gender: 'Male', dob: '1978-11-30', phone: '+91 98421 00277', email: 'm.sterling@capital.io', address: '88 Montgomery St, San Francisco, CA', idProofType: 'Passport', idProofNumber: 'P3301984', nationality: 'United Kingdom', registeredDate: '2026-10-08 09:00:00' },
  { id: 4, fullName: 'Sophia Chen', gender: 'Female', dob: '1995-02-18', phone: '+91 98421 00391', email: 'sophia.chen@tech.org', address: '720 Pine St, Seattle, WA', idProofType: 'National_Id', idProofNumber: 'NID-99201', nationality: 'Canada', registeredDate: '2026-10-10 16:45:00' },
  { id: 5, fullName: 'David Miller', gender: 'Male', dob: '1989-07-04', phone: '+91 98421 00419', email: 'dmiller@innovate.com', address: '310 Michigan Ave, Chicago, IL', idProofType: 'Passport', idProofNumber: 'P7712044', nationality: 'United States', registeredDate: '2026-10-04 11:30:00' }
];

export const initialReservations: ReservationItem[] = [
  { id: 1001, guestId: 1, guestName: 'Alexander Vance', guestPhone: '+91 98421 00142', roomId: 2, roomNumber: '102', roomType: 'Double', checkInDate: '2026-10-10', checkOutDate: '2026-10-15', numberOfGuests: 2, status: 'Checked_In', specialRequests: 'High floor, late checkout if available', createdAt: '2026-10-08 14:00:00' },
  { id: 1002, guestId: 2, guestName: 'Elena Rostova', guestPhone: '+91 98421 00188', roomId: 5, roomNumber: '202', roomType: 'Deluxe', checkInDate: '2026-10-11', checkOutDate: '2026-10-16', numberOfGuests: 2, status: 'Checked_In', specialRequests: 'Champagne on arrival & extra down pillows', createdAt: '2026-10-09 09:30:00' },
  { id: 1003, guestId: 3, guestName: 'Marcus Sterling', guestPhone: '+91 98421 00277', roomId: 6, roomNumber: '301', roomType: 'Suite', checkInDate: '2026-10-09', checkOutDate: '2026-10-14', numberOfGuests: 3, status: 'Checked_In', specialRequests: 'Airport transfer required', createdAt: '2026-10-07 11:15:00' },
  { id: 1004, guestId: 4, guestName: 'Sophia Chen', guestPhone: '+91 98421 00391', roomId: 4, roomNumber: '201', roomType: 'Deluxe', checkInDate: '2026-10-14', checkOutDate: '2026-10-18', numberOfGuests: 2, status: 'Confirmed', specialRequests: 'Quiet room away from elevators', createdAt: '2026-10-11 16:00:00' },
  { id: 1005, guestId: 5, guestName: 'David Miller', guestPhone: '+91 98421 00419', roomId: 7, roomNumber: '302', roomType: 'Executive', checkInDate: '2026-10-05', checkOutDate: '2026-10-09', numberOfGuests: 4, status: 'Checked_Out', specialRequests: 'VIP Executive lounge access', createdAt: '2026-10-02 10:00:00' }
];

export const initialGuestServices: GuestServiceCharge[] = [
  { id: 1, reservationId: 1001, guestName: 'Alexander Vance', roomNumber: '102', serviceId: 1, serviceName: 'Gourmet In-Suite Breakfast', quantity: 2, unitPrice: 1200.00, totalPrice: 2400.00, serviceDate: '2026-10-11 08:30:00', notes: 'Breakfast billed to room', status: 'Completed', completedAt: '2026-10-11 08:50:00' },
  { id: 2, reservationId: 1002, guestName: 'Elena Rostova', roomNumber: '202', serviceId: 2, serviceName: 'Ayurvedic Spa & Herbal Massage', quantity: 1, unitPrice: 3500.00, totalPrice: 3500.00, serviceDate: '2026-10-12 15:00:00', notes: 'Spa appointment', status: 'Completed', completedAt: '2026-10-12 16:15:00' },
  { id: 3, reservationId: 1003, guestName: 'Marcus Sterling', roomNumber: '301', serviceId: 3, serviceName: 'Airport Luxury Chauffeur Transfer', quantity: 1, unitPrice: 2200.00, totalPrice: 2200.00, serviceDate: '2026-10-09 18:00:00', notes: 'Arrival pickup', status: 'Completed', completedAt: '2026-10-09 18:45:00' },
  { id: 4, reservationId: 1003, guestName: 'Marcus Sterling', roomNumber: '301', serviceId: 6, serviceName: 'Sunset Champagne & Canapes Basket', quantity: 1, unitPrice: 2800.00, totalPrice: 2800.00, serviceDate: '2026-10-10 21:00:00', notes: 'Mini bar restock', status: 'Completed', completedAt: '2026-10-10 21:20:00' }
];

export const initialInvoices: InvoiceItem[] = [
  {
    id: 1,
    invoiceNumber: 'INV-2026-1001',
    reservationId: 1001,
    guestName: 'Alexander Vance',
    roomNumber: '102',
    roomType: 'Double',
    checkInDate: '2026-10-10',
    checkOutDate: '2026-10-15',
    numberOfNights: 5,
    roomCharges: 27500.00,
    serviceCharges: 2400.00,
    additionalCharges: 0.00,
    subtotal: 29900.00,
    discount: 0.00,
    taxPercentage: 18.0,
    taxAmount: 5382.00,
    grandTotal: 35282.00,
    paidAmount: 35282.00,
    balanceAmount: 0.00,
    paymentStatus: 'Paid',
    invoiceDate: '2026-10-10',
    payments: [
      { id: 1, amount: 35282.00, method: 'Credit_Card', transactionRef: 'TXN-CC-8849102', date: '2026-10-10 10:15:00', notes: 'Full settlement at front desk' }
    ]
  },
  {
    id: 2,
    invoiceNumber: 'INV-2026-1002',
    reservationId: 1002,
    guestName: 'Elena Rostova',
    roomNumber: '202',
    roomType: 'Deluxe',
    checkInDate: '2026-10-11',
    checkOutDate: '2026-10-16',
    numberOfNights: 5,
    roomCharges: 42500.00,
    serviceCharges: 3500.00,
    additionalCharges: 0.00,
    subtotal: 46000.00,
    discount: 2000.00,
    taxPercentage: 18.0,
    taxAmount: 7920.00,
    grandTotal: 51920.00,
    paidAmount: 25000.00,
    balanceAmount: 26920.00,
    paymentStatus: 'Partially_Paid',
    invoiceDate: '2026-10-11',
    payments: [
      { id: 2, amount: 25000.00, method: 'Credit_Card', transactionRef: 'TXN-CC-9102834', date: '2026-10-11 14:30:00', notes: 'Advance deposit at check-in' }
    ]
  },
  {
    id: 3,
    invoiceNumber: 'INV-2026-1003',
    reservationId: 1003,
    guestName: 'Marcus Sterling',
    roomNumber: '301',
    roomType: 'Suite',
    checkInDate: '2026-10-09',
    checkOutDate: '2026-10-14',
    numberOfNights: 5,
    roomCharges: 70000.00,
    serviceCharges: 5000.00,
    additionalCharges: 0.00,
    subtotal: 75000.00,
    discount: 5000.00,
    taxPercentage: 18.0,
    taxAmount: 12600.00,
    grandTotal: 82600.00,
    paidAmount: 82600.00,
    balanceAmount: 0.00,
    paymentStatus: 'Paid',
    invoiceDate: '2026-10-09',
    payments: [
      { id: 3, amount: 82600.00, method: 'Bank_Transfer', transactionRef: 'TXN-UPI-992019', date: '2026-10-09 09:00:00', notes: 'Corporate UPI / Bank transfer' }
    ]
  },
  {
    id: 4,
    invoiceNumber: 'INV-2026-1004',
    reservationId: 1004,
    guestName: 'Sophia Chen',
    roomNumber: '201',
    roomType: 'Deluxe',
    checkInDate: '2026-10-14',
    checkOutDate: '2026-10-18',
    numberOfNights: 4,
    roomCharges: 34000.00,
    serviceCharges: 0.00,
    additionalCharges: 0.00,
    subtotal: 34000.00,
    discount: 0.00,
    taxPercentage: 18.0,
    taxAmount: 6120.00,
    grandTotal: 40120.00,
    paidAmount: 0.00,
    balanceAmount: 40120.00,
    paymentStatus: 'Pending',
    invoiceDate: '2026-10-13',
    payments: []
  },
  {
    id: 5,
    invoiceNumber: 'INV-2026-1005',
    reservationId: 1005,
    guestName: 'David Miller',
    roomNumber: '302',
    roomType: 'Executive',
    checkInDate: '2026-10-05',
    checkOutDate: '2026-10-09',
    numberOfNights: 4,
    roomCharges: 56000.00,
    serviceCharges: 5350.00,
    additionalCharges: 0.00,
    subtotal: 61350.00,
    discount: 0.00,
    taxPercentage: 18.0,
    taxAmount: 11043.00,
    grandTotal: 72393.00,
    paidAmount: 72393.00,
    balanceAmount: 0.00,
    paymentStatus: 'Paid',
    invoiceDate: '2026-10-05',
    payments: [
      { id: 5, amount: 72393.00, method: 'Credit_Card', transactionRef: 'TXN-CC-4019241', date: '2026-10-05 10:45:00', notes: 'Settlement at checkout' }
    ]
  }
];

export const initialServices: ServiceItem[] = [
  { id: 1, name: 'Gourmet In-Suite Breakfast', category: 'Dining', price: 1200.00, description: 'Continental & South Indian breakfast spread with freshly squeezed juice and artisan breads', status: 'Active' },
  { id: 2, name: 'Ayurvedic Spa & Herbal Massage', category: 'Wellness', price: 3500.00, description: '60-minute relaxing organic herbal oil therapy by licensed masseurs', status: 'Active' },
  { id: 3, name: 'Airport Luxury Chauffeur Transfer', category: 'Transport', price: 2200.00, description: 'Private Mercedes E-Class transfer to/from Chennai International Airport', status: 'Active' },
  { id: 4, name: 'Evening Fine-Dining Tasting Menu', category: 'Dining', price: 4500.00, description: '5-course chef-curated royal tasting menu at The Jewel of KR dining room', status: 'Active' },
  { id: 5, name: 'Express Garment Pressing & Laundry', category: 'Housekeeping', price: 850.00, description: 'Same-day laundry, steam press, and luxury garment packaging', status: 'Active' },
  { id: 6, name: 'Sunset Champagne & Canapes Basket', category: 'Amenities', price: 2800.00, description: 'Chilled Moët & Chandon bottle with imported cheeses and fresh berries', status: 'Active' }
];

export const initialCheckIns: CheckInLog[] = [
  { id: 1, reservationId: 1001, guestName: 'Alexander Vance', roomNumber: '102', roomKeyIssued: 'Key Card #102', verifiedIdProof: true, checkInTime: '2026-10-10 14:15:00', processedBy: 'Priya Sundaram (Receptionist)', notes: 'Assigned 1st floor room' },
  { id: 2, reservationId: 1002, guestName: 'Elena Rostova', roomNumber: '202', roomKeyIssued: 'Key Card #202', verifiedIdProof: true, checkInTime: '2026-10-11 15:05:00', processedBy: 'Priya Sundaram (Receptionist)', notes: 'Welcome pack delivered to room' },
  { id: 3, reservationId: 1003, guestName: 'Marcus Sterling', roomNumber: '301', roomKeyIssued: 'Keycards #301-A, #301-B', verifiedIdProof: true, checkInTime: '2026-10-09 18:30:00', processedBy: 'Yugan G (Manager)', notes: 'VIP welcome pack delivered' }
];

export const initialCheckOuts: CheckOutLog[] = [
  { id: 1, reservationId: 1005, guestName: 'David Miller', roomNumber: '302', roomInspected: true, keyReturned: true, checkOutTime: '2026-10-09 10:45:00', processedBy: 'Priya Sundaram (Receptionist)', notes: 'Room in good condition. Housekeeping scheduled.' }
];

export const initialStaff: StaffItem[] = [
  { id: 1, employeeCode: 'EMP-001', fullName: 'Arthur Sterling', username: 'admin', role: 'Admin', phone: '+91 98421 00100', email: 'admin@grandluxehotel.com', salary: 95000.00, joiningDate: '2024-01-15', status: 'Active' },
  { id: 2, employeeCode: 'EMP-002', fullName: 'Yugan G', username: 'yugan', role: 'Manager', phone: '+91 98421 00101', email: 'yugan0302@gmail.com', salary: 75000.00, joiningDate: '2024-03-01', status: 'Active' },
  { id: 3, employeeCode: 'EMP-003', fullName: 'Sarah Jenkins', username: 'receptionist', role: 'Receptionist', phone: '+91 98421 00102', email: 'sjenkins@grandluxehotel.com', salary: 45000.00, joiningDate: '2024-06-15', status: 'Active' },
  { id: 4, employeeCode: 'EMP-004', fullName: 'Maria Rodriguez', username: 'housekeeping', role: 'Housekeeping', phone: '+91 98421 00103', email: 'mrodriguez@grandluxehotel.com', salary: 32000.00, joiningDate: '2024-07-01', status: 'Active' },
  { id: 5, employeeCode: 'EMP-005', fullName: 'Chef Jean-Luc', username: 'chef', role: 'Chef', phone: '+91 98421 00104', email: 'jeanluc@grandluxehotel.com', salary: 65000.00, joiningDate: '2024-08-01', status: 'Active' }
];

export const initialNotifications: GuestNotification[] = [
  {
    id: 'notif-001',
    guestName: 'Alexander Vance',
    guestEmail: 'alexander@example.com',
    roomNumber: '102',
    type: 'checkout_reminder',
    title: 'Upcoming Check-Out Alert',
    message: 'Your stay in Room 102 concludes on August 30 at 11:00 AM. Please ensure personal luggage is gathered. You may review your folio statement or request a late check-out privilege (until 4 PM).',
    timestamp: 'Today at 08:30 AM',
    read: false,
    actionLabel: 'Review Folio & Departure',
    actionTab: 'bookings'
  },
  {
    id: 'notif-002',
    guestName: 'Alexander Vance',
    guestEmail: 'alexander@example.com',
    roomNumber: '102',
    type: 'room_service',
    title: 'Room Service Completed & Delivered',
    message: 'Your order of "Gourmet Breakfast Buffet" (2x) has been prepared and delivered to Room 102. Freshly plated with compliments of our culinary staff!',
    timestamp: 'Yesterday at 08:50 AM',
    read: true,
    actionLabel: 'View Concierge Services',
    actionTab: 'services',
    orderId: 1
  }
];
