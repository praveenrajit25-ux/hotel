import { InvoiceItem, HotelSettingsConfig } from '../types/pms';

/**
 * Formats numeric values to Indian Rupee currency display for canvas.
 * Note: Uses standard 'Rs.' or '₹' with fallback to ensure universal font rendering across platforms.
 */
function formatRupees(amount: number): string {
  return '₹' + amount.toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

/**
 * Draws rounded rectangle on HTML5 canvas context.
 */
function roundRect(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  width: number,
  height: number,
  radius: number
) {
  ctx.beginPath();
  ctx.moveTo(x + radius, y);
  ctx.lineTo(x + width - radius, y);
  ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
  ctx.lineTo(x + width, y + height - radius);
  ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
  ctx.lineTo(x + radius, y + height);
  ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
  ctx.lineTo(x, y + radius);
  ctx.quadraticCurveTo(x, y, x + radius, y);
  ctx.closePath();
}

export interface FolioImageOptions {
  hotelName?: string;
  hotelAddress?: string;
  hotelContact?: string;
  hotelEmail?: string;
  format?: 'png' | 'jpeg';
}

/**
 * Generates an ultra-high-definition, publication-grade hotel folio receipt image
 * using HTML5 Canvas and triggers an immediate download in PNG or JPEG format.
 */
export function downloadFolioReceiptImage(
  invoice: InvoiceItem,
  settings?: Partial<HotelSettingsConfig>,
  options?: FolioImageOptions
): Promise<string> {
  return new Promise((resolve, reject) => {
    try {
      const hotelName = settings?.hotelName || options?.hotelName || 'KR LUXE HOTEL & RESORT';
      const hotelAddress = settings?.address || options?.hotelAddress || '108 Grand Luxury Boulevard, Suite 500, Chennai, India';
      const hotelPhone = settings?.phone || options?.hotelContact || '+91 98421 00000';
      const hotelEmail = settings?.email || options?.hotelEmail || 'reservations@krluxe.com';
      const taxRate = settings?.taxPercentage ?? invoice.taxPercentage ?? 18;

      // Base dimensions for high quality print resolution (scaled by 2 for Retina)
      const baseWidth = 840;
      const baseHeight = 1180;
      const scale = 2; // 2x Retina super-sampling

      const canvas = document.createElement('canvas');
      canvas.width = baseWidth * scale;
      canvas.height = baseHeight * scale;

      const ctx = canvas.getContext('2d');
      if (!ctx) {
        throw new Error('Canvas 2D context not available');
      }

      ctx.scale(scale, scale);

      // --- 1. Background Gradient ---
      const bgGradient = ctx.createLinearGradient(0, 0, baseWidth, baseHeight);
      bgGradient.addColorStop(0, '#090D16'); // Deep Midnight
      bgGradient.addColorStop(0.5, '#0F172A'); // Rich Obsidian Slate
      bgGradient.addColorStop(1, '#0B0F19');
      ctx.fillStyle = bgGradient;
      ctx.fillRect(0, 0, baseWidth, baseHeight);

      // Outer Decorative Luxury Border
      ctx.strokeStyle = '#334155';
      ctx.lineWidth = 1.5;
      roundRect(ctx, 16, 16, baseWidth - 32, baseHeight - 32, 20);
      ctx.stroke();

      // Inner Gold Accent Trim
      ctx.strokeStyle = '#D97706';
      ctx.lineWidth = 1;
      roundRect(ctx, 22, 22, baseWidth - 44, baseHeight - 44, 16);
      ctx.stroke();

      // --- 2. Header Section: Logo Crest & Hotel Name ---
      const headerY = 52;

      // Crest Monogram Badge
      const crestSize = 64;
      const crestX = 52;
      const crestGrad = ctx.createLinearGradient(crestX, headerY, crestX + crestSize, headerY + crestSize);
      crestGrad.addColorStop(0, '#F59E0B');
      crestGrad.addColorStop(1, '#D97706');
      ctx.fillStyle = crestGrad;
      roundRect(ctx, crestX, headerY, crestSize, crestSize, 14);
      ctx.fill();

      // Crest Border
      ctx.strokeStyle = '#FCD34D';
      ctx.lineWidth = 1.5;
      roundRect(ctx, crestX, headerY, crestSize, crestSize, 14);
      ctx.stroke();

      // Crest Text
      ctx.fillStyle = '#0F172A';
      ctx.font = 'bold 28px "Georgia", "Times New Roman", serif';
      ctx.textAlign = 'center';
      ctx.fillText('KR', crestX + crestSize / 2, headerY + 43);

      // Hotel Titles
      ctx.textAlign = 'left';
      ctx.fillStyle = '#FFFFFF';
      ctx.font = 'bold 24px "Georgia", "Times New Roman", serif';
      ctx.fillText(hotelName.toUpperCase(), crestX + crestSize + 20, headerY + 28);

      ctx.fillStyle = '#F59E0B';
      ctx.font = '600 11px sans-serif';
      ctx.fillText('5-STAR LUXURY HOSPITALITY • OFFICIAL GUEST FOLIO & INVOICE', crestX + crestSize + 20, headerY + 46);

      ctx.fillStyle = '#94A3B8';
      ctx.font = '11px sans-serif';
      ctx.fillText(`${hotelAddress} • Tel: ${hotelPhone}`, crestX + crestSize + 20, headerY + 62);

      // Header Divider Line
      const dividerY = 136;
      const dividerGrad = ctx.createLinearGradient(50, dividerY, baseWidth - 50, dividerY);
      dividerGrad.addColorStop(0, 'rgba(217, 119, 6, 0.1)');
      dividerGrad.addColorStop(0.5, 'rgba(245, 158, 11, 0.8)');
      dividerGrad.addColorStop(1, 'rgba(217, 119, 6, 0.1)');
      ctx.strokeStyle = dividerGrad;
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.moveTo(50, dividerY);
      ctx.lineTo(baseWidth - 50, dividerY);
      ctx.stroke();

      // --- 3. Invoice & Status Bar ---
      const infoBarY = 156;

      // Left: Folio Number & Date
      ctx.fillStyle = '#94A3B8';
      ctx.font = '10px sans-serif';
      ctx.fillText('FOLIO NUMBER', 52, infoBarY + 12);

      ctx.fillStyle = '#F8FAFC';
      ctx.font = 'bold 18px "Georgia", serif';
      ctx.fillText(invoice.invoiceNumber, 52, infoBarY + 34);

      ctx.fillStyle = '#94A3B8';
      ctx.font = '11px sans-serif';
      ctx.fillText(`Issued: ${invoice.invoiceDate || new Date().toISOString().split('T')[0]}`, 52, infoBarY + 52);

      // Right: Payment Status Badge
      const statusBadgeWidth = 170;
      const statusBadgeHeight = 44;
      const statusBadgeX = baseWidth - 52 - statusBadgeWidth;
      const statusBadgeY = infoBarY + 8;

      const isPaid = invoice.paymentStatus === 'Paid';
      const isPartial = invoice.paymentStatus === 'Partially_Paid';

      if (isPaid) {
        ctx.fillStyle = 'rgba(16, 185, 129, 0.18)';
        ctx.strokeStyle = '#10B981';
      } else if (isPartial) {
        ctx.fillStyle = 'rgba(245, 158, 11, 0.18)';
        ctx.strokeStyle = '#F59E0B';
      } else {
        ctx.fillStyle = 'rgba(239, 68, 68, 0.18)';
        ctx.strokeStyle = '#EF4444';
      }

      ctx.lineWidth = 1.5;
      roundRect(ctx, statusBadgeX, statusBadgeY, statusBadgeWidth, statusBadgeHeight, 10);
      ctx.fill();
      ctx.stroke();

      ctx.textAlign = 'center';
      ctx.font = 'bold 13px sans-serif';
      if (isPaid) {
        ctx.fillStyle = '#34D399';
        ctx.fillText('PAID IN FULL', statusBadgeX + statusBadgeWidth / 2, statusBadgeY + 22);
        ctx.font = '10px sans-serif';
        ctx.fillStyle = '#A7F3D0';
        ctx.fillText('BALANCE SETTLED', statusBadgeX + statusBadgeWidth / 2, statusBadgeY + 36);
      } else if (isPartial) {
        ctx.fillStyle = '#FBBF24';
        ctx.fillText('PARTIALLY PAID', statusBadgeX + statusBadgeWidth / 2, statusBadgeY + 22);
        ctx.font = '10px sans-serif';
        ctx.fillStyle = '#FDE68A';
        ctx.fillText(`DUE: ${formatRupees(invoice.balanceAmount)}`, statusBadgeX + statusBadgeWidth / 2, statusBadgeY + 36);
      } else {
        ctx.fillStyle = '#F87171';
        ctx.fillText('PAYMENT PENDING', statusBadgeX + statusBadgeWidth / 2, statusBadgeY + 22);
        ctx.font = '10px sans-serif';
        ctx.fillStyle = '#FECACA';
        ctx.fillText(`DUE: ${formatRupees(invoice.balanceAmount)}`, statusBadgeX + statusBadgeWidth / 2, statusBadgeY + 36);
      }

      // --- 4. Guest & Stay Details Card ---
      const detailsCardY = 236;
      const detailsCardHeight = 138;
      ctx.fillStyle = 'rgba(15, 23, 42, 0.85)';
      ctx.strokeStyle = '#1E293B';
      ctx.lineWidth = 1;
      roundRect(ctx, 50, detailsCardY, baseWidth - 100, detailsCardHeight, 14);
      ctx.fill();
      ctx.stroke();

      // Details Columns
      ctx.textAlign = 'left';

      // Col 1: Guest Information
      const col1X = 74;
      ctx.fillStyle = '#94A3B8';
      ctx.font = '10px sans-serif';
      ctx.fillText('GUEST NAME', col1X, detailsCardY + 28);
      ctx.fillStyle = '#FFFFFF';
      ctx.font = 'bold 15px sans-serif';
      ctx.fillText(invoice.guestName, col1X, detailsCardY + 48);

      ctx.fillStyle = '#94A3B8';
      ctx.font = '10px sans-serif';
      ctx.fillText('RESERVATION REFERENCE', col1X, detailsCardY + 80);
      ctx.fillStyle = '#E2E8F0';
      ctx.font = '600 13px sans-serif';
      ctx.fillText(`RES-00${invoice.reservationId}`, col1X, detailsCardY + 98);

      // Col 2: Room Assigned
      const col2X = 320;
      ctx.fillStyle = '#94A3B8';
      ctx.font = '10px sans-serif';
      ctx.fillText('ROOM ASSIGNED', col2X, detailsCardY + 28);
      ctx.fillStyle = '#F59E0B';
      ctx.font = 'bold 15px sans-serif';
      ctx.fillText(`Room ${invoice.roomNumber}`, col2X, detailsCardY + 48);

      ctx.fillStyle = '#94A3B8';
      ctx.font = '10px sans-serif';
      ctx.fillText('CATEGORY / SUITE TYPE', col2X, detailsCardY + 80);
      ctx.fillStyle = '#E2E8F0';
      ctx.font = '600 13px sans-serif';
      ctx.fillText(`${invoice.roomType} Suite`, col2X, detailsCardY + 98);

      // Col 3: Dates & Duration
      const col3X = 560;
      ctx.fillStyle = '#94A3B8';
      ctx.font = '10px sans-serif';
      ctx.fillText('DATES OF STAY', col3X, detailsCardY + 28);
      ctx.fillStyle = '#FFFFFF';
      ctx.font = '600 13px sans-serif';
      ctx.fillText(`${invoice.checkInDate} to ${invoice.checkOutDate}`, col3X, detailsCardY + 48);

      ctx.fillStyle = '#94A3B8';
      ctx.font = '10px sans-serif';
      ctx.fillText('TOTAL NIGHTS', col3X, detailsCardY + 80);
      ctx.fillStyle = '#F59E0B';
      ctx.font = 'bold 14px sans-serif';
      ctx.fillText(`${invoice.numberOfNights} Night${invoice.numberOfNights > 1 ? 's' : ''}`, col3X, detailsCardY + 98);

      // --- 5. Itemized Charges Table ---
      const tableY = 400;
      const tableWidth = baseWidth - 100;
      const tableHeaderHeight = 36;

      // Table Header Row
      ctx.fillStyle = '#1E293B';
      roundRect(ctx, 50, tableY, tableWidth, tableHeaderHeight, 8);
      ctx.fill();

      ctx.fillStyle = '#E2E8F0';
      ctx.font = 'bold 11px sans-serif';
      ctx.textAlign = 'left';
      ctx.fillText('ITEM DESCRIPTION', 68, tableY + 23);
      ctx.fillText('CATEGORY', 420, tableY + 23);
      ctx.textAlign = 'right';
      ctx.fillText('AMOUNT', baseWidth - 70, tableY + 23);

      // Table Line Items
      interface LineItem {
        title: string;
        subtitle: string;
        category: string;
        amount: number;
        isDiscount?: boolean;
      }

      const lineItems: LineItem[] = [
        {
          title: `Room Accommodation (${invoice.numberOfNights} Nights)`,
          subtitle: `Room ${invoice.roomNumber} - ${invoice.roomType} Suite`,
          category: 'Accommodation',
          amount: invoice.roomCharges
        },
        {
          title: 'Concierge & In-Room Dining Services',
          subtitle: invoice.serviceCharges > 0 ? 'Food, Beverage & Spa charges' : 'No service charges incurred',
          category: 'Room Services',
          amount: invoice.serviceCharges
        }
      ];

      if (invoice.discount && invoice.discount > 0) {
        lineItems.push({
          title: 'Loyalty Privilege Privilege Voucher',
          subtitle: 'Exclusive member promo discount applied',
          category: 'Privilege Voucher',
          amount: invoice.discount,
          isDiscount: true
        });
      }

      lineItems.push({
        title: `Applicable Hotel GST / Service Tax (${taxRate}%)`,
        subtitle: 'Government statutory tax',
        category: 'Statutory Tax',
        amount: invoice.taxAmount
      });

      let currentItemY = tableY + tableHeaderHeight + 14;

      lineItems.forEach((item, index) => {
        // Subtle row background alternate
        if (index % 2 === 1) {
          ctx.fillStyle = 'rgba(255, 255, 255, 0.02)';
          roundRect(ctx, 52, currentItemY - 8, tableWidth - 4, 48, 6);
          ctx.fill();
        }

        ctx.textAlign = 'left';
        ctx.fillStyle = item.isDiscount ? '#34D399' : '#FFFFFF';
        ctx.font = '600 13px sans-serif';
        ctx.fillText(item.title, 68, currentItemY + 12);

        ctx.fillStyle = '#64748B';
        ctx.font = '10px sans-serif';
        ctx.fillText(item.subtitle, 68, currentItemY + 28);

        ctx.fillStyle = '#94A3B8';
        ctx.font = '11px sans-serif';
        ctx.fillText(item.category, 420, currentItemY + 18);

        ctx.textAlign = 'right';
        ctx.fillStyle = item.isDiscount ? '#34D399' : '#F8FAFC';
        ctx.font = 'bold 13px sans-serif';
        const displayAmount = (item.isDiscount ? '-' : '') + formatRupees(item.amount);
        ctx.fillText(displayAmount, baseWidth - 70, currentItemY + 18);

        // Divider
        ctx.strokeStyle = '#1E293B';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(68, currentItemY + 38);
        ctx.lineTo(baseWidth - 68, currentItemY + 38);
        ctx.stroke();

        currentItemY += 52;
      });

      // --- 6. Grand Totals & Billing Breakdown Box ---
      const summaryBoxY = currentItemY + 24;
      const summaryBoxHeight = 175;

      ctx.fillStyle = 'rgba(15, 23, 42, 0.9)';
      ctx.strokeStyle = '#334155';
      ctx.lineWidth = 1.2;
      roundRect(ctx, 50, summaryBoxY, tableWidth, summaryBoxHeight, 14);
      ctx.fill();
      ctx.stroke();

      // Left column: Payment summary & Method
      const sumLeftX = 74;
      ctx.textAlign = 'left';
      ctx.fillStyle = '#94A3B8';
      ctx.font = '11px sans-serif';
      ctx.fillText('PAYMENT METHOD', sumLeftX, summaryBoxY + 30);

      ctx.fillStyle = '#FFFFFF';
      ctx.font = 'bold 13px sans-serif';
      ctx.fillText('Digital Express / UPI / Card', sumLeftX, summaryBoxY + 50);

      ctx.fillStyle = '#94A3B8';
      ctx.font = '11px sans-serif';
      ctx.fillText('SETTLEMENT TIME', sumLeftX, summaryBoxY + 84);

      ctx.fillStyle = '#CBD5E1';
      ctx.font = '12px sans-serif';
      ctx.fillText(new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }), sumLeftX, summaryBoxY + 104);

      // Authenticity Seal
      ctx.strokeStyle = '#D97706';
      ctx.lineWidth = 1;
      roundRect(ctx, sumLeftX, summaryBoxY + 120, 260, 32, 6);
      ctx.stroke();
      ctx.fillStyle = '#F59E0B';
      ctx.font = 'bold 10px sans-serif';
      ctx.fillText('★ OFFICIAL KR LUXE DIGITAL FOLIO SEAL ★', sumLeftX + 16, summaryBoxY + 140);

      // Right column: Financial Totals
      const sumRightLabelX = baseWidth - 280;
      const sumRightValueX = baseWidth - 74;

      // Subtotal
      ctx.textAlign = 'left';
      ctx.fillStyle = '#94A3B8';
      ctx.font = '12px sans-serif';
      ctx.fillText('Subtotal (Net):', sumRightLabelX, summaryBoxY + 30);
      ctx.textAlign = 'right';
      ctx.fillStyle = '#E2E8F0';
      ctx.font = '600 12px sans-serif';
      ctx.fillText(formatRupees(invoice.subtotal), sumRightValueX, summaryBoxY + 30);

      // Tax
      ctx.textAlign = 'left';
      ctx.fillStyle = '#94A3B8';
      ctx.fillText(`Statutory GST (${taxRate}%):`, sumRightLabelX, summaryBoxY + 54);
      ctx.textAlign = 'right';
      ctx.fillStyle = '#E2E8F0';
      ctx.fillText(formatRupees(invoice.taxAmount), sumRightValueX, summaryBoxY + 54);

      // Grand Total Highlight
      const grandTotalY = summaryBoxY + 84;
      ctx.fillStyle = 'rgba(245, 158, 11, 0.12)';
      roundRect(ctx, sumRightLabelX - 10, grandTotalY - 14, 230, 34, 6);
      ctx.fill();

      ctx.textAlign = 'left';
      ctx.fillStyle = '#FCD34D';
      ctx.font = 'bold 13px sans-serif';
      ctx.fillText('GRAND TOTAL:', sumRightLabelX, grandTotalY + 8);
      ctx.textAlign = 'right';
      ctx.fillStyle = '#F59E0B';
      ctx.font = 'bold 16px sans-serif';
      ctx.fillText(formatRupees(invoice.grandTotal), sumRightValueX, grandTotalY + 8);

      // Paid & Balance
      ctx.textAlign = 'left';
      ctx.fillStyle = '#34D399';
      ctx.font = '600 12px sans-serif';
      ctx.fillText('Total Amount Paid:', sumRightLabelX, summaryBoxY + 132);
      ctx.textAlign = 'right';
      ctx.fillText(formatRupees(invoice.paidAmount), sumRightValueX, summaryBoxY + 132);

      ctx.textAlign = 'left';
      ctx.fillStyle = invoice.balanceAmount > 0 ? '#F87171' : '#94A3B8';
      ctx.font = 'bold 12px sans-serif';
      ctx.fillText('Outstanding Balance:', sumRightLabelX, summaryBoxY + 154);
      ctx.textAlign = 'right';
      ctx.fillText(formatRupees(invoice.balanceAmount), sumRightValueX, summaryBoxY + 154);

      // --- 7. Footer: Hotel Policy, Thank You & Sign-off ---
      const footerY = baseHeight - 110;

      ctx.textAlign = 'center';
      ctx.fillStyle = '#F59E0B';
      ctx.font = 'bold 13px "Georgia", serif';
      ctx.fillText('Thank you for choosing KR LUXE HOTEL & RESORTS', baseWidth / 2, footerY + 16);

      ctx.fillStyle = '#64748B';
      ctx.font = '10px sans-serif';
      ctx.fillText('This is an official authenticated computer-generated hotel receipt and requires no physical signature.', baseWidth / 2, footerY + 36);
      ctx.fillText(`Customer Support: ${hotelEmail} • 24/7 Front Office Desk: ${hotelPhone}`, baseWidth / 2, footerY + 52);

      // Convert Canvas to Image and Trigger Download
      const fileFormat = options?.format === 'jpeg' ? 'image/jpeg' : 'image/png';
      const fileExtension = options?.format === 'jpeg' ? 'jpg' : 'png';
      const fileName = `KR_LUXE_Folio_${invoice.invoiceNumber}.${fileExtension}`;

      canvas.toBlob((blob) => {
        if (!blob) {
          reject(new Error('Failed to generate image blob from canvas'));
          return;
        }

        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        setTimeout(() => {
          URL.revokeObjectURL(url);
        }, 1500);

        resolve(fileName);
      }, fileFormat, 0.95);

    } catch (err) {
      reject(err);
    }
  });
}
