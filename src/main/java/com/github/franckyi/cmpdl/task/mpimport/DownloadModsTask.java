package com.github.franckyi.cmpdl.task.mpimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.franckyi.cmpdl.CMPDL;
import com.github.franckyi.cmpdl.api.response.AddonFile;
import com.github.franckyi.cmpdl.model.ModpackManifest;
import com.github.franckyi.cmpdl.task.TaskBase;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

public class DownloadModsTask extends TaskBase<Void> {

	public final ExecutorService DOWNLOAD_EXECUTOR_SERVICE = Executors.newWorkStealingPool(8);

    private final File modsFolder, progressFile;
    private final List<ModpackManifest.ModpackManifestMod> mods;

    private final ObjectProperty<Task<?>> task = new SimpleObjectProperty<>();

    public DownloadModsTask(File modsFolder, File progressFile, List<ModpackManifest.ModpackManifestMod> mods) {
        this.modsFolder = modsFolder;
        this.progressFile = progressFile;
        this.mods = mods;
    }

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		boolean ret = super.cancel(mayInterruptIfRunning);
		if (ret) {
			DOWNLOAD_EXECUTOR_SERVICE.shutdownNow();
		}
		return ret;
	}

    @Override
	protected Void call0() throws IOException {
        int max = mods.size();
        int start = 0;
        if (progressFile.exists() && progressFile.isFile()) {
            BufferedReader reader = new BufferedReader(new FileReader(progressFile));
            String line;
            while ((line = reader.readLine()) != null && !isCancelled()) {
                ModpackManifest.ModpackManifestMod mod = new ModpackManifest.ModpackManifestMod(line);
                mods.remove(mod);
                CMPDL.progressPane.getController().log("File %d:%d already downloaded - skipping", mod.getProjectId(), mod.getFileId());
                start++;
            }
            reader.close();
        } else {
            progressFile.createNewFile();
        }
		long startTime = System.currentTimeMillis();
		System.out.println(startTime);
		try (FileWriter writer = new FileWriter(progressFile, true)) {
			Phaser downloadsPending = new Phaser(1);
			AtomicInteger downloadsDone = new AtomicInteger(start);
            for (int i = start; i < max; i++) {
                if (isCancelled()) {
					// returning -> leaving try with resource block -> closing writer
                    return null;
                }
                ModpackManifest.ModpackManifestMod mod = mods.get(i - start);
				TaskBase<Void> resolveTask = new TaskBase<Void>() {

					@Override
					protected Void call0() throws Throwable {
						try {
							CMPDL.progressPane.getController().log("Resolving file %d:%d", mod.getProjectId(), mod.getFileId());
							AddonFile file = CMPDL.getAPI().getFile(mod.getProjectId(), mod.getFileId()).execute().body();
							if (file != null) {
								DownloadFileTask downloadTask = new DownloadFileTask(file.getDownloadUrl(), new File(modsFolder, file.getFileName()));
								setTask(downloadTask);
								CMPDL.progressPane.getController().log("Downloading file %s", file.getFileName().replaceAll("%", ""));
								downloadTask.setOnSucceeded(e -> {
									try {
										writer.write(String.format("%d:%d\n", mod.getProjectId(), mod.getFileId()));
										writer.flush();
									} catch (IOException e1) {
										e1.printStackTrace();
									} finally {
										downloadsPending.arriveAndDeregister();
										int done = downloadsDone.incrementAndGet();
										Platform.runLater(new Runnable() {
											public void run() {
												DownloadModsTask.this.updateTitle(String.format("Downloading mods (%d/%d)", done, max));
												DownloadModsTask.this.updateProgress(done, max);
											}
										});
									}
								});
								downloadTask.setOnFailed(e -> {
									downloadsPending.arriveAndDeregister();
									int done = downloadsDone.incrementAndGet();
									Platform.runLater(new Runnable() {
										public void run() {
											DownloadModsTask.this.updateTitle(String.format("Downloading mods (%d/%d)", done, max));
											DownloadModsTask.this.updateProgress(done, max);
										}
									});
								});
								downloadsPending.register();
								DOWNLOAD_EXECUTOR_SERVICE.execute(downloadTask);
							} else {
								CMPDL.progressPane.getController().log("!!! Unknown file %d:%d - skipping !!!", mod.getProjectId(), mod.getFileId());
							}
							return null;
						} finally {
							downloadsPending.arriveAndDeregister();
						}
					}
				};
				downloadsPending.register();
				DOWNLOAD_EXECUTOR_SERVICE.execute(resolveTask);
            }
			// wait for all files to be downloaded before closing the writer
			try {
				downloadsPending.awaitAdvance(downloadsPending.arrive());
				DOWNLOAD_EXECUTOR_SERVICE.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
				if (isCancelled()) {
					CMPDL.progressPane.getController().log("!!! Did NOT finish downloading. got cancelled !!!");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception occured during mods download", e);
		}
		long endTime = System.currentTimeMillis();
		CMPDL.progressPane.getController().log("Mods download took: " + (endTime - startTime) + " ms");
        return null;
    }


    private void setTask(Task<?> task) {
        Platform.runLater(() -> this.task.setValue(task));
    }

    public ReadOnlyObjectProperty<Task<?>> taskProperty() {
        return task;
    }
}
