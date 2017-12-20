package id.amoled.iwritenew.tools;

/**
 * Created by TIUNIDA on 11/12/2017.
 *
 * Kelas ini berfungsi untuk mengambil data-data FOLLOWER dari Firebase. Data yang diambil berupa JSON
 */

public class Follower {

    private String user_id;

    public Follower(){

    }

    public Follower(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
