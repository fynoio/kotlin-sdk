package io.fyno.core_java.helpers;

public class Config {
    public String key;
    public String value;
    public Integer id;

    public Config(String key, String value, Integer id) {
        this.key = key;
        this.value = value;
        this.id = id;
    }
    public Config(String key) {
        this.key = key;
    }
    public Config(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
