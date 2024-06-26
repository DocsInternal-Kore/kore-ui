package kore.botssdk.models;

import java.io.Serializable;

public class BrandingBodyBackgroundModel implements Serializable {
    private String type;
    private String color;
    private String img;

    public String getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public String getImg() {
        return img;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
