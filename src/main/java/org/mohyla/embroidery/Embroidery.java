package org.mohyla.embroidery;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Embroidery extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        EmbroideryModel model = new EmbroideryModel();
        EmbroideryView view = new EmbroideryView(model);
        EmbroideryController controller = new EmbroideryController(model, view);

        Scene scene = new Scene(view.getRoot(), EmbroideryConstants.WINDOW_SIZE, EmbroideryConstants.WINDOW_SIZE);
        try {
            Paint paint = new ImagePattern(new Image(getClass().getResource("/canvas.jpg").toExternalForm()));
            scene.setFill(paint);
        } catch (Exception e) {
            scene.setFill(Color.BEIGE);
        }

        stage.setTitle("Вишивка (Дарина Ансімова)");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        controller.initialize();
    }
}

class EmbroideryConstants {
    public static final int WINDOW_SIZE = 600;
    public static final int EMBROIDERY_SIZE = 11;
    public static final int CROSS_SIZE = 15;
    public static final double MIN_SIZE = 1;

    private static final boolean[][] LETTER_D = {
            {},
            {},
            {},
            {},
            {},
            {true, false, false, false, false, false, false, false, false, false, false},
            {false, true, false, false, false, false, false, false, false, false, false},
            {false, false, true, false, false, false, false, false, false, false, false},
            {false, false, false, true, false, false, false, false, false, false, false},
            {false, false, false, false, true, false, false, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false}};
    private static final boolean[][] LETTER_A = {
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            {true, false, false, false, false, false, false, false, false, false, false},
            {true, true, false, false, false, false, false, false, false, false, false},
            {false, false, true, false, false, false, false, false, false, false, false},
            {false, false, true, true, false, false, false, false, false, false, false}};
    private static final boolean[][] LETTER_R = {
            {},
            {},
            {},
            {},
            {},
            {false, false, false, true, true, true, false, false, false, false, false},
            {false, false, false, false, true, true, false, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {},
            {},
            {}};
    private static final boolean[][] LETTER_Y = {
            {},
            {false, false, false, false, false, false, true, false, false, true, false},
            {false, false, false, false, false, false, true, false, true, false, false},
            {false, false, false, false, false, false, false, true, false, false, false},
            {false, false, false, false, false, false, true, false, true, true, false},
            {},
            {},
            {},
            {},
            {},
            {}};
    private static final boolean[][] LETTER_N = {
            {false, false, false, false, false, false, false, true, true, true, true},
            {false, false, false, false, false, false, false, false, false, true, true},
            {false, false, false, false, false, false, false, false, false, false, true},
            {false, false, false, false, false, false, false, false, false, false, true},
            {},
            {},
            {},
            {},
            {},
            {},
            {}};
    public static final boolean[][][] NAME = {LETTER_D, LETTER_A, LETTER_R, LETTER_Y, LETTER_N, LETTER_A};
}

class EmbroideryModel {
    private Color mainColor = Color.BLACK;
    private Color additionalColor = Color.DARKRED;
    private Group mainPattern = new Group();
    private Group additionalPattern = new Group();
    private SequentialTransition animation = new SequentialTransition();
    private Group editTools;
    private EditMode editMode = EditMode.PAINT;

    public enum EditMode {
        PAINT, ERASE
    }

    public Group getEditTools() {
        return editTools;
    }

    public void setEditTools(Group editTools) {
        this.editTools = editTools;
    }

    public EditMode getEditMode() {
        return editMode;
    }

    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }

    public Color getMainColor() {
        return mainColor;
    }

    public void setMainColor(Color mainColor) {
        this.mainColor = mainColor;
    }

    public Color getAdditionalColor() {
        return additionalColor;
    }

    public void setAdditionalColor(Color additionalColor) {
        this.additionalColor = additionalColor;
    }

    public Group getMainPattern() {
        return mainPattern;
    }

    public Group getAdditionalPattern() {
        return additionalPattern;
    }

    public SequentialTransition getAnimation() {
        return animation;
    }

    public void toggleColors() {
        if (mainColor == Color.BLACK && additionalColor == Color.DARKRED) {
            mainColor = Color.ROYALBLUE;
            additionalColor = Color.GOLD;
        } else if (mainColor == Color.ROYALBLUE && additionalColor == Color.GOLD) {
            mainColor = Color.BLACK;
            additionalColor = Color.DARKRED;
        }
    }
}

class EmbroideryView {
    private final Group root = new Group();
    private final Text titleText;
    private final Button mirrorXButton;
    private final Button mirrorYButton;
    private final Button colorButton;
    private final Button saveButton;
    private final Button loadButton;
    private final Button editButton;

    public EmbroideryView(EmbroideryModel model) {
        // Initialize title text
        titleText = new Text("Дарина");
        titleText.setFont(new Font("Helvetica", 50));
        titleText.setFill(model.getAdditionalColor());
        titleText.setStyle("-fx-font-weight: bold");

        // Initialize buttons
        mirrorXButton = new Button("Відобразити горизонтально");
        mirrorXButton.setFont(new Font("Helvetica", 10));
        mirrorXButton.setDisable(true);

        mirrorYButton = new Button("Відобразити вертикально");
        mirrorYButton.setFont(new Font("Helvetica", 10));
        mirrorYButton.setDisable(true);

        colorButton = new Button("Змінити колір");
        colorButton.setFont(new Font("Helvetica", 10));

        saveButton = new Button("Зберегти PNG");
        saveButton.setFont(new Font("Helvetica", 10));
        saveButton.setDisable(true);

        loadButton = new Button("Відкрити PNG");
        loadButton.setFont(new Font("Helvetica", 10));
        loadButton.setDisable(true);

        editButton = new Button("Редагувати");
        editButton.setFont(new Font("Helvetica", 10));
        editButton.setDisable(true);

        // Add all elements to root
        root.getChildren().addAll(
                titleText,
                model.getMainPattern(),
                model.getAdditionalPattern(),
                mirrorXButton,
                mirrorYButton,
                colorButton,
                saveButton,
                loadButton,
                editButton
        );

        // Position elements
        Platform.runLater(this::positionElements);
    }

    private void positionElements() {
        // Position title text
        titleText.setX((double) EmbroideryConstants.WINDOW_SIZE / 2 - titleText.getBoundsInLocal().getWidth() / 2);
        titleText.setY(titleText.getBoundsInLocal().getHeight());

        // Position buttons
        double baseY = (double) (EmbroideryConstants.WINDOW_SIZE * 4) / 5;

        mirrorXButton.setLayoutX((double) (EmbroideryConstants.WINDOW_SIZE / 3) - mirrorXButton.getWidth() / 2);
        mirrorXButton.setLayoutY(baseY);

        mirrorYButton.setLayoutX((double) (EmbroideryConstants.WINDOW_SIZE / 3) - mirrorYButton.getWidth() / 2);
        mirrorYButton.setLayoutY(mirrorXButton.getLayoutY() + mirrorXButton.getHeight() + 5);

        editButton.setLayoutX((double) (EmbroideryConstants.WINDOW_SIZE / 3) - editButton.getWidth() / 2);
        editButton.setLayoutY(mirrorYButton.getLayoutY() + editButton.getHeight() + 5);

        colorButton.setLayoutX((double) (EmbroideryConstants.WINDOW_SIZE * 2 / 3) - colorButton.getWidth() / 2);
        colorButton.setLayoutY(baseY);

        saveButton.setLayoutX((double) (EmbroideryConstants.WINDOW_SIZE * 2 / 3) - saveButton.getWidth() / 2);
        saveButton.setLayoutY(colorButton.getLayoutY() + colorButton.getHeight() + 5);

        loadButton.setLayoutX((double) (EmbroideryConstants.WINDOW_SIZE * 2 / 3) - loadButton.getWidth() / 2);
        loadButton.setLayoutY(saveButton.getLayoutY() + saveButton.getHeight() + 5);
    }

    public Group getRoot() {
        return root;
    }

    public Button getMirrorXButton() {
        return mirrorXButton;
    }

    public Button getMirrorYButton() {
        return mirrorYButton;
    }

    public Button getColorButton() {
        return colorButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getLoadButton() {
        return loadButton;
    }

    public Button getEditButton() {
        return editButton;
    }

    public void updateTitleColor(Color color) {
        titleText.setFill(color);
    }
}

class EmbroideryController {
    private final EmbroideryModel model;
    private final EmbroideryView view;

    public EmbroideryController(EmbroideryModel model, EmbroideryView view) {
        this.model = model;
        this.view = view;
    }

    public void initialize() {
        setupEventHandlers();
        createInitialPattern();
    }

    private void setupEventHandlers() {
        view.getMirrorXButton().setOnAction(event -> mirrorX());
        view.getMirrorYButton().setOnAction(event -> mirrorY());
        view.getColorButton().setOnAction(event -> changeColor());
        view.getSaveButton().setOnAction(event -> savePattern());
        view.getLoadButton().setOnAction(event -> loadPattern());
        view.getEditButton().setOnAction(event -> editPattern());
    }

    private void editPattern() {
        if (view.getEditButton().getText().equals("Редагувати")) {
            // Enter edit mode
            view.getEditButton().setText("Готово");

            // Disable other buttons while editing
            view.getMirrorXButton().setDisable(true);
            view.getMirrorYButton().setDisable(true);
            view.getColorButton().setDisable(true);
            view.getSaveButton().setDisable(true);
            view.getLoadButton().setDisable(true);

            // Create editing tools
            Group editTools = new Group();

            // Create a fine grid overlay
            Group gridOverlay = new Group();
            for (int i = ((int)model.getMainPattern().getBoundsInLocal().getMinX() % EmbroideryConstants.CROSS_SIZE); i < EmbroideryConstants.WINDOW_SIZE; i += EmbroideryConstants.CROSS_SIZE) {
                for (int j = ((int)model.getMainPattern().getBoundsInLocal().getMinY() % EmbroideryConstants.CROSS_SIZE); j < EmbroideryConstants.WINDOW_SIZE; j += EmbroideryConstants.CROSS_SIZE) {
                    Rectangle gridCell = new Rectangle(i, j, 1, 1);
                    gridCell.setFill(Color.BLACK);
                    gridOverlay.getChildren().add(gridCell);
                }
            }

            // Create color picker
            ColorPicker colorPicker = new ColorPicker(model.getMainColor());
            colorPicker.setLayoutX(10);
            colorPicker.setLayoutY(10);

            // Create rubber (eraser) button
            Button rubberButton = new Button("Гумка");
            rubberButton.setLayoutX(10);
            rubberButton.setLayoutY(50);

            // Add tools to edit group
            editTools.getChildren().addAll(gridOverlay, colorPicker, rubberButton);

            // Store reference to edit tools in model
            model.setEditTools(editTools);

            // Add edit tools to root
            view.getRoot().getChildren().add(editTools);

            // Set up mouse interaction for editing
            model.getMainPattern().setOnMousePressed(e -> {
                if (e.getTarget() instanceof Rectangle) {
                    Rectangle target = (Rectangle) e.getTarget();
                    if (e.isShiftDown() || model.getEditMode()== EmbroideryModel.EditMode.ERASE) { // Use rubber when Shift is pressed
                        target.setFill(Color.TRANSPARENT);
                    } else {
                        target.setFill(colorPicker.getValue());
                    }
                }
            });

            // Set up rubber button action
            rubberButton.setOnAction(e -> {
                model.setEditMode(EmbroideryModel.EditMode.ERASE);
            });

            // Set up color picker change listener
            colorPicker.setOnAction(e -> {
                model.setEditMode(EmbroideryModel.EditMode.PAINT);
            });

        } else {
            // Exit edit mode
            view.getEditButton().setText("Редагувати");

            // Re-enable other buttons
            view.getMirrorXButton().setDisable(false);
            view.getMirrorYButton().setDisable(false);
            view.getColorButton().setDisable(false);
            view.getSaveButton().setDisable(false);
            view.getLoadButton().setDisable(false);
            view.getEditButton().setDisable(false);

            // Remove edit tools
            view.getRoot().getChildren().remove(model.getEditTools());

            // Remove mouse handlers
            model.getMainPattern().setOnMouseClicked(null);
        }
    }

    private void loadPattern() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Відкрити схему вишивки");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                if(image.getHeight()!=image.getWidth()) {
                    showAlert("Помилка","Зображення має бути квадратним!");
                    return;
                }
                if(file.length()>300*1024) {
                    showAlert("Помилка","Зображення має бути не більше 300 КБ!");
                    return;
                }
                model.getAnimation().getChildren().clear();
                processLoadedImage(image);
            } catch (Exception e) {
                showAlert("Помилка завантаження",
                        "Не вдалося завантажити зображення: " + e.getMessage());
            }
        }
    }

    private void processLoadedImage(Image image) {
        // Очищаємо поточний візерунок
        model.getMainPattern().getChildren().clear();
        model.getMainPattern().setScaleX(1);
        model.getMainPattern().setScaleY(1);
        model.getMainPattern().setLayoutX(0);
        model.getMainPattern().setLayoutY(0);
        model.getAnimation().getChildren().clear();
        view.getColorButton().setDisable(true);

        // Визначаємо розміри зображення
        int imgWidth = (int)image.getWidth();
        int imgHeight = (int)image.getHeight();

        // Створюємо PixelReader для читання кольорів
        PixelReader pixelReader = image.getPixelReader();

        // Визначаємо крок для вибірки пікселів (враховуємо розмір хрестика)
        int step = (imgWidth/21);

        for (int y = 0; y < imgHeight-imgHeight/21; y += step) {
            for (int x = 0; x < imgWidth-imgWidth/21; x += step) {
                // Отримуємо колір центрального пікселя області
                Color color = pixelReader.getColor(
                        Math.min(x + step/2, imgWidth-1),
                        Math.min(y + step/2, imgHeight-1));
                    // Створюємо хрестик вишивки
                Rectangle rectangle = createCrossStitch(
                        EmbroideryConstants.WINDOW_SIZE / 2 - EmbroideryConstants.CROSS_SIZE*21/2 + EmbroideryConstants.CROSS_SIZE * (x / step),
                        EmbroideryConstants.WINDOW_SIZE / 2 - EmbroideryConstants.CROSS_SIZE*21/2 + EmbroideryConstants.CROSS_SIZE * (y / step),
                        color);

                // Додаємо до основного візерунку
                model.getMainPattern().getChildren().add(rectangle);
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createInitialPattern() {
        // Create name pattern
        for (boolean[][] letter : EmbroideryConstants.NAME) {
            addPatternToGroup(model.getMainPattern(), letter, model.getMainColor());
        }

        // Create additional pattern
        boolean[][] mainPattern = createMainPattern();
        boolean[][] additionalPattern = createAdditionalPattern(mainPattern);
        boolean[][] backgroundPattern = createBackgroundPattern(mainPattern, additionalPattern);
        addPatternToGroup(model.getMainPattern(), additionalPattern, model.getAdditionalColor());
        addPatternToGroup(model.getMainPattern(), backgroundPattern, Color.TRANSPARENT);

        // Mirror patterns
        mirrorGroup(model.getMainPattern(), false, true, true);
        mirrorGroup(model.getMainPattern(), true, true, true);

        // Play animation
        model.getAnimation().play();
        model.getAnimation().setOnFinished(event -> {
            view.getMirrorXButton().setDisable(false);
            view.getMirrorYButton().setDisable(false);
            view.getSaveButton().setDisable(false);
            view.getLoadButton().setDisable(false);
            view.getEditButton().setDisable(false);
        });
    }

    private void addPatternToGroup(Group group, boolean[][] pattern, Color color) {
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length; j++) {
                if (pattern[i][j]) {
                    Rectangle rectangle = createCrossStitch(
                            (double) EmbroideryConstants.WINDOW_SIZE / 2 + EmbroideryConstants.CROSS_SIZE * j,
                            (double) EmbroideryConstants.WINDOW_SIZE / 2 - (double) ((EmbroideryConstants.EMBROIDERY_SIZE * 2 - 1) * EmbroideryConstants.CROSS_SIZE) / 2 + EmbroideryConstants.CROSS_SIZE * i,
                            color
                    );
                    group.getChildren().add(rectangle);
                    addFadeAnimation(rectangle);
                }
            }
        }
    }

    private Rectangle createCrossStitch(double x, double y, Color color) {
        Rectangle rectangle = new Rectangle(EmbroideryConstants.CROSS_SIZE, EmbroideryConstants.CROSS_SIZE);
        rectangle.setFill(color);
        rectangle.setX(x);
        rectangle.setY(y);
        return rectangle;
    }

    private void addFadeAnimation(Node node) {
        FadeTransition transition = new FadeTransition(Duration.millis(100), node);
        transition.setFromValue(0);
        transition.setToValue(1);
        model.getAnimation().getChildren().add(transition);
    }

    private boolean[][] createMainPattern() {
        boolean[][] pattern = new boolean[EmbroideryConstants.EMBROIDERY_SIZE][EmbroideryConstants.EMBROIDERY_SIZE];
        for (boolean[][] letter : EmbroideryConstants.NAME) {
            for (int i = 0; i < letter.length; i++) {
                for (int j = 0; j < letter[i].length; j++) {
                    if (letter[i][j]) {
                        pattern[i][j] = true;
                    }
                }
            }
        }
        return pattern;
    }

    private boolean[][] createAdditionalPattern(boolean[][] mainPattern) {
        boolean[][] additional = new boolean[EmbroideryConstants.EMBROIDERY_SIZE][EmbroideryConstants.EMBROIDERY_SIZE];
        for (int i = 0; i < mainPattern.length; i++) {
            for (int j = 0; j < mainPattern[i].length; j++) {
                if (!mainPattern[i][j] && Math.random() > 0.7) {
                    additional[i][j] = true;
                }
            }
        }
        return additional;
    }
    private boolean[][] createBackgroundPattern(boolean[][] mainPattern, boolean[][] additionalPattern) {
        boolean[][] background = new boolean[EmbroideryConstants.EMBROIDERY_SIZE][EmbroideryConstants.EMBROIDERY_SIZE];
        for (int i = 0; i < mainPattern.length; i++) {
            for (int j = 0; j < mainPattern[i].length; j++) {
                if (!mainPattern[i][j] && !additionalPattern[i][j]) {
                    background[i][j] = true;
                }
            }
        }
        return background;
    }

    private void mirrorX() {
        Group main = model.getMainPattern();
        double centerXBefore = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
        double centerYBefore = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;
        double height = main.getBoundsInParent().getHeight();
        double width = main.getBoundsInParent().getWidth();
        double scaleOld = main.getScaleX();

        if (height * 2 > width) {
            main.setScaleX(main.getScaleX() * 0.5);
            main.setScaleY(main.getScaleY() * 0.5);
        }

        mirrorGroup(main, false, true, false);

        double centerXAfter = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
        double centerYAfter = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;

        main.setLayoutX(main.getLayoutX() + (centerXBefore - centerXAfter));
        main.setLayoutY(main.getLayoutY() + (centerYBefore - centerYAfter));

        if ((main.getScaleX() < scaleOld
                || (main.getScaleX() == scaleOld && main.getBoundsInParent().getHeight() * 2 > main.getBoundsInParent().getWidth()))
                && (main.getScaleX() * EmbroideryConstants.CROSS_SIZE < EmbroideryConstants.MIN_SIZE
                || main.getScaleY() * EmbroideryConstants.CROSS_SIZE < EmbroideryConstants.MIN_SIZE)) {
            view.getMirrorXButton().setDisable(true);
        }
    }

    private void mirrorY() {
        Group main = model.getMainPattern();
        double centerXBefore = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
        double centerYBefore = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;
        double height = main.getBoundsInParent().getHeight();
        double width = main.getBoundsInParent().getWidth();
        double scaleOld = main.getScaleY();

        if (width * 2 > height) {
            main.setScaleX(main.getScaleX() * 0.5);
            main.setScaleY(main.getScaleY() * 0.5);
        }

        mirrorGroup(main, true, true, false);

        double centerXAfter = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
        double centerYAfter = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;

        main.setLayoutX(main.getLayoutX() + (centerXBefore - centerXAfter));
        main.setLayoutY(main.getLayoutY() + (centerYBefore - centerYAfter));

        if ((main.getScaleY() < scaleOld
                || (main.getScaleY() == scaleOld && main.getBoundsInParent().getWidth() * 2 > main.getBoundsInParent().getHeight()))
                && (main.getScaleX() * EmbroideryConstants.CROSS_SIZE < EmbroideryConstants.MIN_SIZE
                || main.getScaleY() * EmbroideryConstants.CROSS_SIZE < EmbroideryConstants.MIN_SIZE)) {
            view.getMirrorYButton().setDisable(true);
        }
    }

    private void mirrorGroup(Group group, boolean mirrorY, boolean includeCenter, boolean doTransition) {
        Bounds bounds = group.getBoundsInLocal();
        double centerX = (bounds.getMinX() + bounds.getMaxX()) / 2;
        double centerY = (bounds.getMinY() + bounds.getMaxY()) / 2;
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        List<Node> originalChildren = new ArrayList<>(group.getChildren());
        for (Node node : originalChildren) {
            if (node instanceof Rectangle r) {
                Paint paint = r.getFill();
                Rectangle mirroredRect;

                if (mirrorY) {
                    // Mirror across Y axis (vertical)
                    double mirroredX = 2 * centerX - r.getX() - r.getWidth()
                            - width
                            + (includeCenter ? 0 : r.getWidth());
                    mirroredRect = new Rectangle(mirroredX, r.getY(), r.getWidth(), r.getHeight());
                } else {
                    // Mirror across X axis (horizontal)
                    double mirroredY = 2 * centerY - r.getY() - r.getHeight()
                            + height
                            - (includeCenter ? 0 : r.getHeight());
                    mirroredRect = new Rectangle(r.getX(), mirroredY, r.getWidth(), r.getHeight());
                }

                mirroredRect.setFill(paint);
                group.getChildren().add(mirroredRect);

                if (doTransition) {
                    FadeTransition tr = new FadeTransition(Duration.millis(50), mirroredRect);
                    tr.setFromValue(0);
                    tr.setToValue(1);
                    model.getAnimation().getChildren().add(tr);
                }
            }
        }
    }

    private void changeColor() {
        model.toggleColors();
        view.updateTitleColor(model.getAdditionalColor());

        for (Node node : model.getMainPattern().getChildren()) {
            if (node instanceof Rectangle r) {
                Paint paint = r.getFill();
                if (paint instanceof Color) {
                    if (paint == Color.BLACK) {
                        r.setFill(Color.ROYALBLUE);
                    } else if (paint == Color.DARKRED) {
                        r.setFill(Color.GOLD);
                    } else if (paint == Color.ROYALBLUE) {
                        r.setFill(Color.BLACK);
                    } else if (paint == Color.GOLD) {
                        r.setFill(Color.DARKRED);
                    }
                }
            }
        }
    }

    private void savePattern() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PNG");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            saveNodeAsHighResPng(model.getMainPattern(), 10.0, file);
        }
    }

    private void saveNodeAsHighResPng(Node node, double scaleFactor, File outputFile) {
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(new Scale(scaleFactor, scaleFactor));

        WritableImage image = node.snapshot(params, null);
        PixelReader reader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        int[] buffer = new int[width * height];
        reader.getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), buffer, 0, width);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, width, height, buffer, 0, width);

        try {
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}