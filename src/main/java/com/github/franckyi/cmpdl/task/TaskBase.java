package com.github.franckyi.cmpdl.task;

import java.util.Optional;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.stage.Modality;

public abstract class TaskBase<V> extends Task<Optional<V>> {

    @Override
	protected Optional<V> call() throws Exception {
        try {
            return Optional.ofNullable(call0());
        } catch (Throwable t) {
			Platform.runLater(() -> {
				Alert alert = new Alert(Alert.AlertType.ERROR, t.getMessage());
				alert.initModality(Modality.NONE);
				alert.show();
			});
			if (t instanceof Exception) {
				throw (Exception) t;
			} else {
				throw new Exception(t);
			}
        }
    }

    protected abstract V call0() throws Throwable;

}
