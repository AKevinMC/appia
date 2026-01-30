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
                printSetup.setLandscape(false); // Vertical
                sheet.setFitToPage(true);
                printSetup.setFitWidth((short) 1);
                printSetup.setFitHeight((short) 0);

                // MÁRGENES
                sheet.setMargin(Sheet.TopMargin, 0.6);    // ~1.5 cm (NUEVO)
                sheet.setMargin(Sheet.BottomMargin, 0.6); // ~1.5 cm
                sheet.setMargin(Sheet.LeftMargin, 0.8);   // ~2.0 cm
                sheet.setMargin(Sheet.RightMargin, 0.4);  // ~1.0 cm
            } catch (Throwable e) {
                Log.w("Excel", "Advertencia: Configuración de página omitida");
            }

            // --- ESTILOS ---

            // 1. Estilo CITE (Negrita y Subrayado)
            CellStyle estiloCite = workbook.createCellStyle();
            Font fontCite = workbook.createFont();
            fontCite.setBold(true);
            fontCite.setUnderline(Font.U_SINGLE);
            fontCite.setFontHeightInPoints((short) 10);
            estiloCite.setFont(fontCite);
            estiloCite.setAlignment(HorizontalAlignment.LEFT);

            // 2. Estilo Texto Normal
            CellStyle estiloTexto = workbook.createCellStyle();
            Font fontTexto = workbook.createFont();
            fontTexto.setFontHeightInPoints((short) 10);
            fontTexto.setBold(false);
            estiloTexto.setFont(fontTexto);
            estiloTexto.setWrapText(true);
            estiloTexto.setAlignment(HorizontalAlignment.LEFT);
            estiloTexto.setVerticalAlignment(VerticalAlignment.TOP);

            // 3. Estilo Título Negrita (Para totales)
            CellStyle estiloNegrita = workbook.createCellStyle();
            Font fontNegrita = workbook.createFont();
            fontNegrita.setFontHeightInPoints((short) 10);
            fontNegrita.setBold(true);
            estiloNegrita.setFont(fontNegrita);
            estiloNegrita.setWrapText(true);
            estiloNegrita.setAlignment(HorizontalAlignment.LEFT);

            // 4. Estilo Firmas
            CellStyle estiloFirma = workbook.createCellStyle();
            estiloFirma.setFont(fontTexto);
            estiloFirma.setAlignment(HorizontalAlignment.CENTER);

            // 5. Estilo Header Tabla
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

            // 6. Estilo Celda Tabla
            CellStyle estiloCelda = workbook.createCellStyle();
            estiloCelda.setBorderBottom(BorderStyle.THIN);
            estiloCelda.setBorderLeft(BorderStyle.THIN);
            estiloCelda.setBorderRight(BorderStyle.THIN);
            estiloCelda.setAlignment(HorizontalAlignment.CENTER);
            Font fontCelda = workbook.createFont();
            fontCelda.setFontHeightInPoints((short) 8);
            estiloCelda.setFont(fontCelda);

            // --- 1. ENCABEZADO Y CUERPO (PARTE DE ARRIBA) ---

            // Fila 0: CITE
            String anioActual = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
            createCell(sheet, 0, 10, "CITE: CAM/01/" + anioActual, estiloCite);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 15));

            // Fila 1: Señor
            createCell(sheet, 1, 0, "Señor:", estiloTexto);

            // Fecha
            String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());
            createCell(sheet, 1, 10, "Mojocoya, " + fechaHoy, estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 10, 16));

            // Fila 2: NOMBRE ALCALDE
            createCell(sheet, 2, 0, nombreAlcalde, estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 8));

            // Fila 3: CARGO
            createCell(sheet, 3, 0, "H. ALCALDE MUNICIPAL", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 8));

            // Fila 4: ENTIDAD
            createCell(sheet, 4, 0, "GOBIERNO AUTONOMO MUNICIPAL DE VILLA MOJOCOYA", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 12));

            // Fila 5: Presente
            createCell(sheet, 5, 0, "Presente.-", estiloTexto);

            // Fila 7: Saludo
            createCell(sheet, 7, 0, "De mi mayor consideración,", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 6));

            // Fila 8: CUERPO
            String cuerpoCarta = "Reciba un atento y cordial saludo, el motivo de la presente es solicitar a su autoridad autorizar por el medio que corresponda la cancelación por el consumo de agua potable de las diferentes instituciones que se encuentran en el centro poblado de Mojocoya (Gestión " + anio + "), a efectos de cumplir con la normativa impositiva solicitamos constituirse en agente de retención.";

            Row rowCuerpo = sheet.createRow(8);
            Cell cellCuerpo = rowCuerpo.createCell(0);
            cellCuerpo.setCellValue(cuerpoCarta);
            cellCuerpo.setCellStyle(estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(8, 11, 0, 17));

            // --- 2. TABLAS ---
            int rowNum = 12; // Empieza justo después del cuerpo

            String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

            double granTotalImpuesto = 0;     // Con Impuestos
            double granTotalSinImpuesto = 0;  // Sin Impuestos

            for (int trimestre = 0; trimestre < 4; trimestre++) {

                Row rowMeses = sheet.createRow(rowNum++);
                Row rowSubHeaders = sheet.createRow(rowNum++);

                // Header Institución
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

                // Header Totales
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

                    if(inst.getNombre() != null && inst.getNombre().length() > maxLongitudNombre) {
                        maxLongitudNombre = inst.getNombre().length();
                    }

                    colIndex = 1;
                    double totalImpFila = 0;
                    double totalImpMasIFila = 0;

                    for (int m = startMonth + 1; m <= startMonth + 3; m++) {
                        Lectura lec = inst.getLectura(m);
                        if (lec != null) {
                            createCell(row, colIndex++, String.valueOf((int)lec.getLecturaAnterior()), estiloCelda);
                            createCell(row, colIndex++, String.valueOf((int)lec.getLecturaActual()), estiloCelda);

                            double consumo = lec.getLecturaActual() - lec.getLecturaAnterior();
                            if(consumo < 0) consumo = 0;
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

                // ESPACIO ENTRE TABLAS
                rowNum++;
            }

            // --- 3. PIE DE PÁGINA (RESUMEN Y TEXTO EXTRA) ---

            // NOTA: No agregamos rowNum++ aquí porque el bucle ya dejó una línea libre.
            // Así tenemos 1 sola fila de separación exacta.

            // Total Con Impuestos
            Row rowTotConImp = sheet.createRow(rowNum++);
            createCell(rowTotConImp, 0, "TOTAL GENERAL ENERO A DICIEMBRE DE " + anio + " (Incluye Impuestos)", estiloNegrita);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 14));
            createCell(rowTotConImp, 15, String.format(Locale.US, "%.2f", granTotalImpuesto), estiloNegrita);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 15, 17));

            // Total Sin Impuestos
            Row rowTotSinImp = sheet.createRow(rowNum++);
            createCell(rowTotSinImp, 0, "TOTAL GENERAL ENERO A DICIEMBRE DE " + anio + " (No incluye Impuestos)", estiloNegrita);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 14));
            createCell(rowTotSinImp, 15, String.valueOf((int)granTotalSinImpuesto), estiloNegrita);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 15, 17));

            // Explicación Impuestos
            Row rowExplicacion = sheet.createRow(rowNum++);
            createCell(rowExplicacion, 0, "El Cálculo de impuestos aplica a la compra de un bien (5% IUE y 3% IT)", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // Cheque
            Row rowCheque = sheet.createRow(rowNum++);
            createCell(rowCheque, 0, "Solicitamos por favor que el cheque sea emitido a nombre de " + tesoNombre + ", Tesorera del Comité de Agua", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // Tarifa Header
            Row rowTarifaHead = sheet.createRow(rowNum++);
            createCell(rowTarifaHead, 0, "La Tarifa aplicada para el cálculo es el siguiente:", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // Tarifa 1
            Row rowTarifa1 = sheet.createRow(rowNum++);
            createCell(rowTarifa1, 0, "1.- Consumo mínimo Bs. 10 por 6 m3", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // Tarifa 2
            Row rowTarifa2 = sheet.createRow(rowNum++);
            createCell(rowTarifa2, 0, "2.- Excedente Bs. 4 por m3", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // Despedida
            rowNum++;
            Row rowDespedida = sheet.createRow(rowNum++);
            createCell(rowDespedida, 0, "Agradeciendo su gentil atención le reiteramos nuestros saludos cordiales", estiloTexto);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 17));

            // --- 4. FIRMAS ---
            rowNum += 4; // CAMBIO: Subimos las firmas una fila (4 espacios en vez de 5)

            Row rowNombres = sheet.createRow(rowNum++);
            // Presidente
            createCell(rowNombres, 2, presiNombre, estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 2, 6));

            // Tesorero
            createCell(rowNombres, 11, tesoNombre, estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 11, 15));

            Row rowCargos = sheet.createRow(rowNum++);
            createCell(rowCargos, 2, "PRESIDENTE COMITÉ DE AGUA DE MOJOCOYA", estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 2, 6));

            createCell(rowCargos, 11, "TESORERA COMITÉ DE AGUA DE MOJOCOYA", estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 11, 15));

            Row rowCis = sheet.createRow(rowNum++);
            createCell(rowCis, 2, "C.I. " + presiCI, estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 2, 6));

            createCell(rowCis, 11, "C.I. " + tesoCI, estiloFirma);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 11, 15));

            // --- 5. AJUSTE DE ANCHOS ---
            int anchoCalculadoNombre = (int)((maxLongitudNombre + 1) * 256);
            if (anchoCalculadoNombre > 7000) anchoCalculadoNombre = 7000;
            sheet.setColumnWidth(0, anchoCalculadoNombre);

            for (int i = 1; i <= 15; i++) {
                int mod = (i - 1) % 5;
                if (mod == 2) {
                    sheet.setColumnWidth(i, 900); // m3
                } else if (mod == 3) {
                    sheet.setColumnWidth(i, 1100); // Imp
                } else if (mod == 4) {
                    sheet.setColumnWidth(i, 1500); // Imp+i
                } else {
                    sheet.setColumnWidth(i, 1250); // Ant/Act
                }
            }
            sheet.setColumnWidth(16, 1250);
            sheet.setColumnWidth(17, 1500);

            // --- 6. GUARDAR ---
            String fileName = "Carta_Institucional_" + anio + ".xlsx";
            return guardarArchivo(context, workbook, fileName);

        } catch (Throwable e) {
            Log.e("Excel", "Error FATAL al generar el Excel", e);
            e.printStackTrace();
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