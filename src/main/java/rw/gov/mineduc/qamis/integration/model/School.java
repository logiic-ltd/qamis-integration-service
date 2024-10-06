package rw.gov.mineduc.qamis.integration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class School {

    @Id
    private Integer schoolCode;

    @Column(nullable = false)
    private String schoolName;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private String cell;

    @Column(nullable = false)
    private String village;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchoolStatus schoolStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchoolOwner schoolOwner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchoolType schoolType;

    private Double latitude;
    private Double longitude;

    // Getters and Setters
    public Integer getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(Integer schoolCode) {
        this.schoolCode = schoolCode;
    }

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

    public SchoolStatus getSchoolStatus() {
        return schoolStatus;
    }

    public void setSchoolStatus(SchoolStatus schoolStatus) {
        this.schoolStatus = schoolStatus;
    }

    public SchoolOwner getSchoolOwner() {
        return schoolOwner;
    }

    public void setSchoolOwner(SchoolOwner schoolOwner) {
        this.schoolOwner = schoolOwner;
    }

    public SchoolType getSchoolType() {
        return schoolType;
    }

    public void setSchoolType(SchoolType schoolType) {
        this.schoolType = schoolType;
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

    // Enums for SchoolStatus, SchoolOwner, and SchoolType
    public enum SchoolStatus {
        PUBLIC, PRIVATE
    }

    public enum SchoolOwner {
        GOVERNMENT, PARENTS, TEACHERS, OTHERS
    }

    public enum SchoolType {
        DAY, BOARDING, OTHER
    }
}
