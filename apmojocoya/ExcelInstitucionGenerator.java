package com.example.apmojocoya;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // USAMOS XSSF (Igual que tu otro generador)

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExcelInstitucionGenerator {

    private static final String CARPETA_DESTINO = "AP_Mojocoya_Cartas";

    public static String generarReporte(Context context, int anio, List<InstitucionRow> filas) {
        // Usamos XSSFWorkbook (.xlsx) que es el que soporta tu librería actual
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Carta Cobro " + anio);

        // --- ESTILOS ---
        CellStyle estiloTitulo = workbook.createCellStyle();
        Font fontTitulo = workbook.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 14);
        estiloTitulo.setFont(fontTitulo);
        estiloTitulo.setAlignment(HorizontalAlignment.LEFT);

        CellStyle estiloTexto = workbook.createCellStyle();
        estiloTexto.setWrapText(true);
        estiloTexto.setAlignment(HorizontalAlignment.LEFT);
        estiloTexto.setVerticalAlignment(VerticalAlignment.TOP);

        CellStyle estiloHeaderTabla = workbook.createCellStyle();
        Font fontHeader = workbook.createFont();
        fontHeader.setBold(true);
        estiloHeaderTabla.setFont(fontHeader);
        estiloHeaderTabla.setAlignment(HorizontalAlignment.CENTER);
        estiloHeaderTabla.setBorderBottom(BorderStyle.THIN);
        estiloHeaderTabla.setBorderTop(BorderStyle.THIN);
        estiloHeaderTabla.setBorderLeft(BorderStyle.THIN);
        estiloHeaderTabla.setBorderRight(BorderStyle.THIN);

        CellStyle estiloCelda = workbook.createCellStyle();
        estiloCelda.setBorderBottom(BorderStyle.THIN);
        estiloCelda.setBorderLeft(BorderStyle.THIN);
        estiloCelda.setBorderRight(BorderStyle.THIN);
        estiloCelda.setAlignment(HorizontalAlignment.CENTER);

        // --- 1. MEMBRETE Y CARTA ---
        createCell(sheet, 0, 0, "AP MOJOCOYA - AGUA POTABLE", estiloTitulo);
        createCell(sheet, 1, 0, "CITE: CAM/01/" + anio, estiloTexto);

        String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());
        createCell(sheet, 1, 8, "Mojocoya, " + fechaHoy, estiloTexto);

        createCell(sheet, 3, 0, "Señor:", estiloTitulo);
        createCell(sheet, 4, 0, "H. ALCALDE MUNICIPAL", estiloTexto);
        createCell(sheet, 5, 0, "GOBIERNO AUTÓNOMO MUNICIPAL DE VILLA MOJOCOYA", estiloTexto);
        createCell(sheet, 6, 0, "Presente.-", estiloTitulo);

        createCell(sheet, 8, 0, "De mi mayor consideración:", estiloTexto);

        String cuerpoCarta = "Reciba un atento y cordial saludo. El motivo de la presente es solicitar a su autoridad " +
                "autorizar por el medio que corresponda la cancelación por el consumo de agua potable de las " +
                "diferentes instituciones correspondiente a la GESTIÓN " + anio + ". A efectos de cumplir " +
                "con la normativa impositiva solicitamos constituirse en agente de retención.";

        Row rowCuerpo = sheet.createRow(10);
        Cell cellCuerpo = rowCuerpo.createCell(0);
        cellCuerpo.setCellValue(cuerpoCarta);
        cellCuerpo.setCellStyle(estiloTexto);
        sheet.addMergedRegion(new CellRangeAddress(10, 10, 0, 20));
        rowCuerpo.setHeightInPoints(40);

        // --- 2. ENCABEZADOS DE TABLA ---
        int rowNum = 12;
        Row rowMeses = sheet.createRow(rowNum++);
        Row rowSubHeaders = sheet.createRow(rowNum++);

        createHeaderCell(rowMeses, 0, "INSTITUCIÓN", estiloHeaderTabla);
        sheet.addMergedRegion(new CellRangeAddress(12, 13, 0, 0));

        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        int colIndex = 1;

        for (String mes : meses) {
            createHeaderCell(rowMeses, colIndex, mes, estiloHeaderTabla);
            sheet.addMergedRegion(new CellRangeAddress(12, 12, colIndex, colIndex + 3));

            createHeaderCell(rowSubHeaders, colIndex++, "Ant", estiloHeaderTabla);
            createHeaderCell(rowSubHeaders, colIndex++, "Act", estiloHeaderTabla);
            createHeaderCell(rowSubHeaders, colIndex++, "Cons", estiloHeaderTabla);
            createHeaderCell(rowSubHeaders, colIndex++, "Bs", estiloHeaderTabla);
        }

        createHeaderCell(rowMeses, colIndex, "TOTAL ANUAL", estiloHeaderTabla);
        sheet.addMergedRegion(new CellRangeAddress(12, 13, colIndex, colIndex));

        // --- 3. DATOS ---
        for (InstitucionRow inst : filas) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, inst.getNombre(), estiloCelda);

            int col = 1;
            for (int m = 1; m <= 12; m++) {
                Lectura lec = inst.getLectura(m);
                if (lec != null) {
                    createCell(row, col++, String.valueOf(lec.getLecturaAnterior()), estiloCelda);
                    createCell(row, col++, String.valueOf(lec.getLecturaActual()), estiloCelda);
                    double consumo = lec.getLecturaActual() - lec.getLecturaAnterior();
                    if(consumo < 0) consumo = 0;
                    createCell(row, col++, String.valueOf(consumo), estiloCelda);
                    createCell(row, col++, String.format(Locale.US, "%.2f", lec.getMontoTotal()), estiloCelda);
                } else {
                    createCell(row, col++, "-", estiloCelda);
                    createCell(row, col++, "-", estiloCelda);
                    createCell(row, col++, "-", estiloCelda);
                    createCell(row, col++, "0.00", estiloCelda);
                }
            }
            createCell(row, col, String.format(Locale.US, "%.2f", inst.getTotalAnual()), estiloHeaderTabla);
        }

        // --- 4. FIRMAS ---
        rowNum += 4;
        Row rowFirma = sheet.createRow(rowNum);
        createCell(rowFirma, 5, "__________________________", null);
        rowNum++;
        Row rowNombreFirma = sheet.createRow(rowNum);
        createCell(rowNombreFirma, 5, "RESPONSABLE AP-MOJOCOYA", null);

        // --- 5. GUARDAR (Mismo método que ExcelReportGenerator) ---
        String fileName = "Carta_Institucional_" + anio + ".xlsx";
        return guardarArchivo(context, workbook, fileName);
    }

    // Método robusto copiado de tu ExcelReportGenerator funcional
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
            Log.e("Excel", "Error al guardar", e);
        }
        return null;
    }

    private static void createCell(Sheet sheet, int rowNum, int colNum, String value, CellStyle style) {
        Row row = sheet.getRow(rowNum);
        if (row == null) row = sheet.createRow(rowNum);
        createCell(row, colNum, value, style);
    }

    private static void createCell(Row row, int colNum, String value, CellStyle style) {
        Cell cell = row.createCell(colNum);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private static void createHeaderCell(Row row, int colNum, String value, CellStyle style) {
        createCell(row, colNum, value, style);
    }
}