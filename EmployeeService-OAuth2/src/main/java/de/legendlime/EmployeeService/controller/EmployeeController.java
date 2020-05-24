package de.legendlime.EmployeeService.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
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

import de.legendlime.EmployeeService.config.logging.ResponseLoggingFilter;
import de.legendlime.EmployeeService.config.oauth2.OAuth2RestTemplateBean;
import de.legendlime.EmployeeService.config.oauth2.SecurityContextUtils;
import de.legendlime.EmployeeService.config.opa.OPARole;
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

	
	/*--------------------------------*
	 * Controller actions             *
	 *--------------------------------*/

	@GetMapping(value = "/employees", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Employee> getAll(HttpServletRequest request, HttpServletResponse response) {

		audit.publishAuditMessage(auditHelper("GET", null, request, response));
		return repo.findAll();
	}

	@GetMapping(value = "/employees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Employee getSingle(@PathVariable(name = "id", required = true) Long id, HttpServletRequest request,
			HttpServletResponse response) {

		Optional<Employee> empOpt = repo.findById(id);
		if (!empOpt.isPresent())
			throw new ResourceNotFoundException(NOT_FOUND + id);

		Employee e = empOpt.get();
		Department d = this.getDept(e.getDeptId());
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

		if (emp == null)
			throw new IllegalArgumentException(NOT_NULL);

		// map DTO, direct use of entity leads to security vulnerability
		Employee persistentEmp = new Employee();
		persistentEmp.setEmpId(emp.getEmpId());
		persistentEmp.setFirstname(emp.getFirstname());
		persistentEmp.setLastname(emp.getLastname());
		persistentEmp.setDeptId(emp.getDeptId());

		audit.publishAuditMessage(auditHelper("CREATE", persistentEmp, request, response));

		return repo.save(persistentEmp);
	}

	@PutMapping(value = "/employees/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Employee update(@Valid @RequestBody EmployeeDTO emp, @PathVariable(name = "id", required = true) Long id,
			HttpServletRequest request, HttpServletResponse response) {

		Optional<Employee> empOpt = repo.findById(id);
		if (!empOpt.isPresent())
			throw new ResourceNotFoundException(NOT_FOUND + id);
		Employee e = empOpt.get();
		e.setEmpId(emp.getEmpId());
		e.setFirstname(emp.getFirstname());
		e.setLastname(emp.getLastname());
		e.setDeptId(emp.getDeptId());

		audit.publishAuditMessage(auditHelper("UPDATE", e, request, response));

		return repo.save(e);
	}

	@DeleteMapping(value = "/employees/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<?> delete(@PathVariable(name = "id", required = true) Long id, HttpServletRequest request,
			HttpServletResponse response) {

		Optional<Employee> empOpt = repo.findById(id);
		if (!empOpt.isPresent())
			throw new ResourceNotFoundException(NOT_FOUND + id);
		repo.delete(empOpt.get());
		audit.publishAuditMessage(auditHelper("DELETE", empOpt.get(), request, response));

		return ResponseEntity.ok().build();
	}
	
	
	/*--------------------------------*
	 * Downstream REST client methods *
	 *--------------------------------*/

	public Department getDept(Long deptId) {

		// check Redis cache and retrieve object from there if found
		Department dept = checkRedisCache(deptId);
		if (dept != null) {
			LOG.debug("Got department object with ID {} from Redis cache", dept);
			return dept;
		}
		// department not cached, get it from downstream service
		ResponseEntity<Department> restExchange = oauth2RestTemplateBean.getoAuth2RestTemplate().exchange(URI,
				HttpMethod.GET, null, Department.class, deptId);
		dept = restExchange.getBody();
		
		if (dept != null) {
			// get authorities header and build role list
			List<String> authorities = restExchange.getHeaders().get("authorities");
			if (redisProperties.isEnabled() && authorities != null) {
				List<OPARole> roles = new ArrayList<>();
				for (String authority : authorities) {
					OPARole opaRole = new OPARole();
					opaRole.setRole(authority);
					roles.add(opaRole);
				}
				cacheDepartmentObject(dept, roles);
			}
		}
		return dept;
	}

	
	/*--------------------------------*
	 * Caching methods                *
	 *--------------------------------*/

	private Department checkRedisCache(long deptId) {
		// if caching is enabled, try to find the downstream object from Redis cache
		if (redisProperties.isEnabled()) {
			try {
				CachedDepartment cDept = redisRepository.findDepartment(deptId);

				// TODO: check here the authorities
				Set<String> userRoles = SecurityContextUtils.getUserRoles();
				List<OPARole> cacheRoles = cDept.getRoles();
				boolean authorized = false;
				for (OPARole role : cacheRoles) {
					if (userRoles.contains(role.getRole()) == true) {
						LOG.debug("Redis cache access authorized with role {}.", role.getRole());
						authorized = true;
						break;
					}
				}
				if (authorized == true) {
					return cDept.getDepartment();
				} else {
					LOG.debug("Redis cache access not authorized for client roles {}. Granted authorities {}", userRoles, cacheRoles);
					return null;
				}
			} catch (Exception e) {
				LOG.error("Error while trying to retrieve department {} from Redis.  Exception {}", deptId, e);
				return null;
			}
		} else {
			return null;
		}
	}

	private void cacheDepartmentObject(Department dept, List<OPARole> roles) {
		// if caching is enabled, store the just received object in Redis cache
		if (redisProperties.isEnabled()) {
			try {
				redisRepository.saveDepartment(new CachedDepartment(dept, roles));
			} catch (Exception e) {
				LOG.error("Unable to cache department object with ID {} in Redis. Exception {}", 
						dept.getDeptId(), e);
			}
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
		if ("CREATE".equalsIgnoreCase(method) || "UPDATE".equalsIgnoreCase(method)) {
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
