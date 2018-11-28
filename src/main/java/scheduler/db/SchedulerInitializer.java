package scheduler.db;

import com.rethinkdb.RethinkDB;
import org.springframework.amqp.core.Queue;

import com.rethinkdb.net.Connection;
import com.rethinkdb.net.ConnectionInstance;
import com.rethinkdb.net.Cursor;

import scheduler.db.RabbitMQManagement;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SchedulerInitializer implements InitializingBean {
    @Autowired
    private RethinkDBConnectionFactory connectionFactory;
    
    @Autowired
    private RabbitMQManagement rabbitmq ;

    @Autowired
    private DeviceAvailabilityChangesListener deviceAvailabilityChangesListener;

    private static final RethinkDB r = RethinkDB.r;
    
    private static List<String> listenerlist = new ArrayList<String>();
    
    
    @Scheduled(fixedRate = 10000, initialDelay = 20000)
    public void scheduleTaskWithFixedRate() {
        Connection<ConnectionInstance> connection = connectionFactory.createConnection();
        List<String> dbList = r.dbList().run(connection);
         if(dbList.contains("stf")) {
        	List<String> tables = r.db("stf").tableList().run(connection);
            
            if ( tables.contains("devices")) {
            	Cursor serialList = r.db("stf").table("devices").g("serial").run(connection);
            	
            	if(serialList != null)
	            	for (Object serial : serialList) {
	            		
	            		// 연결된 디바이스 중 큐에 등록되지 않은 디바이스를 큐에 등록한다.
	            		if(!listenerlist.contains(serial)) {
	            			rabbitmq.addQueue((String) serial);
	            			//Queue queue = new Queue((String) serial, true, false, false);
	            			deviceAvailabilityChangesListener.registerDeviceListener(serial);
	            			listenerlist.add((String) serial);
	            		}
	            	}
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    	
    	rabbitmq.initialize();
    	List<Queue> list = rabbitmq.getQueueList();
    	
    	// 현재 등록된 모든 큐를 가져온다. 	
    	for(int i = 0 ; i < list.size() ; ++ i) {
    		String serial = list.get(i).getName();
    		if( serial != null ) {
    			registerBootedDevices(serial);
    			listenerlist.add(serial);
    		}
    	}
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
            	
            	if(serialexist != null)
            		deviceAvailabilityChangesListener.registerDeviceListener(serial);
            }
        }
    }
}
