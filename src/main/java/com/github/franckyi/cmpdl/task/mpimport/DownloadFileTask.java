package com.github.franckyi.cmpdl.task.mpimport;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

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

	@Override
	protected Optional<Void> call() throws Exception {
		try {
			return Optional.ofNullable(call0());
		} catch (Throwable t) {
			CMPDL.progressPane.getController().log("!!! Failed to download file !!! src=" + src + ", dst=" + dst);
			if (t instanceof Exception) {
				throw (Exception) t;
			} else {
				throw new Exception(t);
			}
		}
	}


	// shows way to many warnings. But all should be fine
	@SuppressWarnings("resource")
	private void download(@NotNull String url, @NotNull File destFile) throws IOException {
		Request request = new Request.Builder().url(url).build();
		if (isCancelled()) {
			throw new IOException();
		}
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
						throw new IOException();
					}
				}
			}
		}
	}

    @Override
	protected Void call0() throws Throwable {
		try {
			// boolean throwE = false;
			// if (throwE) {
			// throw new IOException();
			// }
			download(src, dst);
		} catch (Throwable e) {
			throw new Exception("Downloading file failed! src=" + src + ", dst=" + dst, e);
		}
        return null;
    }
}
