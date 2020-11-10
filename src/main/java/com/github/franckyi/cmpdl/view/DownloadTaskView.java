package com.github.franckyi.cmpdl.view;

import com.github.franckyi.cmpdl.task.mpimport.DownloadFileTask;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DownloadTaskView extends ListCell<DownloadFileTask> {

	private final Label fileName = new Label();

	private final ProgressBar progressBar = new ProgressBar(0);

	private final ProgressIndicator progressIndicator = new ProgressIndicator(0);

	private final VBox hbox = new VBox();
	private final HBox line = new HBox(20);

    public DownloadTaskView() {
		progressBar.setPrefHeight(15);
		progressBar.setPrefWidth(350);
		progressIndicator.setPrefHeight(30);
		progressIndicator.setPrefWidth(30);
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(fileName, progressBar);
		line.setAlignment(Pos.CENTER);
		line.getChildren().addAll(hbox, progressIndicator);
    }

    @Override
	protected void updateItem(DownloadFileTask item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
			fileName.setText(item.getFileName());
			progressBar.progressProperty().bind(item.progressProperty());
			progressIndicator.progressProperty().bind(item.progressProperty());
			setGraphic(line);
        }
    }

}
