package scheduler.db;

import com.rethinkdb.RethinkDB;

import com.rethinkdb.net.Connection;
import com.rethinkdb.net.ConnectionInstance;
import com.rethinkdb.net.Cursor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;

import java.util.List;

public class SchedulerInitializer implements InitializingBean {
    @Autowired
    private RethinkDBConnectionFactory connectionFactory;

    @Autowired
    private DeviceAvailabilityChangesListener deviceAvailabilityChangesListener;

    private static final RethinkDB r = RethinkDB.r;

    @Override
    public void afterPropertiesSet() throws Exception {
    	
    	RabbitManagementTemplate rmt = new RabbitManagementTemplate("http://localhost:15672/api/", "guest", "guest");

    	List<Queue> list = rmt.getQueues();
    	
    	for(int i = 0 ; i < list.size() ; ++ i) {
    		String serial = list.get(i).getName();
    		if( serial != null )
    			registerBootedDevices(serial);
    	}
    	
    	System.out.println("====================== list.length = " + list.size());
    	
        
        
    }

    private void registerBootedDevices(String serial) {
    	Connection<ConnectionInstance> connection = connectionFactory.createConnection();
        List<String> dbList = r.dbList().run(connection);

        if(dbList.contains("stf")) {
        	List<String> tables = r.db("stf").tableList().run(connection);
            
            if ( tables.contains("devices")) {
            	Cursor serialexist = r.db("stf").table("devices").filter(row -> row.g("serial").eq(serial)).run(connection);
            	//obj.g("owner");
            	
            	if(serialexist != null)
            		deviceAvailabilityChangesListener.registerDeviceListener(serial);
            }
        }
    }
}
