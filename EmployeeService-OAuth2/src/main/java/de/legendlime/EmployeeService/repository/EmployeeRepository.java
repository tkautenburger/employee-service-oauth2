package de.legendlime.EmployeeService.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.legendlime.EmployeeService.domain.Employee;


public interface EmployeeRepository extends CrudRepository<Employee, Long> {
	
	public List<Employee> findAll();

}
