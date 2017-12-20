package id.amoled.iwritenew.tools;

/**
 * Created by TIUNIDA on 15/11/2017.
 *
 * Kelas ini berfungsi untuk mengambil data-data USERS dari Firebase. Data yang diambil berupa JSON
 */

public class ProfilUser {

    private String disp_name;
    private String disp_nope;
    private String disp_image;
    private String email;

    public ProfilUser() {

    }

    public String getDisp_name() {
        return disp_name;
    }

    public void setDisp_name(String disp_name) {
        this.disp_name = disp_name;
    }

    public String getDisp_nope() {
        return disp_nope;
    }

    public void setDisp_nope(String disp_nope) {
        this.disp_nope = disp_nope;
    }

    public String getDisp_image() {
        return disp_image;
    }

    public void setDisp_image(String disp_image) {
        this.disp_image = disp_image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ProfilUser(String disp_name, String disp_nope, String disp_image, String email) {
        this.disp_name = disp_name;
        this.disp_nope = disp_nope;
        this.disp_image = disp_image;
        this.email = email;
    }
}
