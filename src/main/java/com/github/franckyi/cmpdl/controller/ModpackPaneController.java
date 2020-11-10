package com.github.franckyi.cmpdl.controller;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import com.github.franckyi.cmpdl.CMPDL;
import com.github.franckyi.cmpdl.api.response.Addon;
import com.github.franckyi.cmpdl.task.api.CallTask;
import com.github.franckyi.cmpdl.task.api.GetProjectIdTask;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class ModpackPaneController implements Initializable, IContentController {

    @FXML
    private TextField urlField;

    @FXML
    private TextField idField;

    @FXML
    private TextField zipField;

    @FXML
    private TextField destinationField;

    @FXML
    private RadioButton urlButton;

    @FXML
    private RadioButton idButton;

    @FXML
    private RadioButton zipButton;

    @FXML
    private Button chooseSourceButton;

    @FXML
    private Button chooseDestinationButton;

    @FXML
    private ToggleGroup modpack;

    public RadioButton getZipButton() {
        return zipButton;
    }

	private void updateUrlFieldDisabled(String newValue) {
		boolean valid = false;
		try {
			new URL(newValue.trim());
			valid = true;
		} catch (MalformedURLException ignore) {
		}
		CMPDL.mainWindow.getController().getNextButton().setDisable(!valid);
	}

	private void updateIdFieldDisabled(String newValue) {
		boolean valid = false;
		try {
			Integer.parseInt(idField.getText().trim());
			valid = true;
		} catch (NumberFormatException ignore) {
		}
		CMPDL.mainWindow.getController().getNextButton().setDisable(!valid);
	}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        urlField.disableProperty().bind(urlButton.selectedProperty().not());
		urlField.textProperty().addListener((o, oldValue, newValue) -> {
			updateUrlFieldDisabled(newValue);
		});
		urlButton.selectedProperty().addListener((o, oldValue, newValue) -> {
			if (newValue) {
				updateUrlFieldDisabled(urlField.getText());
			}
		});
        idField.disableProperty().bind(idButton.selectedProperty().not());
		idField.textProperty().addListener((o, oldValue, newValue) -> {
			updateIdFieldDisabled(newValue);
		});
		idButton.selectedProperty().addListener((o, oldValue, newValue) -> {
			if (newValue) {
				updateIdFieldDisabled(idField.getText());
			}
		});
        zipField.disableProperty().bind(zipButton.selectedProperty().not());
		zipButton.selectedProperty().addListener((o, oldValue, newValue) -> {
			CMPDL.mainWindow.getController().getNextButton().setDisable(newValue);
		});
        destinationField.disableProperty().bind(zipButton.selectedProperty().not());
        chooseSourceButton.disableProperty().bind(zipButton.selectedProperty().not());
        chooseDestinationButton.disableProperty().bind(zipButton.selectedProperty().not());
    }

    @Override
    public void handleNext() {
        if (modpack.getSelectedToggle() == urlButton) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Parsing project ID...", ButtonType.CLOSE);
            alert.show();
			GetProjectIdTask task = new GetProjectIdTask(urlField.getText().trim());
            task.setOnSucceeded(event -> {
                alert.hide();
                handleNext(task.getValue().orElse(-1));
            });
            CMPDL.EXECUTOR_SERVICE.execute(task);
        } else if (modpack.getSelectedToggle() == idButton) {
            int projectId = -1;
            try {
				projectId = Integer.parseInt(idField.getText().trim());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "The project ID must be an integer", ButtonType.OK).show();
            }
            handleNext(projectId);
        }
    }

    @Override
    public void handlePrevious() {

    }

    @Override
    public void handleStart() {
        if (modpack.getSelectedToggle() == zipButton) {
            File zipFile = new File(zipField.getText());
            File dstFolder = new File(destinationField.getText());
            if (zipFile.exists()) {
                dstFolder.mkdirs();
                CMPDL.mainWindow.getController().getStartButton().disableProperty().unbind();
                CMPDL.mainWindow.getController().getNextButton().disableProperty().unbind();
                CMPDL.mainWindow.getController().getStartButton().setDisable(true);
                CMPDL.progressPane.getController().setData(zipFile, dstFolder);
                CMPDL.progressPane.getController().unzipModpack();
                CMPDL.mainWindow.getController().setContent(CMPDL.progressPane);
            }
        }
    }

    public void handleNext(int addonId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Loading project data...", ButtonType.CLOSE);
        alert.show();
        CallTask<Addon> task = new CallTask<>(String.format("Getting project data for project %d", addonId), CMPDL.getAPI().getAddon(addonId));
		task.setOnSucceeded(event -> {
			task.getValue().ifPresentOrElse(addon -> Platform.runLater(() -> {
				if (addon.getCategorySection().getName().equals("Modpacks")) {
					CMPDL.mainWindow.getController().getStartButton().disableProperty().unbind();
					CMPDL.mainWindow.getController().getNextButton().disableProperty().unbind();
					CMPDL.filePane.getController().setAddon(addon);
					CMPDL.filePane.getController().viewLatestFiles();
					CMPDL.mainWindow.getController().setContent(CMPDL.filePane);
					alert.hide();
				} else {
					alert.hide();
					new Alert(Alert.AlertType.ERROR, "The addon isn't a modpack !", ButtonType.OK).show();
				}
			}), () -> alert.hide());
		});
		task.setOnFailed(event -> alert.hide());
		task.setOnCancelled(event -> alert.hide());
        CMPDL.EXECUTOR_SERVICE.execute(task);
    }

    @FXML
    void actionChooseSource(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose the modpack file :");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Modpack archive", "*.zip"));
        File src = fc.showOpenDialog(CMPDL.stage);
        if (src != null) {
            zipField.setText(src.getAbsolutePath());
        }
    }

    @FXML
    void actionChooseDestination(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose the destination folder :");
        File dst = dc.showDialog(CMPDL.stage);
        if (dst != null) {
            destinationField.setText(dst.getAbsolutePath());
        }
    }
}
