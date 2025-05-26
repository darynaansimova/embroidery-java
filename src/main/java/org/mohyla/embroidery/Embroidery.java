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
import javafx.scene.control.Button;
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

    private static final int WINDOW_SIZE = 600;

    private static final int EMBROIDERY_SIZE = 11;
    private static final int CROSS_SIZE = 15;
    private static final double MIN_SIZE = 1;
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
    private static final boolean[][][] NAME = {LETTER_D, LETTER_A, LETTER_R, LETTER_Y, LETTER_N, LETTER_A};
    private static final Group root = new Group();
    private static Group main = new Group();
    private static final Group additional = new Group();
    private static final Button buttonX = new Button();
    private static final Button buttonY = new Button();
    private static final Button buttonColor = new Button();
    private static final Button buttonSave = new Button();
    SequentialTransition transition = new SequentialTransition();
    private static Color colorMain = Color.BLACK;
    private static Color colorAdditional = Color.DARKRED;
    @Override
    public void start(Stage stage){
        stage.setTitle("Вишивка");
        stage.setResizable(false);
        Paint paint = Color.BEIGE;
        embroideryRoot();
        try{
            paint = new ImagePattern(new Image(getClass().getResource("/canvas.jpg").toExternalForm()));
        } catch (Exception e) {} finally {
            Scene scene = new Scene(root, WINDOW_SIZE, WINDOW_SIZE, paint);
            stage.setScene(scene);
            stage.show();
        }
    }

    private void embroideryRoot(){
        Text text = new Text("Дарина");
        text.setFont(new Font("Helvetica", 50));
        text.setFill(colorAdditional);
        text.setStyle("-fx-font-weight: bold");
        text.setX((double) WINDOW_SIZE/2 - text.getBoundsInLocal().getWidth() / 2);
        text.setY(text.getBoundsInLocal().getHeight());
        root.getChildren().add(text);

        initButtons();

        root.getChildren().add(main);
        root.getChildren().add(additional);

        for (boolean[][] oneLetter : NAME) {
            main = embroidery(main, oneLetter, colorMain);
        }

        boolean[][] mainP = mainPattern();
        boolean[][] addP = additionalPattern(mainP);

        main = embroidery(main, addP, colorAdditional);

        main = mirrorX(main, false, true);

        main = mirrorY(main, false, true);

        transition.play();

        transition.setOnFinished(event -> {
                    buttonX.setDisable(false);
                    buttonY.setDisable(false);
                    buttonSave.setDisable(false);
                });
    }

    private Group mirrorX(Group original, boolean includeCenter, boolean doTransition) {
        Bounds bounds = original.getBoundsInLocal();
        double centerY = (bounds.getMinY() + bounds.getMaxY()) / 2;
        double height = bounds.getHeight();

        // Створюємо копію списку дітей
        List<Node> originalChildren = new ArrayList<>(original.getChildren());

        for (Node node : originalChildren) {
            if (node instanceof Rectangle r) {
                Paint paint = r.getFill();

                // Mirror across centerY
                double mirroredY = 2 * centerY - r.getY() - r.getHeight()
                        + height
                        - (includeCenter ? 0 : r.getHeight());

                Rectangle mirroredRect = new Rectangle(r.getX(), mirroredY, r.getWidth(), r.getHeight());
                mirroredRect.setFill(paint);

                original.getChildren().add(mirroredRect);

                if(doTransition) {
                    FadeTransition tr = new FadeTransition(Duration.millis(50), mirroredRect);
                    tr.setFromValue(0);
                    tr.setToValue(1);
                    transition.getChildren().add(tr);
                }
            }
        }

        return original;
    }

    private Group mirrorY(Group original, boolean includeCenter, boolean doTransition) {
        Bounds bounds = original.getBoundsInLocal();
        double centerX = (bounds.getMinX() + bounds.getMaxX()) / 2;
        double width = bounds.getWidth();

        // Створюємо копію списку дітей
        List<Node> originalChildren = new ArrayList<>(original.getChildren());

        for (Node node : originalChildren) {
            if (node instanceof Rectangle r) {
                Paint paint = r.getFill();

                // Mirror across centerX (по вертикальній осі)
                double mirroredX = 2 * centerX - r.getX() - r.getWidth()
                        - width
                        + (includeCenter ? 0 : r.getWidth());

                Rectangle mirroredRect = new Rectangle(mirroredX, r.getY(), r.getWidth(), r.getHeight());
                mirroredRect.setFill(paint);

                original.getChildren().add(mirroredRect);

                if(doTransition) {
                    FadeTransition tr = new FadeTransition(Duration.millis(50), mirroredRect);
                    tr.setFromValue(0);
                    tr.setToValue(1);
                    transition.getChildren().add(tr);
                }
            }
        }

        return original;
    }



    private boolean[][] mainPattern(){
        boolean[][] pattern = new boolean[EMBROIDERY_SIZE][EMBROIDERY_SIZE];
        for(int l = 0; l <NAME.length; l++){
            for(int m = 0; m <NAME[l].length; m++){
                for(int k = 0; k<NAME[l][m].length; k++){
                    if(NAME[l][m][k]){
                        pattern[m][k] = true;
                    }
                }
            }
        }
        return pattern;
    }

    private boolean[][] additionalPattern(boolean[][] pattern){
        boolean[][] additional = new boolean[EMBROIDERY_SIZE][EMBROIDERY_SIZE];
        for(int i = 0; i < pattern.length; i++){
            for(int j = 0; j < pattern[i].length; j++){
                if(!pattern[i][j]){
                    if(Math.random()>0.7){
                        additional[i][j] = true;
                    }
                }
                else{
                    additional[i][j] = false;
                }
            }
        }
        return additional;
    }

    private Group embroidery(Group group, boolean[][] pattern, Color color){
        for(int i = 0; i < pattern.length; i++){
            for(int j = 0; j < pattern[i].length; j++){
                if(pattern[i][j]){
                    Rectangle rectangle = new Rectangle(CROSS_SIZE, CROSS_SIZE);
                    rectangle.setFill(color);
                    rectangle.setX((double) WINDOW_SIZE / 2 + CROSS_SIZE*j);
                    rectangle.setY((double) WINDOW_SIZE / 2 - (double) ((EMBROIDERY_SIZE*2-1) * CROSS_SIZE) / 2 + CROSS_SIZE*i);
                    group.getChildren().add(rectangle);
                    FadeTransition tr = new FadeTransition(Duration.millis(100), rectangle);
                    tr.setFromValue(0);
                    tr.setToValue(1);
                    transition.getChildren().add(tr);
                }
            }
        }
        return group;
    }

    private void initButtons(){
        buttonX.setText("Відобразити горизонтально");
        buttonX.setFont(new Font("Helvetica", 10));
        buttonX.setDisable(true);
        root.getChildren().add(buttonX);

        buttonY.setText("Відобразити вертикально");
        buttonY.setFont(new Font("Helvetica", 10));
        buttonY.setDisable(true);
        root.getChildren().add(buttonY);

        buttonColor.setText("Змінити колір");
        buttonColor.setFont(new Font("Helvetica", 10));
        root.getChildren().add(buttonColor);

        buttonSave.setText("Зберегти PNG");
        buttonSave.setFont(new Font("Helvetica", 10));
        buttonSave.setDisable(true);
        root.getChildren().add(buttonSave);

// Відкласти позиціонування до моменту, коли розміри вже будуть обчислені
        Platform.runLater(() -> {
            double baseY = (double) (WINDOW_SIZE * 4) / 5;

            buttonX.setLayoutX((double) (WINDOW_SIZE / 3) - buttonX.getWidth() / 2);
            buttonX.setLayoutY(baseY);

            buttonY.setLayoutX((double) (WINDOW_SIZE / 3) - buttonY.getWidth() / 2);
            buttonY.setLayoutY(buttonX.getLayoutY() + buttonX.getHeight() + 5);

            buttonColor.setLayoutX((double) (WINDOW_SIZE*2 / 3) - buttonColor.getWidth() / 2);
            buttonColor.setLayoutY(baseY);

            buttonSave.setLayoutX((double) (WINDOW_SIZE * 2 / 3) - buttonSave.getWidth() / 2);
            buttonSave.setLayoutY(buttonColor.getLayoutY() + buttonColor.getHeight() + 5);
        });


        buttonX.setOnAction(actionEvent -> {
            mirrorX();
        });

        buttonY.setOnAction(actionEvent -> {
            mirrorY();
        });

        buttonColor.setOnAction(actionEvent -> {
            changeColor();
        });

        buttonSave.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PNG");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                saveNodeAsHighResPng(main, 10.0, file);  // 10.0 = 10x resolution
            }
        });

    }

    private void changeColor() {
        if(colorMain==Color.BLACK&&colorAdditional==Color.DARKRED){
            colorMain=Color.ROYALBLUE;
            colorAdditional=Color.GOLD;
        }
        else if(colorMain==Color.ROYALBLUE&&colorAdditional==Color.GOLD){
            colorMain=Color.BLACK;
            colorAdditional=Color.DARKRED;
        }
        List<Node> originalChildren = new ArrayList<>(main.getChildren());
        for (Node node : originalChildren) {
            if (node instanceof Rectangle r) {
                Paint paint = r.getFill();

                if(paint instanceof Color){
                    if(paint == Color.BLACK){
                        r.setFill(Color.ROYALBLUE);
                    }
                    else if(paint == Color.DARKRED){
                        r.setFill(Color.GOLD);
                    }
                    else if(paint == Color.ROYALBLUE){
                        r.setFill(Color.BLACK);
                    }
                    else if(paint == Color.GOLD){
                        r.setFill(Color.DARKRED);
                    }
                }
            }
        }
    }

    private void mirrorX(){
        // Центр до масштабування
        double centerXBefore = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
        double centerYBefore = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;

        double height = main.getBoundsInParent().getHeight();
        double width = main.getBoundsInParent().getWidth();

        double scaleOld = main.getScaleX();

        if(height*2>width) {
            // Масштабуємо
            main.setScaleX(main.getScaleX() * 0.5);
            main.setScaleY(main.getScaleY() * 0.5);
        }

        main = mirrorX(main, true, false);

        // Центр після змін
        double centerXAfter = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
        double centerYAfter = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;

        // Компенсуємо зсув
        double dx = centerXBefore - centerXAfter;
        double dy = centerYBefore - centerYAfter;

        main.setLayoutX(main.getLayoutX() + dx);
        main.setLayoutY(main.getLayoutY() + dy);

        if((main.getScaleX()<scaleOld
                || (main.getScaleX()==scaleOld && main.getBoundsInParent().getHeight()*2>main.getBoundsInParent().getWidth()))
                && (main.getScaleX()*CROSS_SIZE<MIN_SIZE || main.getScaleY()*CROSS_SIZE<MIN_SIZE)){
            buttonX.setDisable(true);
        }
    }

    private void mirrorY(){
        // Зберігаємо центр до трансформацій
        double centerXBefore = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
        double centerYBefore = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;

        double height = main.getBoundsInParent().getHeight();
        double width = main.getBoundsInParent().getWidth();

        double scaleOld = main.getScaleY();

        if(width*2>height) {
            // Масштабуємо
            main.setScaleX(main.getScaleX() * 0.5);
            main.setScaleY(main.getScaleY() * 0.5);
        }

        // Дзеркалимо по осі Y (горизонтальній)
        main = mirrorY(main, true, false);

        // Зберігаємо центр після трансформацій
        double centerXAfter = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
        double centerYAfter = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;

        // Компенсуємо зміщення, щоб зберегти положення по центру
        double dx = centerXBefore - centerXAfter;
        double dy = centerYBefore - centerYAfter;

        main.setLayoutX(main.getLayoutX() + dx);
        main.setLayoutY(main.getLayoutY() + dy);

        if((main.getScaleY()<scaleOld
                || (main.getScaleY()==scaleOld && main.getBoundsInParent().getWidth()*2>main.getBoundsInParent().getHeight()))
                && (main.getScaleX()*CROSS_SIZE<MIN_SIZE || main.getScaleY()*CROSS_SIZE<MIN_SIZE)){
            buttonY.setDisable(true);
        }
    }

    private void saveNodeAsHighResPng(Node node, double scaleFactor, File outputFile) {
        // Set up snapshot parameters with scaling
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(new Scale(scaleFactor, scaleFactor));

        // Get the snapshot
        WritableImage image = node.snapshot(params, null);

        // Extract pixels manually (no SwingFXUtils)
        PixelReader reader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        int[] buffer = new int[width * height];
        reader.getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), buffer, 0, width);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, width, height, buffer, 0, width);

        // Save to PNG
        try {
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}