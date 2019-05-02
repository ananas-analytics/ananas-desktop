package org.ananas.runner.model.steps.files;

import org.ananas.runner.model.errors.AnanasException;
import org.ananas.runner.model.errors.ExceptionHandler;
import org.ananas.runner.model.steps.commons.ErrorHandler;
import org.ananas.runner.model.steps.commons.paginate.AbstractPaginator;
import org.ananas.runner.model.steps.commons.paginate.Paginator;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import spark.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExcelPaginator extends AbstractPaginator implements Paginator {

	ExcelStepConfig excelConfig;

	public ExcelPaginator(String id, ExcelStepConfig excelConfig) {
		super(id, null);
		this.excelConfig = excelConfig;
	}

	@Override
	public Iterable<Row> iterateRows(Integer page, Integer pageSize) {
		MutablePair<Schema, Iterable<Row>> rs =
				extractWorkbook(new ErrorHandler(), this.excelConfig, page * pageSize, page * pageSize + pageSize,
						e -> e);
		this.schema = rs.getLeft();
		return rs.getRight();
	}

	interface ParDoExcelRow<T> {
		T rowTo(Row r);
	}

	public static <T> MutablePair<Schema, Iterable<T>> extractWorkbook(ErrorHandler errorHandler,
																	   ExcelStepConfig config,
																	   int offset,
																	   int limit,
																	   ParDoExcelRow<T> lambdaFunction) {
		Schema schema = null;
		List<T> rows = new LinkedList<>();
		Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(new File(config.path));
			// Retrieving the number of sheets in the Workbook
			System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

			Sheet sheet = workbook.getSheetAt(0);

			if (StringUtils.isNotEmpty(config.sheetName)) {
				sheet = workbook.getSheet(config.sheetName);
				if (sheet == null) {
					throw new RuntimeException("No sheet with name " + config.sheetName);
				}
			}

			int realFirstRow = sheet.getFirstRowNum();

			System.out.println("\n\nIterating over Rows and Columns with lambda\n");
			System.out.println("\n\nFirst row " + sheet.getFirstRowNum());
			System.out.println("\n\nLast row " + sheet.getLastRowNum());
			Schema.Builder schemaBuilder = Schema.builder();
			for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
				Iterator<Cell> header = sheet.getRow(i).iterator();
				Iterator<Cell> firstRow = sheet.getRow(Math.min(i + 1, sheet.getLastRowNum())).iterator();
				schemaBuilder = Schema.builder();
				Map<String, Schema.FieldType> fields = new HashMap<>();
				boolean isHeader = true;
				while (header.hasNext()) {
					Cell cellHeader = header.next();
					if (!firstRow.hasNext()) {
						throw new RuntimeException("No data exist after header line " + i +
								"Expected a cell value for each header cells");
					}
					Cell firstRowCell = firstRow.next();
					MutablePair<Schema.FieldType, Object> firstRowCol = toRowField(firstRowCell);
					MutablePair<Schema.FieldType, Object> headerCol = toRowField(cellHeader);
					if (fields.get(headerCol.getRight()) != null || headerCol.getLeft() != Schema.FieldType.STRING) {
						isHeader = false;
						break;
					}
					fields.put((String) headerCol.getRight(), headerCol.getLeft());
					schemaBuilder.addNullableField((String) headerCol.getRight(), firstRowCol.getLeft());
				}
				if (isHeader) {
					realFirstRow = Math.min(i + 1, sheet.getLastRowNum());
					break;
				}
			}
			schema = schemaBuilder.build();

			int firstRow = Math.min(sheet.getLastRowNum(), realFirstRow + offset);
			int lastRow = Math.min(sheet.getLastRowNum(), firstRow + limit);

			System.out.println("\n\nLimit " + limit);
			System.out.println("\n\nOffset " + offset);
			System.out.println("\n\nFirst row " + firstRow);
			System.out.println("\n\nLast row " + lastRow);

			for (int j = firstRow; j <= lastRow && sheet.getRow(j) != null; j++) {
				org.apache.beam.sdk.values.Row.Builder r = org.apache.beam.sdk.values.Row.withSchema(schema);
				Iterator<Cell> row = sheet.getRow(j).iterator();
				while (row.hasNext()) {
					Cell c = row.next();
					r.addValue(toRowField(c).getRight());
				}
				rows.add(lambdaFunction.rowTo(r.build()));
			}

		} catch (IOException e) {
			throw new AnanasException(
					MutablePair.of(ExceptionHandler.ErrorCode.GENERAL, "Can't read your Excel file. "));
		} catch (InvalidFormatException e) {
			throw new AnanasException(
					MutablePair.of(ExceptionHandler.ErrorCode.GENERAL, "Oops. Invalid format? "));
		} catch (Exception e) {
			errorHandler.addError(ExceptionHandler.ErrorCode.GENERAL,
					e.getMessage());

		} finally {
			try {
				workbook.close();
			} catch (Exception e) {
			}
		}
		return MutablePair.of(schema, rows);
	}

	/**
	 * Return a Row field tuple [fieldType,fieldValue]
	 *
	 * @param cell
	 * @return
	 */
	private static MutablePair<Schema.FieldType, Object> toRowField(Cell cell) {
		switch (cell.getCellTypeEnum()) {
			case BOOLEAN:
				return MutablePair.of(Schema.FieldType.BOOLEAN, cell.getBooleanCellValue());
			case STRING:
				return MutablePair.of(Schema.FieldType.STRING, cell.getRichStringCellValue().getString());
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					Date d = cell.getDateCellValue();
					return MutablePair.of(Schema.FieldType.DATETIME,
							new DateTime(d.getTime()).toDateTime(DateTimeZone.UTC));
				} else {
					return MutablePair.of(Schema.FieldType.DOUBLE, cell.getNumericCellValue());
				}
			case FORMULA:
				return MutablePair.of(Schema.FieldType.STRING, cell.getCellFormula());
			case ERROR:
				return MutablePair.of(Schema.FieldType.STRING, "");
			case BLANK:
				return MutablePair.of(Schema.FieldType.STRING, "");
			default:
				return MutablePair.of(Schema.FieldType.STRING, "");
		}
	}

}
