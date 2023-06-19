package com.example.mhasnikhalid_motionpilot_projet;

public class User {
    private String fname;
    private String lname;
    private String email;
    private String password;
    private String telephone;
    private String sexe;
    private String filiere;
    private String userId;
    private String profilePicture;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String fname, String lname, String email, String password, String telephone, String sexe, String filiere, String userId, String profilePicture) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.sexe = sexe;
        this.filiere = filiere;
        this.userId = userId;
        this.profilePicture = profilePicture;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getFiliere() {
        return filiere;
    }

    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
