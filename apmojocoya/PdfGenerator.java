package com.example.apmojocoya;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    public static void generarCartaAlcaldia(Context context, int anio, List<InstitucionDeuda> listaDeudas) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // Tamaño A4
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        // Configuración de fuentes
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(14);
        titlePaint.setColor(Color.BLACK);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(11);
        paint.setColor(Color.BLACK);

        int x = 50;
        int y = 50;

        // 1. CABECERA
        canvas.drawText("AP MOJOCOYA - AGUA POTABLE", x, y, titlePaint);
        y += 20;
        canvas.drawText("CITE: CAM/01/" + anio, x, y, paint); // Formato visto en tus CSVs

        // Fecha actual automática
        String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Mojocoya, " + fechaHoy, 550, y, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        y += 40;

        // 2. DESTINATARIO (Basado en tus archivos)
        canvas.drawText("Señor:", x, y, titlePaint);
        y += 15;
        canvas.drawText("H. ALCALDE MUNICIPAL", x, y, paint);
        y += 15;
        canvas.drawText("GOBIERNO AUTÓNOMO MUNICIPAL DE VILLA MOJOCOYA", x, y, paint);
        y += 15;
        canvas.drawText("Presente.-", x, y, titlePaint);

        y += 30;
        canvas.drawText("De mi mayor consideración:", x, y, paint);
        y += 20;

        // 3. CUERPO DE LA CARTA (Texto extraído de tus archivos 2020-2024)
        String parrafo = "Reciba un atento y cordial saludo. El motivo de la presente es solicitar a su autoridad " +
                "autorizar por el medio que corresponda la cancelación por el consumo de agua potable de las " +
                "diferentes instituciones que se encuentran en el centro poblado de Mojocoya. A efectos de cumplir " +
                "con la normativa impositiva solicitamos constituirse en agente de retención.";

        // Dibujar texto multilínea básico
        y = dibujarTextoMultilinea(canvas, parrafo, x, y, 500, paint);

        y += 20;
        canvas.drawText("A continuación se detalla el consumo de la Gestión " + anio + ":", x, y, titlePaint);
        y += 20;

        // 4. TABLA DE DEUDAS
        // Cabecera Tabla
        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.LTGRAY);
        headerPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x, y, 550, y + 20, headerPaint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("INSTITUCIÓN", x + 5, y + 14, paint);
        canvas.drawText("MESES", x + 300, y + 14, paint);
        canvas.drawText("TOTAL (Bs)", x + 400, y + 14, paint);

        y += 25;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        double granTotal = 0;

        for (InstitucionDeuda item : listaDeudas) {
            canvas.drawText(item.getNombre(), x + 5, y, paint);
            canvas.drawText(String.valueOf(item.getMesesAdeudados()), x + 300, y, paint);
            canvas.drawText(String.format(Locale.US, "%.2f", item.getMontoTotal()), x + 400, y, paint);

            // Línea separadora
            canvas.drawLine(x, y + 5, 550, y + 5, paint);

            granTotal += item.getMontoTotal();
            y += 20;
        }

        y += 10;
        titlePaint.setTextSize(12);
        canvas.drawText("TOTAL A PAGAR:  " + String.format(Locale.US, "%.2f Bs", granTotal), x + 300, y, titlePaint);

        y += 60;

        // 5. DESPEDIDA Y FIRMAS
        dibujarTextoMultilinea(canvas, "Sin otro particular, me despido deseándole éxitos en sus funciones.", x, y, 500, paint);

        y += 80;
        // Línea de firma
        canvas.drawLine(200, y, 400, y, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("RESPONSABLE AP-MOJOCOYA", 300, y + 15, paint);

        document.finishPage(page);

        // 6. GUARDAR ARCHIVO
        String fileName = "Carta_Alcaldia_" + anio + ".pdf";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);

        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "Carta guardada en Documentos: " + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        document.close();
    }

    // Método auxiliar simple para ajustar texto
    private static int dibujarTextoMultilinea(Canvas canvas, String text, int x, int y, int width, Paint paint) {
        String[] words = text.split(" ");
        String line = "";
        for (String word : words) {
            if (paint.measureText(line + word) < width) {
                line += word + " ";
            } else {
                canvas.drawText(line, x, y, paint);
                y += 15; // Altura de línea
                line = word + " ";
            }
        }
        canvas.drawText(line, x, y, paint);
        return y + 15;
    }
}