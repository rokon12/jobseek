package com.bazlur.jobseek;

import java.time.LocalDate;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/27/16.
 */
public class JobSummery {
    private String url;
    private LocalDate publishedOn;
    private int vacancies;
    private String jobTitle;
    private String companyName;
    private String jobNature;
    private String experience;
    private String jobLocation;
    private String salaryRange;
    private LocalDate deadLine;

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getUrl() {
        return url;
    }

    public JobSummery setUrl(String url) {
        this.url = url;
        return this;
    }

    public LocalDate getPublishedOn() {
        return publishedOn;
    }

    public JobSummery setPublishedOn(LocalDate publishedOn) {
        this.publishedOn = publishedOn;
        return this;
    }

    public int getVacancies() {
        return vacancies;
    }

    public JobSummery setVacancies(int vacancies) {
        this.vacancies = vacancies;
        return this;
    }

    public String getJobNature() {
        return jobNature;
    }

    public JobSummery setJobNature(String jobNature) {
        this.jobNature = jobNature;
        return this;
    }

    public String getExperience() {
        return experience;
    }

    public JobSummery setExperience(String experience) {
        this.experience = experience;
        return this;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public JobSummery setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
        return this;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public JobSummery setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
        return this;
    }

    public LocalDate getDeadLine() {
        return deadLine;
    }

    public JobSummery setDeadLine(LocalDate deadLine) {
        this.deadLine = deadLine;
        return this;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("JobSummery{");
        sb.append("url='").append(url).append('\'');
        sb.append(", publishedOn=").append(publishedOn);
        sb.append(", vacancies=").append(vacancies);
        sb.append(", jobNature='").append(jobNature).append('\'');
        sb.append(", experience='").append(experience).append('\'');
        sb.append(", jobLocation='").append(jobLocation).append('\'');
        sb.append(", salaryRange='").append(salaryRange).append('\'');
        sb.append(", deadLine=").append(deadLine);
        sb.append('}');
        return sb.toString();
    }
}
