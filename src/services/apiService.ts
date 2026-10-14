import { RoomItem, ReservationItem, InvoiceItem, ServiceItem, GuestServiceCharge, HotelSettingsConfig } from '../types/pms';

export interface DatabaseStatus {
  databaseFile: string;
  status: string;
  tables: Record<string, number>;
  fileSizeBytes: number;
}

export const apiService = {
  // Check Database Status
  async getDatabaseStatus(): Promise<DatabaseStatus | null> {
    try {
      const res = await fetch('/api/database/status');
      if (!res.ok) return null;
      return await res.json();
    } catch {
      return null;
    }
  },

  // Reset Database (Clears all dummy bookings, retains rooms and settings)
  async resetDatabase(): Promise<{ success: boolean; message: string }> {
    try {
      const res = await fetch('/api/database/reset', { method: 'POST' });
      return await res.json();
    } catch (err: any) {
      return { success: false, message: err.message || 'Failed to reset database' };
    }
  },

  // Download SQL schema or export dump
  async downloadSqlDump(): Promise<void> {
    const res = await fetch('/api/database/export');
    if (!res.ok) throw new Error('Failed to download SQL dump');
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `kr_luxe_hotel_database_${new Date().toISOString().split('T')[0]}.sql`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  },

  // Fetch Rooms
  async getRooms(): Promise<RoomItem[] | null> {
    try {
      const res = await fetch('/api/rooms');
      if (!res.ok) return null;
      return await res.json();
    } catch {
      return null;
    }
  },

  // Fetch Reservations
  async getReservations(): Promise<ReservationItem[] | null> {
    try {
      const res = await fetch('/api/reservations');
      if (!res.ok) return null;
      return await res.json();
    } catch {
      return null;
    }
  },

  // Create Hotel Booking - STORES IN SQL DATABASE
  async createReservation(bookingData: {
    guestName: string;
    guestEmail: string;
    guestPhone: string;
    roomId?: number;
    roomNumber: string;
    roomType: string;
    checkInDate: string;
    checkOutDate: string;
    numberOfGuests?: number;
    numberOfNights: number;
    roomCharges: number;
    discount?: number;
    taxAmount: number;
    grandTotal: number;
    specialRequests?: string;
    paymentMethod?: string;
    payNow?: boolean;
  }): Promise<{ reservation: ReservationItem; invoice: InvoiceItem } | null> {
    try {
      const res = await fetch('/api/reservations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bookingData)
      });
      if (!res.ok) return null;
      const data = await res.json();
      return {
        reservation: data.reservation,
        invoice: data.invoice
      };
    } catch (err) {
      console.error('Failed to create reservation in SQL database:', err);
      return null;
    }
  },

  // Fetch Invoices
  async getInvoices(): Promise<InvoiceItem[] | null> {
    try {
      const res = await fetch('/api/invoices');
      if (!res.ok) return null;
      return await res.json();
    } catch {
      return null;
    }
  },

  // Settle Invoice in SQL
  async settleInvoice(id: number): Promise<boolean> {
    try {
      const res = await fetch(`/api/invoices/${id}/pay`, { method: 'PUT' });
      return res.ok;
    } catch {
      return false;
    }
  },

  // Fetch Services
  async getServices(): Promise<ServiceItem[] | null> {
    try {
      const res = await fetch('/api/services');
      if (!res.ok) return null;
      return await res.json();
    } catch {
      return null;
    }
  },

  // Fetch Guest Service Orders
  async getGuestServices(): Promise<GuestServiceCharge[] | null> {
    try {
      const res = await fetch('/api/guest-services');
      if (!res.ok) return null;
      return await res.json();
    } catch {
      return null;
    }
  },

  // Create Guest Service Order
  async addGuestService(orderData: {
    reservationId: number;
    guestName: string;
    roomNumber: string;
    serviceId: number;
    serviceName: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
    notes?: string;
  }): Promise<GuestServiceCharge | null> {
    try {
      const res = await fetch('/api/guest-services', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(orderData)
      });
      if (!res.ok) return null;
      return await res.json();
    } catch {
      return null;
    }
  },

  // Fetch Settings
  async getSettings(): Promise<HotelSettingsConfig | null> {
    try {
      const res = await fetch('/api/settings');
      if (!res.ok) return null;
      return await res.json();
    } catch {
      return null;
    }
  }
};
