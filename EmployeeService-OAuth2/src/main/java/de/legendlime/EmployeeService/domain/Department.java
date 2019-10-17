package de.legendlime.EmployeeService.domain;

import java.io.Serializable;

public class Department implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private long deptId;
	private String name;
	private String description;
	private String podServed;
	
	public Department() {
		super();
	}

	public Department(long deptId, String name, String description) {
		super();
		this.deptId = deptId;
		this.name = name;
		this.description = description;
		this.podServed = "";
	}

	public long getDeptId() {
		return deptId;
	}

	public void setDeptId(long deptId) {
		this.deptId = deptId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPodServed() {
		return podServed;
	}

	public void setPodServed(String podServed) {
		this.podServed = podServed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (deptId ^ (deptId >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Department other = (Department) obj;
		if (deptId != other.deptId)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Department [deptId=" + deptId + ", name=" + name + ", description=" + description + ", podServed="
				+ podServed + "]";
	}

}
