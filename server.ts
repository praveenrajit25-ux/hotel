import express from 'express';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';
import {
  getDatabase,
  getDbStats,
  getAllRooms,
  addRoom,
  getAllReservations,
  createReservationAndInvoice,
  updateReservationStatus,
  getAllInvoices,
  settleInvoicePayment,
  getAllServices,
  getAllGuestServices,
  addGuestService,
  updateGuestServiceStatus,
  getAllStaff,
  getHotelSettings,
  resetDatabase,
  exportSqlDump
} from './server/db';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

async function startServer() {
  const app = express();
  const PORT = Number(process.env.PORT) || 3000;
  const isProduction = process.env.NODE_ENV === 'production';

  app.use(express.json());

  // Initialize SQLite database on startup
  try {
    await getDatabase();
    console.log('✅ SQLite Hotel Database successfully initialized at database/hotel_management.db');
  } catch (err) {
    console.error('Database initialization warning:', err);
  }

  // =====================================================================
  // SQL DATABASE & API ROUTES
  // =====================================================================

  // Database Health & Status
  app.get('/api/database/status', async (_req, res) => {
    try {
      const stats = await getDbStats();
      res.json({ success: true, ...stats });
    } catch (err: any) {
      res.status(500).json({ success: false, error: err.message });
    }
  });

  // Get raw schema.sql content
  app.get('/api/database/schema', (_req, res) => {
    try {
      const schemaPath = path.resolve(process.cwd(), 'database', 'schema.sql');
      const content = fs.readFileSync(schemaPath, 'utf-8');
      res.setHeader('Content-Type', 'text/plain');
      res.send(content);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  // Download complete live database SQL dump file
  app.get('/api/database/export', async (_req, res) => {
    try {
      const sqlDump = await exportSqlDump();
      res.setHeader('Content-Type', 'application/sql');
      res.setHeader('Content-Disposition', 'attachment; filename="kr_luxe_hotel_database.sql"');
      res.send(sqlDump);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  // Reset database (wipes dummy bookings and seeds clean room inventory)
  app.post('/api/database/reset', async (_req, res) => {
    try {
      const result = await resetDatabase();
      res.json(result);
    } catch (err: any) {
      res.status(500).json({ success: false, error: err.message });
    }
  });

  // Hotel Settings
  app.get('/api/settings', async (_req, res) => {
    try {
      const settings = await getHotelSettings();
      res.json(settings);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  // Rooms
  app.get('/api/rooms', async (_req, res) => {
    try {
      const rooms = await getAllRooms();
      res.json(rooms);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  app.post('/api/rooms', async (req, res) => {
    try {
      const newRoom = await addRoom(req.body);
      res.status(201).json(newRoom);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  // Reservations (Hotel Bookings)
  app.get('/api/reservations', async (_req, res) => {
    try {
      const reservations = await getAllReservations();
      res.json(reservations);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  // CREATE NEW BOOKING -> STORES DIRECTLY IN SQL DATABASE
  app.post('/api/reservations', async (req, res) => {
    try {
      const result = await createReservationAndInvoice(req.body);
      res.status(201).json({
        success: true,
        message: 'Booking inserted into SQL database successfully!',
        ...result
      });
    } catch (err: any) {
      console.error('Error creating SQL booking:', err);
      res.status(500).json({ success: false, error: err.message });
    }
  });

  app.put('/api/reservations/:id/status', async (req, res) => {
    try {
      const id = Number(req.params.id);
      const { status } = req.body;
      const updated = await updateReservationStatus(id, status);
      res.json(updated);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  // Invoices & Billing
  app.get('/api/invoices', async (_req, res) => {
    try {
      const invoices = await getAllInvoices();
      res.json(invoices);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  app.put('/api/invoices/:id/pay', async (req, res) => {
    try {
      const id = Number(req.params.id);
      const updated = await settleInvoicePayment(id);
      res.json({ success: true, invoice: updated });
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  // Services Catalog
  app.get('/api/services', async (_req, res) => {
    try {
      const services = await getAllServices();
      res.json(services);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  // Guest Service Orders
  app.get('/api/guest-services', async (_req, res) => {
    try {
      const orders = await getAllGuestServices();
      res.json(orders);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  app.post('/api/guest-services', async (req, res) => {
    try {
      const order = await addGuestService(req.body);
      res.status(201).json(order);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  app.put('/api/guest-services/:id/status', async (req, res) => {
    try {
      const id = Number(req.params.id);
      const { status } = req.body;
      const updated = await updateGuestServiceStatus(id, status);
      res.json(updated);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  // Staff Directory
  app.get('/api/staff', async (_req, res) => {
    try {
      const staff = await getAllStaff();
      res.json(staff);
    } catch (err: any) {
      res.status(500).json({ error: err.message });
    }
  });

  // =====================================================================
  // VITE INTEGRATION
  // =====================================================================

  if (!isProduction) {
    const { createServer: createViteServer } = await import('vite');
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.resolve(__dirname, 'dist');
    app.use(express.static(distPath));
    app.get('*', (_req, res) => {
      res.sendFile(path.resolve(distPath, 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 KR Luxe Hotel Server running at http://localhost:${PORT}`);
    console.log(`📁 SQL Database storage: database/hotel_management.db`);
  });
}

startServer().catch(err => {
  console.error('Fatal server startup error:', err);
  process.exit(1);
});
