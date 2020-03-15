package de.legendlime.EmployeeService.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

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

import de.legendlime.EmployeeService.config.oauth2.OAuth2RestTemplateBean;
import de.legendlime.EmployeeService.domain.Department;
import de.legendlime.EmployeeService.domain.Employee;
import de.legendlime.EmployeeService.domain.EmployeeDTO;
import de.legendlime.EmployeeService.repository.EmployeeRepository;
import de.legendlime.EmployeeService.config.logging.ResponseLoggingFilter;
import de.legendlime.EmployeeService.messaging.AuditRecord;
import de.legendlime.EmployeeService.messaging.AuditSourceBean;
import io.opentracing.Tracer;

@RestController
@RequestMapping(value = "v1")
public class EmployeeController {

	private final static String SERVICE_PORT = "8090";
	private final static String SERVICE_HOST = "department-service-oauth2";
	private final static String URI = "https://" + 
	                            SERVICE_HOST + ":" +
			                    SERVICE_PORT + "/v1/departments/{deptId}";
	
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

	@GetMapping(value = "/employees", 
			    produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Employee> getAll(HttpServletRequest request, HttpServletResponse response) {

		audit.publishAuditMessage(auditHelper("GET", null, request, response));
		return repo.findAll();
	}
	
	@GetMapping(value = "/employees/{id}", 
			    produces = MediaType.APPLICATION_JSON_VALUE)
	public Employee getSingle(@PathVariable(name = "id", required = true) Long id,
			HttpServletRequest request, HttpServletResponse response) {

		Optional<Employee> empOpt = repo.findById(id);
		if (!empOpt.isPresent())
			throw new ResourceNotFoundException(NOT_FOUND + id);
		
		Employee e = empOpt.get();
		Department d = this.getDept(e.getDeptId());
		if (d != null ) {
			e.setDeptName(d.getName());
			e.setDeptDesc(d.getDescription());
			e.setDeptPodServed(d.getPodServed());
		}
		audit.publishAuditMessage(auditHelper("GET", e, request, response));
		return e;
	}

	@PostMapping(value = "/employees", 
			     consumes = MediaType.APPLICATION_JSON_VALUE, 
			     produces = MediaType.APPLICATION_JSON_VALUE)
	public Employee create(@Valid @RequestBody EmployeeDTO emp,
			HttpServletRequest request, HttpServletResponse response) {

		if (emp == null)
			throw new IllegalArgumentException(NOT_NULL);
		
		//map DTO, direct use of entity leads to security vulnerability
		Employee persistentEmp = new Employee();
		persistentEmp.setEmpId(emp.getEmpId());
		persistentEmp.setFirstname(emp.getFirstname());
		persistentEmp.setLastname(emp.getLastname());
		persistentEmp.setDeptId(emp.getDeptId());
		
		audit.publishAuditMessage(auditHelper("CREATE", persistentEmp, request, response));

		return repo.save(persistentEmp);
	}

	@PutMapping(value = "/employees/{id}", 
			    consumes = MediaType.APPLICATION_JSON_VALUE, 
			    produces = MediaType.APPLICATION_JSON_VALUE)
	public Employee update(@Valid @RequestBody EmployeeDTO emp, 
			               @PathVariable(name = "id", required = true) Long id,
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
	public ResponseEntity<?> delete(@PathVariable(name = "id", required = true) Long id,
			HttpServletRequest request, HttpServletResponse response) {

		Optional<Employee> empOpt = repo.findById(id);
		if (!empOpt.isPresent())
			throw new ResourceNotFoundException(NOT_FOUND + id);
		repo.delete(empOpt.get());
		audit.publishAuditMessage(auditHelper("DELETE", empOpt.get(), request, response));

		return ResponseEntity.ok().build();
	}
	
    @GetMapping("/getConfigFromVault")
    public String getConfigFromProperty() throws JsonProcessingException {
   	return "Datasource Username: " + dsProperties.getUsername() + 
   		   " - Datasource Password: " + dsProperties.getPassword();
    }
  
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot!";
    }

	public Department getDept(Long deptId) {
    /*
		String traceId;
		if (tracer.activeSpan() != null)
			traceId = tracer.activeSpan().context().toTraceId();
		else
			traceId = tracer.buildSpan(applicationContext.getId()).start().context().toTraceId();

		HttpHeaders headers = new HttpHeaders();
		headers.set("x-trace-id", traceId);
		HttpEntity<String> entity = new HttpEntity<>(headers);
	*/
		ResponseEntity<Department> restExchange =
                oauth2RestTemplateBean.getoAuth2RestTemplate()
                  .exchange(URI, HttpMethod.GET, null, Department.class, deptId);
    /*
		if (tracer.activeSpan() != null)
			tracer.activeSpan().finish();
    */
        return restExchange.getBody();
	}
	
	private AuditRecord auditHelper(String method, Employee obj, 
			HttpServletRequest request, HttpServletResponse response) {
		
		AuditRecord record = new AuditRecord();
		
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
		record.setObjectType(obj.getClass().getName());
		record.setObjectId(obj.getEmpId());
		
		return record;
	}
}
