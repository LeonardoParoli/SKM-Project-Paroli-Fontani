package businessLogic.dto;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

import domainModel.ConversionRatio;
import domainModel.Unit;

public class ConversionRatioDTO implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	@Expose (deserialize=false)
	private String id;
	@Expose 
	private String startUnitName;
	@Expose (deserialize=false)
	private String startUnitId;
	@Expose
	private String endUnitName;
	@Expose (deserialize=false)
	private String endUnitId;
	@Expose
	private String ratio;
	
	public ConversionRatioDTO() {
		
	}
	
	public ConversionRatioDTO(Long id, Unit start, Unit end, double ratio){
		this.id = id.toString();
		this.startUnitName = start.getName();
		this.startUnitId = start.getId().toString();
		this.endUnitName = end.getName();
		this.endUnitId = end.getId().toString();
		this.ratio = Double.toString(ratio);
	}
	
	public ConversionRatioDTO(ConversionRatio conversion) {
		this.id = conversion.getId().toString();
		this.startUnitName = conversion.getFirst().getName();
		this.startUnitId = conversion.getFirst().getId().toString();
		this.endUnitName = conversion.getSecond().getName();
		this.endUnitId = conversion.getSecond().getId().toString();
		this.ratio = Double.toString(conversion.getRatio());
	}
	
	public String getConversionRatioId() {
		return id;
	}

	public void setConversionRatioId(String id) {
		this.id = id;
	}

	public String getStartUnitName() {
		return startUnitName;
	}

	public void setStartUnitName(String startUnitName) {
		this.startUnitName = startUnitName;
	}

	public String getStartUnitId() {
		return startUnitId;
	}

	public void setStartUnitId(String startUnitId) {
		this.startUnitId = startUnitId;
	}

	public String getEndUnitName() {
		return endUnitName;
	}

	public void setEndUnitName(String endUnitName) {
		this.endUnitName = endUnitName;
	}

	public String getEndUnitId() {
		return endUnitId;
	}

	public void setEndUnitId(String endUnitId) {
		this.endUnitId = endUnitId;
	}

	public String getRatio() {
		return ratio;
	}

	public void setRatio(String ratio) {
		this.ratio = ratio;
	}

	public JsonElement toJSON() {
		return new Gson().toJsonTree(this);
	}
	
	public static ConversionRatioDTO fromJson(String json) {
		return new Gson().fromJson(json, ConversionRatioDTO.class);
	}
}
