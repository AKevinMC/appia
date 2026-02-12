package com.example.apmojocoya.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.apmojocoya.models.Movimiento;
import com.example.apmojocoya.models.ReporteItem;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExcelReportGenerator {

    private static final String CARPETA_DESTINO = "AP_Mojocoya_Reportes";

    public static String generarReporteMensual(Context context, List<ReporteItem> datos, int anio, int mes) {
        if (datos == null || datos.isEmpty()) return null;

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Mes " + mes);

        // --- ESTILOS ---
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        // 1. CABECERAS (Idénticas a tu Excel)
        String[] columnas = {
                "Nº",
                "NOMBRE USUARIO",
                "Lect. Anterior",
                "Lect. Actual",
                "Consumo (m³)",
                "Imp. Mínimo",
                "Imp. Exceso",
                "TOTAL A PAGAR",
                "OBS"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        // 2. LLENAR DATOS
        int rowNum = 1;
        double sumaTotal = 0;

        for (ReporteItem item : datos) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(item.getNumeroLista());
            row.createCell(1).setCellValue(item.getNombreSocio());
            row.createCell(2).setCellValue(item.getLecturaAnterior());
            row.createCell(3).setCellValue(item.getLecturaActual());
            row.createCell(4).setCellValue(item.getConsumo());

            // Columnas de Dinero
            Cell cellMin = row.createCell(5);
            cellMin.setCellValue(item.getImporteMinimo());
            cellMin.setCellStyle(currencyStyle);

            Cell cellExc = row.createCell(6);
            cellExc.setCellValue(item.getImporteExceso());
            cellExc.setCellStyle(currencyStyle);

            Cell cellTot = row.createCell(7);
            cellTot.setCellValue(item.getTotalPagar());
            cellTot.setCellStyle(currencyStyle);

            row.createCell(8).setCellValue(item.getObservacion());

            sumaTotal += item.getTotalPagar();
        }

        // 3. FILA DE TOTALES
        Row totalRow = sheet.createRow(rowNum + 1);
        Cell labelTotal = totalRow.createCell(6);
        labelTotal.setCellValue("TOTAL GRAL:");
        labelTotal.setCellStyle(headerStyle);

        Cell valueTotal = totalRow.createCell(7);
        valueTotal.setCellValue(sumaTotal);
        valueTotal.setCellStyle(headerStyle); // Negrita

        // Ajustar anchos
        sheet.setColumnWidth(1, 35 * 256); // Nombre ancho
        for(int i=2; i<=8; i++) sheet.setColumnWidth(i, 12 * 256);

        // 4. GUARDAR
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        String fileName = "Planilla_" + mes + "_" + anio + "_" + timeStamp + ".xlsx";

        return guardarArchivo(context, workbook, fileName);
    }

    private static String guardarArchivo(Context context, Workbook workbook, String fileName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/" + CARPETA_DESTINO);

                Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    OutputStream os = context.getContentResolver().openOutputStream(uri);
                    workbook.write(os);
                    os.close();
                    workbook.close();
                    return "Documentos/" + CARPETA_DESTINO + "/" + fileName;
                }
            } else {
                File docFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File miCarpeta = new File(docFolder, CARPETA_DESTINO);
                if (!miCarpeta.exists()) miCarpeta.mkdirs();
                File file = new File(miCarpeta, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                workbook.write(fos);
                fos.close();
                return file.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e("Excel", "Error", e);
        }
        return null;
    }
    // --- NUEVO METODO PARA BALANCE ---
    public static String generarBalance(Context context, int mes, int anio, double totalIngresosAgua, List<Movimiento> egresos) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Balance " + mes);

        // Estilos
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 \"Bs\""));

        // TITULO
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("RENDICIÓN DE CUENTAS - MES " + mes + "/" + anio);

        // CABECERAS
        Row headerRow = sheet.createRow(2);
        headerRow.createCell(0).setCellValue("INGRESOS (Cobro Agua)");
        headerRow.createCell(1).setCellValue("MONTO");
        headerRow.createCell(3).setCellValue("EGRESOS (Detalle)");
        headerRow.createCell(4).setCellValue("MONTO");

        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);
        headerRow.getCell(3).setCellStyle(headerStyle);
        headerRow.getCell(4).setCellStyle(headerStyle);

        // DATOS
        // Columna Izquierda: Ingresos
        Row rowIngreso = sheet.createRow(3);
        rowIngreso.createCell(0).setCellValue("Recaudación Mensual de Agua");
        Cell cellIngreso = rowIngreso.createCell(1);
        cellIngreso.setCellValue(totalIngresosAgua);
        cellIngreso.setCellStyle(currencyStyle);

        // Columna Derecha: Egresos (Lista)
        int rowNum = 3;
        double sumaEgresos = 0;

        for (Movimiento m : egresos) {
            Row row = sheet.getRow(rowNum);
            if (row == null) row = sheet.createRow(rowNum);

            row.createCell(3).setCellValue(m.getConcepto());
            Cell cellMonto = row.createCell(4);
            cellMonto.setCellValue(m.getMonto());
            cellMonto.setCellStyle(currencyStyle);

            sumaEgresos += m.getMonto();
            rowNum++;
        }

        // TOTALES
        int filaFinal = Math.max(rowNum, 5) + 2;
        Row totalRow = sheet.createRow(filaFinal);

        totalRow.createCell(0).setCellValue("TOTAL INGRESOS:");
        Cell cellTotIng = totalRow.createCell(1);
        cellTotIng.setCellValue(totalIngresosAgua);
        cellTotIng.setCellStyle(headerStyle);

        totalRow.createCell(3).setCellValue("TOTAL EGRESOS:");
        Cell cellTotEgr = totalRow.createCell(4);
        cellTotEgr.setCellValue(sumaEgresos);
        cellTotEgr.setCellStyle(headerStyle);

        // SALDO FINAL
        Row saldoRow = sheet.createRow(filaFinal + 2);
        saldoRow.createCell(0).setCellValue("SALDO EN CAJA DEL MES:");
        Cell cellSaldo = saldoRow.createCell(1);
        cellSaldo.setCellValue(totalIngresosAgua - sumaEgresos);

        // Color verde si positivo, rojo si negativo
        CellStyle saldoStyle = workbook.createCellStyle();
        saldoStyle.setFont(font);
        saldoStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 \"Bs\""));
        if ((totalIngresosAgua - sumaEgresos) >= 0) {
            // Podrías poner color verde aquí si quisieras complicarlo más
        }
        cellSaldo.setCellStyle(saldoStyle);

        // Ajustar anchos
        sheet.setColumnWidth(0, 30 * 256);
        sheet.setColumnWidth(3, 30 * 256);

        String fileName = "Balance_" + mes + "_" + anio + ".xlsx";
        return guardarArchivo(context, workbook, fileName);
    }
}