package TestData;



import com.aventstack.extentreports.Status;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import Utilities.ExtentReport;
import Utilities.TestUtils;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



public class TestData_compare {

    static Map<String, Integer> csv1HeaderPosition;
    static Map<String, Integer> csv2HeaderPosition;
    static Map<String, Integer> headerCountMap = new LinkedHashMap<String, Integer>();
    public static String megeredFile;
    public static int TotalRowCount;
    public static List<String> headerList1 = new ArrayList<String>();
    public static List<String> headers1 = new ArrayList<String>();
    public static List dupuid = new ArrayList<String>();
    public static List<String> dupuid1= new ArrayList<String>();
   


	public static void main(String[] args) throws IOException, CsvValidationException {
		ExtentReport.initialize(TestData_constant.reports,TestData_constant.reportname,TestData_constant.DocumentTitle);
		TotalRowCount=0;
		align_scorecard alignedScorecard=new align_scorecard(TestData_constant.scratchFile,TestData_constant.alignscorecardcsv);
		alignedScorecard.readCSVFile(TestData_constant.scratchFile, TestData_constant.alignscorecardcsv);
		Convertion convert=new Convertion(TestData_constant.txtFile, TestData_constant.convertcsvFile);
		convert.convertTextToCSV(TestData_constant.txtFile, TestData_constant.convertcsvFile);
		
        String primaryKeyName = TestData_constant.PrimaryKey; 
        String file1Path = TestData_constant.convertcsvFile;
        String file2Path = TestData_constant.alignscorecardcsv;
        

        Map<String, String[]> csv1Data =  readCSVFile(file1Path, primaryKeyName, 1);
        
        Map<String, String[]> csv2Data =  readCSVFile(file2Path, primaryKeyName, 2);
        System.out.println(csv2Data.keySet());
        
        merge(csv1Data, csv2Data, TestData_constant.outputFile);

      
    }

    public static Map<String, String[]> readCSVFile(String filename, String primaryKeyColumn, int fileNumber) throws IOException, CsvValidationException {
        Map<String, String[]> data = new HashMap<>();

        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            String[] header = reader.readNext();
            String uniqueKey = header[0];
            String entity = header[1];
            if(fileNumber==1) {
            	csv1HeaderPosition = new LinkedHashMap<>();
            } else if(fileNumber==2) {
            	csv2HeaderPosition = new LinkedHashMap<>();
            }
            for(int i=0;i<header.length;i++) {
            	String headerKey = header[i];
            	if(fileNumber==1) {
            		csv1HeaderPosition.put(headerKey.toLowerCase(), i);
            	} else if(fileNumber==2) {
            		csv2HeaderPosition.put(headerKey.toLowerCase(), i);
            	}
            	
            }
            //System.out.println(csv1HeaderPosition.size());
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String primaryKey = nextLine[0];
                String entity1 = nextLine[1];
                data.put(primaryKey.trim()+","+entity1.trim(), nextLine);
            }
            
            
        }
        System.out.println(data);
        return data;
    }
    
    public static void merge(Map<String, String[]> csv1Data, Map<String, String[]> csv2Data, String outputFile) throws IOException, CsvValidationException{
    	CSVWriter writer = new CSVWriter(new FileWriter(outputFile));
    	CSVWriter writer1 = new CSVWriter(new FileWriter(TestData_constant.mismatch));
    	List<String> headerList = new ArrayList<String>();
    	List<String> headers = new ArrayList<String>();
    	
    	
    	for(String column : csv1HeaderPosition.keySet()) {
			if(!column.equalsIgnoreCase("unique_key")) {
					
				
    			headerList1.add(column.trim());	
    			headers1.add(column.trim());
    			//System.out.println(headerList.toArray());
			}
			else {
				headerList1.add("unique_key");
				headers1.add(column);
			}
		}
    	writer1.writeNext(headerList1.toArray(new String[headerList1.size()]));
    	
    	
    	if(csv1HeaderPosition.size() == csv2HeaderPosition.size()) {
    		String column1="";
    		String column2="";
    		String column3="";
    		for(String column : csv1HeaderPosition.keySet()) {
    			if(column.trim().equalsIgnoreCase("unique_key")) {
    				headerList.add("unique_key");
    				headers.add(column);	
    			}
    			else if(column.equalsIgnoreCase("Entity")) {
    				 headerList.add("Entity");
	    				headers.add(column);	
    			}
    			else {
    				column1 = column+"_Py";
    				column2 = column+"_Mo";
    				column3 = column+"_STATUS";	
    				
        			headerList.add(column1);	
        			headerList.add(column2);	
        			headerList.add(column3);
        			
        			headers.add(column);
    				
    			}
    			
    		}
    		
    		
    		String[]arr = new String [headerList.size()];
    		System.out.println(headerList.toArray());
    		
    		
    		

    		writer.writeNext(headerList.toArray(new String[headerList.size()]));
    		int headerCount=0;
    		List<String> rowArray=null;
    		for(String uid: csv1Data.keySet()) {
    			rowArray = new ArrayList<String>();
				String[] csv1DataObject = csv1Data.get(uid);
				//System.out.println(csv2Data.get(uid));
				String[] csv2DataObject = csv2Data.get(uid);
				if(csv1DataObject == null || csv2DataObject == null) {
				
					Mismatch(csv1Data,csv2Data,uid,headerList1, writer1);
					continue;
				}
				TotalRowCount++;
				
    			for(int i=0;i<headers.size();i++) {	
    				
    				String header = headers.get(i);
    				
    				
    				//System.out.println("values -"+csv1HeaderPosition.get(header)+":"+csv2HeaderPosition.get(header));
    				int csv1Position = csv1HeaderPosition.get(header);
    		
    				int csv2Position = csv2HeaderPosition.get(header);
    				
    				boolean status = false;   //flag
    				String csv1RowData = csv1DataObject[csv1Position];   				
    				String csv2RowData = csv2DataObject[csv2Position];
    				
    				
                    if(header.trim().equalsIgnoreCase("unique_key")) {
                    	column1 = csv1RowData;
                    	rowArray.add(column1);
    					continue;
    				}
                    else if(header.trim().equalsIgnoreCase("Entity")) {
                    	column1 = csv1RowData;
                    	rowArray.add(column1);
    					continue;	
       			}
                    else if(header.trim().equalsIgnoreCase("mia1facility_type6")||header.trim().equalsIgnoreCase("mia1facility_type6")) {
                    	if(csv1RowData.equalsIgnoreCase(csv2RowData)){
                    	
     					status = true;
 						
     					if(headerCountMap.containsKey(header)) { 
     						headerCount = headerCountMap.get(header); 
     						headerCountMap.put(header, headerCount+1); 
     					} else { 
     						headerCountMap.put(header, 1); 
     					}
                    	}
                	
                
               
            }
                    
                    
                    else if(!header.trim().equalsIgnoreCase("mia1facility_type6")&&!header.trim().equalsIgnoreCase("mia1facility_type6")) {
                    	if(!csv1RowData.contentEquals("")&&(!csv2RowData.contentEquals(""))) {
                    	column1 = csv1RowData;
        				String dennis = column1;
        				double f = Double.parseDouble(dennis);
        				csv1RowData=String.format("%.4f", new BigDecimal(f));
        				column2 = csv2RowData;
        				String dennis1 = column2;
        				double f1 = Double.parseDouble(dennis1);
        				csv2RowData=String.format("%.4f", new BigDecimal(f1));
        				if(csv1RowData.equalsIgnoreCase(csv2RowData)) {
        					status = true;
    						
        					if(headerCountMap.containsKey(header)) { 
        						headerCount = headerCountMap.get(header); 
        						headerCountMap.put(header, headerCount+1); 
        					} else { 
        						headerCountMap.put(header, 1); 
        					}
        					
        				}
        				
                    	}
                    	else {
                    	 if(csv1RowData.equalsIgnoreCase(csv2RowData)) {
         					status = true;
     						
         					if(headerCountMap.containsKey(header)) { 
         						headerCount = headerCountMap.get(header); 
         						headerCountMap.put(header, headerCount+1); 
         					} else { 
         						headerCountMap.put(header, 1); 
         					}

         					//
         				}
                    	}
                    	
               }
                   
                  
                   
                   
                    
    				column1 = csv1RowData;
    				column2 = csv2RowData;
    				column3 = Boolean.toString(status);
    				rowArray.add(column1);
    				rowArray.add(column2);
    				rowArray.add(column3);
    				
    			}
    			
                
    			ExtentReport.test=TestUtils.reportconfig(uid,TestData_constant.tester,TestData_constant.pro_name);
    			 if(!rowArray.contains("false")) 
   			  { 
    				 ExtentReport.test.pass(uid);
    				 ExtentReport.test.info(rowArray.toString()); 
    				 ExtentReport.test.log(Status.PASS, "csv1columndata and csv2columndata file are same ");
   			  } else { 
   				  ExtentReport.test.fail(uid);
   				  ExtentReport.test.info(rowArray.toString()); 
   				  ExtentReport.test.log(Status.FAIL, uid+" has false value in this record");
   				  }
    			
    			writer.writeNext(rowArray.toArray(new String[rowArray.size()]));	
    		}
    		
    		
    		int arraySize =( headers.size()*4);
			rowArray = new ArrayList<String>(arraySize);
			
			for(int i=0;i<arraySize;i++) {
				rowArray.add("");//if arraySize % 3==0
			}
			//System.out.println("rowArray size "+rowArray.size());
			for(int i=2;i<headers.size();i++) {
				
				if(headerCountMap.containsKey(headers.get(i))) {
 				int statusCount =  headerCountMap.get(headers.get(i));
				double percentage = (double)statusCount* 100 / TotalRowCount ;
			    rowArray.add((i*3)-2,  String.format("%.2f", percentage) );
				}
				else {
				 rowArray.add((i*3)-2, "0" );
				}
				
					
				
			}
			
			 
			 			
			writer.writeNext(rowArray.toArray(new String[rowArray.size()]));

    		writer.flush();
    		writer.close();
    		writer1.flush();
    		writer1.close();
    		ExtentReport.reports.flush();
    	} else {
    		
    		System.out.println("number of columns are not matching");
    		ExtentReport.reports.flush();
    	}
    }
    
    public static void Mismatch(Map<String, String[]> csv1Data, Map<String, String[]> csv2Data,String uid,List<String> headerList1, CSVWriter writer1 ) throws CsvValidationException, IOException {
    	
    	List<String> rowArray=null;
    	
			rowArray = new ArrayList<String>();
			String[] csv1DataObject = csv1Data.get(uid);
			
			for(int i=0;i<headers1.size();i++) {	
				String header = headers1.get(i);
				int csv1Position = csv1HeaderPosition.get(header);
				String csv1RowData = csv1DataObject[csv1Position];
				rowArray.add(csv1RowData);
				
				
			}
			
			writer1.writeNext(rowArray.toArray(new String[rowArray.size()]));
	   }
    	


}

