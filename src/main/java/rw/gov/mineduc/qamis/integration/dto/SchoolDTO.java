package rw.gov.mineduc.qamis.integration.dto;

import javax.validation.constraints.NotEmpty;

public class SchoolDTO {

  @NotEmpty(message = "School name is mandatory")
  private String schoolName;

  @NotEmpty(message = "Province is mandatory")
  private String province;

  @NotEmpty(message = "District is mandatory")
  private String district;

  @NotEmpty(message = "Sector is mandatory")
  private String sector;

  @NotEmpty(message = "Cell is mandatory")
  private String cell;

  @NotEmpty(message = "Village is mandatory")
  private String village;
  private String schoolStatus;
  private String schoolOwner;
  private Double latitude;
  private Double longitude;
  private String day;
  private String boarding;
  private String schoolEmail;

  // Getters and Setters
  public String getSchoolName() {
    return schoolName;
  }

  public void setSchoolName(String schoolName) {
    this.schoolName = schoolName;
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getSector() {
    return sector;
  }

  public void setSector(String sector) {
    this.sector = sector;
  }

  public String getCell() {
    return cell;
  }

  public void setCell(String cell) {
    this.cell = cell;
  }

  public String getVillage() {
    return village;
  }

  public void setVillage(String village) {
    this.village = village;
  }

  public String getSchoolStatus() {
    return schoolStatus;
  }

  public void setSchoolStatus(String schoolStatus) {
    this.schoolStatus = schoolStatus;
  }

  public String getSchoolOwner() {
    return schoolOwner;
  }

  public void setSchoolOwner(String schoolOwner) {
    this.schoolOwner = schoolOwner;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public String getDay() {
    return day;
  }

  public void setDay(String day) {
    this.day = day;
  }

  public String getBoarding() {
    return boarding;
  }

  public void setBoarding(String boarding) {
    this.boarding = boarding;
  }

  public String getSchoolEmail() {
    return schoolEmail;
  }

  public void setSchoolEmail(String schoolEmail) {
    this.schoolEmail = schoolEmail;
  }
}
