package org.mcupdater.model.curse.feed;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="PROJECTS")
public class Project {

    @SerializedName("Id")
    @Expose
    private Long id;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Authors")
    @Expose
    private List<Author> authors = new ArrayList<Author>();
    @SerializedName("Attachments")
    @Expose
    private List<Attachment> attachments = new ArrayList<Attachment>();
    @SerializedName("WebSiteURL")
    @Expose
    private String webSiteURL;
    @SerializedName("GameId")
    @Expose
    private Integer gameId;
    @SerializedName("Summary")
    @Expose
    private String summary;
    @SerializedName("DefaultFileId")
    @Expose
    private Integer defaultFileId;
    @SerializedName("CommentCount")
    @Expose
    private Integer commentCount;
    @SerializedName("DownloadCount")
    @Expose
    private Double downloadCount;
    @SerializedName("Rating")
    @Expose
    private Integer rating;
    @SerializedName("InstallCount")
    @Expose
    private Integer installCount;
    @SerializedName("IconId")
    @Expose
    private Integer iconId;
    @SerializedName("LatestFiles")
    @Expose
    private List<LatestFile> latestFiles = new ArrayList<LatestFile>();
    @SerializedName("Categories")
    @Expose
    private List<Category> categories = new ArrayList<Category>();
    @SerializedName("PrimaryAuthorName")
    @Expose
    private String primaryAuthorName;
    @SerializedName("ExternalUrl")
    @Expose
    private Object externalUrl;
    @SerializedName("Status")
    @Expose
    private Integer status;
    @SerializedName("Stage")
    @Expose
    private Integer stage;
    @SerializedName("DonationUrl")
    @Expose
    private Object donationUrl;
    @SerializedName("PrimaryCategoryId")
    @Expose
    private Integer primaryCategoryId;
    @SerializedName("PrimaryCategoryName")
    @Expose
    private String primaryCategoryName;
    @SerializedName("PrimaryCategoryAvatarUrl")
    @Expose
    private String primaryCategoryAvatarUrl;
    @SerializedName("Likes")
    @Expose
    private Long likes;
    @SerializedName("CategorySection")
    @Expose
    private CategorySection categorySection;
    @SerializedName("PackageType")
    @Expose
    private Integer packageType;
    @SerializedName("AvatarUrl")
    @Expose
    private Object avatarUrl;
    @SerializedName("GameVersionLatestFiles")
    @Expose
    private List<GameVersionLatestFile> gameVersionLatestFiles = new ArrayList<GameVersionLatestFile>();
    @SerializedName("IsFeatured")
    @Expose
    private Integer isFeatured;
    @SerializedName("PopularityScore")
    @Expose
    private Double popularityScore;
    @SerializedName("GamePopularityRank")
    @Expose
    private Integer gamePopularityRank;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String getWebSiteURL() {
        return webSiteURL;
    }

    public void setWebSiteURL(String webSiteURL) {
        this.webSiteURL = webSiteURL;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getDefaultFileId() {
        return defaultFileId;
    }

    public void setDefaultFileId(Integer defaultFileId) {
        this.defaultFileId = defaultFileId;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Double getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Double downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getInstallCount() {
        return installCount;
    }

    public void setInstallCount(Integer installCount) {
        this.installCount = installCount;
    }

    public Integer getIconId() {
        return iconId;
    }

    public void setIconId(Integer iconId) {
        this.iconId = iconId;
    }

    public List<LatestFile> getLatestFiles() {
        return latestFiles;
    }

    public void setLatestFiles(List<LatestFile> latestFiles) {
        this.latestFiles = latestFiles;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getPrimaryAuthorName() {
        return primaryAuthorName;
    }

    public void setPrimaryAuthorName(String primaryAuthorName) {
        this.primaryAuthorName = primaryAuthorName;
    }

    public Object getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(Object externalUrl) {
        this.externalUrl = externalUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public Object getDonationUrl() {
        return donationUrl;
    }

    public void setDonationUrl(Object donationUrl) {
        this.donationUrl = donationUrl;
    }

    public Integer getPrimaryCategoryId() {
        return primaryCategoryId;
    }

    public void setPrimaryCategoryId(Integer primaryCategoryId) {
        this.primaryCategoryId = primaryCategoryId;
    }

    public String getPrimaryCategoryName() {
        return primaryCategoryName;
    }

    public void setPrimaryCategoryName(String primaryCategoryName) {
        this.primaryCategoryName = primaryCategoryName;
    }

    public String getPrimaryCategoryAvatarUrl() {
        return primaryCategoryAvatarUrl;
    }

    public void setPrimaryCategoryAvatarUrl(String primaryCategoryAvatarUrl) {
        this.primaryCategoryAvatarUrl = primaryCategoryAvatarUrl;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public CategorySection getCategorySection() {
        return categorySection;
    }

    public void setCategorySection(CategorySection categorySection) {
        this.categorySection = categorySection;
    }

    public Integer getPackageType() {
        return packageType;
    }

    public void setPackageType(Integer packageType) {
        this.packageType = packageType;
    }

    public Object getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(Object avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<GameVersionLatestFile> getGameVersionLatestFiles() {
        return gameVersionLatestFiles;
    }

    public void setGameVersionLatestFiles(List<GameVersionLatestFile> gameVersionLatestFiles) {
        this.gameVersionLatestFiles = gameVersionLatestFiles;
    }

    public Integer getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Integer isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Double getPopularityScore() {
        return popularityScore;
    }

    public void setPopularityScore(Double popularityScore) {
        this.popularityScore = popularityScore;
    }

    public Integer getGamePopularityRank() {
        return gamePopularityRank;
    }

    public void setGamePopularityRank(Integer gamePopularityRank) {
        this.gamePopularityRank = gamePopularityRank;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[] {""});
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(name).append(authors).append(attachments).append(webSiteURL).append(gameId).append(summary).append(defaultFileId).append(commentCount).append(downloadCount).append(rating).append(installCount).append(iconId).append(latestFiles).append(categories).append(primaryAuthorName).append(externalUrl).append(status).append(stage).append(donationUrl).append(primaryCategoryId).append(primaryCategoryName).append(primaryCategoryAvatarUrl).append(likes).append(categorySection).append(packageType).append(avatarUrl).append(gameVersionLatestFiles).append(isFeatured).append(popularityScore).append(gamePopularityRank).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Project) == false) {
            return false;
        }
        Project rhs = ((Project) other);
        return new EqualsBuilder().append(id, rhs.id).append(name, rhs.name).append(authors, rhs.authors).append(attachments, rhs.attachments).append(webSiteURL, rhs.webSiteURL).append(gameId, rhs.gameId).append(summary, rhs.summary).append(defaultFileId, rhs.defaultFileId).append(commentCount, rhs.commentCount).append(downloadCount, rhs.downloadCount).append(rating, rhs.rating).append(installCount, rhs.installCount).append(iconId, rhs.iconId).append(latestFiles, rhs.latestFiles).append(categories, rhs.categories).append(primaryAuthorName, rhs.primaryAuthorName).append(externalUrl, rhs.externalUrl).append(status, rhs.status).append(stage, rhs.stage).append(donationUrl, rhs.donationUrl).append(primaryCategoryId, rhs.primaryCategoryId).append(primaryCategoryName, rhs.primaryCategoryName).append(primaryCategoryAvatarUrl, rhs.primaryCategoryAvatarUrl).append(likes, rhs.likes).append(categorySection, rhs.categorySection).append(packageType, rhs.packageType).append(avatarUrl, rhs.avatarUrl).append(gameVersionLatestFiles, rhs.gameVersionLatestFiles).append(isFeatured, rhs.isFeatured).append(popularityScore, rhs.popularityScore).append(gamePopularityRank, rhs.gamePopularityRank).isEquals();
    }

}
