package id.amoled.iwritenew.tools;

/**
 * Created by TIUNIDA on 12/12/2017.
 *
 * Kelas ini berfungsi untuk mengambil data-data KARYAKU dari Firebase. Data yang diambil berupa JSON
 */

public class Karyaku {

    private String post_id;

    public Karyaku(){

    }

    public Karyaku(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }
}
