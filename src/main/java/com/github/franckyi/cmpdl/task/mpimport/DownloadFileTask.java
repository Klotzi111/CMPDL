package com.github.franckyi.cmpdl.task.mpimport;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.jetbrains.annotations.NotNull;

import com.github.franckyi.cmpdl.CMPDL;
import com.github.franckyi.cmpdl.task.TaskBase;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Segment;

public class DownloadFileTask extends TaskBase<Void> {

	private static final int DOWNLOAD_CHUNK_SIZE = Segment.SIZE;

    private final String src;
    private final File dst;

    public DownloadFileTask(String src, File dst) {
        this.src = src;
        this.dst = dst;
    }

	public String getFileName() {
		return dst.getName();
	}

	// shows way to many warnings. But all should be fine
	@SuppressWarnings("resource")
	private void download(@NotNull String url, @NotNull File destFile) throws IOException {
		Request request = new Request.Builder().url(url).build();
		try (Response response = CMPDL.okHttpClient.newCall(request).execute()) {
			ResponseBody body = response.body();
			long contentLength = body.contentLength();
			BufferedSource source = body.source();

			try (BufferedSink sink = Okio.buffer(Okio.sink(destFile))) {
				Buffer sinkBuffer = sink.getBuffer();

				long totalBytesRead = 0;
				long bytesRead;
				while ((bytesRead = source.read(sinkBuffer, DOWNLOAD_CHUNK_SIZE)) != -1) {
					sink.emit();
					totalBytesRead += bytesRead;
					this.updateProgress(totalBytesRead, contentLength);
					if (isCancelled()) {
						return;
					}
				}
			}
		}
	}

    @Override
    protected Void call0() throws IOException, URISyntaxException {
        updateTitle(String.format("Downloading %s", dst.getName()));
		download(src, dst);
        return null;
    }
}
