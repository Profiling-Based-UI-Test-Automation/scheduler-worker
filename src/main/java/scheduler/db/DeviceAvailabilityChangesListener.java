package scheduler.db;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Cursor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DeviceAvailabilityChangesListener {
    protected final Logger log = LoggerFactory.getLogger(DeviceAvailabilityChangesListener.class);

    private static final RethinkDB r = RethinkDB.r;

    @Autowired
    private RethinkDBConnectionFactory connectionFactory;

    @Async
    public void registerDeviceListener(Object serial) {
    	
    	Cursor changeCursor = r.db("stf").table("devices").filter(row -> row.g(serial).eq("0305e0a908e51ee4")).pluck("owner").changes().run(connectionFactory.createConnection());//.g("serial")
    	
    	for (Object change : changeCursor) {
    		// queue에서 test를 꺼내 컨테이너를 생성해 태운다.
    	    System.out.println(change);
    	}
    	
    }

}
