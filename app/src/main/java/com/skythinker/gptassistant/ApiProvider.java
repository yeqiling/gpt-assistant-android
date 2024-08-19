package com.skythinker.gptassistant;

import java.io.Serializable;

public class ApiProvider implements Serializable {

    private static final long serialVersionUID = 2279047712444757922L;

    private String host;
    private String key;
    private String model;
    private Boolean checked;

    public ApiProvider(String host, String key, String model, Boolean checked) {
        this.host = host;
        this.key = key;
        this.model = model;
        this.checked = checked;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return "ApiProvider{" +
                "host='" + host + '\'' +
                ", key='" + key + '\'' +
                ", model='" + model + '\'' +
                ", checked=" + checked +
                '}';
    }
}
