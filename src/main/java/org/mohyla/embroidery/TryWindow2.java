package org.mohyla.embroidery;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TryWindow2 extends Application {

    private static final int WINDOW_SIZE = 600;

    private static final int EMBROIDERY_SIZE = 11;
    private static final int CROSS_SIZE = 10;
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
    @Override
    public void start(Stage stage){
        stage.setTitle("TryWindow");
        Group root = embroideryRoot();

        //root.getChildren().add();
        //

        Scene scene = new Scene(root, WINDOW_SIZE, WINDOW_SIZE, Color.PALEGOLDENROD);
        stage.setScene(scene);
        stage.show();
    }

    private Group embroideryRoot(){
        Group root = new Group();
        Text text = new Text("Дарина");
        text.setFont(new Font("Helvetica", 50));
        text.setX((double) WINDOW_SIZE/2 - text.getBoundsInLocal().getWidth() / 2);
        text.setY(text.getBoundsInLocal().getHeight());
        root.getChildren().add(text);

        Group main = mainEmbroidery();
        main = mirrorX(main, false, Color.BLACK);
        main = mirrorY(main, false, Color.BLACK);
        root.getChildren().add(main);

        boolean[][] mainP = mainPattern();
        boolean[][] addP = additionalPattern(mainP);
        Group additional = additionalEmbroidery(addP);
        additional = mirrorX(additional, false, Color.RED);
        additional = mirrorY(additional, false, Color.RED);

        root.getChildren().add(additional);
        return root;
    }

    private Group mirrorX(Group original, boolean includeCenter, Color color) {
        Group mirrored = new Group();

        Bounds bounds = original.getBoundsInLocal();
        double centerY = (bounds.getMinY() + bounds.getMaxY()) / 2;

        for (Node node : original.getChildren()) {
            if (node instanceof Rectangle) {
                Rectangle r = (Rectangle) node;

                // Add original rectangle
                Rectangle originalCopy = new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
                originalCopy.setFill(r.getFill());
                mirrored.getChildren().add(originalCopy);

                // Mirror across centerY
                double mirroredY = 2 * centerY - r.getY() - r.getHeight() + original.getBoundsInLocal().getHeight() - (includeCenter ? 0 : r.getHeight());

                Rectangle mirroredRect = new Rectangle(r.getX(), mirroredY, r.getWidth(), r.getHeight());
                mirroredRect.setFill(color);
                mirrored.getChildren().add(mirroredRect);
            }
        }

        return mirrored;
    }



    private Group mirrorY(Group original, boolean includeCenter, Color color) {
        Group mirrored = new Group();

        Bounds bounds = original.getBoundsInLocal();
        double centerX = (bounds.getMinX() + bounds.getMaxX()) / 2;

        for (Node node : original.getChildren()) {
            if (node instanceof Rectangle) {
                Rectangle r = (Rectangle) node;

                // Add original rectangle
                Rectangle originalCopy = new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
                originalCopy.setFill(r.getFill());
                mirrored.getChildren().add(originalCopy);

                // Mirror across centerX
                double mirroredX = 2 * centerX - r.getX() - r.getWidth() - original.getBoundsInLocal().getWidth() + (includeCenter ? 0 : r.getWidth());

                Rectangle mirroredRect = new Rectangle(mirroredX, r.getY(), r.getWidth(), r.getHeight());
                mirroredRect.setFill(color);
                mirrored.getChildren().add(mirroredRect);
            }
        }

        return mirrored;
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

    private Group mainEmbroidery(){
        Group group = new Group();
        for(int l = 0; l <NAME.length; l++){
            for(int m = 0; m <NAME[l].length; m++){
                for(int k = 0; k<NAME[l][m].length; k++){
                    if(NAME[l][m][k]){
                        Rectangle rectangle = new Rectangle(CROSS_SIZE, CROSS_SIZE);
                        rectangle.setX((double) WINDOW_SIZE / 2 + CROSS_SIZE*k);
                        rectangle.setY((double) WINDOW_SIZE / 2 - (double) ((EMBROIDERY_SIZE*2-1) * CROSS_SIZE) / 2 + CROSS_SIZE*m);
                        group.getChildren().add(rectangle);
                    }
                }
            }
        }
        return group;
    }
    private Group additionalEmbroidery(boolean[][] pattern){
        Group group = new Group();
        for(int i = 0; i < pattern.length; i++){
            for(int j = 0; j < pattern[i].length; j++){
                if(pattern[i][j]){
                    Rectangle rectangle = new Rectangle(CROSS_SIZE, CROSS_SIZE);
                    rectangle.setFill(Color.RED);
                    rectangle.setX((double) WINDOW_SIZE / 2 + CROSS_SIZE*j);
                    rectangle.setY((double) WINDOW_SIZE / 2 - (double) ((EMBROIDERY_SIZE*2-1) * CROSS_SIZE) / 2 + CROSS_SIZE*i);
                    group.getChildren().add(rectangle);
                }
            }
        }
        return group;
    }
}