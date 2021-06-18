package it.gepo.engine.writer;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;

public class FileExcelWriter implements ItemWriter<String>, InitializingBean {

	private String dirFile;
	private String header;
	protected List<String> headers;
	private int rowNum = 1;
	private boolean eseguito = false;
	private List<String> listaScarti = new ArrayList<String>();
	protected HSSFWorkbook workbook;
	private DataSource dataSource;
	protected HSSFSheet sheet;
	protected String sheetName;
	private HSSFCellStyle dataCellStyle;
	private CellStyle dateCellStyle;
	private String fileName;
	protected int currRow = 0;
	private HashMap<String, String> mapFileName = new HashMap<String, String>();

	public void autoSizeColumns(Workbook workbook) {
		HSSFSheet sheet = (HSSFSheet) workbook.getSheet(sheetName);

		sheet.getPhysicalNumberOfRows();
		for (int i = 0; i < headers.size(); i++) {
			sheet.autoSizeColumn(i);
		}
	}

	protected void createStringCell(Row row, String val, int col) {
		Cell cell = row.createCell(col);
		cell.setCellStyle(dataCellStyle);
		cell.setCellType(CellType.STRING);
		cell.setCellValue(val);
	}

	private void addHeaders(Sheet sheet) {

		Workbook workbook = sheet.getWorkbook();

		HSSFCellStyle style = (HSSFCellStyle) workbook.createCellStyle();
		Font font = workbook.createFont();

		font.setFontHeightInPoints((short) 10);
		font.setFontName("Arial");
		font.setBold(true);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFont(font);

		Row row = sheet.createRow(currRow);
		int col = 0;
		for (String header : headers) {
			Cell cell = row.createCell(col);
			cell.setCellValue(header);
			cell.setCellStyle(style);
			col++;
		}
	}

	public CellStyle getDateCellStyle() {
		return dateCellStyle;
	}

	public void setDateCellStyle(CellStyle dateCellStyle) {
		this.dateCellStyle = dateCellStyle;
	}

	@Override
	public void write(List<? extends String> liste) throws Exception {

		HSSFWorkbook workbook = new HSSFWorkbook();
		if (!eseguito) {
			eseguito = true;
			headers = Arrays.asList(header.split(";"));
			int rowNum = 1;
			Cell cellaConto = null;
			Cell cellaAgente = null;
			HSSFSheet sheet = workbook.createSheet("Scarti");
			addHeaders(sheet);
			ListIterator<? extends String> iterator = liste.listIterator();
			while (iterator.hasNext()) {
				Object valoriExcel = iterator.next();
				ArrayList<String> list = (ArrayList<String>) valoriExcel;
				for (int i = 0; i < list.size(); i++) {
					Row row = sheet.createRow(rowNum++);
					cellaConto = row.createCell(0);
					cellaAgente = row.createCell(1);
					String [] array = list.get(i).split(";");
					String conto = array[0];
					String agente = array[1];
					cellaConto.setCellValue(conto != null ?conto.toString().trim() : "");
					cellaAgente.setCellValue(agente != null ?agente.toString().trim() : "");
				}

			}
			for (int i = 0; i < headers.size(); i++) {
				sheet.autoSizeColumn(i);
			}
			System.out.println(mapFileName.get("nomeFile"));
			System.out.println(dirFile);
			String excelFilePath = (dirFile + mapFileName.get("nomeFile"));
			FileOutputStream fileOut = new FileOutputStream(excelFilePath);
			workbook.write(fileOut);
			workbook.close();
			fileOut.close();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}


	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public List<String> getListaScarti() {
		return listaScarti;
	}

	public void setListaScarti(List<String> listaScarti) {
		this.listaScarti = listaScarti;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public boolean isEseguito() {
		return eseguito;
	}

	public void setEseguito(boolean eseguito) {
		this.eseguito = eseguito;
	}

	public HSSFWorkbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(HSSFWorkbook workbook) {
		this.workbook = workbook;
	}

	public HSSFSheet getSheet() {
		return sheet;
	}

	public void setSheet(HSSFSheet sheet) {
		this.sheet = sheet;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public HSSFCellStyle getDataCellStyle() {
		return dataCellStyle;
	}

	public void setDataCellStyle(HSSFCellStyle dataCellStyle) {
		this.dataCellStyle = dataCellStyle;
	}

	public int getCurrRow() {
		return currRow;
	}

	public void setCurrRow(int currRow) {
		this.currRow = currRow;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getDirFile() {
		return dirFile;
	}

	public void setDirFile(String dirFile) {
		this.dirFile = dirFile;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public HashMap<String, String> getMapFileName() {
		return mapFileName;
	}

	public void setMapFileName(HashMap<String, String> mapFileName) {
		this.mapFileName = mapFileName;
	}


}
