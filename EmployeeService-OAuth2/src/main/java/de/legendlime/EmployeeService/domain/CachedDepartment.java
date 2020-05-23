package de.legendlime.EmployeeService.domain;

import java.io.Serializable;
import java.util.List;

import de.legendlime.EmployeeService.config.opa.OPARole;

public class CachedDepartment implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Department department;
	private List<OPARole> roles;
	
	public CachedDepartment() {
		super();
	}

	public CachedDepartment(Department department, List<OPARole> roles) {
		super();
		this.department = department;
		this.roles = roles;
	}

	public List<OPARole> getRoles() {
		return roles;
	}

	public void setRoles(List<OPARole> roles) {
		this.roles = roles;
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
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
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
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CachedDepartment [department=" + department + ", roles=" + roles + "]";
	}

}
