package de.legendlime.EmployeeService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.legendlime.EmployeeService.domain.Employee;

@SpringBootApplication
public class EmployeeServiceApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(EmployeeServiceApplication.class, args);
	}

	@RestController
	public class DummyController {
		
		@GetMapping(value = "/dummy", produces = MediaType.APPLICATION_JSON_VALUE)
		@PreAuthorize("hasAnyAuthority('ROLE_ANONYMOUS')")
		public Employee getDummy() {
			
			return new Employee(9999, "Dummy", "Dummy", 0L);
		}
		
	}
}
