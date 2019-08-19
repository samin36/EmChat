package sa.home.projects.emchat.Model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String name;
    private String image;
    private String thumbImage;
    private String status;

    public User() {
    }

    public User(String name, String image, String thumbImage, String status) {
        this.name = name;
        this.image = image;
        this.thumbImage = thumbImage;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumbImage() { return thumbImage; }

    public void setThumbImage(String thumbImage) { this.thumbImage = thumbImage;};

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String
                .format("Name: %s\n Status: %s", this.name, this.status);
    }


}
