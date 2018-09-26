package scheduler.db;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.ConnectionInstance;
import com.rethinkdb.net.Cursor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SchedulerInitializer implements InitializingBean {
    @Autowired
    private RethinkDBConnectionFactory connectionFactory;

    @Autowired
    private DeviceAvailabilityChangesListener deviceAvailabilityChangesListener;

    private static final RethinkDB r = RethinkDB.r;

    @Override
    public void afterPropertiesSet() throws Exception {
        registerBootedDevices();
        
    }

    private void registerBootedDevices() {
    	Connection<ConnectionInstance> connection = connectionFactory.createConnection();
        List<String> dbList = r.dbList().run(connection);

        if(dbList.contains("stf")) {
        	List<String> tables = r.db("stf").tableList().run(connection);
            
            if ( tables.contains("devices")) {
            	Cursor serialList = r.db("stf").table("devices").g("serial").run(connection);
            	//obj.g("owner");
            	
            	if(serialList != null)
	            	for (Object serial : serialList) {
	            		deviceAvailabilityChangesListener.registerDeviceListener(serial);
	            	    System.out.println("doc = " + serial);
	            	}
            	
            }
        	
        	
        }
    }
}
