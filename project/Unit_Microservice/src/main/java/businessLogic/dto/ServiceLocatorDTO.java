package businessLogic.dto;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

public class ServiceLocatorDTO {
    @Expose
    private String location;

    public ServiceLocatorDTO(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public JsonElement toJson() {
        return new Gson().toJsonTree(this);
    }

    public static ServiceLocatorDTO fromJson(JsonElement json) {
        return new Gson().fromJson(json, ServiceLocatorDTO.class);
    }
}
