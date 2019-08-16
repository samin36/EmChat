package sa.home.projects.emchat.Model;

public class User {

    private String name;
    private String image;
    private String status;

    public User() {
    }

    public User(String name, String image, String status) {
        this.name = name;
        this.image = image;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Name: %s\n Image: %s\n Status: %s", this.name, this.image,
                             this.status);
    }

}
