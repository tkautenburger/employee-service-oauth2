package de.legendlime.EmployeeService.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "employee", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = "empId"))
public class Employee implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	private long empId;
	private String firstname;
	private String lastname;
	private Long deptId;
	
	@Transient
	private String deptName = "";
	@Transient
	private String deptDesc = "";
	@Transient
	private String podServed;
	@Transient
	private String deptPodServed;
	
	public Employee() {
		super();
		this.podServed = System.getenv("HOSTNAME");
	}

	public Employee(long empId, String firstname, String lastname, Long deptId) {
		super();
		this.empId = empId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.deptId = deptId;
		this.podServed = System.getenv("HOSTNAME");
	}

	public long getEmpId() {
		return empId;
	}

	public void setEmpId(long empId) {
		this.empId = empId;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getDeptDesc() {
		return deptDesc;
	}

	public void setDeptDesc(String deptDesc) {
		this.deptDesc = deptDesc;
	}

	public String getPodServed() {
		return podServed;
	}

	public void setPodServed(String podServed) {
		this.podServed = podServed;
	}
	public String getDeptPodServed() {
		return deptPodServed;
	}

	public void setDeptPodServed(String deptPodServed) {
		this.deptPodServed = deptPodServed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (empId ^ (empId >>> 32));
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
		Employee other = (Employee) obj;
		if (empId != other.empId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Employee [empId=" + empId + ", firstname=" + firstname + ", lastname=" + lastname + ", deptId=" + deptId
				+ ", deptName=" + deptName + ", deptDesc=" + deptDesc + ", podServed=" + podServed + ", deptPodServed="
				+ deptPodServed + "]";
	}
}
