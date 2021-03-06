package edu.neu.ccs.constants;

public class Constants {
	
	public static final String COMMA = ",";
	public static final String DATE_DELIMITER_1 = "-";
	public static final String DATE_DELIMITER_2 = "/";
	public static final String EMPTY_STRING = "";
	public static final String PLUS = "\\+";
	public static final String SPACE = " ";
	
	public static final String SECOND_OUTPUT_FOLDER = "SECOND_OUTPUT_FOLDER";
	
	public static final int END_YEAR = 2012;
	public static final String INDUSTRY_SECTOR_FILE = "industry_sector_file";
	public static final int START_YEAR = 1980;
	public static final String TAG_INDUSTRY_FILE = "/tmp/tag_industries.txt";
	public static final String TAG_SECTOR_FILE = "/tmp/tag_sector.txt";
	public static final String TOP_TAGS_SECTOR = "/tmp/top_tags_sector.txt";
	public static final String SECTOR_HUNT = "/tmp/sector_hunt.csv";
	public static final String MODELS = "/tmp/models/";
	public static final String SECTOR_CSV = "/tmp/sector.csv";
	public static final String COUNTRY_CITY_CSV = "/tmp/countrycity.csv";
	public static final String YEAR_COUNTER_GRP = "YEAR";
	
	//Tags
	public static final String SECTOR_TAG = "#ST#";
	public static final String UNIQUE_INDUSTRIES_KEY_TAG = "#I#";
	public static final String PRUNED_DATA = "#PD#";
	
	public static final String MODULE = "MODULE";
	public static final String NULL_SECTOR = "NULL_SECTOR";
	
	// JOB 1 output names
	public static final String TAG_INDUSTRY = "tagindustry";
	public static final String TAG_SECTOR = "tagsector";
	public static final String TOP_TAGS = "toptags";
	
	public enum ClassLabel {
		
		YES("1"), NO("0");
		
		private String value;
		
		private ClassLabel(String value) {
			
			this.value = value;
		}
		
		@Override
		public String toString() {
			
			return this.value;
		}
	}
	public static final String TEST_YEAR = "testyear";
	
	// JOB 2 output names
	
	public static final String DATA_MODEL_TAG = "datamodel";
	public static final String PRUNED_DATA_TAG = "pruneddata";
	public static final String TEST_DATA_TAG = "testdata";
	public static final String TOP_TAGS_FILE_TAG = "toptagsfile";
	
	public static final String COLUMN_FAMILY = "LINKEDIN";
	public static final byte[] COLUMN_FAMILY_BYTES = COLUMN_FAMILY.getBytes();
	
	public static enum UserProfileEnum {
		
		FIRSTNAME, LASTNAME, NUMCONNECTIONS, LOCATION, POSITION_LAST_KNOWN_TITLE, POSITION_LAST_KNOWN_COMPANY, REL_EXPERIENCE;
	}
	
	public static final String HBASE_DATA_LOAD = "HBASE_DATA_LOAD";
	public static final String EMITTED_DATA = "EMITTED_DATA";
}
