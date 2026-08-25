package com.health.platform.auth.dto;

public class CaptchaVO {

    private String captchaId;
    private String image;

    public CaptchaVO() {
    }

    public CaptchaVO(String captchaId, String image) {
        this.captchaId = captchaId;
        this.image = image;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
