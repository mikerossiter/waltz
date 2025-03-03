/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.web.endpoints.extracts;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.eclipse.jetty.http.MimeTypes;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple2;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.FunctionUtilities.time;


public abstract class DirectQueryBasedDataExtractor implements DataExtractor {

    protected DSLContext dsl;


    public DirectQueryBasedDataExtractor(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    protected Object writeExtract(String suggestedFilenameStem,
                                  Select<?> qry,
                                  Request request,
                                  Response response) throws IOException {
        ExtractFormat format = parseExtractFormat(request);
        switch (format) {
            case XLSX:
                return writeAsExcel(suggestedFilenameStem, qry, response);
            case CSV:
                return writeAsCSV(suggestedFilenameStem, qry, response);
            default:
                throw new IllegalArgumentException("Cannot write extract using unknown format: " + format);
        }
    }


    @SafeVarargs
    public static Object writeAsMultiSheetExcel(DSLContext dsl,
                                                String suggestedFilenameStem,
                                                Response response,
                                                Tuple2<String, Select<?>>... sheetDefinitions) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(2000);

        for (Tuple2<String, Select<?>> sheetDef : sheetDefinitions) {
            time("preparing excel sheet: " + sheetDef.v1, () -> {
                SXSSFSheet sheet = workbook.createSheet(ExtractorUtilities.sanitizeSheetName(sheetDef.v1));
                writeExcelHeader(sheetDef.v2, sheet);
                time("writing body", () -> writeExcelBody(sheetDef.v2, sheet, dsl));

                int endFilterColumnIndex = sheetDef.v2.fields().length == 0
                        ? 0
                        : sheetDef.v2.fields().length - 1;

                sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, endFilterColumnIndex));
                sheet.createFreezePane(0, 1);
            });
        }

        return time("writing excel", Unchecked.supplier(() -> writeExcelToResponse(
                suggestedFilenameStem,
                response,
                workbook)));
    }


    private static Object writeAsExcel(String suggestedFilenameStem,
                                       Select<?> qry,
                                       Response response) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = workbook.createSheet(ExtractorUtilities.sanitizeSheetName(suggestedFilenameStem));

        writeExcelHeader(qry, sheet);
        writeExcelBody(qry, sheet, null);

        int endFilterColumnIndex = qry.fields().length == 0
                ? 0
                : qry.fields().length - 1;

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, endFilterColumnIndex));
        sheet.createFreezePane(0, 1);

        return writeExcelToResponse(suggestedFilenameStem, response, workbook);
    }


    private static HttpServletResponse writeExcelToResponse(String suggestedFilenameStem,
                                                            Response response,
                                                            SXSSFWorkbook workbook) throws IOException {
        byte[] bytes = ExtractorUtilities.convertExcelToByteArray(workbook);

        HttpServletResponse httpResponse = response.raw();

        httpResponse.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        httpResponse.setHeader("Content-Disposition", "attachment; filename=" + suggestedFilenameStem + ".xlsx");
        httpResponse.setHeader("Content-Transfer-Encoding", "7bit");

        httpResponse.setContentLength(bytes.length);
        httpResponse.getOutputStream().write(bytes);
        httpResponse.getOutputStream().flush();
        httpResponse.getOutputStream().close();

        return httpResponse;
    }


    private Object writeAsCSV(String suggestedFilenameStem,
                              Select<?> qry,
                              Response response) {
        String csv = qry.fetch().formatCSV();
        response.type(MimeTypes.Type.TEXT_PLAIN.name());
        response.header("Content-disposition", "attachment; filename=" + suggestedFilenameStem + ".csv");
        return csv;
    }


    private static void writeExcelBody(Select<?> qry,
                                       SXSSFSheet sheet,
                                       DSLContext dsl) {
        AtomicInteger rowCounter = new AtomicInteger(1);
        Result<?> records = dsl == null
                ? qry.fetch()
                : time("fetch", () -> dsl.fetch(dsl.renderInlined(qry)));

        time("record chomper", () -> {
            int colCount = qry.fields().length;
            records.forEach(r -> {
                int rowNum = rowCounter.getAndIncrement();
                Row row = sheet.createRow(rowNum);
                for (int col = 0; col < colCount; col++) {
                    Cell cell = row.createCell(col);
                    Object val = r.get(col);
                    if (val != null) {
                        cell.setCellValue(val.toString());
                    }
                }
            });
        });
    }


    public static void writeExcelHeader(Select<?> qry, SXSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        AtomicInteger colNum = new AtomicInteger();
        qry.fieldStream().forEach(f -> {
            Cell cell = headerRow.createCell(colNum.getAndIncrement());
            cell.setCellValue(Objects.toString(f.getName()));
        });
    }

}
