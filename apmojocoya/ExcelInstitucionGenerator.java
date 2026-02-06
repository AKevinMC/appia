package com.example.apmojocoya;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

    public static String generarReporte(Context context, int anio, List<InstitucionRow> filas,
                                        String nombreAlcalde, String presiNombre, String presiCI,
                                        String tesoNombre, String tesoCI) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Carta Cobro " + anio);

            int maxLongitudNombre = 11;

            // --- 0. CONFIGURACIÓN DE PÁGINA ---
            try {
                PrintSetup printSetup = sheet.getPrintSetup();
                printSetup.setPaperSize(PrintSetup.LEGAL_PAPERSIZE);
                printSetup.setLandscape(false);
                sheet.setFitToPage(true);
                printSetup.setFitWidth((short) 1);
                printSetup.setFitHeight((short) 0);

                sheet.setMargin(Sheet.TopMargin, 0.6);
                sheet.setMargin(Sheet.BottomMargin, 0.6);
                sheet.setMargin(Sheet.LeftMargin, 0.8);
                sheet.setMargin(Sheet.RightMargin, 0.4);
            } catch (Throwable e) {
                Log.w("Excel", "Advertencia: Configuración de página omitida");
            }

            // --- ESTILOS ---
            CellStyle estiloCite = workbook.createCellStyle();
            Font fontCite = workbook.createFont();
            fontCite.setBold(true);
            fontCite.setUnderline(Font.U_SINGLE);
            fontCite.setFontHeightInPoints((short) 10);
            estiloCite.setFont(fontCite);
            estiloCite.setAlignment(HorizontalAlignment.LEFT);

            CellStyle estiloTexto = workbook.createCellStyle();
            Font fontTexto = workbook.createFont();
            fontTexto.setFontHeightInPoints((short) 10);
            fontTexto.setBold(false);
            estiloTexto.setFont(fontTexto);
            estiloTexto.setWrapText(true);
            estiloTexto.setAlignment(HorizontalAlignment.LEFT);
            estiloTexto.setVerticalAlignment(VerticalAlignment.TOP);

            CellStyle estiloNegrita = workbook.createCellStyle();
            Font fontNegrita = workbook.createFont();
            fontNegrita.setFontHeightInPoints((short) 10);
            fontNegrita.setBold(true);
            estiloNegrita.setFont(fontNegrita);
            estiloNegrita.setWrapText(true);
            estiloNegrita.setAlignment(HorizontalAlignment.LEFT);

            CellStyle estiloFirma = workbook.createCellStyle();
            estiloFirma.setFont(fontTexto);
            estiloFirma.setAlignment(HorizontalAlignment.CENTER);

            CellStyle estiloHeaderTabla = workbook.createCellStyle();
            Font fontHeader = workbook.createFont();
            fontHeader.setBold(true);
            fontHeader.setFontHeightInPoints((short) 8);
            estiloHeaderTabla.setFont(fontHeader);
            estiloHeaderTabla.setAlignment(HorizontalAlignment.CENTER);
            estiloHeaderTabla.setVerticalAlignment(VerticalAlignment.CENTER);
            estiloHeaderTabla.setBorderBottom(BorderStyle.THIN);
            estiloHeaderTabla.setBorderTop(BorderStyle.THIN);
            estiloHeaderTabla.setBorderLeft(BorderStyle.THIN);
            estiloHeaderTabla.setBorderRight(BorderStyle.THIN);
            estiloHeaderTabla.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estiloHeaderTabla.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle estiloCelda = workbook.createCellStyle();
            estiloCelda.setBorderBottom(BorderStyle.THIN);
            estiloCelda.setBorderLeft(BorderStyle.THIN);
            estiloCelda.setBorderRight(BorderStyle.THIN);
            estiloCelda.setAlignment(HorizontalAlignment.CENTER);
            Font fontCelda = workbook.createFont();
            fontCelda.setFontHeightInPoints((short) 8);
            estiloCelda.setFont(fontCelda);

            // --- 1. ENCABEZADO ---
            String anioActual = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
            createCell(sheet, 0, 10, "CITE: CAM/01/" + anioActual, estiloCite);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 15));
            createCell(sheet, 1, 0, "Señor:", estiloTexto);
            String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());
            createCell(sheet, 1, 10, "Mojocoya, " + fechaHoy, estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 10, 16));
            createCell(sheet, 2, 0, nombreAlcalde, estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 8));
            createCell(sheet, 3, 0, "H. ALCALDE MUNICIPAL", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 8));
            createCell(sheet, 4, 0, "GOBIERNO AUTONOMO MUNICIPAL DE VILLA MOJOCOYA", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 12));
            createCell(sheet, 5, 0, "Presente.-", estiloTexto);
            createCell(sheet, 7, 0, "De mi mayor consideración,", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 6));

            String cuerpoCarta = "Reciba un atento y cordial saludo, el motivo de la presente es solicitar a su autoridad autorizar por el medio que corresponda la cancelación por el consumo de agua potable de las diferentes instituciones que se encuentran en el centro poblado de Mojocoya (Gestión " + anio + "), a efectos de cumplir con la normativa impositiva solicitamos constituirse en agente de retención.";
            Row rowCuerpo = sheet.createRow(8);
            Cell cellCuerpo = rowCuerpo.createCell(0);
            cellCuerpo.setCellValue(cuerpoCarta);
            cellCuerpo.setCellStyle(estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(8, 10, 0, 17));

            // --- 2. TABLAS ---
            int rowNum = 11;
            String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            double granTotalImpuesto = 0;
            double granTotalSinImpuesto = 0;

            for (int trimestre = 0; trimestre < 4; trimestre++) {
                Row rowMeses = sheet.createRow(rowNum++);
                Row rowSubHeaders = sheet.createRow(rowNum++);
                createHeaderCell(rowMeses, 0, "INSTITUCIÓN", estiloHeaderTabla);
                CellRangeAddress regionInst = new CellRangeAddress(rowNum - 2, rowNum - 1, 0, 0);
                sheet.addMergedRegion(regionInst);
                dibujarBordes(regionInst, sheet);
                int startMonth = trimestre * 3;
                int colIndex = 1;

                for (int m = startMonth; m < startMonth + 3; m++) {
                    createHeaderCell(rowMeses, colIndex, meses[m], estiloHeaderTabla);
                    CellRangeAddress regionMes = new CellRangeAddress(rowNum - 2, rowNum - 2, colIndex, colIndex + 4);
                    sheet.addMergedRegion(regionMes);
                    dibujarBordes(regionMes, sheet);
                    createHeaderCell(rowSubHeaders, colIndex++, "Ant", estiloHeaderTabla);
                    createHeaderCell(rowSubHeaders, colIndex++, "Act", estiloHeaderTabla);
                    createHeaderCell(rowSubHeaders, colIndex++, "m3", estiloHeaderTabla);
                    createHeaderCell(rowSubHeaders, colIndex++, "Imp.", estiloHeaderTabla);
                    createHeaderCell(rowSubHeaders, colIndex++, "Imp.+i", estiloHeaderTabla);
                }

                createHeaderCell(rowMeses, colIndex, "TOTALES", estiloHeaderTabla);
                CellRangeAddress regionTotalHead = new CellRangeAddress(rowNum - 2, rowNum - 2, colIndex, colIndex + 1);
                sheet.addMergedRegion(regionTotalHead);
                dibujarBordes(regionTotalHead, sheet);
                createHeaderCell(rowSubHeaders, colIndex++, "Imp.", estiloHeaderTabla);
                createHeaderCell(rowSubHeaders, colIndex++, "Total+i", estiloHeaderTabla);

                double sumaTotalImpMasITrimestre = 0;
                double sumaTotalImpTrimestre = 0;

                for (InstitucionRow inst : filas) {
                    Row row = sheet.createRow(rowNum++);
                    createCell(row, 0, inst.getNombre(), estiloCelda);
                    if(inst.getNombre() != null && inst.getNombre().length() > maxLongitudNombre) maxLongitudNombre = inst.getNombre().length();

                    colIndex = 1;
                    double totalImpFila = 0;
                    double totalImpMasIFila = 0;

                    for (int m = startMonth + 1; m <= startMonth + 3; m++) {
                        Lectura lec = inst.getLectura(m);
                        if (lec != null) {
                            createCell(row, colIndex++, String.valueOf((int)lec.getLecturaAnterior()), estiloCelda);
                            createCell(row, colIndex++, String.valueOf((int)lec.getLecturaActual()), estiloCelda);
                            double consumo = Math.max(0, lec.getLecturaActual() - lec.getLecturaAnterior());
                            createCell(row, colIndex++, String.valueOf((int)consumo), estiloCelda);
                            double monto = lec.getMontoTotal();
                            double montoConImpuesto = monto + (monto * 0.08);
                            createCell(row, colIndex++, String.valueOf((int)monto), estiloCelda);
                            createCell(row, colIndex++, String.format(Locale.US, "%.2f", montoConImpuesto), estiloCelda);
                            totalImpFila += monto;
                            totalImpMasIFila += montoConImpuesto;
                        } else {
                            createCell(row, colIndex++, "-", estiloCelda);
                            createCell(row, colIndex++, "-", estiloCelda);
                            createCell(row, colIndex++, "-", estiloCelda);
                            createCell(row, colIndex++, "0", estiloCelda);
                            createCell(row, colIndex++, "0.00", estiloCelda);
                        }
                    }
                    createCell(row, colIndex++, String.valueOf((int)totalImpFila), estiloHeaderTabla);
                    createCell(row, colIndex++, String.format(Locale.US, "%.2f", totalImpMasIFila), estiloHeaderTabla);
                    sumaTotalImpTrimestre += totalImpFila;
                    sumaTotalImpMasITrimestre += totalImpMasIFila;
                }
                Row rowTotalTrim = sheet.createRow(rowNum++);
                createCell(rowTotalTrim, 0, "TOTAL TRIMESTRE", estiloHeaderTabla);
                CellRangeAddress regionTotTrim = new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 15);
                sheet.addMergedRegion(regionTotTrim);
                dibujarBordes(regionTotTrim, sheet);
                createCell(rowTotalTrim, 16, String.valueOf((int)sumaTotalImpTrimestre), estiloHeaderTabla);
                createCell(rowTotalTrim, 17, String.format(Locale.US, "%.2f", sumaTotalImpMasITrimestre), estiloHeaderTabla);
                granTotalImpuesto += sumaTotalImpMasITrimestre;
                granTotalSinImpuesto += sumaTotalImpTrimestre;
                rowNum++;
            }

            // --- 3. PIE DE PÁGINA (RESUMEN Y TEXTO EXTRA - RESTAURADO EXACTAMENTE) ---
            Row rowTotConImp = sheet.createRow(rowNum++);
            createCell(rowTotConImp, 0, "TOTAL GENERAL ENERO A DICIEMBRE DE " + anio + " (Incluye Impuestos)", estiloNegrita);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 14));
            createCell(rowTotConImp, 15, String.format(Locale.US, "%.2f", granTotalImpuesto), estiloNegrita);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 15, 17));

            Row rowTotSinImp = sheet.createRow(rowNum++);
            createCell(rowTotSinImp, 0, "TOTAL GENERAL ENERO A DICIEMBRE DE " + anio + " (No incluye Impuestos)", estiloNegrita);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 14));
            createCell(rowTotSinImp, 15, String.valueOf((int)granTotalSinImpuesto), estiloNegrita);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 15, 17));

            // RESTAURADO: Explicación Impuestos con variables originales
            Row rowExplicacion = sheet.createRow(rowNum++);
            createCell(rowExplicacion, 0, "El Cálculo de impuestos aplica a la compra de un bien (5% IUE y 3% IT)", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // RESTAURADO: Cheque con variables originales
            Row rowCheque = sheet.createRow(rowNum++);
            createCell(rowCheque, 0, "Solicitamos por favor que el cheque sea emitido a nombre de " + tesoNombre + ", Tesorera del Comité de Agua", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // RESTAURADO: Tarifa Header con variables originales
            Row rowTarifaHead = sheet.createRow(rowNum++);
            createCell(rowTarifaHead, 0, "La Tarifa aplicada para el cálculo es el siguiente:", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // RESTAURADO: Tarifa 1 con variables originales
            Row rowTarifa1 = sheet.createRow(rowNum++);
            createCell(rowTarifa1, 0, "1.- Consumo mínimo Bs. 10 por 6 m3", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // RESTAURADO: Tarifa 2 con variables originales
            Row rowTarifa2 = sheet.createRow(rowNum++);
            createCell(rowTarifa2, 0, "2.- Excedente Bs. 4 por m3", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            rowNum++;
            Row rowDespedida = sheet.createRow(rowNum++);
            createCell(rowDespedida, 0, "Agradeciendo su gentil atención le reiteramos nuestros saludos cordiales", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // --- 4. FIRMAS ---
            rowNum += 4;
            Row rowNombres = sheet.createRow(rowNum++);
            createCell(rowNombres, 1, presiNombre, estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 1, 7));
            createCell(rowNombres, 10, tesoNombre, estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 10, 16));

            Row rowCargos = sheet.createRow(rowNum++);
            createCell(rowCargos, 1, "PRESIDENTE COMITÉ DE AGUA DE MOJOCOYA", estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 1, 7));
            createCell(rowCargos, 10, "TESORERO/A COMITÉ DE AGUA DE MOJOCOYA", estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 10, 16));

            Row rowCis = sheet.createRow(rowNum++);
            createCell(rowCis, 2, "C.I. " + presiCI, estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 2, 6));
            createCell(rowCis, 11, "C.I. " + tesoCI, estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 11, 15));

            // --- 5. AJUSTE DE ANCHOS ---
            sheet.setColumnWidth(0, (int)Math.min(7000, (maxLongitudNombre + 1) * 256));
            for (int i = 1; i <= 15; i++) {
                int mod = (i - 1) % 5;
                if (mod == 2) sheet.setColumnWidth(i, 900);
                else if (mod == 3) sheet.setColumnWidth(i, 1100);
                else if (mod == 4) sheet.setColumnWidth(i, 1500);
                else sheet.setColumnWidth(i, 1250);
            }
            sheet.setColumnWidth(16, 1250);
            sheet.setColumnWidth(17, 1500);

            return guardarArchivo(context, workbook, "Carta_Institucional_" + anio + ".xlsx");

        } catch (Throwable e) {
            Log.e("Excel", "Error FATAL", e);
            return null;
        }
    }

    private static void dibujarBordes(CellRangeAddress region, Sheet sheet) {
        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
    }

    private static String guardarArchivo(Context context, Workbook workbook, String fileName) {
        try {
            // Guardar en cache/interno para Drive
            File internalFile = new File(context.getCacheDir(), fileName);
            FileOutputStream fosInt = new FileOutputStream(internalFile);
            workbook.write(fosInt);
            fosInt.close();

            // Guardar en Documentos (Público)
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
                }
            } else {
                File docFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File miCarpeta = new File(docFolder, CARPETA_DESTINO);
                if (!miCarpeta.exists()) miCarpeta.mkdirs();
                File file = new File(miCarpeta, fileName);
                FileOutputStream fosPub = new FileOutputStream(file);
                workbook.write(fosPub);
                fosPub.close();
            }
            workbook.close();
            return internalFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e("Excel", "Error al guardar", e);
            return null;
        }
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