package com.github.franckyi.cmpdl.api;

import java.util.List;

import com.github.franckyi.cmpdl.api.response.Addon;
import com.github.franckyi.cmpdl.api.response.AddonFile;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TwitchAppAPI {

	@GET("addon/search?gameId=432")
	Call<List<Addon>> searchAddons(@Query("searchFilter") String searchFilter, @Query("index") int index, @Query("pageSize") int pageSize);

    @GET("addon/{addonId}")
    Call<Addon> getAddon(@Path("addonId") int addonId);

    @GET("addon/{addonId}/file/{fileId}")
    Call<AddonFile> getFile(@Path("addonId") int addonId, @Path("fileId") int fileId);

    @GET("addon/{addonId}/files")
    Call<List<AddonFile>> getAddonFiles(@Path("addonId") int addonId);

}
