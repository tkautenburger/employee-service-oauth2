package de.legendlime.EmployeeService.repository;

import de.legendlime.EmployeeService.domain.CachedDepartment;

public interface DepartmentRedisRepository {
	void saveDepartment(CachedDepartment dept);
	void updateDepartment(CachedDepartment dept);
	void deleteDepartment(long deptId);
	CachedDepartment findDepartment(long deptId);
}
