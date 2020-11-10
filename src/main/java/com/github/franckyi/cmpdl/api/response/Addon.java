package com.github.franckyi.cmpdl.api.response;

import java.util.List;

import com.github.franckyi.cmpdl.api.IBean;

public class Addon implements IBean {

    private int id;
    private String name;
    private List<Author> authors;
    private List<Attachment> attachments;
    private String websiteUrl;
    private String summary;
    private List<AddonFile> latestFiles;
    private List<Category> categories;
    private int primaryCategoryId;
    private CategorySection categorySection;
	private String slug;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getSummary() {
        return summary;
    }

    public List<AddonFile> getLatestFiles() {
        return latestFiles;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public int getPrimaryCategoryId() {
        return primaryCategoryId;
    }

    public CategorySection getCategorySection() {
        return categorySection;
    }

	public String getSlug() {
		return slug;
	}

}
