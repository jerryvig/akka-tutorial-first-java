package akka.tutorial.first.java;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;
 
public class JavaFXwithJBox2d extends Application {
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	@Override
    public void start(Stage stage) {
		stage.setTitle("JavaFX with JBox2d");
		stage.setFullScreen(false);
		stage.setResizable(false);
		
		final Group rootGroup = new Group();
		final Scene scene = new Scene(rootGroup, Utils.WIDTH, Utils.HEIGHT, Color.BLACK);
		
		//List of  balls
        final ArrayList<Ball> balls = new ArrayList<Ball>();
        
        Random random = new Random(System.currentTimeMillis());
        
        for ( int i=0; i<Utils.NUMBER_OF_BALLS; i++ ) {
        	balls.add( new Ball(new Float(random.nextInt(90)+5), new Float(random.nextInt(400)+100)) );
        }
        //Add ground to the application, this is where balls will land
        Utils.addGround(100, 10);
        Utils.addWall(0,100,1,100); //Left wall
        Utils.addWall(99,100,1,100); //Right wall
        
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        Duration duration = Duration.seconds(1.0/60.0); //Set frame duration.
        
        //Create an ActionEvent, on trigger it executes a world time step and moves the balls to new position
        EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
        	@Override
            public void handle(ActionEvent t) {
                        //Create time step. Set Iteration count 8 for velocity and 3 for positions
                       Utils.world.step(1.0f/60.0f, 8, 3);
                        
                       //Move balls to the new position computed by JBox2D
                       for(int i=0;i<Utils.NUMBER_OF_BALLS; i++) {
                            Body body = (Body)(balls.get(i).node).getUserData();
                            float xpos = (float) Utils.toPixelPosX(new Float(body.getPosition().x));
                            float ypos = (float) Utils.toPixelPosY(new Float(body.getPosition().y));
                            balls.get(i).node.setLayoutX(xpos);
                            balls.get(i).node.setLayoutY(ypos);
                       }
           }
        };
        
        KeyFrame frame = new KeyFrame(duration, eventHandler, null,null);
        
        timeline.getKeyFrames().add(frame);
        
        //Create button to start simulation.
        final Button button = new Button();
        button.setLayoutX((Utils.WIDTH/2));
        button.setLayoutY((Utils.HEIGHT-30));
        button.setText("Start");
        button.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                        timeline.playFromStart();
                        button.setVisible(false);
            }
        });
        
        //Draw hurdles on mouse event.
        EventHandler<MouseEvent> addHurdle = new EventHandler<MouseEvent>(){
            public void handle(MouseEvent mouseEvent) {
                    //Get mouse's x and y coordinates on the scene
                    float dragX = (float)mouseEvent.getSceneX();
                    float dragY = (float)mouseEvent.getSceneY();
                     
                    //Draw ball on this location. Set balls body type to static.
                    Ball hurdle = new Ball(Utils.toPosX(dragX), Utils.toPosY(dragY), new Float(2.0),BodyType.STATIC,Color.BLUE);
                    //Add ball to the root group
                    rootGroup.getChildren().add(hurdle.node);
            }
        };
        
        rootGroup.getChildren().add(button);
        
        for(int i=0; i<Utils.NUMBER_OF_BALLS; i++) {
            rootGroup.getChildren().add(balls.get(i).node);
        }
        
        scene.setOnMouseDragged(addHurdle);
        
        stage.setScene(scene);
        stage.show();
	}
}
