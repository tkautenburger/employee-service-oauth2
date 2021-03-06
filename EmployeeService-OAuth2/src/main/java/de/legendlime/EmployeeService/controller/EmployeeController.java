package de.legendlime.EmployeeService.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import de.legendlime.EmployeeService.config.logging.ResponseLoggingFilter;
import de.legendlime.EmployeeService.config.oauth2.OAuth2RestTemplateBean;
import de.legendlime.EmployeeService.config.oauth2.SecurityContextUtils;
import de.legendlime.EmployeeService.config.opa.OPAProperties;
import de.legendlime.EmployeeService.config.redis.RedisProperties;
import de.legendlime.EmployeeService.domain.CachedDepartment;
import de.legendlime.EmployeeService.domain.Department;
import de.legendlime.EmployeeService.domain.Employee;
import de.legendlime.EmployeeService.domain.EmployeeDTO;
import de.legendlime.EmployeeService.messaging.AuditRecord;
import de.legendlime.EmployeeService.messaging.AuditSourceBean;
import de.legendlime.EmployeeService.repository.DepartmentRedisRepository;
import de.legendlime.EmployeeService.repository.EmployeeRepository;
import io.micrometer.core.annotation.Timed;
import io.opentracing.Tracer;

@RestController
@RequestMapping(value = "v1")
@Timed
public class EmployeeController {

	private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);
	private final static String SERVICE_PORT = "8090";
	private final static String SERVICE_HOST = "department-service-oauth2";
	private final static String URI = "https://" + SERVICE_HOST + ":" + SERVICE_PORT + "/v1/departments/{deptId}";

	private final static String NOT_FOUND = "Employee not found, ID: ";
	private final static String NOT_NULL = "Employee cannot be null";

	@Autowired
	private EmployeeRepository repo;

	@Autowired
	DataSourceProperties dsProperties;

	@Autowired
	private OAuth2RestTemplateBean oauth2RestTemplateBean;

	@Autowired
	Tracer tracer;

	@Autowired
	AuditSourceBean audit;
	
	@Autowired
	RedisProperties redisProperties;

	@Autowired
	DepartmentRedisRepository redisRepository;
	
	@Autowired
	SecurityContextUtils securityContextUtils;
	
	@Autowired
	OPAProperties opaProperties;

	
	/*--------------------------------*
	 * Controller actions             *
	 *--------------------------------*/

	@GetMapping(value = "/employees", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Employee> getAll(HttpServletRequest request, HttpServletResponse response) {

		LOG.debug("request GET /employees");
		audit.publishAuditMessage(auditHelper("GET", null, request, response));
		return repo.findAll();
	}

	@GetMapping(value = "/employees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Employee getSingle(@PathVariable(name = "id", required = true) Long id, HttpServletRequest request,
			HttpServletResponse response) {

		LOG.debug("request GET /employees/{}", id);
		Optional<Employee> empOpt = repo.findById(id);
		if (!empOpt.isPresent()) {
			LOG.error(NOT_FOUND, id);
			throw new ResourceNotFoundException(NOT_FOUND + id);
		}

		Employee e = empOpt.get();
		Department d = this.getDepartment(e.getDeptId());
		if (d != null) {
			e.setDeptName(d.getName());
			e.setDeptDesc(d.getDescription());
			e.setDeptPodServed(d.getPodServed());
		}
		audit.publishAuditMessage(auditHelper("GET", e, request, response));
		return e;
	}

	@PostMapping(value = "/employees", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Employee create(@Valid @RequestBody EmployeeDTO emp, HttpServletRequest request,
			HttpServletResponse response) {

		if (emp == null) {
			LOG.error(NOT_NULL);
			throw new IllegalArgumentException(NOT_NULL);
		}

		// map DTO, direct use of entity leads to security vulnerability
		Employee persistentEmp = new Employee();
		persistentEmp.setEmpId(emp.getEmpId());
		persistentEmp.setFirstname(emp.getFirstname());
		persistentEmp.setLastname(emp.getLastname());
		persistentEmp.setDeptId(emp.getDeptId());
		LOG.debug("request POST /employees, body:", persistentEmp.toString());

		audit.publishAuditMessage(auditHelper("CREATE", persistentEmp, request, response));

		return repo.save(persistentEmp);
	}

	@PutMapping(value = "/employees/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Employee update(@Valid @RequestBody EmployeeDTO emp, @PathVariable(name = "id", required = true) Long id,
			HttpServletRequest request, HttpServletResponse response) {

		Optional<Employee> empOpt = repo.findById(id);
		if (!empOpt.isPresent()) {
			LOG.error(NOT_FOUND, id);
			throw new ResourceNotFoundException(NOT_FOUND + id);
		}
		Employee e = empOpt.get();
		e.setEmpId(emp.getEmpId());
		e.setFirstname(emp.getFirstname());
		e.setLastname(emp.getLastname());
		e.setDeptId(emp.getDeptId());
		LOG.debug("request PUT /employees/{}, body: {}", id, e.toString());

		audit.publishAuditMessage(auditHelper("UPDATE", e, request, response));

		return repo.save(e);
	}

	@DeleteMapping(value = "/employees/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<?> delete(@PathVariable(name = "id", required = true) Long id, HttpServletRequest request,
			HttpServletResponse response) {

		Optional<Employee> empOpt = repo.findById(id);
		if (!empOpt.isPresent()) { 
			LOG.error(NOT_FOUND, id);
			throw new ResourceNotFoundException(NOT_FOUND + id);
		}
		repo.delete(empOpt.get());
		LOG.debug("request DELETE /employees/{}", id);
		
		audit.publishAuditMessage(auditHelper("DELETE", empOpt.get(), request, response));

		return ResponseEntity.ok().build();
	}
	
	
	/*--------------------------------*
	 * Downstream REST client methods *
	 *--------------------------------*/
	
	// Circuit Breaker does not seem to work, maybe because its not a @Component or @Service annotated class

	@HystrixCommand(fallbackMethod = "reliableDepartment")
	public Department getDepartment(Long deptId) {
		
		Department dept;

		// check cache if enabled and retrieve object from there if found
		if (redisProperties.isEnabled()) {
			dept = checkRedisCache(deptId);
			if (dept != null) {
				LOG.info("Got department object with ID {} from Redis cache", dept.getDeptId());
				return dept;
			}
		}		
		// department not cached or cache disabled, get object from downstream service
		ResponseEntity<Department> restExchange = oauth2RestTemplateBean.getoAuth2RestTemplate().exchange(URI,
				HttpMethod.GET, null, Department.class, deptId);
		
		dept = restExchange.getBody();
		
		// save object in cache if enabled
		if (redisProperties.isEnabled() && dept != null) {
			// get authorities header with role from last OPA response
			List<String> authorities = restExchange.getHeaders().get("policy-authority");
			// get current policy version from last OPA response
			List<String> versions = restExchange.getHeaders().get("policy-version");
			if (authorities != null) {
				cacheDepartmentObject(dept, authorities.get(0), versions.get(0));
				LOG.info("Save department object with ID {} in Redis cache", dept.getDeptId());
			}
		}
		return dept;
	}
	
	public Department reliableDepartment(Long deptId) {
		return new Department(999, "Department Service not available", "Department Service not available");
	}

	
	/*--------------------------------*
	 * Caching methods                *
	 *--------------------------------*/

	private Department checkRedisCache(long deptId) {
		try {
			CachedDepartment cDept = redisRepository.findDepartment(deptId);
			if (cDept == null) {
				// entry is not in cache and must be retrieved from downstream service
				return null;
			}
			// check first the policy version, if not equal to cached version, evict all
			// cache entries
			if (!cDept.getVersion().equals(opaProperties.getPolicyVersion())) {
				LOG.debug("Authorization policy version has changed. Evict all cache entries.");
				redisRepository.deleteAll();
				return null;
			}
			// check the authorities from security context if it contains the cached role
			// for the entry
			Set<String> userRoles = SecurityContextUtils.getUserRoles();
			if (userRoles.contains(cDept.getRole()) == true) {
				LOG.debug("Cached object access authorized with role {}.", cDept.getRole());
				return cDept.getDepartment();
			} else {
				LOG.debug("Cached object access not authorized for client authorities {}. Granted authority is {}", 
						userRoles, cDept.getRole());
				return null;
			}
		} catch (Exception e) {
			LOG.error("Error while trying to retrieve department {} from cache.  Exception {}", deptId, e);
			return null;
		}
	}

	private void cacheDepartmentObject(Department dept, String role, String version) {
		try {
			redisRepository.saveDepartment(new CachedDepartment(dept, role, version));
		} catch (Exception e) {
			LOG.error("Unable to cache department object with ID {}. Exception {}", dept.getDeptId(), e);
		}
	}
	
	/*--------------------------------*
	 * Auditing methods               *
	 *--------------------------------*/
	private AuditRecord auditHelper(String method, Employee obj, HttpServletRequest request,
			HttpServletResponse response) {

		AuditRecord record = new AuditRecord();

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		record.setTimestamp(timestamp.toInstant().toString());

		record.setNodeName(System.getenv("NODE_NAME"));
		record.setHostName(System.getenv("HOSTNAME"));
		record.setPodName(System.getenv("POD_NAME"));

		record.setMethod(method);
		record.setUri(request.getRequestURI());
		record.setClient(request.getRemoteAddr());

		String user = request.getRemoteUser();
		if (user != null) {
			record.setUser(user);
		}
		HttpSession session = request.getSession(false);
		if (session != null) {
			record.setSessionId(session.getId());
		}
		record.setTraceId(response.getHeader(ResponseLoggingFilter.TRACE_ID));
		if (obj != null) {
			record.setObjectType(obj.getClass().getName());
			record.setObjectId(obj.getEmpId());
		}
		if (obj != null && ("CREATE".equalsIgnoreCase(method) || "UPDATE".equalsIgnoreCase(method))) {
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			// Converting the Object to JSONString
			try {
				record.setJsonObject(mapper.writeValueAsString(obj));
			} catch (JsonProcessingException e) {
				LOG.error("Error converting audit employee object with ID {} to JSON string. Exception {}",
						obj.getEmpId(), e);
			}
		}
		return record;
	}
}
