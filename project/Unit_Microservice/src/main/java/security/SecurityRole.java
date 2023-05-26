package security;

import java.util.ArrayList;
import java.util.List;

public class SecurityRole {

	private List<SecurityRole> subRoles;
	private String name;
	
	public SecurityRole(String name) {
		this.name=name;
		this.subRoles= new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean addRole(SecurityRole role) {
		if(this.containsRole(role)) {
			return false;
		}
		subRoles.add(role);
		return true;
	}
	
	public boolean containsRole(SecurityRole role) {
		for(SecurityRole roleEntry : subRoles) {
			if(role.getName().equals(roleEntry.getName())) {
				return true;
			}else {
				return roleEntry.containsRole(role);
			}
		}
		return false;
	}
	
	public boolean containsRole(String role) {
		for(SecurityRole roleEntry : subRoles) {
			if(role.equals(roleEntry.getName())) {
				return true;
			}else {
				return roleEntry.containsRole(role);
			}
		}
		return false;
	}
}
