
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Date;
import java.sql.Timestamp;

public class DBSummary {
	static Connection con, con1, con2;
    static String usr;
    static String pwd;
    public static Properties props;
	static String url;
	
	static String catalog, ip_address, pn, datasrc;
	static String schema;
	static String tablename;
	static String previousSPRid=""; static String currentSPRid="";
	static String enable64,mode="32bit";
	static String viewname_with_owners="";
	static String viewname_without_owners="";
	static String view_ptprtp="";
	static String view_pendingRuns="";
	static String view_pendingAnalysis="";
	static String ctn = "E2EMX.SCH.CLOBDATATBL",clobTable="";
	static BufferedReader brreader = null;
	
	public static void main(String[] args) {
			
		brreader = new BufferedReader(new InputStreamReader(System.in));
		//System.setProperty("clobTableName",ctn);
		
		try
        {	try{
				tablename = System.getenv("UpdateTestStatusTableName");
				ip_address = System.getenv("TestRepoIPAddress");
				pn = System.getenv("TestRepoPortNumber");
				usr = System.getenv("TestRepoUserName");
				pwd = System.getenv("TestRepoPassword");
				datasrc=System.getenv("TestRepoServerDataSource");
				enable64 = System.getenv("ENABLE_64BIT");
				clobTable = System.getenv("clobTableName");
			}catch(Exception e){
				System.out.println(e.getMessage());
				System.exit(0);
			}
			
			url="jdbc:t4sqlmx://"+ip_address.toUpperCase()+":"+pn+"/:";
			
			int cat_index = tablename.indexOf(".");
    		catalog = tablename.substring(0,cat_index);
			tablename = tablename.substring(cat_index+1);
			int sch_index = tablename.indexOf(".");
			schema = tablename.substring(0,sch_index);
    		tablename = tablename.substring(sch_index+1); 
            
			FileWriter fw=new FileWriter("DBSummary.properties");    
    	    fw.write("url="+url);    
    	    fw.write("\nuser="+usr);
    	    fw.write("\npassword="+pwd);
    	    fw.write("\ncatalog="+catalog);
    	    fw.write("\nschema="+schema);
    	    fw.write("\ntablename="+tablename);
    	    fw.write("\nserverDataSource="+datasrc);
			fw.write("\nclobTableName="+clobTable);
    	    fw.close();  
			
			
			String propFile ="DBSummary.properties";
            if (propFile != null)
            {
                FileInputStream fs = new FileInputStream(new File(propFile));
                props = new Properties();
                props.load(fs);

                
            } else {
                System.out.println("Error: Properties file could not be set. Exiting.---");
                System.exit(0);
            } 
			
            Class.forName("com.tandem.t4jdbc.SQLMXDriver");
			if (props == null)
				throw new SQLException ("Error: t4sqlmx.properties is null. Exiting.");
			
			viewname_with_owners = catalog+"."+schema+".ALL_COL_STATS_WITH_OWNER_"+tablename;
			viewname_without_owners = catalog+"."+schema+".ALL_COL_STATS_"+tablename;
			view_ptprtp=catalog+"."+schema+".PTPRTPVIEW_"+tablename;
			view_pendingRuns=catalog+"."+schema+".PENDINGRUNS_"+tablename;
			view_pendingAnalysis=catalog+"."+schema+".PENDINGANALYSIS_"+tablename;
			
			if(enable64.equalsIgnoreCase("on"))
				mode="64bit";
			else 
				mode = "32bit";
			
		} catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(0);
        } 
        
		try{
			//System.out.println("Trying to get connection");
			con = DriverManager.getConnection(url, props);
			System.out.println("----\t WORKING ON TABLE : "+tablename.toUpperCase()+" ----");
			//System.out.println("Got connection successfully");
			
		} catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(0);
        } 
		
		if (args.length != 0)
		{
		  display_res_with_arg(args);
		}	
		
		
		//createView();
		boolean life = true;
		int choice;
		
		while(life) {
			System.out.println("\n=============================================================");
			System.out.println("\t\tDisplay Results Menu: ");
			System.out.println("1. Export failed test cases to file");
			System.out.println("2. Update testcase Status and Analysis Comments");
			System.out.println("3. View test results");
			System.out.println("4. View Pending Runs/Analysis");
			System.out.println("5. View / Update CurrDiff");
			System.out.println("6. Compare CurrDiff between releases");
			System.out.println("9. Exit");
			System.out.println("=============================================================");
			System.out.println("\n\t\tEnter your choice! ");
			try{
				choice = Integer.parseInt(brreader.readLine());
			
				switch(choice) {
					case 1: {
							int ch = getFailedCategoryChoice();
							if(ch==0){
								break;
							}else if(ch==1) {
								String output_file_location="";
								try{
									System.out.println("Enter the output file location, eg /home/e2e");
									output_file_location = brreader.readLine();
									File f = new File(output_file_location);
									if (! f.exists()){
										f.mkdir();
									}
								}catch(Exception e) {
									e.printStackTrace();
								}
								exportAllFailedTestCases(output_file_location);
							}else if (ch==2) {
								String output_file_location="";
								
								try{
									System.out.println("Enter the output file location, eg /home/e2e");
									output_file_location = brreader.readLine();
									File f = new File(output_file_location);
									if (! f.exists()){
										f.mkdir();
									}
								}catch(Exception e) {
									e.printStackTrace();
								}
							
								exportFailedTestCasesAnalysisPending(output_file_location);
							}else {
								String output_file_location="";
								try{
									System.out.println("Enter the output file location, eg /home/e2e");
									output_file_location = brreader.readLine();
									File f = new File(output_file_location);
									if (! f.exists()){
										f.mkdir();
									}
								}catch(Exception e) {
									e.printStackTrace();
								}
								
								exportFailedTestCasesWithAnalysisDone(output_file_location);
							}
							
							}
							break;
					case 6:{
							String tab1="E2EMX.SCH.TESTCASESTATUS_";
							String tab2="E2EMX.SCH.TESTCASESTATUS_";
							System.out.println("Enter old SPR ID, eg. R37_64BIT");
							previousSPRid = brreader.readLine();
							System.out.println("Enter new SPR ID, eg. R37_64BIT_CYCLE2");
							currentSPRid = brreader.readLine();
							
							
							tab1 = tab1 + previousSPRid;
							tab2 = tab2 + currentSPRid;
							tab1=tab1.toUpperCase(); tab2=tab2.toUpperCase();
							System.out.println("The two tables to be compared are: "+tab1+ " & "+tab2);
							getCurrDiffComaprison(tab1,tab2);
							}
							break;
					case 2: {
							String input_file;
					
							try{
								System.out.println("Enter the input file from which you want to update the comments, eg /home/e2e/APURVA_allFailedTests_64bit.txt \n Enter 0 to return to main menu");
								input_file=brreader.readLine();
								if(input_file.equals("0"))
									break;
								else{
									File f = new File(input_file);
									if(f.exists()==false) {
										System.out.println("File does not exists! Please enter again !");
									}else {
										updateAnalysisThroughFile(input_file);
									}
								}
								
							}catch(Exception e) {
								e.printStackTrace();
							}
							}
							break;
					case 4: {
							int ch = getpendingChoice();
							if(ch==0)
								break;
							else if(ch==1) {
								
								Statement stmt = con.createStatement();
								String query = "select * from "+view_pendingRuns+" order by qaowner,RTP";
								ResultSet rs = stmt.executeQuery(query);
								System.out.println("\n\t***Working on DB table "+tablename.toUpperCase()+"***\n");
								System.out.println("SUITE ,CATEGORY ,QAOWNER ,RTP ");
								System.out.println("---------------------------------------------------------------");
								
								try{
									while(rs.next()) {
										System.out.println();
										for(int v = 1; v <=4 ; v++){
											if(v<4)
												System.out.print(rs.getObject(v)+",");
											else
												System.out.print(rs.getObject(v));
										}
									}
								}catch(Exception e){
									System.out.println(e.getMessage());
								}
								
							}else {
								Statement stmt = con.createStatement();
								
								String query = "select suite_name, category_name, qaowner,NUM as PA from "+ view_pendingAnalysis+" where NUM>0 order by qaowner, PA "; 
								ResultSet rs = stmt.executeQuery(query);
								System.out.println("\n\t***Working on DB table "+tablename.toUpperCase()+"***\n");
								System.out.println("SUITE ,CATEGORY ,QAOWNER ,PENDING ANALYSIS ");
								System.out.println("---------------------------------------------------------------");
								
								try{
									while(rs.next()) {
										System.out.println();
										for(int v = 1; v <=4 ; v++){
											if(v<4)
												System.out.print(rs.getObject(v)+",");
											else
												System.out.print(rs.getObject(v));
										}
									}
								}catch(Exception e){
									System.out.println(e.getMessage());
								}
							}
								
							}
						    break;
					case 5: {
							int ch = getCurrDiffChoice();
							if(ch==0)
								break;
							else if(ch==1) {
								String qaowner = getOwner();
								String suite_name="", cat_name="", unit_name="", case_name="";
								int num_of_records=0;
								boolean keep_running = true;
								while(keep_running) {
									System.out.println("Enter the test suite, test category, test unit and test case with spaces in between:");
									System.out.println("e.g. Suite Category Unit Case");
									String test = brreader.readLine();
									String[] test_arr = test.split(" ",-1);
									suite_name = test_arr[0];
									cat_name = test_arr[1];
									unit_name = test_arr[2];
									case_name = test_arr[3];
								
									
									try{
										Statement stmt = con.createStatement();
										String query = "select count(*) from "+tablename+" where SUITE_NAME=\'"+suite_name+"\' and CATEGORY_NAME=\'"+cat_name+"\' and TESTUNIT_NAME=\'"+unit_name+"\' and TESTCASE_NAME=\'"+case_name+"\'";
										ResultSet rs = stmt.executeQuery(query);
										while(rs.next()) {
											num_of_records=Integer.parseInt(rs.getObject(1).toString());
										}
									System.out.println(num_of_records + " records found that match this criteria");
									}catch(SQLException e) {
										System.out.print(e.getMessage());
									}
													
								
									if(num_of_records == 0) {
										System.out.println("Please enter valid details");
										keep_running = true;
									}else
										keep_running = false;
								}
								try{
									Statement stmt = con.createStatement();
									String query = "select BUILD_ID,CURRDIFF from "+tablename+" where SUITE_NAME=\'"+suite_name+"\' and CATEGORY_NAME=\'"+cat_name+"\' and TESTUNIT_NAME=\'"+unit_name+"\' and TESTCASE_NAME=\'"+case_name+"\'";
									ResultSet rs = stmt.executeQuery(query);
									//System.out.println("Test:  -->"+suite_name+"|"+cat_name+"|"+unit_name+"|"+case_name);
									
									
									if(!rs.next())
										System.out.println("No CurrDiff value set for the above test");	
									else {
										String buildd = rs.getString(1);
										Clob myclob = rs.getClob(2);
										System.out.println("Enter the output file location, eg /home/e2e");
										String output_file_location = brreader.readLine();
										File f = new File(output_file_location);
										if (! f.exists()){
											f.mkdir();
										}
										String output_file= output_file_location+"/"+qaowner.toUpperCase()+"_"+suite_name.toUpperCase()+"_"+cat_name.toUpperCase()+"_"+unit_name+"_"+case_name+"";
										System.out.println("\nCURRDIFF for :"+suite_name+"|"+cat_name+"|"+unit_name+"|"+case_name+"\n");
										readClob(myclob,output_file);
									}
								}catch(Exception e) {
									e.printStackTrace();
									
								}
								
							}else {
								//update clob
								/* String qaowner = getOwner();
								String suite_name="", cat_name="", unit_name="", case_name="";
								int num_of_records=0;
								boolean keep_running = true;
								while(keep_running) {
									System.out.println("Enter the test suite, test category, test unit and test case with spaces in between:");
									System.out.println("e.g. Suite Category Unit Case");
									String test = brreader.readLine();
									String[] test_arr = test.split(" ",-1);
									suite_name = test_arr[0];
									cat_name = test_arr[1];
									unit_name = test_arr[2];
									case_name = test_arr[3];
								
									
									try{
										Statement stmt = con.createStatement();
										String query = "select count(*) from "+tablename+" where SUITE_NAME=\'"+suite_name+"\' and CATEGORY_NAME=\'"+cat_name+"\' and TESTUNIT_NAME=\'"+unit_name+"\' and TESTCASE_NAME=\'"+case_name+"\'";
										ResultSet rs = stmt.executeQuery(query);
										while(rs.next()) {
											num_of_records=Integer.parseInt(rs.getObject(1).toString());
										}
									}catch(SQLException e) {
										System.out.print(e.getMessage());
									}
													
								
									if(num_of_records == 0) {
										System.out.println("Please enter valid details");
										keep_running = true;
									}else
										keep_running = false;
								}
								String input_file;
								
								try{
									System.out.println("Enter the input file from which you want to update the CURRDIFF, eg /home/e2e/APURVA_allFailedTests_64bit.txt \n Enter 0 to return to main menu");
									input_file=brreader.readLine();
									if(input_file.equals("0"))
										break;
									else{
										File f = new File(input_file);
										if(f.exists()==false) {
											System.out.println("File does not exists! Please enter again !");
										}else {
											updateCurrDiffThroughFile(input_file,qaowner,suite_name,cat_name,unit_name,case_name);
										}
									}
									
								}catch(Exception e) {
									e.printStackTrace();
								} */
								String qaowner = getOwner();
								String suite_name="";
								String input_file;
								
								try{
									System.out.println("Enter the test_suite for which CURRDIFF has to be updated");
									suite_name = brreader.readLine();
									System.out.println("Enter the input file from which you want to update the CURRDIFF, eg /home/e2e/APURVA_allFailedTests_64bit.txt \n Enter 0 to return to main menu");
									input_file=brreader.readLine();
									if(input_file.equals("0"))
										break;
									else{
										File f = new File(input_file);
										if(f.exists()==false) {
											System.out.println("File does not exists! Please enter again !");
										}else {
											updateCurrDiffThroughFile1(input_file,qaowner,suite_name);
										}
									}
									
								}catch(Exception e) {
									e.printStackTrace();
								}
								
							}
						    break;
							}
					case 3:{
							Statement stmt = con.createStatement();
							int ch = getDRChoice();
							String query_all="";
							if(ch==0)
								break;
							else if(ch ==1) {
								query_all = "select suite_name, (Fail+Inplan+Notplan+KDiff+APass+InProg+Pass) as Total, (InPlan+Fail+KDiff+APass+InProg+Pass) as Plan, (Pass+Fail+APass+KDiff) as Run, Pass, Fail, APass, InProg ,KDiff, ((Pass+Fail+APass+KDiff)/(InPlan+Fail+KDiff+APass+InProg+Pass))*100 as RTP,  ((Pass+APass+KDiff)/(InPlan+Fail+KDiff+APass+InProg+Pass))*100 as PTP " 
										+ "from "+viewname_without_owners;
								ResultSet rs = stmt.executeQuery(query_all);
								double total_total=0, plan_total=0, run_total=0, pass_total=0, fail_total=0, apass_total = 0, iprog_total = 0, kdiff_total=0;
								System.out.println("\n\t***Working on DB table "+tablename.toUpperCase()+"***\n");
								System.out.println("SUITE \t|TOTAL \t|PLAN \t|RUN \t|PASS \t|FAIL \t|APASS \t|IPROG \t|KDIF \t|RTP \t|PTP \t|");
								System.out.println("-----------------------------------------------------------------------------------------");
								
								try{
									while(rs.next()) {
										System.out.println();
										for(int v = 1; v <=11 ; v++){
											if(v==10 || v==11){
												System.out.print(rs.getObject(v)+"%\t|");
											}else{
												System.out.print(rs.getObject(v)+"\t|");
											}
											if(v==2) total_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==3) plan_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==4) run_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==5) pass_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==6) fail_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==7) apass_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==8) iprog_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==9) kdiff_total+=Integer.parseInt(rs.getObject(v).toString());
										}
									}
								}catch(ArithmeticException e){
									System.out.println("Encountered / by 0... check if Planned column got updated correctly!");
								}
								
								
								
								double RTP_tot=0,PTP_tot=0;
								try{
									RTP_tot = (run_total/plan_total)*100;
									PTP_tot = ((apass_total+pass_total+kdiff_total)/plan_total)*100;
								}catch(ArithmeticException e){
									System.out.println("Encountered / by 0... check if Planned column got updated correctly!");
								}
								RTP_tot = round(RTP_tot,2);
								PTP_tot = round(PTP_tot,2);
								System.out.println("\n-----------------------------------------------------------------------------------------");
								System.out.println("TOTAL \t|"+(int)total_total+" \t|"+(int)plan_total+" \t|"+(int)run_total+" \t|"+(int)pass_total+" \t|"+(int)fail_total+" \t|"+(int)apass_total+" \t|"+(int)iprog_total+" \t|"+(int)kdiff_total+" \t|"+RTP_tot+"% |"+PTP_tot+"% |");
								
								rs.close();
								stmt.close();
							}else {
								String qaowner=getOwner();
								
								query_all = "select suite_name, category_name, (InPlan+Fail+KDiff+APass+InProg+Pass) as Plan, (Pass+Fail+APass+KDiff) as Run, Pass, Fail, APass, InProg ,KDiff, ((Pass+Fail+APass+KDiff)/(InPlan+Fail+KDiff+APass+InProg+Pass))*100 as RTP, ((Pass+APass+KDiff)/(InPlan+Fail+KDiff+APass+InProg+Pass))*100 as PTP " 
								+ "from  "+viewname_with_owners+" where qaowner= \'"+qaowner+"\' and NotPlan=0 order by suite_name;";
								
								ResultSet rs = stmt.executeQuery(query_all);
								double  plan_total=0, run_total=0, pass_total=0, fail_total=0, apass_total = 0, iprog_total = 0, kdiff_total=0;
								
								System.out.println("\n\t***Working on DB table "+tablename.toUpperCase()+"***\n");
								System.out.println("SUITE \t|CATEGORY \t\t|PLAN \t|RUN \t|PASS \t|FAIL \t|APASS \t|IPROG \t|KDIF \t|RTP \t|PTP \t|");
								System.out.println("---------------------------------------------------------------------------------------------------");
								
								try{
									while(rs.next()) {
										System.out.println();
										for(int v = 1; v <=11 ; v++){
											if(v==10 || v==11){
												System.out.print(rs.getObject(v)+"%\t|");
											}else if(v==2){
												String temp = rs.getObject(v).toString();
												if(temp.length()<=4)
													System.out.print(temp+temp.length()+"\t\t\t|");
												else if(temp.length()<=12)
													System.out.print(temp+temp.length()+"\t\t|");
												else
													System.out.print(temp+"\t|");
											}else{
												System.out.print(rs.getObject(v)+"\t|");
											}
											
											if(v==3) plan_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==4) run_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==5) pass_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==6) fail_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==7) apass_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==8) iprog_total+=Integer.parseInt(rs.getObject(v).toString());
											if(v==9) kdiff_total+=Integer.parseInt(rs.getObject(v).toString());
										}
									}
								}catch(ArithmeticException e){
									System.out.println("Encountered / by 0... check if Planned column got updated correctly!");
									e.printStackTrace();
								}
								
								
								
								double RTP_tot=0,PTP_tot=0;
								try{
									RTP_tot = (run_total/plan_total)*100;
									PTP_tot = ((apass_total+pass_total+kdiff_total)/plan_total)*100;
								}catch(ArithmeticException e){
									System.out.println("Encountered / by 0... check if Planned column got updated correctly!");
								}
								RTP_tot = round(RTP_tot,2);
								PTP_tot = round(PTP_tot,2);
								System.out.println("\n---------------------------------------------------------------------------------------------------");
								System.out.println("\tTOTAL \t\t\t|"+(int)plan_total+" \t|"+(int)run_total+" \t|"+(int)pass_total+" \t|"+(int)fail_total+" \t|"+(int)apass_total+" \t|"+(int)iprog_total+" \t|"+(int)kdiff_total+" \t|"+RTP_tot+"% |"+PTP_tot+"% |");
								
								rs.close();
								stmt.close();
								
							}
							
							}
							break;
					case 9: System.out.println("Goodbye, friend ! Have a great day !");
							life = false;
							break;
					default: System.out.println("Invalid entry. Enter again friend !");
				
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	
	}
	


	private static boolean compareFiles(String file1, String file2) {
	
		boolean returnValue = true;
		try {
			StringBuffer contents = new StringBuffer();
			FileInputStream fStream1 = new FileInputStream(file1);
			FileInputStream fStream2 = new FileInputStream(file2);
			byte[] readData1 = new byte[65536];
			byte[] readData2 = new byte[65536];
			int charRead1 = 0;
			int charRead2 = 0;
			while (charRead1 == charRead2 && charRead1 != -1) {
				charRead1 = fStream1.read(readData1);
				charRead2 = fStream2.read(readData2);
				if (charRead1 != charRead2) {
					returnValue = false;
					break;
				}
				if (charRead1 != -1) {
					for (int i = 0; i < charRead1; i++) {
						if (readData1[i] != readData2[i]) {
							returnValue = false;
							break;
						}
					}
				}
			}
			fStream1.close();
			fStream2.close();

		} catch (Exception e) {
			returnValue = false;
		}
		
		return returnValue;
	}
	private static void getCurrDiffComaprison(String tab1, String tab2) {
		// TODO Auto-generated method stub
		String query = "select t1.SUITE_NAME, t1.CATEGORY_NAME, t1.TESTUNIT_NAME, t1.TESTCASE_NAME,t1.CURRDIFF CUR1,t2.CURRDIFF CUR2 "+
						" from "+tab1+" t1 inner join "+tab2+" t2 "+
						" on t1.SUITE_NAME=t2.SUITE_NAME and t1.CATEGORY_NAME=t2.CATEGORY_NAME and t1.TESTUNIT_NAME=t2.TESTUNIT_NAME and  t1.TESTCASE_NAME=t2.TESTCASE_NAME "+
						" where t1.CURRDIFF is not null and t2.CURRDIFF is not null ";
		Connection cont = null; Statement st=null; ResultSet rs = null; ResultSet rs1=null;
		try {
			cont = DriverManager.getConnection(url, props);
			st = cont.createStatement();
			rs = st.executeQuery(query);
			boolean c = rs.next();
			if(c==false) {
				System.out.println("No matching test cases found with CURRDIFF updated in both SPR tables");
				return;
			}
			
			rs = st.executeQuery(query);
			String suite_name = ""; String cat_name=""; String unit_name=""; String case_name="";
			Clob clob1 = cont.createClob();
			Clob clob2 = cont.createClob();
			String file1 = "curdiff_val1.txt"; String file2 = "currdiff_val2.txt";
			System.out.println("TestSuite \t|TestCategory \t|TestUnit \t|TestCase \t|CurrDiff Comparison\n");
			System.out.println("------------------------------------------------------------------------\n");
			
			while(rs.next()) {
				suite_name=rs.getString("SUITE_NAME");
				cat_name=rs.getString("CATEGORY_NAME");
				unit_name=rs.getString("TESTUNIT_NAME");
				case_name=rs.getString("TESTCASE_NAME");
				clob1 = rs.getClob("CUR1");
				clob2 = rs.getClob("CUR2");
				readClob(clob1,file1);
				readClob(clob2,file2);
				boolean check = compareFiles(file1,file2);
				System.out.println(suite_name+" \t|"+cat_name+" \t|"+unit_name+" \t|"+case_name+" \t|"+check+"\n");
				if (check==true) {
					String q="update "+tab2+" set status=\'KDIFF\' where SUITE_NAME=\'"+suite_name+"\' and CATEGORY_NAME=\'"+cat_name+"\' and TESTUNIT_NAME=\'"+unit_name+"\' and TESTCASE_NAME=\'"+case_name+"\' ;";
					Statement s = cont.createStatement();
					s.executeUpdate(q);
					s.close();
				} 
				
			}
			rs.close();
			cont.close();
			
		}catch(SQLException sqle) {
			System.out.println(sqle.getMessage());
			sqle.printStackTrace();
		}catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		
		 
		
	}
	private static void updateCurrDiffThroughFile(String input_file, String qaowner, String suite_name, String cat_name, String unit_name, String case_name) throws Exception {
				Connection con10 = null;BufferedReader br = null;
		try{
			con10 = DriverManager.getConnection(url, props);
			con10.setAutoCommit(false);
			File file = new File(input_file); 
			 br = new BufferedReader(new FileReader(file)); 
			long clobLen = file.length();        
			if(clobLen==0) {
				System.out.println("Input file not present");
				
			}
			else {
				PreparedStatement ps1 = con10.prepareStatement("update "+tablename+" set CURRDIFF = ? where SUITE_NAME=\'"+suite_name+"\' and CATEGORY_NAME=\'"+cat_name+"\' and TESTUNIT_NAME=\'"+unit_name+"\' and TESTCASE_NAME=\'"+case_name+"\'");   
				ps1.setClob(1,br);
				ps1.executeUpdate();
				con10.commit();
				ps1.close();
			}
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			
		}
			br.close();
			con10.close();
		
	}
	private static void updateCurrDiffThroughFile1(String input_file, String qaowner, String suite_name) throws Exception {
		
		Connection con10 =null; BufferedReader br = null;
		String temp_file="./"+qaowner.toUpperCase()+"_currdiff_temp.txt";
		String cat_name = "", unit_name = "", case_name="";
		boolean flag = true;
		try{
			con10 = DriverManager.getConnection(url,props);
			con10.setAutoCommit(false);
			
			File file = new File(input_file); 
			br = new BufferedReader(new FileReader(file)); 
			String s = br.readLine();
			boolean keeprunning = true;
			while(s!=null) {
				if(s.startsWith("@@")) {
					String temp = s;
					s=s.substring(2);
					File file1 = new File(temp_file); 
					BufferedWriter bw = new BufferedWriter(new FileWriter(file1));
				bw.write(temp);
					String[] test_arr = s.split("\\|",-1);
					cat_name=test_arr[0];
					unit_name=test_arr[1];
					unit_name=unit_name.substring(0,unit_name.length()-4);
					case_name=test_arr[2];
					System.out.println("Tests:  --> "+cat_name+" "+unit_name+" "+case_name);
					while (keeprunning) {
						
						String s1 = br.readLine();
						if(s1==null) {
							keeprunning=false;
						}else {
							bw.write(s1);
							bw.newLine();
							if(s1.endsWith("#endtestcase")) {
								keeprunning=false;
							}
						}
						
						
					}
					bw.close();
					
					File f = new File(temp_file);
					BufferedReader br1 = new BufferedReader(new FileReader(f));
					long clobLen = f.length();        
					if(clobLen==0) {
						System.out.println("No currdiff for ");
					}
					else {
						try{
							flag = true;
							// case_name.toLowerCase has been done temporarily
							PreparedStatement ps1 = con10.prepareStatement("update "+tablename+" set CURRDIFF = ? where SUITE_NAME=\'"+suite_name+"\' and CATEGORY_NAME=\'"+cat_name+"\' and TESTUNIT_NAME=\'"+unit_name+"\' and lower(TESTCASE_NAME)=\'"+case_name.toLowerCase()+"\'");   
							System.out.println("Updating CURRDIFF for  SUITE:"+suite_name+" CATEGORY:"+cat_name+" TESTUNIT:"+unit_name+" TESTCASE:"+case_name+" ");
							ps1.setClob(1,br1);
							ps1.executeUpdate();
							con10.commit();
							ps1.close();
							
						}catch(Exception se){
							System.out.println(se.getMessage());
							se.printStackTrace();
							flag = false;
						}
						 
						if(flag == true){
							System.out.println("Update Done!!");
						}
					}
					
					br1.close();
					keeprunning = true;
					s="";
				}
				s = br.readLine();
			}
			
			
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			
		}
			try{
				br.close();
			con10.close();
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		
	}
	private static void readClob(Clob clob, String output_file) {
		
		   Writer wr=null;
	       Reader rd=null;
	        try {
	            wr= new FileWriter(output_file);
	            rd= clob.getCharacterStream();
	            char[] myClobData = new char[3880];
	            int charRead = 0;

	            while (charRead != -1) {
	                charRead = rd.read(myClobData);
	                if (charRead != -1) {
						//System.out.println(myClobData);
	                    wr.write(myClobData, 0, charRead);
	                }
	            }

	        } catch (Exception e) {
	        } finally {
	            try {
	                if (wr != null) {
	                    wr.close();
	                }
	            } catch (Exception ee) {
	            }
	        }
		
	}
	private static int getCurrDiffChoice() {
		boolean keeprunning = true;
		int catCh=0;
		
		try{
			while(keeprunning){
		
				System.out.println("1. View CurrDiff\n2. Update CurrDiff\n0. Return to main menu\nEnter your choice - 1 / 2 / 0?");
				int ch = Integer.parseInt(brreader.readLine());
	
					switch(ch){
						case 1: catCh=1;keeprunning=false; break;
						case 2: catCh=2;keeprunning=false;  break;
						case 0: catCh=0;keeprunning=false;  break;
						default: System.out.println("Invalid choice Enter again friend!"); 
					}
		}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return catCh;
	}
	private static int getpendingChoice() {
		boolean keeprunning = true;
		int catCh=0;
		
		try{
			while(keeprunning){
		
				System.out.println("View : ");
				System.out.println("1. Pending Runs\n2. Pending Analysis\n0. Return to main menu\nEnter your choice - 1 / 2 / 0?");
				int ch = Integer.parseInt(brreader.readLine());
	
					switch(ch){
						case 1: catCh=1;keeprunning=false; break;
						case 2: catCh=2;keeprunning=false;  break;
						case 0: catCh=0;keeprunning=false;  break;
						default: System.out.println("Invalid choice Enter again friend!"); 
					}
		}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return catCh;
	}
	private static void display_res_with_arg(String[] args) {
		// TODO Auto-generated method stub
		try{
			String qaowner="%";
			String suite_name="%", cat_name="%";
			if(args.length == 1) {
				qaowner=args[0];
			}else if(args.length == 2) {
				qaowner=args[0];
				suite_name=args[1];
			}else if(args.length == 3) {
				qaowner=args[0];
				suite_name=args[1];
				cat_name=args[2];
			}
			
			con = DriverManager.getConnection(url, props);
			Statement stmt = con.createStatement();
			String query = "select suite_name, category_name, (InPlan+Fail+KDiff+APass+InProg+Pass) as Plan, (Pass+Fail+APass+KDiff) as Run, Pass, Fail, APass, InProg ,KDiff,  ((Pass+Fail+APass+KDiff)/(InPlan+Fail+KDiff+APass+InProg+Pass))*100 as RTP, ((Pass+APass+KDiff)/(InPlan+Fail+KDiff+APass+InProg+Pass))*100 as PTP  " 
					+ "from  "+viewname_with_owners+" where qaowner= \'"+qaowner+"\' and NotPlan=0 and suite_name like \'"+suite_name+"\' and category_name like \'"+cat_name+"\' order by suite_name";
			ResultSet rs = stmt.executeQuery(query);
			double  plan_total=0, run_total=0, pass_total=0, fail_total=0, apass_total = 0, iprog_total = 0, kdiff_total=0;
					
			System.out.println("\n\t***Working on DB table "+tablename.toUpperCase()+"***\n");
			System.out.println("SUITE \t|CATEGORY \t\t|PLAN \t|RUN \t|PASS \t|FAIL \t|APASS \t|IPROG \t|KDIF \t|RTP \t|PTP \t|");
			System.out.println("---------------------------------------------------------------------------------------------------");
					
					try{
						while(rs.next()) {
							System.out.println();
							for(int v = 1; v <=11 ; v++){
								if(v==10 || v==11){
									System.out.print(rs.getObject(v)+"%\t|");
								}else if(v==2){
									String temp = rs.getObject(v).toString();
									if(temp.length()<=4)
										System.out.print(temp+temp.length()+"\t\t\t|");
									else if(temp.length()<=12)
										System.out.print(temp+temp.length()+"\t\t|");
									else
										System.out.print(temp+"\t|");
								}else{
									System.out.print(rs.getObject(v)+"\t|");
								}
								
								if(v==3) plan_total+=Integer.parseInt(rs.getObject(v).toString());
								if(v==4) run_total+=Integer.parseInt(rs.getObject(v).toString());
								if(v==5) pass_total+=Integer.parseInt(rs.getObject(v).toString());
								if(v==6) fail_total+=Integer.parseInt(rs.getObject(v).toString());
								if(v==7) apass_total+=Integer.parseInt(rs.getObject(v).toString());
								if(v==8) iprog_total+=Integer.parseInt(rs.getObject(v).toString());
								if(v==9) kdiff_total+=Integer.parseInt(rs.getObject(v).toString());
							}
						}
					}catch(ArithmeticException e){
						System.out.println("Encountered / by 0... check if Planned column got updated correctly!");
						e.printStackTrace();
					}
					
					
					
					double RTP_tot=0,PTP_tot=0;
					try{
						RTP_tot = (run_total/plan_total)*100;
						PTP_tot = ((apass_total+pass_total+kdiff_total)/plan_total)*100;
					}catch(ArithmeticException e){
						System.out.println("Encountered / by 0... check if Planned column got updated correctly!");
					}
					RTP_tot = round(RTP_tot,2);
					PTP_tot = round(PTP_tot,2);
					System.out.println("\n---------------------------------------------------------------------------------------------------");
					System.out.println("\tTOTAL \t\t\t|"+(int)plan_total+" \t|"+(int)run_total+" \t|"+(int)pass_total+" \t|"+(int)fail_total+" \t|"+(int)apass_total+" \t|"+(int)iprog_total+" \t|"+(int)kdiff_total+" \t|"+RTP_tot+"% |"+PTP_tot+"% |");
					
					rs.close();
					stmt.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	private static int getDRChoice() {
		boolean keeprunning = true;
		int catCh=0;
		
		try{
			while(keeprunning){
		
				System.out.println("View test results: ");
				System.out.println("1. Test suite wise\n2. Qaowner wise\n0. Return to main menu\nEnter your choice - 1 / 2 / 0?");
				int ch = Integer.parseInt(brreader.readLine());
	
					switch(ch){
						case 0:catCh=0;keeprunning=false; break;
						case 1: catCh=1;keeprunning=false; break;
						case 2: catCh=2;keeprunning=false;  break;
						default: System.out.println("Invalid choice Enter again friend!"); 
					}
		}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return catCh;
	}
	public static double round(double value, int places) {

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
	private static int getFailedCategoryChoice() {
		boolean keeprunning = true;
		int catCh=0;
		
		try{
			while(keeprunning){
		
				System.out.println("Fetch failed tests menu: ");
				System.out.println("1. All Failed Tests\n2. Tests with Pending Analysis\n3. Tests with Analysis Done\n0. Go back to main menu\nEnter your choice - 1 / 2 / 3 /0?");
				int ch = Integer.parseInt(brreader.readLine());
	
					switch(ch){
						case 0: catCh=0;keeprunning=false; break;
						case 1: catCh=1;keeprunning=false; break;
						case 2: catCh=2;keeprunning=false;  break;
						case 3: catCh=3;keeprunning=false;  break;
						default: System.out.println("Invalid choice Enter again friend!"); 
					}
		}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return catCh;
		
	}
	private static String getOwner() {
		boolean keeprunning = true;
		String qaowner="";
		try{
			while(keeprunning){
		
				System.out.println("Select owner of test: ");
				System.out.println("1. Bhaskar\n2. Neethi\n3. Apurva\n4. DeepakM\n5. Lakshmi\n6. Shriram\n7. Suganya\n8. KD\n9. Sagar\n10. TBD\n11. NaveenJP\n12. Selvi\nEnter your choice - 1..12?");
				int ch = Integer.parseInt(brreader.readLine());
	
					switch(ch){
						case 1: qaowner = "BHASKAR"; keeprunning=false; break;
						case 2: qaowner = "NEETHI";keeprunning=false;  break;
						case 3: qaowner = "APURVA";keeprunning=false;  break;
						case 4: qaowner = "DEEPAKM";keeprunning=false;  break;
						case 5: qaowner = "LAKSHMI"; keeprunning=false; break;
						case 6: qaowner = "SHRIRAM";keeprunning=false;  break;
						case 7: qaowner = "SUGANYA";keeprunning=false;  break;
						case 8: qaowner = "KD";keeprunning=false;  break;
						case 9: qaowner = "SAGAR"; keeprunning=false; break;
						case 10: qaowner = "TBD";keeprunning=false;  break;
						case 11: qaowner = "NAVEENJP";keeprunning=false;  break;
						case 12: qaowner = "SELVI";keeprunning=false;  break;
						default: System.out.println("Invalid choice Enter again friend!"); 
					}
		}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return qaowner;
	}

	private static void createView() {
		Statement st=null;
		String query_view="CREATE VIEW "+viewname_with_owners+" AS "
				+ "SELECT suite_name, category_name, qaowner,"
				+ "SUM(CASE WHEN status = \'NOT PLAN\' THEN 1 ELSE 0 END) AS NotPlan,"
				+ "SUM(CASE WHEN status = \'IN PLAN\' THEN 1 ELSE 0 END) as Inplan,"
				+ "SUM(CASE WHEN status = \'FAIL\' THEN 1 ELSE 0 END) as Fail,"
				+ "SUM(CASE WHEN status = \'APASS\' THEN 1 ELSE 0 END) as APass,"
				+ "SUM(CASE WHEN status = \'INPROG\' THEN 1 ELSE 0 END) as InProg,"
				+ "SUM(CASE WHEN status = \'PASS\' THEN 1 ELSE 0 END) as Pass,"
				+ "SUM(CASE WHEN status = \'KNOWNDIFF\' THEN 1 ELSE 0 END) as KDiff "
				+ "FROM "+catalog+"."+schema+"."+tablename+" "
				+ "GROUP BY suite_name, category_name, qaowner ;";
		try {
			st = con.createStatement();
			try{
				st.executeUpdate("drop view "+viewname_with_owners);
			}catch (SQLException e) {
				System.out.println(e.getMessage());
			}
			
			st.executeUpdate(query_view);
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		query_view="CREATE VIEW "+viewname_without_owners+" AS "
				+ "SELECT suite_name, "
				+ "SUM(CASE WHEN status = \'NOT PLAN\' THEN 1 ELSE 0 END) as Notplan,"
				+ "SUM(CASE WHEN status = \'IN PLAN\' THEN 1 ELSE 0 END) as InPlan,"
				+ "SUM(CASE WHEN status = \'FAIL\' THEN 1 ELSE 0 END) as Fail,"
				+ "SUM(CASE WHEN status = \'APASS\' THEN 1 ELSE 0 END) as APass,"
				+ "SUM(CASE WHEN status = \'INPROG\' THEN 1 ELSE 0 END) as InProg,"
				+ "SUM(CASE WHEN status = \'PASS\' THEN 1 ELSE 0 END) as Pass,"
				+ "SUM(CASE WHEN status = \'KNOWNDIFF\' THEN 1 ELSE 0 END) as KDiff "
				+ "FROM "+catalog+"."+schema+"."+tablename+" "
				+ "GROUP BY suite_name ;";
		try {
			st = con.createStatement();
			try{
				st.executeUpdate("drop view "+viewname_without_owners);
			}catch (SQLException e) {
			}
			
			st.executeUpdate(query_view);
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		try {
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
				
	} 
	private static void exportFailedTestCasesAnalysisPending(String output_file_location) {
		try{
			con = DriverManager.getConnection(url, props);
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			
    		Statement st= con.createStatement();

    		String qaowner=getOwner();
			String output_file;
			String query = "select suite_name,category_name,testunit_name,testcase_name,build_id,status,analysis from "+catalog+"."+schema+"."+tablename
					+" where qaowner=\'"+qaowner+"\' and status in (\'FAIL\') and analysis in (\'\',\'TBA\')";
			
			
			output_file = output_file_location+"/"+qaowner+"_failedTestsWithoutAnalysis_"+mode+".txt";
		
			File fout = new File(output_file);
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			ResultSet rs = st.executeQuery(query);
			
			String suite_name,category_name, testunit_name, testcase_name, build_id,status,analysis;
			while(rs.next()) {
				
				suite_name = rs.getString(1);
				category_name= rs.getString(2);
				testunit_name = rs.getString(3);
				
				testcase_name= rs.getString(4);
				build_id = rs.getString(5);
				status = rs.getString(6);
				analysis=rs.getString(7);
				
				bw.write(suite_name+"|"+category_name+"|"+testunit_name+"|"+testcase_name+"|"+build_id+"|"+status+"|"+analysis+"|");
				bw.newLine();
			}
				 
			bw.close();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
		
	}


	private static void exportFailedTestCasesWithAnalysisDone(String output_file_location) {
		try{
			con = DriverManager.getConnection(url, props);
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			
    		Statement st= con.createStatement();

    		String qaowner=getOwner();
			String query;
			String output_file;
			
			query = "select suite_name,category_name,testunit_name,testcase_name,build_id,status,analysis from "+catalog+"."+schema+"."+tablename+" where qaowner=\'"+qaowner+"\' and status in (\'FAIL\',\'APASS\',\'KDIFF\') and analysis not in (\'\',\'TBA\')";
			output_file = output_file_location+"/"+qaowner+"_failedTestsWithAnalysis_"+mode+".txt";
		
			File fout = new File(output_file);
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			ResultSet rs = st.executeQuery(query);
			
			String suite_name,category_name, testunit_name, testcase_name, build_id,status,analysis;
			while(rs.next()) {
				
				suite_name = rs.getString(1);
				category_name= rs.getString(2);
				testunit_name = rs.getString(3);
				testcase_name= rs.getString(4);
				build_id = rs.getString(5);
				status = rs.getString(6);
				analysis = rs.getString(7);
				bw.write(suite_name+"|"+category_name+"|"+testunit_name+"|"+testcase_name+"|"+build_id+"|"+status+"|"+analysis+"|");
				bw.newLine();
			}
				 
			bw.close();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
		
	}


	private static void exportAllFailedTestCases(String output_file_location) {
		try{
			con1 = DriverManager.getConnection(url, props);
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			
    		Statement st= con1.createStatement();
    		String qaowner=getOwner();
			String output_file;
			String query = "select suite_name,category_name,testunit_name,testcase_name,build_id,status,analysis from "+catalog+"."+schema+"."+tablename
					+" where qaowner=\'"+qaowner+"\' and status in (\'FAIL\',\'APASS\',\'KDIFF\')";
			
			output_file = output_file_location+"/"+qaowner+"_allFailedTests_"+mode+".txt";
		
			File fcheck = new File(output_file);
			Timestamp ts = new Timestamp(new Date().getTime());
			if(fcheck.exists()) {
				//System.out.println("OUT PUT FILE PRESENT ALREADY ts is:"+ts);
				fcheck.renameTo(new File(output_file+ts));
			}
			//fcheck.close();
			
			File fout = new File(output_file); 
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			ResultSet rs = st.executeQuery(query);
			
			String suite_name,category_name, testunit_name, testcase_name, build_id,status,analysis;
			while(rs.next()) {
				
				suite_name = rs.getString(1);
				category_name= rs.getString(2);
				testunit_name = rs.getString(3);
				testcase_name= rs.getString(4);
				build_id = rs.getString(5);
				status = rs.getString(6);
				analysis = rs.getString(7);
				bw.write(suite_name+"|"+category_name+"|"+testunit_name+"|"+testcase_name+"|"+build_id+"|"+status+"|"+analysis+"|");
				bw.newLine();
			}
				 
			bw.close();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
		
	}

	private static void updateAnalysisThroughFile(String input_file) {
		BufferedReader br; Statement st;
		String suite_name="",category_name="", testunit_name="", testcase_name="", build_id="",status="",comments="";
		String query;String stat="";
		ArrayList<String> DBupdateFailed = new ArrayList<String>();
		try{
			con = DriverManager.getConnection(url, props);
		}catch(Exception e){
			e.printStackTrace();
		}
		try {
    			br = new BufferedReader(new FileReader(input_file));
    			String line = br.readLine();
    			while (line != null) {
    				//System.out.println(line);
    				try{
						String[] arr = line.split("\\|",-1);
						suite_name = arr[0].trim();
						category_name= arr[1].trim();
						testunit_name = arr[2].trim();
						testcase_name= arr[3].trim();
						build_id = arr[4].trim();
						status = arr[5].trim();
						status=status.toUpperCase().trim();
						comments = arr[6].trim();
						
						st = con.createStatement();
						query = "select status from "+catalog+"."+schema+"."+tablename+" where suite_name=\'"+suite_name+"\' and category_name =\'"+category_name+"\' and testunit_name =\'"+testunit_name+"\' and testcase_name =\'"+testcase_name+"\' and build_id =\'"+build_id+"\' ";
						ResultSet rs_status = st.executeQuery(query);
						while(rs_status.next())
							stat = rs_status.getObject(1).toString();
						rs_status.close();
					
							
						if(status.equals("APASS")==false && status.equals("FAIL")==false && status.equals("KDIFF")==false){
							DBupdateFailed.add("\nSuite: "+suite_name+"; Category: "+category_name+"; Unit: "+testunit_name+"; Testcases: "+testcase_name+" \n ----- Update failed: status values not in (APASS,FAIL,KDIFF)");
							line = br.readLine();
							continue;
						}
						if(status.equals("NOT PLAN") || status.equals("INPROG") || status.equals("IN PLAN") || status.equals("PASS")){
							DBupdateFailed.add("\nSuite: "+suite_name+"; Category: "+category_name+"; Unit: "+testunit_name+"; Testcases: "+testcase_name+" \n ----- Update failed: status values not in (APASS,FAIL,KDIFF)");
							line = br.readLine();
							continue;
						}
						if(status.equals("KDIFF") || stat.equals("KDIFF")){
							line = br.readLine();
							continue;
						}
						if(stat.equals("PASS")){
							line = br.readLine();
							continue;
						}
						if(status.equals("APASS") && (comments.equals("TBA") || comments.equals(""))){
							DBupdateFailed.add("\nSuite: "+suite_name+"; Category: "+category_name+"; Unit: "+testunit_name+"; Testcases: "+testcase_name+" \n ----- Update failed: Comments need to be added when status=APASS ");
							line = br.readLine();
							continue;
						}
							
						if(testcase_name.equalsIgnoreCase("ALL")==true) {
							query = "update "+catalog+"."+schema+"."+tablename+" set status=\'"+status+"\' , analysis=\'"+comments+"\' "+
									"where suite_name=\'"+suite_name+"\' and category_name=\'"+category_name+"\' and testunit_name=\'"+testunit_name+"\' and"+
									" build_id=\'"+build_id+"\' ";
							try{
								st.executeUpdate(query);
							}catch(SQLException e) {
								//System.out.println(e.getMessage());
								DBupdateFailed.add("\nSuite: "+suite_name+"; Category: "+category_name+"; Unit: "+testunit_name+"; Testcases: ALL"+" \n ----- Update failed: "+e.getMessage());
							}
							
						}else if (testcase_name.contains(",")==false) {
							query = "update "+catalog+"."+schema+"."+tablename+" set status=\'"+status+"\' , analysis=\'"+comments+"\' "+"where suite_name=\'"+suite_name+"\' and category_name=\'"+category_name+"\' and testunit_name=\'"+testunit_name+"\' and"+" testcase_name=\'"+testcase_name+"\' and build_id=\'"+build_id+"\' ";
							try{
								st.executeUpdate(query);
							}catch(SQLException e) {
								//System.out.println(e.getMessage());
								DBupdateFailed.add("\nSuite: "+suite_name+"; Category: "+category_name+"; Unit: "+testunit_name+"; Testcases: "+testcase_name+" \n ----- Update failed: "+e.getMessage());
							}
							
						}else {
							String[] testcase_arr = testcase_name.split(",");
							for(int k =0; k<testcase_arr.length;k++) {
								query = "update "+catalog+"."+schema+"."+tablename+" set status=\'"+status+"\' , analysis=\'"+comments+"\' "+
										"where suite_name=\'"+suite_name+"\' and category_name=\'"+category_name+"\' and testunit_name=\'"+testunit_name+"\' and"+
										" testcase_name=\'"+testcase_arr[k]+"\' and build_id=\'"+build_id+"\' ";
								
								try{
									st.executeUpdate(query);
								}catch(SQLException e) {
									//System.out.println(e.getMessage());
									DBupdateFailed.add("\nSuite: "+suite_name+"; Category: "+category_name+"; Unit: "+testunit_name+"; Testcases: "+testcase_arr[k]+" \n ----- Update failed: "+e.getMessage());
								}
							}
						}
					}catch (Exception e) {
						System.out.println(e.getMessage());
						DBupdateFailed.add("\nSuite: "+suite_name+"; Category: "+category_name+"; Unit: "+testunit_name+"; Testcases: ALL"+" \n ----- Update failed: "+e.getMessage());
					}
	    		line = br.readLine();
    			}
    			br.close();
    		} catch (Exception e) {
    			System.out.println(e.getMessage());
    		}
			if(DBupdateFailed.size()==0)
				System.out.println("DB update passed for all the testcases! ");
			else {
				System.out.println("DB update failed for the following testcases: ");
				for(int cnt=0;cnt<DBupdateFailed.size();cnt++)
					System.out.println(DBupdateFailed.get(cnt));
			}
		
	}
}
