package sa.home.projects.emchat.Model;

public class User {

    private String name;
    private String image;
    private String thumbImage;
    private String status;
    private Boolean saveFingerprint;
    private String tokenId;

    public User() {
    }

    public User(String name, String image, String thumbImage, String status,
                Boolean saveFingerprint, String tokenId) {
        this.name = name;
        this.image = image;
        this.thumbImage = thumbImage;
        this.status = status;
        this.saveFingerprint = saveFingerprint;
        this.tokenId = tokenId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public Boolean isSaveFingerprint() {
        return saveFingerprint;
    }

    public void setSaveFingerprint(Boolean saveFingerprint) {
        this.saveFingerprint = saveFingerprint;
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
