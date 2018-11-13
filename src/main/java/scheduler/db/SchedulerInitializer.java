package scheduler.db;

import com.rethinkdb.RethinkDB;

import com.rethinkdb.net.Connection;
import com.rethinkdb.net.ConnectionInstance;
import com.rethinkdb.net.Cursor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;

import java.util.ArrayList;
import java.util.List;

public class SchedulerInitializer implements InitializingBean {
    @Autowired
    private RethinkDBConnectionFactory connectionFactory;

    @Autowired
    private DeviceAvailabilityChangesListener deviceAvailabilityChangesListener;

    private static final RethinkDB r = RethinkDB.r;
    
    private static List<String> listenerlist = new ArrayList<String>();

    @Override
    public void afterPropertiesSet() throws Exception {
    	
    	// 현재 등록된 모든 큐를 가져온다. 
    	RabbitManagementTemplate rmt = new RabbitManagementTemplate("http://localhost:15672/api/", "guest", "guest");

    	List<Queue> list = rmt.getQueues();
    	
    	for(int i = 0 ; i < list.size() ; ++ i) {
    		String serial = list.get(i).getName();
    		if( serial != null ) {
    			registerBootedDevices(serial);
    			listenerlist.add(serial);
    		}
    	}
    	
    	System.out.println("====================== list.length = " + list.size());
    	
        
        
    }

    //파라미터로 전달된 큐 이름이 stf device에 목록 상에 있는지 확인하고, 있는 경우 서비스를 실행시킨다. 
    //서비스는 해당 stf의 device에 대한 chagefeed를 걸어, 디바이스가 유휴해지거나 예약되는 것에 대한 알림을 받는다. 
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
