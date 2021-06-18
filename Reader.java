package it.gepo.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import it.gepo.engine.model.RecordConto;

public class Reader implements StepExecutionListener, ItemReader<List<String>> {
	private Logger logger = Logger.getLogger(Reader.class);
	private String posizioneFile;
	private String tempFile;
	private String query;
	private DataSource dataSource;
	boolean readDone = false;
	private String data;
	private File[] listOfFiles;
	private List<String> listaScarti = new ArrayList<String>();
	private String fileName;
	private HashMap<String, String> mapFileName = new HashMap<String, String>();
	private String extractedData;
	private ExecutionContext jobExecutionContext;

	@Override
	public List<String> read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if (!readDone) {
			List<RecordConto> contenutoConto = new ArrayList<RecordConto>();
			File folder = new File(tempFile);
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {

				if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("trasferimenti")
						&& (listOfFiles[i].getName().endsWith("xls"))) {
					fileName = (listOfFiles[i].getName());
					mapFileName.put("nomeFile", fileName);
					jobExecutionContext.put("nomeFile", fileName);
					posizioneFile = tempFile + (listOfFiles[i].getName());
					data = fileName.replace("trasferimenti ", "");
					String ch = "\\";
					data = data.replace(".xls", "");
					data = data.substring(0, 2) + ch + data.substring(2, 4) + ch + data.substring(4, 8);
					break;
				}

			}
			FileInputStream inputStream = new FileInputStream(posizioneFile);

			HSSFWorkbook workbook = null;
			try {
				workbook = new HSSFWorkbook(inputStream);
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row;
			HSSFCell cell;
			int rows;
			rows = sheet.getPhysicalNumberOfRows();
			int cols = 0;
			int tmp = 0;
			for (int i = 0; i < 10 || i < rows; i++) {
				row = sheet.getRow(i);
				if (row != null) {
					tmp = sheet.getRow(i).getPhysicalNumberOfCells();
					if (tmp > cols)
						cols = tmp;
				}
			}
			for (int r = 4; r < rows; r++) {
				row = sheet.getRow(r);
				if (row != null) {
					cell = row.getCell(7);
					row.getCell(11);
					if (cell != null) {
						String contenutoCellaConto = row.getCell(7).toString().toUpperCase();
						String contenutoCellaAgente = row.getCell(11).toString().toUpperCase();
						if (contenutoCellaConto.length() == 0) {
							continue;
						}

						if (contenutoCellaConto.length() < 12 ) {
							contenutoCellaConto = StringUtils.leftPad(contenutoCellaConto.toUpperCase(), 12, '0');
						}

						RecordConto record = new RecordConto();
						record.setAgente(contenutoCellaAgente);
						record.setConto(contenutoCellaConto);
						
						contenutoConto.add(record);
					}

				}

			}
			try {
				workbook.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			Connection conn = null;
			try {
				conn = dataSource.getConnection();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			try {
				conn.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			PreparedStatement stmt = null;
			try {
				String sql = (query);
				sql = sql.replace(":data", data);
				stmt = conn.prepareStatement(sql);

			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			List<String> listaValoriDb = new ArrayList<String>();

			try (ResultSet rs = stmt.executeQuery()) {

				while (rs.next()) {
					listaValoriDb.add(rs.getString(1));
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			} finally {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			ListIterator<RecordConto> listIterator = contenutoConto.listIterator();
			while (listIterator.hasNext()) {
				RecordConto recordConto =listIterator.next(); 
				String contoXls = recordConto.getConto();
				String agenteXls = recordConto.getAgente();
				if (listaValoriDb.contains(contoXls)) {
				} else {
					listaScarti.add(contoXls + ";" + agenteXls);
					
				}
			}
			readDone = true;
			return listaScarti;
		}
		return null;

	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean isReadDone() {
		return readDone;
	}

	public void setReadDone(boolean readDone) {
		this.readDone = readDone;
	}

	public List<String> getListaScarti() {
		return listaScarti;
	}

	public void setListaScarti(List<String> listaScarti) {
		this.listaScarti = listaScarti;
	}

	@Override
	public ExitStatus afterStep(StepExecution arg0) {

		return ExitStatus.COMPLETED;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
	}

	public File[] getListOfFiles() {
		return listOfFiles;
	}

	public void setListOfFiles(File[] listOfFiles) {
		this.listOfFiles = listOfFiles;
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

	public String getExtractedData() {
		return extractedData;
	}

	public void setExtractedData(String extractedData) {
		this.extractedData = extractedData;
	}

	public String getPosizioneFile() {
		return posizioneFile;
	}

	public void setPosizioneFile(String posizioneFile) {
		this.posizioneFile = posizioneFile;
	}

	public String getTempFile() {
		return tempFile;
	}

	public void setTempFile(String tempFile) {
		this.tempFile = tempFile;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public ExecutionContext getJobExecutionContext() {
		return jobExecutionContext;
	}

	public void setJobExecutionContext(ExecutionContext jobExecutionContext) {
		this.jobExecutionContext = jobExecutionContext;
	}
}
