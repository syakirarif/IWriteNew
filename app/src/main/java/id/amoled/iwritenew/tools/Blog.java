package id.amoled.iwritenew.tools;

/**
 * Created by TIUNIDA on 17/10/2017.
 *
 * Kelas ini berfungsi untuk mengambil data-data BLOG dari Firebase. Data yang diambil berupa JSON
 */

public class Blog {

    private String title;
    private String desc;
    private String image;
    private String user_id;
    private String time_post;
    private String date_post;


    public Blog(){

    }

    public Blog(String title, String desc, String image, String user_id, String time_post, String date_post) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.user_id = user_id;
        this.time_post = time_post;
        this.date_post = date_post;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTime_post() {
        return time_post;
    }

    public void setTime_post(String time_post) {
        this.time_post = time_post;
    }

    public String getDate_post() {
        return date_post;
    }

    public void setDate_post(String date_post) {
        this.date_post = date_post;
    }
}
