package com.pts.pojo;

public class RouteType {

    private Integer id;
    private String typeName;
    private String description;
    private String iconUrl;
    private String colorCode;

    // Constructor mặc định
    public RouteType() {
    }

    // Constructor với các tham số
    public RouteType(String typeName, String colorCode) {
        this.typeName = typeName;
        this.colorCode = colorCode;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    @Override
    public String toString() {
        return "RouteType{"
                + "id=" + id
                + ", typeName='" + typeName + '\''
                + ", colorCode='" + colorCode + '\''
                + '}';
    }
}
