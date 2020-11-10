package com.github.franckyi.cmpdl;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.franckyi.cmpdl.api.CMPDLConverterFactory;
import com.github.franckyi.cmpdl.api.TwitchAppAPI;
import com.github.franckyi.cmpdl.controller.DestinationPaneController;
import com.github.franckyi.cmpdl.controller.FilePaneController;
import com.github.franckyi.cmpdl.controller.MainWindowController;
import com.github.franckyi.cmpdl.controller.ModpackPaneController;
import com.github.franckyi.cmpdl.controller.ProgressPaneController;
import com.github.franckyi.cmpdl.core.ContentControllerView;
import com.github.franckyi.cmpdl.core.ControllerView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class CMPDL extends Application {

    public static final String NAME = "CMPDL";
    public static final String VERSION = "2.3.0";
    public static final String AUTHOR = "Franckyi";
    public static final String TITLE = String.format("%s v%s by %s", NAME, VERSION, AUTHOR);

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
	public static OkHttpClient okHttpClient;

    public static Stage stage;
    public static TwitchAppAPI api;

    public static ControllerView<MainWindowController> mainWindow;
    public static ContentControllerView<ModpackPaneController> modpackPane;
    public static ContentControllerView<FilePaneController> filePane;
    public static ContentControllerView<DestinationPaneController> destinationPane;
    public static ContentControllerView<ProgressPaneController> progressPane;

    public static ContentControllerView<?> currentContent;

    public static void main(String[] args) {
		okHttpClient = new OkHttpClient();
        api = new Retrofit.Builder()
			.client(okHttpClient)
            .baseUrl("https://addons-ecs.forgesvc.net/api/v2/")
            .addConverterFactory(CMPDLConverterFactory.create())
            .build()
            .create(TwitchAppAPI.class);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        modpackPane = new ContentControllerView<>("fxml/ModpackPane.fxml");
        filePane = new ContentControllerView<>("fxml/FilePane.fxml");
        destinationPane = new ContentControllerView<>("fxml/DestinationPane.fxml");
        progressPane = new ContentControllerView<>("fxml/ProgressPane.fxml");
        mainWindow = new ControllerView<>("fxml/MainWindow.fxml");
        stage.setScene(new Scene(mainWindow.getView()));
        stage.setTitle(TITLE);
        stage.setOnCloseRequest(e -> currentContent.getController().handleClose());
        stage.show();
    }

    @Override
    public void stop() {
        EXECUTOR_SERVICE.shutdown();
    }

    public static TwitchAppAPI getAPI() {
        return api;
    }

    public static void openBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            EXECUTOR_SERVICE.execute(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException | URISyntaxException e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Can't open URL", ButtonType.OK).show());
                    e.printStackTrace();
                }
            });
        } else {
            new Alert(Alert.AlertType.ERROR, "Desktop not supported", ButtonType.OK).show();
        }
    }
}
