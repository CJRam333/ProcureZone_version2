/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plant.sap.cronScheduler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import seeds.dbconfig.Util.Utils;

/**
 *
 * @author ramesh.a
 */
public class CsvImporter {
    
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/seeds_indent";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "password";
    
    
    public static void main(String[] args) throws IOException, SQLException {
//        String csvFile = "path/to/csvfile.csv";
        Utils utils = new Utils();
        String csvFile = "E:/Plant_Indent(" + utils.DateIn() + ").csv";
//        String tableName = "mytable";
//        String insertSql = "INSERT INTO " + tableName + " (col1, col2, col3) VALUES (?, ?, ?)";
        String insertSql = "INSERT INTO pz_schedule_sap_material_master (company_code, plant_code, storage_location,material_code,"
                    + "material_desc,material_uom,batch,quantity,material_type,"
                    + "material_group,material_group_desc,variety_type,variety,"
                    + "crop_type,crop_group,stl,odv,got,elisa,status,"
                    + "mat_code_id,SDCLS,STATS,SKIPD,"
                    + "INSPDT,MOISTURE,PURE_SEED,INERT_MATTER,OCS_COUNT,WEED_SEED_COUNT,GRAIN,"
                    + "BLACK_SEEDS,PINHOLE_SEEDS,ODV_RES,BULK_DENSITY,THSW,COLD_VIGOUR_GERM_NORMAL,"
                    + "FIRST_COUNT_NORMAL,GERM_NORMAL,FET_NORMAL,SOIL_COUNT_DAYS,AAV_GERM_NORMAL,"
                    + "GOT_GP,GOT_FEMALE,GOT_OTHERS,BG1,BG2,HT,FQR,"
                    + "stp_one,lmd) VALUES (?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,"
                    + "?,?,?,?,?,?,"
                    + "?,?,?,?,?,?,?,"
                    + "?,?)";

       BufferedReader br = new BufferedReader(new FileReader(csvFile));
             Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(insertSql); 

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (hasNullOrEmpty(values)) {
                    // Skip the line if any value is null or empty
                    continue;
                }
                // Insert the data into the database
                stmt.setInt(1, Integer.parseInt(values[0]));
                stmt.setInt(2, Integer.parseInt(values[1]));
                stmt.setDouble(3, Double.parseDouble(values[2]));
                
                
                
                stmt.setInt(1, Integer.parseInt(values[0]));
                stmt.setInt(2, Integer.parseInt(values[1]));
                stmt.setString(3, values[2]);
                stmt.setString(4, values[3]);
                stmt.setString(5, values[4]);
                stmt.setString(6, values[5]);
                stmt.setString(7, values[6]);
                stmt.setDouble(8,  Double.parseDouble(values[7]));
                stmt.setString(9, values[8]);
                stmt.setInt(10, Integer.parseInt(values[9]));
                stmt.setString(11, values[10]);
                stmt.setString(12, values[11]);
                stmt.setString(13, values[12]);
                stmt.setString(14, values[13]);
                stmt.setString(15, values[14]);
                stmt.setString(16, values[15]);
                stmt.setString(17, values[16]);
                stmt.setString(18, values[17]);
                stmt.setString(19, values[18]);
                stmt.setInt(20, Integer.parseInt(values[19]));
                stmt.setInt(21, Integer.parseInt(values[20]));
                stmt.setString(22, values[21]);
                stmt.setString(23, values[22]);
                stmt.setString(24, values[23]);
                stmt.setString(25, values[24]);
                stmt.setString(26, values[25]);
                stmt.setString(27, values[26]);
                stmt.setString(28, values[27]);
                stmt.setString(29, values[28]);
                stmt.setString(30, values[29]);
                stmt.setString(31, values[30]);
                stmt.setString(32, values[31]);
                stmt.setString(33, values[32]);
                stmt.setString(34, values[33]);
                stmt.setString(35, values[34]);
                stmt.setString(36, values[35]);
                stmt.setString(37, values[36]);
                stmt.setString(38, values[37]);
                stmt.setString(39, values[38]);
                stmt.setString(40, values[39]);
                stmt.setString(41, values[40]);
                stmt.setString(42, values[41]);
                
                stmt.setString(43, values[42]);
                stmt.setString(44, values[43]);
                stmt.setString(45, values[44]);
                stmt.setString(46, values[45]);
                stmt.setString(47, values[46]);
                stmt.setString(48, values[47]);
                stmt.setString(49, values[48]);
                stmt.setString(50, values[49]);
                stmt.setString(51, values[50]);
                
                
                
                
                
                
                
                
                stmt.executeUpdate();
            }
        
    }

    private static boolean hasNullOrEmpty(String[] values) {
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
}
