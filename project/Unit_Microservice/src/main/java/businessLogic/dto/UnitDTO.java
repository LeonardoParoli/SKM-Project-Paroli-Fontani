package businessLogic.dto;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import domainModel.Unit;

public class UnitDTO implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = -4305630581710626477L;
	@Expose(deserialize=false)
	private String id;
	@Expose
	@NotNull
	private String name;
	
	public UnitDTO(Unit unit) {
		this.id=unit.getId().toString();
		this.name=unit.getName();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public JsonElement toJSON() {
		return new Gson().toJsonTree(this);
	}
	
	public static UnitDTO fromJson(String json) {
		return new Gson().fromJson(json, UnitDTO.class);
	}
}
