package pfe.stockWatch.Backend.dao;

public class FcmToken {


    private String token;

    public FcmToken() {}

    public FcmToken(String token) {
        this.token = token;
    }

    // getter et setter
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}