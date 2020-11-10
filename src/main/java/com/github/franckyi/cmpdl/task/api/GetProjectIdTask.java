package com.github.franckyi.cmpdl.task.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.github.franckyi.cmpdl.CMPDL;
import com.github.franckyi.cmpdl.api.response.Addon;
import com.github.franckyi.cmpdl.task.TaskBase;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class GetProjectIdTask extends TaskBase<Integer> {

	private static final String CURSEFORGE_URL_MINECRAFT_MODPACKS_PATH = "/minecraft/modpacks/";
	private static final int MAX_SEARCH_ADDON_FOR_SLUG_COUNT = 10;

    private final String projectUrl;

    public GetProjectIdTask(String projectUrl) {
        this.projectUrl = projectUrl;
    }

	private Addon searchAddonBySlug(String slug, int index, int pageSize) throws IOException {
		List<Addon> addons = CMPDL.getAPI().searchAddons(slug, index, pageSize).execute().body();
		for (Addon addon : addons) {
			if (addon.getSlug().equals(slug)) {
				return addon;
			}
		}
		return null;
	}

    @Override
    protected Integer call0() {
        updateTitle(String.format("Getting project ID for %s", projectUrl));
        try {
            URL url = new URL(projectUrl);
            if (url.getHost().equals("www.curseforge.com")) {
				String urlPath = url.getPath();
				if (!urlPath.startsWith(CURSEFORGE_URL_MINECRAFT_MODPACKS_PATH)) {
					throw new MalformedURLException("URL with host=" + url.getHost() + " but not with expected URL path. path=" + url.getPath());
				}
				String slug = urlPath.substring(CURSEFORGE_URL_MINECRAFT_MODPACKS_PATH.length());

				Addon addon = searchAddonBySlug(slug, 0, 1);
				if (addon == null) {
					addon = searchAddonBySlug(slug, 1, MAX_SEARCH_ADDON_FOR_SLUG_COUNT - 1);
				}
				return addon == null ? null : addon.getId();
            } else {
				Document doc = Jsoup.parse(url, 10000);
				return Integer.parseInt(doc.select(".fa-icon-twitch").attr("data-id"));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Malformed URL", ButtonType.OK).show());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Connection error", ButtonType.OK).show());
            return null;
		} catch (Exception e) {
			e.printStackTrace();
			Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "General error", ButtonType.OK).show());
			return null;
		}
    }

}
