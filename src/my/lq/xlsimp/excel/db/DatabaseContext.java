package my.lq.xlsimp.excel.db;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


public final class DatabaseContext extends InitialContext {

    public DatabaseContext() throws NamingException {}

    @Override
    public Object lookup(String name) throws NamingException
    {
        try {
            //our connection strings
            Class.forName("com.mysql.cj.jdbc.Driver");
            DataSource ds1 = new LocalDataSource("jdbc:mysql://localhost:3306/XLSIMPTEST?profileSQL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "xlsimptest","xlsimptest");

            Properties prop = new Properties();
            prop.put("jdbc/ds1", ds1);
            
            Object value = prop.get(name);
            return (value != null) ? value : super.lookup(name);
        }
         catch(Exception e) {
             System.err.println("Lookup Problem " + e.getMessage());
             e.printStackTrace();
         }  
         return null;            
    }        
    
}