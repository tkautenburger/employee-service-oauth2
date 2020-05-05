package de.legendlime.EmployeeService.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import de.legendlime.EmployeeService.config.redis.DepartmentRedisRepository;

@EnableBinding(Sink.class)
public class DataChangeHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(DataChangeHandler.class);
	private static final String OBJECT_NAME = "Department";
	
	@Autowired
	DepartmentRedisRepository repo;
	
	@StreamListener("auditing")
	public void departmentChangeHandler(AuditRecord record) {
		if (record.getObjectType().endsWith(OBJECT_NAME)) {
			switch (record.getMethod()) {
			case "GET":
				break;
			case "CREATE":
				LOG.info("department created");
				break;
			case "UPDATE":
				LOG.info("department updated");
				repo.deleteDepartment(record.getObjectId());
				break;
			case "DELETE":
				LOG.info("department deleted");
				repo.deleteDepartment(record.getObjectId());
				break;
			default:
				// unknown action
				break;
			}
		}
	}

}
