package org.mohyla.embroidery;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class TryWindow2 extends Application {

    private static final int WINDOW_SIZE = 600;

    private static final int EMBROIDERY_SIZE = 11;
    private static final int CROSS_SIZE = 15;
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
    private static int rows = 1;
    private static int cols = 1;
    private static Group root = new Group();
    private static Group main = new Group();
    private static Group additional = new Group();
    @Override
    public void start(Stage stage){
        stage.setTitle("TryWindow");
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
        text.setFill(Color.DARKRED);
        text.setStyle("-fx-font-weight: bold");
        text.setX((double) WINDOW_SIZE/2 - text.getBoundsInLocal().getWidth() / 2);
        text.setY(text.getBoundsInLocal().getHeight());
        root.getChildren().add(text);

        Button button = new Button();
        button.setText("Відобразити горизонтально");
        button.setFont(new Font("Helvetica", 20));
        button.setLayoutX((double) (WINDOW_SIZE / 4));
        button.setLayoutY((double) (WINDOW_SIZE * 4) /5);
        button.setDisable(true);
        root.getChildren().add(button);

        Button button2 = new Button();
        button2.setText("Відобразити вертикально");
        button2.setFont(new Font("Helvetica", 20));
        button2.setLayoutX((double) (WINDOW_SIZE / 4));
        button2.setLayoutY(button.getLayoutY()+ 50);
        button2.setDisable(true);
        root.getChildren().add(button2);

        root.getChildren().add(main);
        root.getChildren().add(additional);

        SequentialTransition letterTransition = new SequentialTransition();
        for (boolean[][] oneLetter : NAME) {
            main = embroidery(main, oneLetter, Color.BLACK);
        }

        boolean[][] mainP = mainPattern();
        boolean[][] addP = additionalPattern(mainP);

        main = embroidery(main, addP, Color.DARKRED);

        main = mirrorX(main, false);

        main = mirrorY(main, false);

        button.setDisable(false);
        button2.setDisable(false);

        button.setOnAction(actionEvent -> {
            // Центр до масштабування
            double centerXBefore = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
            double centerYBefore = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;

            double height = main.getBoundsInParent().getHeight();
            double width = main.getBoundsInParent().getWidth();

            /*main.setScaleX(main.getScaleX() * width/(height*2));
            main.setScaleY(main.getScaleY() * width/(height*2));*/

            if(height*2>width) {
                // Масштабуємо
                main.setScaleX(main.getScaleX() * 0.5);
                main.setScaleY(main.getScaleY() * 0.5);
            }

            // Дзеркалимо
            main = mirrorX(main, true);

            // Центр після змін
            double centerXAfter = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
            double centerYAfter = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;

            // Компенсуємо зсув
            double dx = centerXBefore - centerXAfter;
            double dy = centerYBefore - centerYAfter;

            main.setLayoutX(main.getLayoutX() + dx);
            main.setLayoutY(main.getLayoutY() + dy);
        });

        button2.setOnAction(actionEvent -> {
            // Зберігаємо центр до трансформацій
            double centerXBefore = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
            double centerYBefore = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;

            double height = main.getBoundsInParent().getHeight();
            double width = main.getBoundsInParent().getWidth();

            /*double scale = height/(width*2);
            main.setScaleX(main.getScaleX() * scale);
            main.setScaleY(main.getScaleY() * scale);*/

            if(width*2>height) {
                // Масштабуємо
                main.setScaleX(main.getScaleX() * 0.5);
                main.setScaleY(main.getScaleY() * 0.5);
            }

            // Дзеркалимо по осі Y (горизонтальній)
            main = mirrorY(main, true);

            // Зберігаємо центр після трансформацій
            double centerXAfter = main.getBoundsInParent().getMinX() + main.getBoundsInParent().getWidth() / 2;
            double centerYAfter = main.getBoundsInParent().getMinY() + main.getBoundsInParent().getHeight() / 2;

            // Компенсуємо зміщення, щоб зберегти положення по центру
            double dx = centerXBefore - centerXAfter;
            double dy = centerYBefore - centerYAfter;

            main.setLayoutX(main.getLayoutX() + dx);
            main.setLayoutY(main.getLayoutY() + dy);
        });


    }

    private Group mirrorX(Group original, boolean includeCenter) {
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

                original.getChildren().add(mirroredRect); // ✅ тепер безпечно
            }
        }

        return original;
    }

    private Group mirrorY(Group original, boolean includeCenter) {
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
                }
            }
        }
        return group;
    }
}