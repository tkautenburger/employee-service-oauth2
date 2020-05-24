package de.legendlime.EmployeeService.domain;

import java.io.Serializable;

public class CachedDepartment implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Department department;
	private String role;
	
	public CachedDepartment() {
		super();
	}

	public CachedDepartment(Department department, String role) {
		super();
		this.department = department;
		this.role = role;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((department == null) ? 0 : department.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CachedDepartment other = (CachedDepartment) obj;
		if (department == null) {
			if (other.department != null)
				return false;
		} else if (!department.equals(other.department))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CachedDepartment [department=" + department + ", role=" + role + "]";
	}

}
