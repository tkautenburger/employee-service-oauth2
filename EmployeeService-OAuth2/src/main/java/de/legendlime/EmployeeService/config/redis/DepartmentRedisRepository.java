package de.legendlime.EmployeeService.config.redis;

import de.legendlime.EmployeeService.domain.Department;

public interface DepartmentRedisRepository {
	void saveDepartment(Department dept);
	void updateDepartment(Department dept);
	void deleteDepartment(long deptId);
	Department findDepartment(long deptId);
}
